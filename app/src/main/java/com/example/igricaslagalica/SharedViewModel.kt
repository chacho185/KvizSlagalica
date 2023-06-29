package com.example.igricaslagalica
import androidx.lifecycle.ViewModel
import com.example.igricaslagalica.data.PitanjaKoZnaZna

class SharedViewModel : ViewModel() {
    private lateinit var questionList: MutableList<PitanjaKoZnaZna>

    fun setQuestionList(questions: List<PitanjaKoZnaZna>)
    {
        questionList = questions as MutableList<PitanjaKoZnaZna>
    }

    fun getQuestionList():MutableList<PitanjaKoZnaZna>
    {
        return questionList
    }

    // Dodajte ostale potrebne metode i podatke u skladu sa vašim zahtevima
}