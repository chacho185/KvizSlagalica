package com.example.igricaslagalica.model

data class Game(
    var id: String? = null,
    val player1: String? = null,
    val player2: String? = null,
    val status: String = STATUS_WAITING,
//    var rounds: List<Round> = listOf(),
    var currentRound: Int = 1,
    var player1Score: Int = 0,  // Add score for player1
    var player2Score: Int = 0,
    var currentTurn: String = player1 ?: "",
    var questionInfo: List<Connection> = mutableListOf(),
    var koZnaZnaQuestions: List<KoZnaZna> = mutableListOf(),


    ) {

    companion object {
        const val STATUS_WAITING = "waiting"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_FINISHED = "finished"
    }
}
