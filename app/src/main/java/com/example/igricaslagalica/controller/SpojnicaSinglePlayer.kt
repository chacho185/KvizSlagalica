package com.example.igricaslagalica.controller

import android.util.Log
import com.example.igricaslagalica.model.Connection
import com.example.igricaslagalica.model.Game
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

class SpojnicaSinglePlayer {

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


//        fun startNewRound(game: Game, roundNumber: Int) {
//            // Generate new connections for the round.
//            // Note: You will need to implement the logic for generating the connections.
//            val connections = generateNewConnections()
//
//            val round = Round(id = roundNumber, connections = connections)
//
//            // Add the new round to the game.
//            game.rounds += round
//
//            // Update the game document in Firestore.
//            db.collection("games").document(game.id!!)
//                .update("rounds", game.rounds)
//        }

    fun loadGameData(callback: (List<Connection>, List<Connection>) -> Unit) {
        val connections = generateNewConnections()

        callback(connections, connections)
        // And set up the initial state of the game
        gameState = GameState.ROUND_ONE_PLAYER_ONE
        // Initialize unansweredQuestionIndices with all indices
        unansweredQuestionIndices.addAll(questions.indices)
    }

    fun checkConnection(connection: Connection): Boolean {
        // Here you should implement your logic to check if the connection is correct
        // For now, I'll just check if the `correct` field is true or not
        return connection.correct
    }

    fun makeConnection(questionIndex: Int, answerIndex: Int) {
        val connection = questions[questionIndex]
        if (connection.answeredBy == null  && checkAnswer(questionIndex, answerIndex)) {
            val connectionPair = Pair(questionIndex, answerIndex)
            madeConnections.add(connectionPair)
            // update score...
            playerScores[currentPlayer] = playerScores.getValue(currentPlayer) + 2
            Log.d("GameController", "Correct answer! Score is now: ")
            // Mark the questions as answered
            connection.answered = true
            unansweredConnections.remove(connection)
            checkGameState()
        } else {
            Log.d("GameController", "Incorrect answer or connection already made.")
        }
    }

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
        when (gameState) {
            GameState.ROUND_ONE_PLAYER_ONE -> gameState = GameState.ROUND_ONE_PLAYER_TWO
            GameState.ROUND_ONE_PLAYER_TWO -> gameState = GameState.ROUND_TWO_PLAYER_ONE
            GameState.ROUND_TWO_PLAYER_ONE -> gameState = GameState.ROUND_TWO_PLAYER_TWO
            GameState.ROUND_TWO_PLAYER_TWO -> gameState = GameState.ROUND_ONE_PLAYER_ONE
        }
//        when (gameState) {
//            GameState.ROUND_ONE_PLAYER_ONE -> gameState = GameState.ROUND_ONE_PLAYER_TWO
//            GameState.ROUND_ONE_PLAYER_TWO -> {
//                gameState = GameState.ROUND_TWO_PLAYER_ONE
//                unansweredQuestionIndices.clear()
//                unansweredQuestionIndices.addAll(questions.indices)
//            }
//            GameState.ROUND_TWO_PLAYER_ONE -> gameState = GameState.ROUND_TWO_PLAYER_TWO
//            GameState.ROUND_TWO_PLAYER_TWO -> {
//                gameState = GameState.ROUND_ONE_PLAYER_ONE
//                unansweredQuestionIndices.clear()
//                unansweredQuestionIndices.addAll(questions.indices)
//            }
//        }
    }

    fun getUnansweredConnections(): List<Connection> {
        return unansweredConnections
    }

    fun getScores(): Map<Int, Int> {
        return playerScores
    }
}
