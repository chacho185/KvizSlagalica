package com.example.igricaslagalica.model

data class Round(
    val id: Int,
    val currentPlayer: String? = null, // ID of the player whose turn it is
    val connections: List<Connection> = listOf()
)
