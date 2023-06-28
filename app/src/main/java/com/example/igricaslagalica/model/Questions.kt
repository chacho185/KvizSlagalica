package com.example.igricaslagalica.model

data class Question(val text: String, val correctAnswers: String)
val questions = mutableListOf(
    Question("Izvođač 1", "Pesma 1"),
    Question("Izvođač 2", "Pesma 2"),
    // dodajte ostala pitanja ovde
)

// Shuffle the questions before each game
//Collections.shuffle(questions)