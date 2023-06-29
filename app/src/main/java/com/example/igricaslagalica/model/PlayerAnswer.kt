package com.example.igricaslagalica.model

data class PlayerAnswer(val playerId: String, val question: String, val answer: String)

val playerAnswers = mutableListOf<PlayerAnswer>()
