package com.example.igricaslagalica.model

import com.google.firebase.firestore.DocumentReference

data class Game(
    var id: String? = null,
    val player1: String? = null,
    val player2: String? = null,
    val status: String = STATUS_WAITING,
    var currentRound: Int = 1,
    var player1Score: Int = 0,  // Add score for player1
    var player2Score: Int = 0,
    var currentTurn: String = player1 ?: "",
    var questionInfo: List<Connection> = mutableListOf(),
    var koZnaZnaQuestions: List<KoZnaZna> = mutableListOf(),
    var isPlayer1Done: Boolean = false,
    var isPlayer2Done: Boolean = false,
    var asocijacijaQuestions: List<AsocijacijaMultiplayer> = mutableListOf(), // Include the asocijacija questions here
    var interactionsAsocijacija: List<Map<String, Int>> = mutableListOf(), // List of interactions
    var skockoList: List<Skocko> = mutableListOf(),  // The Skocko combination for the current round
    var player1Attempts: List<Skocko> = mutableListOf(), // Player 1's attempts
    var opponentAnswer: String = "", // Player's attempt in korakPoKorak
    var player1AnswerBroj: Double = 0.0,
    var player2AnswerBroj: Double = 0.0,

    var targetNumber: Int = 0,
    var offeredNumbers: MutableList<Int> = mutableListOf(),
    var korakPoKorakQuestions: List<KorakPoKorak> = mutableListOf(),  // The Skocko combination for the current round


) {

    companion object {
        const val STATUS_WAITING = "waiting"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_FINISHED = "finished"
    }
}
