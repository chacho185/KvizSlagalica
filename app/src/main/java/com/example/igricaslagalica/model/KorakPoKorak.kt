package com.example.igricaslagalica.model

data class KorakPoKorak(
    val id: Int,
    val odgovor: String,
    val pojmovi: List<String>,
    var bodovi: Int = 0
)