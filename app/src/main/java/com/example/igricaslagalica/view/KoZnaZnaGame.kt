package com.example.igricaslagalica.view

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.igricaslagalica.R
import com.example.igricaslagalica.SharedViewModel
import com.example.igricaslagalica.data.PitanjaKoZnaZna
import com.example.igricaslagalica.data.dajPitanja
import com.example.igricaslagalica.databinding.FragmentKoZnaZnaGameBinding

class KoZnaZnaGame : Fragment() {
    private var _binding: FragmentKoZnaZnaGameBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()



    private lateinit var textViewScore: TextView
    private lateinit var questionTextView: TextView
    private lateinit var optionsRadioGroup: RadioGroup
    private lateinit var option1RadioButton: RadioButton
    private lateinit var option2RadioButton: RadioButton
    private lateinit var option3RadioButton: RadioButton
    private lateinit var option4RadioButton: RadioButton

    private lateinit var questionList: List<PitanjaKoZnaZna>
    private var currentQuestionIndex: Int = 0
    private lateinit var timer: CountDownTimer
    private var totalScore: Int = 0
    private var remainingTime: Long = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKoZnaZnaGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        questionTextView = binding.pitanjeView.findViewById(R.id.questionTextView)
        optionsRadioGroup =
            binding.pitanjeView.findViewById(R.id.optionsRadioGroup)
        option1RadioButton = binding.pitanjeView.findViewById(R.id.option1RadioButton)
        option2RadioButton = binding.pitanjeView.findViewById(R.id.option2RadioButton)
        option3RadioButton = binding.pitanjeView.findViewById(R.id.option3RadioButton)
        option4RadioButton = binding.pitanjeView.findViewById(R.id.option4RadioButton)
        textViewScore = binding.textViewScore
        questionList = generateQuestions()


        binding.nextButton.setOnClickListener {
            timer.cancel()
            checkAnswer()
            showNextQuestion()
        }
        binding.finishButton.setOnClickListener {
            sharedViewModel.setQuestionList(questionList)

            val q = sharedViewModel.getQuestionList()
            q.size
            // TODO: sljedeca igra
        }
        showNextQuestion()

    }

    private fun showNextQuestion() {
        if (currentQuestionIndex < 5) {
            if (currentQuestionIndex == 4)
                binding.nextButton.text = "Done"

            resetRadioButtons()

            val question = questionList[currentQuestionIndex]
            questionTextView.text = question.questionText
            option1RadioButton.text = question.options[0]
            option2RadioButton.text = question.options[1]
            option3RadioButton.text = question.options[2]
            option4RadioButton.text = question.options[3]

            currentQuestionIndex++
            startTimer()
        } else {
            binding.pitanjeView.visibility = View.GONE
            binding.textViewScore.visibility = View.VISIBLE
            binding.nextButton.visibility = View.GONE
            binding.finishButton.visibility = View.VISIBLE
            binding.timerTextView.visibility = View.GONE
            updateScore()
            endGame()
        }
    }


    private fun checkAnswer() {
        val currentQuestion = questionList[currentQuestionIndex - 1]
        val selectedAnswerIndex = getSelectedAnswerIndex()

        questionList[currentQuestionIndex - 1].userAnswer = selectedAnswerIndex
        questionList[currentQuestionIndex - 1].remainingTime = remainingTime
        if (selectedAnswerIndex == currentQuestion.correctAnswer) {
            totalScore += 10
        } else {
            if (selectedAnswerIndex != -1)
                totalScore -= 5
        }
    }

    private fun getSelectedAnswerIndex(): Int {
        return when {
            option1RadioButton.isChecked -> 1
            option2RadioButton.isChecked -> 2
            option3RadioButton.isChecked -> 3
            option4RadioButton.isChecked -> 4
            else -> -1
        }
    }

    private fun resetRadioButtons() {
        optionsRadioGroup.clearCheck()
//        for (i in 0 until optionsRadioGroup.childCount) {
//            val radioButton = optionsRadioGroup.getChildAt(i) as RadioButton
//            radioButton.isClickable = true
//        }
    }

    private fun generateQuestions(): List<PitanjaKoZnaZna> {
        val randomQuestions = mutableListOf<PitanjaKoZnaZna>()
        val questions = dajPitanja().shuffled()
        for (i in 0 until 5) {
            randomQuestions.add(questions[i])
        }
        return randomQuestions
    }

    private fun startTimer() {
        timer = object : CountDownTimer(6000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                val secondsLeft = millisUntilFinished / 1000
                binding.timerTextView.text = "$secondsLeft s"
            }

            override fun onFinish() {
//                for (i in 0 until optionsRadioGroup.childCount) {
//                    val radioButton = optionsRadioGroup.getChildAt(i) as RadioButton
//                    radioButton.isClickable = false
//                }
                remainingTime = 0
                binding.nextButton.performClick()
            }
        }
        timer.start()
    }


    private fun endGame() {
        timer.cancel()
        // Prikaz rezultata i ostale logike nakon završetka igre
    }

    private fun updateScore() {
        textViewScore.text = "Ukupan skor je: $totalScore"

    }

}


