package com.example.igricaslagalica.model

import com.google.firebase.Timestamp

data class Player(
    val id: String? = null,
    val email: String? = null,
    val username: String? = null,
    var tokens: Int = 0,
    var lastTokenTime: Timestamp = Timestamp.now()

)
