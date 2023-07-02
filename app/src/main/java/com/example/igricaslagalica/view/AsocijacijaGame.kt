package com.example.igricaslagalica.view

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.igricaslagalica.R
import com.example.igricaslagalica.SharedViewModel
import com.example.igricaslagalica.data.StaticData
import com.example.igricaslagalica.databinding.FragmentAsocijacijaGameBinding


class AsocijacijaGame : Fragment() {

    private var _binding: FragmentAsocijacijaGameBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var timer: CountDownTimer
    private var asocijacija1Buttons = ArrayList<Button>()
    private var asocijacija2Buttons = ArrayList<Button>()
    private var asocijacija3Buttons = ArrayList<Button>()
    private var asocijacija4Buttons = ArrayList<Button>()
    private var zatvorenoPolja1 = 4
    private var zatvorenoPolja2 = 4
    private var zatvorenoPolja3 = 4
    private var zatvorenoPolja4 = 4
    private var neodgovorenihAsocijacija = 4
    private val asocijacije = StaticData.dajAsocijaciju()
    private var oduzmiBodova = 0


    private var bodovi = 0



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAsocijacijaGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        asocijacija1Buttons.add(binding.asocijacija1.findViewById(R.id.hiddenField1))
        asocijacija1Buttons.add(binding.asocijacija1.findViewById(R.id.hiddenField2))
        asocijacija1Buttons.add(binding.asocijacija1.findViewById(R.id.hiddenField3))
        asocijacija1Buttons.add(binding.asocijacija1.findViewById(R.id.hiddenField4))
        asocijacija1Buttons.add(binding.asocijacija1.findViewById(R.id.answerField))

        asocijacija2Buttons.add(binding.asocijacija2.findViewById(R.id.hiddenField1))
        asocijacija2Buttons.add(binding.asocijacija2.findViewById(R.id.hiddenField2))
        asocijacija2Buttons.add(binding.asocijacija2.findViewById(R.id.hiddenField3))
        asocijacija2Buttons.add(binding.asocijacija2.findViewById(R.id.hiddenField4))
        asocijacija2Buttons.add(binding.asocijacija2.findViewById(R.id.answerField))

        asocijacija3Buttons.add(binding.asocijacija3.findViewById(R.id.hiddenField1))
        asocijacija3Buttons.add(binding.asocijacija3.findViewById(R.id.hiddenField2))
        asocijacija3Buttons.add(binding.asocijacija3.findViewById(R.id.hiddenField3))
        asocijacija3Buttons.add(binding.asocijacija3.findViewById(R.id.hiddenField4))
        asocijacija3Buttons.add(binding.asocijacija3.findViewById(R.id.answerField))

        asocijacija4Buttons.add(binding.asocijacija4.findViewById(R.id.hiddenField1))
        asocijacija4Buttons.add(binding.asocijacija4.findViewById(R.id.hiddenField2))
        asocijacija4Buttons.add(binding.asocijacija4.findViewById(R.id.hiddenField3))
        asocijacija4Buttons.add(binding.asocijacija4.findViewById(R.id.hiddenField4))
        asocijacija4Buttons.add(binding.asocijacija4.findViewById(R.id.answerField))

