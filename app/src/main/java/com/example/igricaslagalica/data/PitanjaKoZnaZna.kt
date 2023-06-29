package com.example.igricaslagalica.data

data class PitanjaKoZnaZna(
    val id: Int,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: Int,
    var userAnswer: Int? = -1,
    var remainingTime: Long = -1
)

val pitanjaKoZnaZnas = listOf(
    PitanjaKoZnaZna(
        1,
        "Koja je najveća država na svijetu po površini?",
        listOf("Rusija", "Kanada", "Kina", "Sjedinjene Američke Države"),
        1
    ),
    PitanjaKoZnaZna(
        2,
        "Koja je najduža rijeka na svijetu?",
        listOf("Nil", "Amazona", "Misisipi", "Jangce"),
        1
    ),
    PitanjaKoZnaZna(
        3,
        "Koja je najviša planina na svijetu?",
        listOf("Mont Everest", "K2", "Kraljica Elizabeta", "Makalu"),
        1
    ),
    PitanjaKoZnaZna(
        4,
        "Koja je najnaseljenija zemlja na svijetu?",
        listOf("Kina", "Indija", "Sjedinjene Američke Države", "Indonezija"),
        1
    ),
    PitanjaKoZnaZna(
        5,
        "Koja je najveća pustinja na svijetu?",
        listOf("Sahara", "Gobi", "Atacama", "Antarktika"),
        1
    ),
    PitanjaKoZnaZna(
        6,
        "Koja je najmanja država na svijetu po površini?",
        listOf("Vatikan", "Monako", "Nauru", "Tuvalu"),
        1
    ),
    PitanjaKoZnaZna(
        7,
        "Koja je najveća svjetska okeanska struja?",
        listOf("Golfska struja", "Kuro-šio", "Bengalska struja", "Kanarska struja"),
        1
    ),
    PitanjaKoZnaZna(
        8,
        "Koja zemlja se nalazi na jugu Afrike?",
        listOf("Južna Afrika", "Alžir", "Egipat", "Nigerija"),
        1
    ),
    PitanjaKoZnaZna(
        9,
        "Koji kontinent ima najveći broj zemalja?",
        listOf("Afrika", "Azija", "Europa", "Južna Amerika"),
        1
    ),
    PitanjaKoZnaZna(
        10,
        "Koji je najveći otok na svijetu?",
        listOf("Grönland", "Nova Gvineja", "Borneo", "Madagaskar"),
        1
    ),
    PitanjaKoZnaZna(
        11,
        "Koja je glavni grad Španije?",
        listOf("Madrid", "Barselona", "Sevilja", "Valensija"),
        1
    ),
    PitanjaKoZnaZna(
        12,
        "Koja je najveća svjetska pustinja od leda?",
        listOf("Antarktika", "Sahara", "Gobi", "Atacama"),
        1
    ),
    PitanjaKoZnaZna(
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
    PitanjaKoZnaZna(
        14,
        "Koja je najveća afrička država po površini?",
        listOf("Alžir", "Kongo", "Sudan", "Libija"),
        1
    ),
    PitanjaKoZnaZna(
        15,
        "Koja je najviša vodopad na svijetu?",
        listOf("Anđeoski vodopad", "Iguazu vodopadi", "Viktorijini vodopadi", "Niagarini vodopadi"),
        1
    ),
    PitanjaKoZnaZna(
        16,
        "Koja je najveća zemlja Južne Amerike?",
        listOf("Brazil", "Argentina", "Kolumbija", "Peru"),
        1
    ),
    PitanjaKoZnaZna(
        17,
        "Koja je najnaseljeniji grad na svijetu?",
        listOf("Tokio", "Delhi", "Šangaj", "Mumbai"),
        1
    ),
    PitanjaKoZnaZna(
        18,
        "Koja je najveća planinska lanac na svijetu?",
        listOf("Himalaji", "Andi", "Apenini", "Rocky Mountains"),
        1
    ),
    PitanjaKoZnaZna(
        19,
        "Koja je najveća evropska država po površini?",
        listOf("Rusija", "Ukrajina", "Francuska", "Španija"),
        1
    ),
    PitanjaKoZnaZna(
        20,
        "Koja je najmanje naseljena država na svijetu?",
        listOf("Nauru", "Tuvalu", "Vatikan", "Maldivi"),
        3
    )
)

fun dajPitanja(): List<PitanjaKoZnaZna> {
    return pitanjaKoZnaZnas
}
