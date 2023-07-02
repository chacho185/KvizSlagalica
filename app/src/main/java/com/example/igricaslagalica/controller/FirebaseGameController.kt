package com.example.igricaslagalica.controller

import android.content.ContentValues.TAG
import android.util.Log
import com.example.igricaslagalica.model.Connection
import com.example.igricaslagalica.model.Game
import com.example.igricaslagalica.model.KoZnaZna
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
                    val gameQuestions = allQuestions.shuffled().take(3).map {
                        KoZnaZna(
                            id = it.id,
                            questionText = it.questionText,
                            options = it.options,
                            correctAnswer = it.correctAnswer,
                            userAnswer = it.userAnswer,
                            remainingTime = it.remainingTime
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

//    fun createGame(playerId: String, gameId: String, onQuestionsFetched: (String) -> Unit) {
//        val gameRef = db.collection("games").document(gameId)
//
//        val game = Game(
//            id = gameId,
//            player1 = playerId,
//            currentTurn = playerId
//        )
//
//        val taskIgra1 = fetchQuestionsForIgra1()
//        val taskIgra2 = fetchQuestionsForIgra2()
//
//        taskIgra1.addOnSuccessListener { results ->
//            game.koZnaZnaQuestions = results
//            // Update Firestore
//            gameRef.update("questionInfoIgra1", game.koZnaZnaQuestions).addOnSuccessListener {
//                Log.w(TAG, "Questions for game 1 updated: $gameId")
//                onQuestionsFetched(gameId)
//            }
//        }
//
//        taskIgra2.addOnSuccessListener { results ->
//            game.questionInfo = results
//
//            // Update Firestore
//            gameRef.update("questionInfo", game.questionInfo).addOnSuccessListener {
//                Log.w(TAG, "Questions for game 2 updated: $gameId")
//                onQuestionsFetched(gameId)
//            }
//        }
//
//    }

    fun fetchQuestionsForIgra1(): Task<List<KoZnaZna>> {
        return FirebaseFirestore.getInstance().collection("koZnaZnaQuestions").get().continueWith { task ->
            val allQuestions = task.result?.documents?.mapNotNull { it.toObject(KoZnaZna::class.java) }
            allQuestions?.shuffled()?.take(5)?.map {
                KoZnaZna(
                    id = it.id,
                    questionText = it.questionText,
                    options = it.options,
                    correctAnswer = it.correctAnswer,
                    userAnswer = it.userAnswer,
                    remainingTime = it.remainingTime
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

