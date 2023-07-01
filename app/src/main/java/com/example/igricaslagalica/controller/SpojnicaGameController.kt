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
                } else {
                    Log.d(ContentValues.TAG, "Current data: null")
                    onUpdate(null)
                }
            }
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
    fun endRound(gameId: String) {
        val gameRef = db.collection("games").document(gameId)
        gameRef.get().addOnSuccessListener { document ->
            val game = document.toObject(Game::class.java)

            if (game != null) {
                val isRoundOver = game.questionInfo.all { it.assignedToPlayer != null && it.answered }
                // Provjerite je li svako pitanje odgovoreno
                if (isRoundOver) {
                    // Ako je ovo bio kraj prve runde i bio je red na Igraču 2
                    game.currentRound++
                    if (game.currentRound == 2) {
                        // Generišite novi set pitanja za rundu 2
                        generateNewQuestions(game) { newQuestions ->
                            game.questionInfo = newQuestions
                            gameRef.set(game, SetOptions.merge())
                            Log.w(TAG, "hellou 2 $newQuestions")
                        }



                    }
                    // Ažurirajte igru u Firestore
                    gameRef.set(game, SetOptions.merge())
                } else {
                    if (game.currentRound == 1) {
                        // ...dodjela pitanja igraču 2 (kao što je ranije objašnjeno)...
                        game.questionInfo.forEach {
                            if (!it.correct && it.assignedToPlayer == game.player1) {
                                it.assignedToPlayer = game.player2
                            }
                        }
                        Log.w(TAG, "hellou")
                        // Ažurirajte igru u Firestore
                        game.currentRound++
                        gameRef.set(game, SetOptions.merge())
                    }
                }
            }
        }
    }

    fun generateNewQuestions(game: Game, onQuestionsFetched: (List<Connection>) -> Unit) {
        db.collection("spojnicaQuestions").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val allQuestions = task.result?.documents?.mapNotNull { it.toObject(Connection::class.java) }
                if (allQuestions != null) {
                    val gameQuestions = allQuestions.shuffled().take(5).map {
                        Connection(
                            question = it.question,
                            answer = it.answer,
                            assignedToPlayer = game.currentTurn
                        )
                    }
                    onQuestionsFetched(gameQuestions)
                }
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

    fun processAnswer(questionIndex: Int, answerIndex: Int): Boolean {
        return questionIndex == answerIndex
    }

}
