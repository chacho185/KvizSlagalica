package com.example.igricaslagalica.controller

import android.content.ContentValues
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
                    onUpdate(snapshot.toObject(Game::class.java))
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
                callback(documentSnapshot.toObject(Game::class.java))
            }
            .addOnFailureListener {
                callback(null)
            }
    }
    // Metoda za provjeru veze
    fun checkAnswer(connection: Connection, answer: String): Boolean {
        return connection.answer == answer
    }
    fun processAnswer(questionIndex: Int, answerIndex: Int): Boolean {
        if (questionIndex == answerIndex) {
            playerScores[currentPlayer] = playerScores.getValue(currentPlayer) + 2
            return true
        } else {
            return false
        }
    }

    // Metoda za ažuriranje igre na Firestore-u
    fun updateGame(game: Game, currentPlayerId: String, connection: Connection, callback: (Boolean) -> Unit) {
        // Check the connection
        connection.correct = checkAnswer(connection, connection.answer)
        connection.answered = true
        connection.answeredBy = currentPlayerId

        // Update the player's score
        if (connection.correct) {
            if (game.player1 == currentPlayerId) {
                game.player1Score += 2
            } else if (game.player2 == currentPlayerId) {
                game.player2Score += 2
            }
        }

        game.id?.let { gameId ->
            db.collection("games").document(gameId)
                .update("questionInfo", game.questionInfo, "player1Score", game.player1Score, "player2Score", game.player2Score)
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener {
                    callback(false)
                }
        }

    }

//    fun makeConnection(questionIndex: Int, answerIndex: Int) {
//        val connection = questions[questionIndex]
//        if (connection.answeredBy == null  && checkAnswer(questionIndex, answerIndex)) {
//            val connectionPair = Pair(questionIndex, answerIndex)
//            madeConnections.add(connectionPair)
//            // update score...
//            playerScores[currentPlayer] = playerScores.getValue(currentPlayer) + 2
//            Log.d("GameController", "Correct answer! Score is now: ")
//            // Mark the questions as answered
//            connection.answered = true
//            unansweredConnections.remove(connection)
//            checkGameState()
//        } else {
//            Log.d("GameController", "Incorrect answer or connection already made.")
//        }
//    }

    fun generateNewConnections(): List<Connection> {


        // Shuffle the questions before each game
        questions.shuffle()

        return questions.map { Connection(it.question, it.answer) }
    }
    fun checkGameState() {
        if (questionsAndAnswers.size == 5) {
            // All questions are answered, proceed to the next round or player
            when (gameState) {
                GameState.ROUND_ONE_PLAYER_ONE -> gameState = GameState.ROUND_ONE_PLAYER_TWO
                GameState.ROUND_ONE_PLAYER_TWO -> gameState = GameState.ROUND_TWO_PLAYER_ONE
                GameState.ROUND_TWO_PLAYER_ONE -> gameState = GameState.ROUND_TWO_PLAYER_TWO
                GameState.ROUND_TWO_PLAYER_TWO -> endGame()
            }
        }
    }

    fun endGame() {
        // End the game here
    }

    fun checkAnswer(questionIndex: Int, answerIndex: Int): Boolean {
     //   return questionIndex == answerIndex
        val connection = questions[questionIndex]
        if(questionIndex == answerIndex) {
            connection.answeredBy = currentPlayer.toString()
            return true
        }
        return false
    }
    fun switchPlayer() {
        currentPlayer = if (currentPlayer == 1) 2 else 1

//        when (gameState) {
//            GameState.ROUND_ONE_PLAYER_ONE -> gameState = GameState.ROUND_ONE_PLAYER_TWO
//            GameState.ROUND_ONE_PLAYER_TWO -> gameState = GameState.ROUND_TWO_PLAYER_ONE
//            GameState.ROUND_TWO_PLAYER_ONE -> gameState = GameState.ROUND_TWO_PLAYER_TWO
//            GameState.ROUND_TWO_PLAYER_TWO -> gameState = GameState.ROUND_ONE_PLAYER_ONE
//        }
        when (gameState) {
            GameState.ROUND_ONE_PLAYER_ONE -> gameState = GameState.ROUND_ONE_PLAYER_TWO
            GameState.ROUND_ONE_PLAYER_TWO -> {
                gameState = GameState.ROUND_TWO_PLAYER_ONE
                unansweredQuestionIndices.clear()
                unansweredQuestionIndices.addAll(questions.indices)
            }
            GameState.ROUND_TWO_PLAYER_ONE -> gameState = GameState.ROUND_TWO_PLAYER_TWO
            GameState.ROUND_TWO_PLAYER_TWO -> {
                gameState = GameState.ROUND_ONE_PLAYER_ONE
                unansweredQuestionIndices.clear()
                unansweredQuestionIndices.addAll(questions.indices)
            }
        }
    }
    fun createGame(playerId: String, onQuestionsFetched: (List<Connection>) -> Unit) {
        FirebaseFirestore.getInstance().collection("spojnicaQuestions").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val allQuestions = task.result?.documents?.mapNotNull { it.toObject(Connection::class.java) }

                // Continue only if the questions are fetched successfully
                if (allQuestions != null) {
                    val gameQuestions = allQuestions.shuffled().take(3)

                    val gameRef = db.collection("games").document()
                    val game = Game(
                        id = gameRef.id,
                        player1 = playerId,
                        questionInfo = gameQuestions
                    )

                    gameRef.set(game).addOnSuccessListener {
                        // Ostatak koda za kreiranje igre...

                        // Notify the caller that the questions have been fetched
                        onQuestionsFetched(gameQuestions)
                    }
                } else {
                    // Handle the case where the questions are not fetched successfully
                }
            }
        }
    }

    fun getUnansweredConnections(): List<Connection> {
        return unansweredConnections
    }

    fun getScores(): Map<Int, Int> {
        return playerScores
    }

}
