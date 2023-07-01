package com.example.igricaslagalica.model

data class KoZnaZna(
    val id: Int,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: Int,
    var userAnswer: Int? = -1,
    var remainingTime: Long = -1
)