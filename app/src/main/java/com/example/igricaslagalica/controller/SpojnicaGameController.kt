package com.example.igricaslagalica.controller

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import android.widget.ProgressBar
import com.example.igricaslagalica.model.Connection
import com.example.igricaslagalica.model.Game
import com.example.igricaslagalica.model.Player
import com.example.igricaslagalica.model.Question
import com.example.igricaslagalica.model.Round
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

enum class GameState {
    ROUND_ONE_PLAYER_ONE,
    ROUND_ONE_PLAYER_TWO,
    ROUND_TWO_PLAYER_ONE,
    ROUND_TWO_PLAYER_TWO
    // Add more states as needed
}

class SpojnicaGameController {

    private val db = FirebaseFirestore.getInstance()
    fun watchGame(gameId: String, onUpdate: (Game?) -> Unit) {

//        db.collection("games").document(gameId)
//            .addSnapshotListener { snapshot, e ->
//                if (e != null) {
//                    Log.w(ContentValues.TAG, "Listen failed.", e)
//                    onUpdate(null)
//                    return@addSnapshotListener
//                }
//
//                if (snapshot != null && snapshot.exists()) {
//                    Log.d(ContentValues.TAG, "Current data: ${snapshot.data}")
//                    val game = snapshot.toObject(Game::class.java)
//                    onUpdate(game)
//                } else {
//                    Log.d(ContentValues.TAG, "Current data: null")
//                    onUpdate(null)
//                }
//            }
    }

    // Metoda za preuzimanje igre s Firestore-a
    fun getGame(gameId: String, callback: (Game?) -> Unit) {
        db.collection("games").document(gameId).get()
            .addOnSuccessListener { documentSnapshot ->
                val game = documentSnapshot.toObject(Game::class.java)
                Log.w(TAG,"Game objekt preuzet $game}")

                if (game != null) {
                    Log.w(TAG,"Game objekt preuzet $game lista ${game.questionInfo.size}")
                }
                callback(game)
               // callback(documentSnapshot.toObject(Game::class.java))
            }
            .addOnFailureListener {
                callback(null)
            }
    }
    // Metoda za provjeru veze
    fun checkAnswer(connection: Connection, answer: String): Boolean {
        return connection.answer == answer
    }


