package com.example.igricaslagalica.controller

import android.content.ContentValues.TAG
import android.util.Log
import com.example.igricaslagalica.model.Connection
import com.example.igricaslagalica.model.Game
import com.example.igricaslagalica.model.KoZnaZna
import com.example.igricaslagalica.model.Skocko
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
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
    fun createGame(playerId: String, gameId: String, onQuestionsFetched: (String) -> Unit) {
        val gameRef = db.collection("games").document(gameId)

        val game = Game(
            id = gameId,
            player1 = playerId,
            currentTurn = playerId
        )

        // Kreirajte pitanja za Igra1
        FirebaseFirestore.getInstance().collection("koZnaZnaQuestions").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val allQuestions = task.result?.documents?.mapNotNull { it.toObject(KoZnaZna::class.java) }

                if (allQuestions != null) {
                    val gameQuestions = allQuestions.shuffled().take(5).map {
                        KoZnaZna(
                            id = it.id,
                            questionText = it.questionText,
                            options = it.options,
                            correctAnswer = it.correctAnswer,
                            player1Answered = it.player1Answered,
                            player1AnswerTime = it.player1AnswerTime,
                            player2Answered = it.player2Answered,
                            player2AnswerTime =  it.player2AnswerTime
                        )
                    }
                    game.koZnaZnaQuestions = gameQuestions

                    // Kreirajte pitanja za Igra2
                    FirebaseFirestore.getInstance().collection("spojnicaQuestions").get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val allQuestions = task.result?.documents?.mapNotNull { it.toObject(Connection::class.java) }

                            if (allQuestions != null) {
                                val gameQuestions = allQuestions.shuffled().take(5).map {
                                    Connection(
                                        question = it.question,
                                        answer = it.answer,
                                        assignedToPlayer = playerId
                                    )
                                }
                                game.questionInfo = gameQuestions

                                // Sada kada su obe liste pitanja kreirane, postavite igru u Firestore
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
        }
    }
    fun listenForGameChanges(gameId: String, gameCallback: (Game?) -> Unit) {
        val gameRef = db.collection("games").document(gameId) //db.document(gameId)
        gameRef.addSnapshotListener { documentSnapshot, e ->
            if (e != null) {
                Log.e("ListenForGameChanges", "Error listening for game changes", e)
                gameCallback(null) // Return null in case of failure
                return@addSnapshotListener
            }

            val game = documentSnapshot?.toObject(Game::class.java)
            gameCallback(game)
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
    fun listenForPlayerCompletion(gameId: String, player: String, completionCallback: (Boolean) -> Unit) {
        val gameRef = db.collection("games").document(gameId)
        gameRef.addSnapshotListener { documentSnapshot, e ->
            if (e != null) {
                Log.e("ListenForPlayerCompletion", "Error listening for player completion", e)
                completionCallback(false) // Return false in case of failure
                return@addSnapshotListener
            }

            val game = documentSnapshot?.toObject(Game::class.java)
            val isPlayerDone = when (player) {
                "player1" -> game?.isPlayer1Done ?: false
                "player2" -> game?.isPlayer2Done ?: false
                else -> false
            }

            completionCallback(isPlayerDone)
        }
    }


    fun fetchQuestionsForIgra1(): Task<List<KoZnaZna>> {
        return FirebaseFirestore.getInstance().collection("koZnaZnaQuestions").get().continueWith { task ->
            val allQuestions = task.result?.documents?.mapNotNull { it.toObject(KoZnaZna::class.java) }
            allQuestions?.shuffled()?.take(5)?.map {
                KoZnaZna(
                    id = it.id,
                    questionText = it.questionText,
                    options = it.options,
                    correctAnswer = it.correctAnswer,
                    player1Answered = it.player1Answered,
                    player1AnswerTime = it.player1AnswerTime,
                    player2Answered = it.player2Answered,
                    player2AnswerTime =  it.player2AnswerTime
                )
            } ?: listOf()
        }
    }

    fun fetchQuestionsForIgra2(): Task<List<Connection>> {
        return FirebaseFirestore.getInstance().collection("spojniceQuestion").get().continueWith { task ->
            val allQuestions = task.result?.documents?.mapNotNull { it.toObject(Connection::class.java) }
            allQuestions?.shuffled()?.take(5)?.map {
                Connection(
                    question = it.question,
                    answer = it.answer,
                    assignedToPlayer = it.assignedToPlayer
                )
            } ?: listOf()
        }
    }

}