        for(i in 0 until 4)
        {
            asocijacija1Buttons[i].setOnClickListener {
                asocijacija1Buttons[i].setTextColor(Color.WHITE)
                zatvorenoPolja1--
                oduzmiBodova++
            }
            asocijacija2Buttons[i].setOnClickListener {
                asocijacija2Buttons[i].setTextColor(Color.WHITE)
                zatvorenoPolja2--
                oduzmiBodova++
            }
            asocijacija3Buttons[i].setOnClickListener {
                asocijacija3Buttons[i].setTextColor(Color.WHITE)
                zatvorenoPolja3--
                oduzmiBodova++
            }
            asocijacija4Buttons[i].setOnClickListener {
                asocijacija4Buttons[i].setTextColor(Color.WHITE)
                zatvorenoPolja4--
                oduzmiBodova++
            }
        }
        asocijacija1Buttons[4].setOnClickListener {
            val tacanOdgovor = asocijacija1Buttons[4].text.toString().trim()
            val odgovor = binding.odgovorEditText.text.toString().trim()
            if(odgovor.equals(tacanOdgovor, ignoreCase = true))
            {
                asocijacija1Buttons[4].setTextColor(Color.WHITE)
                bodovi += 2 + zatvorenoPolja1
                neodgovorenihAsocijacija--
                oduzmiBodova -= zatvorenoPolja1
            }
            binding.odgovorEditText.setText("")
        }
        asocijacija2Buttons[4].setOnClickListener {
            val tacanOdgovor = asocijacija2Buttons[4].text.toString().trim()
            val odgovor = binding.odgovorEditText.text.toString().trim()
            if(odgovor.equals(tacanOdgovor, ignoreCase = true))
            {
                asocijacija2Buttons[4].setTextColor(Color.WHITE)
                bodovi += 2 + zatvorenoPolja2
                neodgovorenihAsocijacija--
                oduzmiBodova -= zatvorenoPolja2
            }
            binding.odgovorEditText.setText("")
        }
        asocijacija3Buttons[4].setOnClickListener {
            val tacanOdgovor = asocijacija3Buttons[4].text.toString().trim()
            val odgovor = binding.odgovorEditText.text.toString().trim()
            if(odgovor.equals(tacanOdgovor, ignoreCase = true))
            {
                asocijacija3Buttons[4].setTextColor(Color.WHITE)
                bodovi += 2 + zatvorenoPolja3
                neodgovorenihAsocijacija--
                oduzmiBodova -= zatvorenoPolja3
            }
            binding.odgovorEditText.setText("")
        }
        asocijacija4Buttons[4].setOnClickListener {
            val tacanOdgovor = asocijacija4Buttons[4].text.toString().trim()
            val odgovor = binding.odgovorEditText.text.toString().trim()
            if(odgovor.equals(tacanOdgovor, ignoreCase = true))
            {
                asocijacija4Buttons[4].setTextColor(Color.WHITE)
                bodovi += 2 + zatvorenoPolja4
                neodgovorenihAsocijacija--
                oduzmiBodova -= zatvorenoPolja4
            }
            binding.odgovorEditText.setText("")
        }
        binding.finalAnswerField.setOnClickListener {
            val tacanOdgovor = asocijacije.asocijacijaKonacnoRjesenje.trim()
            val odgovor = binding.odgovorEditText.text.toString().trim()
            if(odgovor.equals(tacanOdgovor, ignoreCase = true))
            {
                bodovi += 7 + (6 * neodgovorenihAsocijacija) - oduzmiBodova
                prikaziRjesenje()
            }
            binding.odgovorEditText.setText("")
        }
        binding.finishButton.setOnClickListener {
            findNavController().navigate(R.id.action_asocijacijaGame_to_singlePlayer)
        }
        startGame()
    }

    private fun startGame() {
        napuniPolja()
        startTimer()
    }

    private fun startTimer() {
        timer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                binding.timerTextView.text =
                    String.format("%2d s", secondsLeft)
            }

            override fun onFinish() {
                prikaziRjesenje()
            }
        }
        timer.start()
    }

    private fun napuniPolja() {

        for (i in 0 until 4)
        {
            asocijacija1Buttons[i].text = asocijacije.asocijacije[0].asocijacijaList[i]
            asocijacija2Buttons[i].text = asocijacije.asocijacije[1].asocijacijaList[i]
            asocijacija3Buttons[i].text = asocijacije.asocijacije[2].asocijacijaList[i]
            asocijacija4Buttons[i].text = asocijacije.asocijacije[3].asocijacijaList[i]
        }
        asocijacija1Buttons[4].text = asocijacije.asocijacije[0].asocijacijaRjesenje

        asocijacija2Buttons[4].text = asocijacije.asocijacije[1].asocijacijaRjesenje

        asocijacija3Buttons[4].text = asocijacije.asocijacije[2].asocijacijaRjesenje

        asocijacija4Buttons[4].text = asocijacije.asocijacije[3].asocijacijaRjesenje

    }


    private fun prikaziRjesenje() {
        timer.cancel()
        val tacanOdgovor = asocijacije.asocijacijaKonacnoRjesenje.trim()
        binding.odgovorEditText.visibility = View.GONE
        binding.finalAnswerField.text = tacanOdgovor
        binding.timerTextView.text = "Vasi bodovi su $bodovi"
        binding.finishButton.visibility = View.VISIBLE
    }

}