package com.example.igricaslagalica.data

import com.example.igricaslagalica.model.Asocijacija
import com.example.igricaslagalica.model.Asocijacije
import com.example.igricaslagalica.model.KoZnaZna
import com.example.igricaslagalica.model.KorakPoKorak

class StaticData() {
    companion object {

        private val pitanjaKoZnaZnas = listOf(
            KoZnaZna(
                1,
                "Koja je najveća država na svijetu po površini?",
                listOf("Rusija", "Kanada", "Kina", "Sjedinjene Američke Države"),
                1
            ),
            KoZnaZna(
                2,
                "Koja je najduža rijeka na svijetu?",
                listOf("Nil", "Amazona", "Misisipi", "Jangce"),
                1
            ),
            KoZnaZna(
                3,
                "Koja je najviša planina na svijetu?",
                listOf("Mont Everest", "K2", "Kraljica Elizabeta", "Makalu"),
                1
            ),
            KoZnaZna(
                4,
                "Koja je najnaseljenija zemlja na svijetu?",
                listOf("Kina", "Indija", "Sjedinjene Američke Države", "Indonezija"),
                1
            ),
            KoZnaZna(
                5,
                "Koja je najveća pustinja na svijetu?",
                listOf("Sahara", "Gobi", "Atacama", "Antarktika"),
                1
            ),
            KoZnaZna(
                6,
                "Koja je najmanja država na svijetu po površini?",
                listOf("Vatikan", "Monako", "Nauru", "Tuvalu"),
                1
            ),
            KoZnaZna(
                7,
                "Koja je najveća svjetska okeanska struja?",
                listOf("Golfska struja", "Kuro-šio", "Bengalska struja", "Kanarska struja"),
                1
            ),
            KoZnaZna(
                8,
                "Koja zemlja se nalazi na jugu Afrike?",
                listOf("Južna Afrika", "Alžir", "Egipat", "Nigerija"),
                1
            ),
            KoZnaZna(
                9,
                "Koji kontinent ima najveći broj zemalja?",
                listOf("Afrika", "Azija", "Europa", "Južna Amerika"),
                1
            ),
            KoZnaZna(
                10,
                "Koji je najveći otok na svijetu?",
                listOf("Grönland", "Nova Gvineja", "Borneo", "Madagaskar"),
                1
            ),
            KoZnaZna(
                11,
                "Koja je glavni grad Španije?",
                listOf("Madrid", "Barselona", "Sevilja", "Valensija"),
                1
            ),
            KoZnaZna(
                12,
                "Koja je najveća svjetska pustinja od leda?",
                listOf("Antarktika", "Sahara", "Gobi", "Atacama"),
                1
            ),
            KoZnaZna(
                13,
                "Koja je najveća jezera na svijetu po površini?",
                listOf(
                    "Kaspijsko jezero",
                    "Sjevernoamerički Veliki jezera",
                    "Bajkalsko jezero",
                    "Viktorijansko jezero"
                ),
                1
            ),
            KoZnaZna(
                14,
                "Koja je najveća afrička država po površini?",
                listOf("Alžir", "Kongo", "Sudan", "Libija"),
                1
            ),
            KoZnaZna(
                15,
                "Koja je najviša vodopad na svijetu?",
                listOf(
                    "Anđeoski vodopad",
                    "Iguazu vodopadi",
                    "Viktorijini vodopadi",
                    "Niagarini vodopadi"
                ),
                1
            ),
            KoZnaZna(
                16,
                "Koja je najveća zemlja Južne Amerike?",
                listOf("Brazil", "Argentina", "Kolumbija", "Peru"),
                1
            ),
            KoZnaZna(
                17,
                "Koja je najnaseljeniji grad na svijetu?",
                listOf("Tokio", "Delhi", "Šangaj", "Mumbai"),
                1
            ),
            KoZnaZna(
                18,
                "Koja je najveća planinska lanac na svijetu?",
                listOf("Himalaji", "Andi", "Apenini", "Rocky Mountains"),
                1
            ),
            KoZnaZna(
                19,
                "Koja je najveća evropska država po površini?",
                listOf("Rusija", "Ukrajina", "Francuska", "Španija"),
                1
            ),
            KoZnaZna(
                20,
                "Koja je najmanje naseljena država na svijetu?",
                listOf("Nauru", "Tuvalu", "Vatikan", "Maldivi"),
                3
            )
        )

        private val pitanjaKorakPoKorakList = listOf(
            KorakPoKorak(
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

            KorakPoKorak(
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

            KorakPoKorak(
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

        val asocijacijeData = Asocijacije(
            asocijacije = listOf(
                Asocijacija(
                    asocijacijaList = listOf("Pizza", "Spaghetti", "Lasagna", "Ravioli"),
                    asocijacijaRjesenje = "Talijanska"
                ),
                Asocijacija(
                    asocijacijaList = listOf("Sushi", "Ramen", "Tempura", "Teriyaki"),
                    asocijacijaRjesenje = "Japanska"
                ),
                Asocijacija(
                    asocijacijaList = listOf("Tortilla", "Nachos", "Burrito", "Enchiladas"),
                    asocijacijaRjesenje = "Meksička"
                ),
                Asocijacija(
                    asocijacijaList = listOf("Curry", "Biryani", "Naan", "Samosa"),
                    asocijacijaRjesenje = "Indijska"
                )
            ),
            asocijacijaKonacnoRjesenje = "Hrana"
        )

        fun dajAsocijaciju() : Asocijacije
        {
            return asocijacijeData
        }
        fun dajPitanjeKorakPoKorak(): List<KorakPoKorak> {
            return pitanjaKorakPoKorakList.shuffled()
        }
        fun dajPitanjaKoZnaZna(): List<KoZnaZna> {
            return pitanjaKoZnaZnas
        }
    }
}

