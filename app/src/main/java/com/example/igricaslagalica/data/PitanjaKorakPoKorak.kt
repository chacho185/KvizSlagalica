package com.example.igricaslagalica.data

data class PitanjaKorakPoKorak(
    val id: Int,
    val odgovor: String,
    val pojmovi: List<String>,
    var bodovi: Int = 0
) {
    companion object {
        val pitanjaKorakPoKorakList = listOf(
            PitanjaKorakPoKorak(
                id = 1,
                odgovor = "Mjesec",
                pojmovi = listOf(
                    "Prirodni satelit",
                    "Luna",
                    "Noćno nebo",
                    "Eklipsa",
                    "Geze",
                    "Tiranija",
                    "Apolon"
                )
            ),

            PitanjaKorakPoKorak(
                id = 2,
                odgovor = "Fotosinteza",
                pojmovi = listOf(
                    "Biljke",
                    "Sunčeva svjetlost",
                    "Hlorofil",
                    "Kiseonik",
                    "Glukoza",
                    "Stoma",
                    "Plod"
                )
            ),

            PitanjaKorakPoKorak(
                id = 3,
                odgovor = "Plima",
                pojmovi = listOf(
                    "Morske vode",
                    "Gravitacija",
                    "Mjesec",
                    "Geze",
                    "Zaliv",
                    "Poboljšanje",
                    "Okean"
                )
            )
        )

        fun dajPitanja(): List<PitanjaKorakPoKorak> {
            return pitanjaKorakPoKorakList.shuffled()
        }
    }
}