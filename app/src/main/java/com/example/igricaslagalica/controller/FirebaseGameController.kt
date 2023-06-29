package com.example.igricaslagalica.controller

import android.content.ContentValues.TAG
import android.util.Log
import com.example.igricaslagalica.model.Connection
import com.example.igricaslagalica.model.Game
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseGameController() {
    private val db = FirebaseFirestore.getInstance()

    fun startGame(playerId: String, onComplete: (Boolean, String) -> Unit) {
        val game = Game(player1 = playerId)
        db.collection("games").add(game).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true, task.result?.id ?: "")
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


//    fun listenForGames(playerId: String, onGameFound: (Game) -> Unit) {
//        db.collection("games")
//            .whereEqualTo("status", "waiting")
//            .whereNotEqualTo("player1", playerId)
//            .addSnapshotListener { value, _ ->
//                for (document in value!!) {
//                    val game = document.toObject(Game::class.java)
//                    if (game.status == "waiting") {
//                        // pronađena partija koja čeka igrača, ažurirajte dokument sa novim statusom i drugim igračem
//                        document.reference.update("status", "playing", "player2", playerId)
//                        onGameFound(game)
//                        break
//                    }
//                }
//            }
//    }


}