package com.example.igricaslagalica.controller

import android.content.ContentValues.TAG
import android.util.Log
import com.example.igricaslagalica.model.Connection
import com.example.igricaslagalica.model.Game
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseGameController() {
    private val db = FirebaseFirestore.getInstance()

    fun startGame(playerId: String, onComplete: (Boolean, String) -> Unit) {
        val game = Game(player1 = playerId, currentTurn = playerId)
        db.collection("games").add(game).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val gameId = task.result?.id ?: ""
                createGame(playerId, gameId) { updatedGameId ->
                    onComplete(true, updatedGameId)
                }
               // onComplete(true, task.result?.id ?: "")
            } else {
                onComplete(false, "")
            }
        }
    }
    fun joinGame(gameId: String, playerId: String, onComplete: (Boolean) -> Unit) {
        db.collection("games").document(gameId)
            .update("player2", playerId, "status", "playing")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true)
                } else {
                    Log.e("FirebaseGameController", "Error joining game: ", task.exception)
                    onComplete(false)
                }
            }
    }
    fun getWaitingGame(playerId: String, onGameFound: (Game?) -> Unit) {
        db.collection("games")
            .whereEqualTo("status", "waiting")
            .whereNotEqualTo("player1", playerId)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                  //  val game = documents.documents[0].toObject(Game::class.java)
                        val document = documents.documents[0]
                        val game = document.toObject(Game::class.java)
                        game?.id = document.id // Set the id field manually
                    onGameFound(game)
                } else {
                    onGameFound(null)
                }
            }
    }

    fun createGame(playerId: String, gameId: String, onQuestionsFetched: (String) -> Unit) {
        FirebaseFirestore.getInstance().collection("spojnicaQuestions").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val allQuestions = task.result?.documents?.mapNotNull { it.toObject(Connection::class.java) }

                // Continue only if the questions are fetched successfully
                if (allQuestions != null) {
                    val gameQuestions = allQuestions.shuffled().take(3)

                    val gameRef = db.collection("games").document(gameId)
                    val game = Game(
                        id = gameId,
                        player1 = playerId,
                        questionInfo = gameQuestions,
                        currentTurn = playerId
                    )

                    gameRef.set(game).addOnSuccessListener {
                        // Ostatak koda za kreiranje igre...
                        Log.w(TAG, "Game updated: $gameId")

                        // Notify the caller that the questions have been fetched
                        onQuestionsFetched(gameId)
                    }
                } else {
                    // Handle the case where the questions are not fetched successfully
                }
            }
        }
    }


}
