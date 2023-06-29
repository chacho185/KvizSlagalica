package com.example.igricaslagalica.view

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.example.igricaslagalica.R
import com.example.igricaslagalica.data.PitanjaKoZnaZna
import com.example.igricaslagalica.data.dajPitanja
import com.example.igricaslagalica.databinding.FragmentKoZnaZnaGameBinding

class KoZnaZnaGame : Fragment() {
    private var _binding: FragmentKoZnaZnaGameBinding? = null
    private val binding get() = _binding!!

    private lateinit var questionAdapter: QuestionAdapter
    private lateinit var questionList: List<PitanjaKoZnaZna>
    private var currentQuestionIndex = 0
    private var score = 0
    private lateinit var timer: CountDownTimer
    var currentVisibleItemPosition = 0

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
        // Inicijalizacija pitanja
        questionList = generateQuestions()


        // Inicijalizacija RecyclerView-a
        questionAdapter = QuestionAdapter(questionList)
        val layoutManager = NonScrollableLinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        binding.pitanjaRecyclerView.layoutManager = layoutManager
        binding.pitanjaRecyclerView.adapter = questionAdapter
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.pitanjaRecyclerView)
        // Pokretanje igre
        startGame()
        binding.nextButton.setOnClickListener {
            if(currentVisibleItemPosition == 3)
                binding.nextButton.text = "Done"
            if(currentVisibleItemPosition == 4)
            {
                binding.pitanjaRecyclerView.visibility = View.GONE
                binding.rezultatiView.visibility = View.VISIBLE
                binding.nextButton.text = "Finish"
            }
            if(currentVisibleItemPosition == 5)
            {
                //TODO prebaciti se na sljedecu igricu
            }
            currentVisibleItemPosition++
            binding.pitanjaRecyclerView.smoothScrollToPosition(currentVisibleItemPosition)
        }
    }

    inner class NonScrollableLinearLayoutManager(
        context: Context,
        orientation: Int,
        reverseLayout: Boolean
    ) :
        LinearLayoutManager(context, orientation, reverseLayout) {
        override fun scrollHorizontallyBy(
            dx: Int,
            recycler: RecyclerView.Recycler?,
            state: RecyclerView.State?
        ): Int {
            // Onemogućavanje klizanja u vodoravnom smjeru
            return if (dx > 0) {
                super.scrollHorizontallyBy(dx, recycler, state)
            } else {
                0
            }
        }
    }

    private fun startGame() {
        score = 0
        currentQuestionIndex = 0
        updateScore()
        //showQuestion(currentQuestionIndex)
        startTimer()
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
        timer = object : CountDownTimer(25000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                binding.timerTextView.text = secondsLeft.toString()
            }

            override fun onFinish() {
                evaluateAnswer(null)
            }
        }
        timer.start()
    }

    private fun evaluateAnswer(selectedOption: RadioButton?) {
//        val question = questionList[currentQuestionIndex]
//        val selectedAnswer = selectedOption?.text.toString()
//
//        if (selectedAnswer == question.correctAnswer) {
//            score += 10
//        } else {
//            score -= 5
//        }
//
//        currentQuestionIndex++
//        updateScore()
//
//        if (currentQuestionIndex < questionList.size) {
//            showQuestion(currentQuestionIndex)
//            timer.start()
//        } else {
//            endGame()
//        }
    }

    private fun endGame() {
        timer.cancel()
        // Prikaz rezultata i ostale logike nakon završetka igre
    }

    private fun updateScore() {
//        textViewScore.text = score.toString()
    }

    inner class QuestionAdapter(private val questionList: List<PitanjaKoZnaZna>) :
        RecyclerView.Adapter<QuestionAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val questionTextView: TextView = itemView.findViewById(R.id.questionTextView)
            private val optionsRadioGroup: RadioGroup =
                itemView.findViewById(R.id.optionsRadioGroup)
            val option1RadioButton: RadioButton = itemView.findViewById(R.id.option1RadioButton)
            val option2RadioButton: RadioButton = itemView.findViewById(R.id.option2RadioButton)
            val option3RadioButton: RadioButton = itemView.findViewById(R.id.option3RadioButton)
            val option4RadioButton: RadioButton = itemView.findViewById(R.id.option4RadioButton)

            init {
                optionsRadioGroup.setOnCheckedChangeListener { group, checkedId ->
                    val selectedOption = group.findViewById<RadioButton>(checkedId)
                    val position = adapterPosition
                    val question = questionList[position]
                    question.userAnswer = selectedOption.id
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.ko_zna_zna_question, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val question = questionList[position]
            holder.questionTextView.text = question.questionText
            holder.option1RadioButton.text = question.options[0]
            holder.option2RadioButton.text = question.options[1]
            holder.option3RadioButton.text = question.options[2]
            holder.option4RadioButton.text = question.options[3]
        }

        override fun getItemCount(): Int {
            return questionList.size
        }
    }

}


