package com.example.igricaslagalica.controller

import android.content.ContentValues
import android.util.Log
import com.example.igricaslagalica.model.Game
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AsocijacijeGameController {

    private val db = FirebaseFirestore.getInstance()

    fun saveResultToDatabase(gameId: String, player1Score: Int, player2Score: Int,  nextTurn: String) {
        updateGameField(gameId, "player1ScoreAssocijacija", player1Score) { success1 ->
            if (success1) {
                updateGameField(gameId, "player1ScoreAssocijacija", player2Score) { success2 ->
                    updateGameField(gameId, "currentTurn", nextTurn) { success3 ->
                    }

                    }
            }
        }
    }
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

}