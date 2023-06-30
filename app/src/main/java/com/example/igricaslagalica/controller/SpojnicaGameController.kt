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

enum class GameState {
    ROUND_ONE_PLAYER_ONE,
    ROUND_ONE_PLAYER_TWO,
    ROUND_TWO_PLAYER_ONE,
    ROUND_TWO_PLAYER_TWO
    // Add more states as needed
}

class SpojnicaGameController {

        private val db = FirebaseFirestore.getInstance()
        private var currentPlayer: Int = 1
        private val playerScores = mutableMapOf(1 to 0, 2 to 0)
        private val questions = mutableListOf(
            Connection("Izvođač 13", "Pesma 1"),
            Connection("Izvođač 23", "Pesma 2"),
            Connection("Izvođač 3", "Pesma 3"),
            Connection("Izvođač 4", "Pesma 4"),
            Connection("Izvođač 5", "Pesma 5"),
            // ...more questions here...
        )
    private val madeConnections = mutableListOf<Pair<Int, Int>>()

    private var gameState = GameState.ROUND_ONE_PLAYER_ONE
    private val questionsAndAnswers = mutableListOf<Pair<String, String>>()
    private val unansweredQuestionIndices = mutableListOf<Int>()
    private val unansweredConnections = questions.toMutableList()

    fun watchGame(gameId: String, onUpdate: (Game?) -> Unit) {

        db.collection("games").document(gameId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(ContentValues.TAG, "Listen failed.", e)
                    onUpdate(null)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(ContentValues.TAG, "Current data: ${snapshot.data}")
                    val game = snapshot.toObject(Game::class.java)
                    onUpdate(game)
                   // onUpdate(snapshot.toObject(Game::class.java))
                } else {
                    Log.d(ContentValues.TAG, "Current data: null")
                    onUpdate(null)
                }
            }
    }

//

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
//    fun processAnswer(questionIndex: Int, answerIndex: Int): Boolean {
//        if (questionIndex == answerIndex) {
//            playerScores[currentPlayer] = playerScores.getValue(currentPlayer) + 2
//            return true
//        } else {
//            return false
//        }
//    }

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
        // Check the connection
        connection.correct = checkAnswer(connection, connection.answer)
        connection.answered = true
        connection.answeredBy = currentPlayerId

        // Update the player's score
        if (connection.correct) {
            if (game.player1 == currentPlayerId) {
                game.player1Score += playerScore
            } else if (game.player2 == currentPlayerId) {
                game.player2Score += playerScore
            }
            Log.w(TAG,"player score ${game.player1Score}")
        }

        // then update fields in Firestore
        game.id?.let { gameId ->
            updateGameField(gameId, "questionInfo", game.questionInfo) { success ->
                if (success) {
                    updateGameField(gameId, "player1Score", game.player1Score) { success1 ->
                        if (success1) {
                            updateGameField(gameId, "player2Score", game.player2Score) { success2 ->
                                callback(success2)
                            }
                        } else {
                            callback(false)
                        }
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

        // Update fields in Firestore
        game.id?.let { gameId ->
            updateGameField(gameId, "questionInfo", game.questionInfo) { success ->
                if (success) {
                    updateGameField(gameId, "player1Score", game.player1Score) { success1 ->
                        if (success1) {
                            updateGameField(gameId, "player2Score", game.player2Score) { success2 ->
                                callback(success2)
                            }
                        } else {
                            callback(false)
                        }
                    }
                } else {
                    callback(false)
                }
            }
        }
    }

    fun switchTurn(game: Game, currentPlayerId: String, callback: (Boolean) -> Unit) {
        val otherPlayerId = if (game.player1 == currentPlayerId) game.player2 else game.player1
        if (otherPlayerId != null) {
            game.currentTurn = otherPlayerId // update the Game object in memory
        }
        game.id?.let { gameId ->
            if (otherPlayerId != null) {
                updateGameField(gameId, "currentTurn", otherPlayerId, callback)
            }
        }
    }
    fun getPlayerName(playerId: String, onComplete: (String?) -> Unit) {
        db.collection("players").document(playerId).get().addOnSuccessListener { documentSnapshot ->
            val player = documentSnapshot.toObject(Player::class.java)
            val playerName = player?.username
            onComplete(playerName)
        }.addOnFailureListener { e ->
            Log.w(TAG, "Error retrieving player name", e)
            onComplete(null)
        }
    }
    fun updatePlayerScore(game: Game, currentPlayerId: String, callback: (Boolean) -> Unit) {
        // Calculate the current player's score
        var currentPlayerScore = game.questionInfo.filter {
            it.answeredBy == currentPlayerId && it.correct
        }.size * 2  // assuming each correct answer gives 2 points

        // Choose the right player's score to update
        val fieldToUpdate = if (game.player1 == currentPlayerId) "player1Score" else "player2Score"

        // Update the player's score in the database
        game.id?.let { gameId ->
            updateGameField(gameId, fieldToUpdate, currentPlayerScore, callback)
        }
    }





    fun endGame() {
        // End the game here
    }


    fun processAnswer(questionIndex: Int, answerIndex: Int): Boolean {
        return questionIndex == answerIndex
    }

    fun getUnansweredConnections(): List<Connection> {
        return unansweredConnections
    }

    fun getScores(): Map<Int, Int> {
        return playerScores
    }

}
