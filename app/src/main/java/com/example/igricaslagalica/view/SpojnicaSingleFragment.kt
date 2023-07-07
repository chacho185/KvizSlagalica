package com.example.igricaslagalica.view

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.igricaslagalica.R
import com.example.igricaslagalica.controller.SpojnicaGameController
import com.example.igricaslagalica.controller.SpojnicaSinglePlayer
import com.example.igricaslagalica.view.adapter.AnswersAdapter
import com.example.igricaslagalica.view.adapter.QuestionsAdapter

class SpojnicaSingleFragment : Fragment() {

    private lateinit var gameController: SpojnicaSinglePlayer
    private lateinit var questionsRecyclerView: RecyclerView
    private lateinit var answersRecyclerView: RecyclerView

    private lateinit var questionsAdapter: QuestionsAdapter
    private lateinit var answersAdapter: AnswersAdapter

    private lateinit var player1Score: TextView
    private lateinit var player2Score: TextView
    var selectedQuestionIndex: Int? = null
    var selectedAnswerIndex: Int? = null

    private lateinit var switchPlayerButton: Button
    private lateinit var timer: CountDownTimer
    private val timerDuration = 30000L // 30 seconds
    private lateinit var timerTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the game controller
        gameController = SpojnicaSinglePlayer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.spojnica_single, container, false)

        // Initialize the RecyclerViews
        questionsRecyclerView = view.findViewById(R.id.questionsRecyclerView)
        answersRecyclerView = view.findViewById(R.id.answersRecyclerView)

        // Set up the RecyclerViews
        questionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        answersRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // set up text view for scores
        player1Score = view.findViewById(R.id.player1Score)
        player2Score = view.findViewById(R.id.player2Score)

        timerTextView = view.findViewById(R.id.gameTimer) // assuming you have a TextView to show the remaining time
        // Initialize timer
        timer = object : CountDownTimer(timerDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerTextView.text = "Time remaining: ${millisUntilFinished / 1000} seconds"
            }

            override fun onFinish() {
                switchPlayer()
            }
        }

        return view // Make sure this line is outside the onCreateView block
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var selectedQuestion: String? = null


        // Load the game data
        gameController.loadGameData { questions, answers ->
            // Initialize the adapters and assign them to the lateinit vars
            questionsAdapter = QuestionsAdapter(questions, this::onQuestionSelected)
            answersAdapter = AnswersAdapter(answers, this::onAnswerSelected)
            // Assign the adapters to the RecyclerViews
            questionsRecyclerView.adapter = questionsAdapter
            answersRecyclerView.adapter = answersAdapter
            timer.start()
        }
        switchPlayerButton = view.findViewById(R.id.submit)
        switchPlayerButton.setOnClickListener {
            findNavController().navigate(R.id.action_spojnica_single_to_asocijacijaGame)
            //switchPlayer()
            //updateScores()
            //timer.start()
        }

    }

    private fun switchPlayer() {
        // stop the timer if it's still counting down
        timer.cancel()

        //gameController.switchPlayer()
        val unansweredQuestions = gameController.getUnansweredConnections()
        questionsAdapter.updateData(unansweredQuestions)
        val correspondingUnansweredAnswers = gameController.getUnansweredConnections()
        answersAdapter.updateData(correspondingUnansweredAnswers)
        // TODO: check if game is over, if not, restart the timer
        timer.start()
    }
    private fun updateScores() {
        val scores = gameController.getScores()
        player1Score.text = "Player 1: ${scores[1]}"
        player2Score.text = "Player 2: ${scores[2]}"
    }

    fun onQuestionSelected(index: Int) {
        selectedQuestionIndex = index
        checkAnswer()
    }

    fun onAnswerSelected(index: Int) {
        selectedAnswerIndex = index
        checkAnswer()
    }

    fun checkAnswer() {
        if (selectedQuestionIndex != null && selectedAnswerIndex != null) {
            gameController.makeConnection(selectedQuestionIndex!!, selectedAnswerIndex!!)
            questionsAdapter.notifyItemChanged(selectedQuestionIndex!!)
            answersAdapter.notifyItemChanged(selectedAnswerIndex!!)
            selectedQuestionIndex = null
            selectedAnswerIndex = null

        }
    }
    companion object {
        @JvmStatic
        fun newInstance() = GameOneFragment()
    }
}
