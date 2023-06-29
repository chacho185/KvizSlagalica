package com.example.igricaslagalica.model

data class Game(
    var id: String? = null,
    val player1: String? = null,
    val player2: String? = null,
    val status: String = STATUS_WAITING,
    var rounds: List<Round> = listOf(),
    val currentRound: Int = 0,
    var questionInfo: List<Connection> = mutableListOf(),
    var player1Score: Int = 0,  // Add score for player1
    var player2Score: Int = 0
) {
    companion object {
        const val STATUS_WAITING = "waiting"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_FINISHED = "finished"
    }
}