    // Metoda za ažuriranje igre na Firestore-u
    fun updateGameField(gameId: String, field: String, value: Any, callback: (Boolean) -> Unit) {
        db.collection("games").document(gameId)
            .update(field, value)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
    fun updateGameAfterAnswer(game: Game, currentPlayerId: String, connection: Connection, callback: (Boolean) -> Unit) {
        var playerScore = 0

        // Loop through all the questions
        for (connection in game.questionInfo) {
            // If the question was answered by the current player and is correct, increment the score
            if (connection.answeredBy == currentPlayerId && connection.correct) {
                playerScore += 2
            }
        }

        // Update the player's score
        if (connection.correct) {
            if (game.player1 == currentPlayerId) {
                game.player1Score += playerScore
            } else if (game.player2 == currentPlayerId) {
                game.player2Score += playerScore
            }
        }
        game.id?.let { gameId ->
            updateGameField(gameId, "player1Score", game.player1Score) { success1 ->
                if (success1) {
                    updateGameField(gameId, "player2Score", game.player2Score) { success2 ->
                        callback(success2)
                    }
                } else {
                    callback(false)
                }
            }
        }

    }

    fun answerQuestion(game: Game, currentPlayerId: String,connection: Connection, questionIndex:Int, answerIndex:Int,  callback: (Boolean) -> Unit) {
        // Check the connection
        connection.correct = processAnswer(questionIndex, answerIndex)
        connection.answered = true
        connection.answeredBy = currentPlayerId


        game.id?.let { gameId ->
            updateGameField(gameId, "questionInfo",  game.questionInfo) { success ->
                if (success) {
                    // If all updates are successful, invoke callback with true
                    callback(true)
                } else {
                    // Invoke callback with false if questionInfo update failed
                    callback(false)
                }
            }
        }

    }

    // Inside your game controller or similar class

    fun handleRound1(game: Game, callback: (Boolean) -> Unit) {
        val allQuestionsAnsweredCorrectly = game.questionInfo.all { it.correct }

        if (allQuestionsAnsweredCorrectly) {
            // Switch to Round 2
            game.currentRound = 2
            game.currentTurn = game.player2!!
            updateGameField(game.id!!, "currentRound", 2) { success ->
                if (success) {
                    updateGameField(game.id!!, "currentTurn", game.player2) { success ->
                        callback(success)
                    }
                } else {
                    callback(false)
                }
            }
        } else {
            // Switch turn to Player 2 in Round 1
            game.player1?.let {
                switchTurn(game, it) { success ->
                    callback(success)
                }
            }
        }
    }

    fun handleRound2(game: Game, callback: (Boolean) -> Unit) {
        val allQuestionsAnsweredCorrectly = game.questionInfo.all { it.correct && it.answeredBy == game.player2 }

        if (allQuestionsAnsweredCorrectly) {
            // End the game

            callback(true)
        } else {
            // Switch turn to Player 1 in Round 2
            game.player1?.let {
                switchTurn(game, it) { success ->
                    callback(success)
                }
            }
        }
    }
    fun switchTurn(game: Game, currentPlayerId: String, callback: (Boolean) -> Unit) {
        val otherPlayerId = if (game.player1 == currentPlayerId) game.player2 else game.player1
        if (otherPlayerId != null) {
            game.currentTurn = otherPlayerId // Update the current turn
        }

        if (game.currentRound == 1) {
            // Round 1 logic
            val allRound1QuestionsAnswered = game.questionInfo.all { it.assignedToPlayer != null && it.correct }
            if (allRound1QuestionsAnswered) {
                // Move to Round 2
                game.currentRound = 2

                game.id?.let { gameId ->
                    updateGameField(gameId, "currentRound", game.currentRound, callback)
                }
                return
            }
        }

        if (game.currentRound == 2 && game.player2 == currentPlayerId) {
            // Round 2 logic for Player 2
            val allRound2QuestionsAnswered = game.questionInfo.all { it.assignedToPlayer != null && it.correct }
            if (allRound2QuestionsAnswered) {
                // Game ends
                game.id?.let { gameId ->
                    updateGameField(gameId, "currentRound", 3, callback)
                }
                callback(true)
                return
            }
        }

        // Update the current turn only
        game.id?.let { gameId ->
            if (otherPlayerId != null) {
                updateGameField(gameId, "currentTurn", otherPlayerId, callback)
            }
        }
    }
    fun saveResultToDatabase(gameId: String, player1Score: Int, player2Score: Int) {
        updateGameField(gameId, "player1Score", player1Score) { success1 ->
            if (success1) {
                updateGameField(gameId, "player2Score", player2Score) { success2 ->
                }
            }
        }
    }
    fun generateNewQuestions(game: Game, assignedToPlayer: String = "", onQuestionsFetched: (List<Connection>) -> Unit) {
        db.collection("spojnicaQuestions").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val allQuestions = task.result?.documents?.mapNotNull { it.toObject(Connection::class.java) }
                if (allQuestions != null) {
                    val gameQuestions = allQuestions.shuffled().take(5).map {
                        Connection(
                            question = it.question,
                            answer = it.answer,

                            assignedToPlayer = if (assignedToPlayer.isNotEmpty()) assignedToPlayer else game.currentTurn // game.currentTurn
                        )
                    }
                    onQuestionsFetched(gameQuestions)
                }
            }
        }
    }

    fun getPlayerName(playerId: String, onComplete: (String?) -> Unit) {
        db.collection("players").document(playerId).get().addOnSuccessListener { documentSnapshot ->
            val playerName = documentSnapshot.getString("username")
            onComplete(playerName)
        }.addOnFailureListener { e ->
            Log.w(TAG, "Error retrieving player name", e)
            onComplete(null)
        }
    }


    fun processAnswer(questionIndex: Int, answerIndex: Int): Boolean {
        return questionIndex == answerIndex
    }

}
