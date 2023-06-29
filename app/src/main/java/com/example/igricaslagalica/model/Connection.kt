package com.example.igricaslagalica.model

data class Connection(
   // val id: Int,
    var question: String = "",
    var answer: String = "",
    var correct: Boolean = false, // This property could hold if the answer was correct or not.
    var answeredBy: String? = null,  // ID of the player who answered this connection
    var answered: Boolean = false
) {
    // No-arg constructor for Firestore
    constructor() : this(
        "",
        "",
        false,
        null,
        false

    )
}

