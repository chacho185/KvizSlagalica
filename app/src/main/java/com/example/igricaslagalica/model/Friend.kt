package com.example.igricaslagalica.model

data class Friend(
    val id: String,
    val username: String,
    val profileImage: Int,
    val monthlyRank: String,
    val stars: Int,
    val isActive: Boolean,
    val isInGame: Boolean
)