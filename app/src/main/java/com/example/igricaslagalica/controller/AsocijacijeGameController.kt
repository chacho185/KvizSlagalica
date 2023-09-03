package com.example.igricaslagalica.controller

import com.example.igricaslagalica.model.AsocijacijaMultiplayer
import com.example.igricaslagalica.model.Game
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

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

    fun updateQuestionAssocijacija(game: Game, callback: (Boolean) -> Unit) {
        val gameDocumentRef = db.collection("games").document(game.id!!)
        val updatedQuestions = game?.asocijacijaQuestions?.toMutableList()
        if (updatedQuestions != null && updatedQuestions.size > 0) {
            val indexToUpdate = 0  // Specify the index you want to update
            updatedQuestions[indexToUpdate].assignedToPlayer = game.player1
            updatedQuestions[1].assignedToPlayer = game.player2


            // Update the entire list back to Firestore
            gameDocumentRef.update("asocijacijaQuestions", updatedQuestions)
                .addOnSuccessListener {
                    // Update successful
                    callback(true)
                }
                .addOnFailureListener { e ->
                    // Handle update failure
                    callback(false)
                }
        }
        // Get the current game from Firestore
//        gameDocumentRef.get()
//            .addOnSuccessListener { documentSnapshot ->
//                if (documentSnapshot.exists()) {
//                    val currentGame = documentSnapshot.toObject(Game::class.java)
//
//
//                }
//            }
//            .addOnFailureListener { e ->
//                // Handle failure to get document
//            }

    }

fun generateNewQuestions(game: Game,assignedPlayer: String = "", onQuestionsFetched: (List<AsocijacijaMultiplayer>) -> Unit) {

    db.collection("asocijacijaQuestions").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val allQuestions = task.result?.documents?.mapNotNull { it.toObject(AsocijacijaMultiplayer::class.java) }
                if (allQuestions != null) {
                    val gameQuestions = allQuestions.shuffled().mapIndexed { index, question ->
                        val assignedPlayer = if (index == 0) game.player1 else game.player2
                        AsocijacijaMultiplayer(
                            question.asocijacijaList,
                            question.asocijacijaListOne,
                            question.asocijacijaTwo,
                            question.asocijacijaThree,
                            question.asocijacijaKonacnoRjesenje,
                            null, // answeredBy, leave it as null for now
                            assignedPlayer
                        )
                    }

//                    val gameQuestions = allQuestions.shuffled().map {
//                        AsocijacijaMultiplayer(
//                            it.asocijacijaList,
//                            it.asocijacijaListOne,
//                            it.asocijacijaTwo,
//                            it.asocijacijaThree,
//                            it.asocijacijaKonacnoRjesenje,
//                            null, // answeredBy, leave it as null for now
//                            if (assignedPlayer.isNotEmpty()) assignedPlayer else game.currentTurn
//                        )
//                    }
                    onQuestionsFetched(gameQuestions)
                    game.id?.let { updateGameField(it,"asocijacijaQuestions", gameQuestions){} }
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
    fun updateInteractions(gameId: String, interactions: List<Map<String, Int>>){
        val gameDocumentRef = db.collection("games").document(gameId)
        gameDocumentRef.update("interactionsAsocijacija", interactions)

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
    fun saveResultPlayer1(gameId: String, player1Score: Int,  nextTurn: String) {
        updateGameField(gameId, "player1Score", player1Score) { success1 ->
            if (success1) {
                updateGameField(gameId, "currentTurn", nextTurn) { success3 ->
                }
                }
            }
    }
    fun saveResultPlayer2(gameId: String, player2Score: Int,  nextTurn: String) {
                updateGameField(gameId, "player2Score", player2Score) { success2 ->
                    if(success2){
                    updateGameField(gameId, "currentTurn", nextTurn) { success3 ->
                    }
            }
        }
    }
}