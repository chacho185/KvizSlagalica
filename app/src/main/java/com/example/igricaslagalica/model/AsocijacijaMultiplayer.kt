package com.example.igricaslagalica.model

data class AsocijacijaMultiplayer(
    val asocijacijaList: List<String>,
    val asocijacijaListOne: List<String>,
    val asocijacijaTwo: List<String>,
    val asocijacijaThree: List<String>,
    val asocijacijaKonacnoRjesenje: String,
    var answeredBy: String? = null,  // ID of the player who answered this connection
    var assignedToPlayer: String? = null
) {
    // Add a default, no-argument constructor
    constructor() : this(
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        "",
        null)
}
