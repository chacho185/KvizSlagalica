package com.example.igricaslagalica.view

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.igricaslagalica.R
import com.example.igricaslagalica.controller.SpojnicaGameController
import com.example.igricaslagalica.model.Game
import com.example.igricaslagalica.view.adapter.AnswersAdapter
import com.example.igricaslagalica.view.adapter.QuestionsAdapter
import com.google.firebase.firestore.FirebaseFirestore

class GameOneFragment : Fragment() {

    private lateinit var gameController: SpojnicaGameController
    private lateinit var questionsRecyclerView: RecyclerView
    private lateinit var answersRecyclerView: RecyclerView

    private lateinit var questionsAdapter: QuestionsAdapter
    private lateinit var answersAdapter: AnswersAdapter

    private lateinit var player1Score: TextView
    private lateinit var player2Score: TextView
    var selectedQuestionIndex: Int? = 0
    var selectedAnswerIndex: Int? = 0

    private lateinit var switchPlayerButton: Button
    private lateinit var timer: CountDownTimer
    private val timerDuration = 30000L // 30 seconds
    private lateinit var timerTextView: TextView
    private lateinit var currentPlayerTurn: TextView
    private var gameId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the game controller
        gameController = SpojnicaGameController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gameone, container, false)

        // Initialize the RecyclerViews
        questionsRecyclerView = view.findViewById(R.id.questionsRecyclerView)
        answersRecyclerView = view.findViewById(R.id.answersRecyclerView)

        // Set up the RecyclerViews
        questionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        answersRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // set up text view for scores
        player1Score = view.findViewById(R.id.player1Score)
        player2Score = view.findViewById(R.id.player2Score)

        timerTextView = view.findViewById(R.id.gameTimer)
        switchPlayerButton = view.findViewById(R.id.submit)
        currentPlayerTurn = view.findViewById(R.id.currentPlayerTurn)
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

        questionsAdapter = QuestionsAdapter(emptyList(), this::onQuestionSelected)
        answersAdapter = AnswersAdapter(emptyList(), this::onAnswerSelected)
        questionsRecyclerView.adapter = questionsAdapter
        answersRecyclerView.adapter = answersAdapter

        val gameId = arguments?.getString("gameId")
        val loadingIndicator: ProgressBar = view.findViewById(R.id.loadingIndicator)
        val sharedPreferences = activity?.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val currentPlayerId = sharedPreferences?.getString("currentPlayerId", null)

        if (gameId != null && currentPlayerId != null) {
            loadingIndicator.visibility = View.VISIBLE

            gameController.watchGame(gameId) { game ->
                loadingIndicator.visibility = View.GONE
                if (game != null) {
                    questionsAdapter.updateData(game.questionInfo)
                   // questionsAdapter.notifyDataSetChanged()
                    answersAdapter.updateData(game.questionInfo)
                    // Check if it's the current player's turn
                    if (game.currentTurn == currentPlayerId) {
                        // It's the current player's turn. Enable the UI.
                        switchPlayerButton.isEnabled = true
                        // TODO: Enable other UI elements

                        getCurrentPlayer(currentPlayerId)
                        timer.start()
                    } else {
                        // It's not the current player's turn. Disable the UI.
                        switchPlayerButton.isEnabled = false
                        // TODO: Disable other UI elements

                        //getPlayerName(currentPlayerId)
                        timer.cancel()
                    }
                } else {
                    // Obradite situaciju kad je igra završena ili ne postoji
                }
            }
        }

        switchPlayerButton.setOnClickListener {

//                     switchPlayer()
//                     updateScores()
            if (gameId != null) {
                gameController.getGame(gameId) { game ->
                    if (game != null) {
                        //  switchPlayer()
                        //   updateScores()

                        Log.w(ContentValues.TAG, "Listen failed panjevi. ${game.questionInfo.size} ++ $selectedQuestionIndex")
                        val connection = game.questionInfo[selectedQuestionIndex!!]
                        //gameController.checkAnswer()
                        gameController.updateGameAfterAnswer(game, game.currentTurn, connection) { success ->
                            if (success) {
                                Log.w(TAG, "Player turn ID ${game.currentTurn} ")
                                // If the update was successful, switch turn
                                val scorePlayer1 = game.player1Score.toString()
                                val scorePlayer2 = game.player2Score.toString()
                                updateScore(scorePlayer1, scorePlayer2)
                                gameController.switchTurn(game, game.currentTurn) { success1 ->
                                    if (success1) {
                                        Log.w(TAG, "Player turn ID 23 =  ${game.currentTurn} ")
                                        getCurrentPlayer(game.currentTurn)
                                        // The turn has been switched successfully
                                        // TODO: Check if the game has ended. If not, restart the timer
                                    } else {
                                        // Handle error
                                    }
                                }
                            } else {
                                // Handle error
                            }
                        }

                    } else {
                        // Handle error
                    }
                }
            }



        }



    }

    private fun updateScore(scorePlayer1: String, scorePlayer2: String){
        player1Score.text = "Player 1: $scorePlayer1"
        player2Score.text = "Player 2: $scorePlayer2"
    }
    private fun getCurrentPlayer(currentPlayerId: String){
        gameController.getPlayerName(currentPlayerId) { playerName ->
            currentPlayerTurn.text =  "$playerName playing" ?: "Unknown Player"
        }
    }
    private fun switchPlayer() {
        // stop the timer if it's still counting down
        timer.cancel()

      //  gameController.switchPlayer()
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
        switchPlayerButton.isEnabled = selectedQuestionIndex != null && selectedAnswerIndex != null
        checkAnswer()
    }

    fun onAnswerSelected(index: Int) {
        selectedAnswerIndex = index
        switchPlayerButton.isEnabled = selectedQuestionIndex != null && selectedAnswerIndex != null
        checkAnswer()
    }
    fun checkAnswer() {
        val gameId = arguments?.getString("gameId")
        if (selectedQuestionIndex != null && selectedAnswerIndex != null) {
            if (gameId != null) {
                gameController.getGame(gameId) { game ->
                    if (game != null) {
                        val connection = game.questionInfo[selectedQuestionIndex!!]
                        val userAnswer = // get user's answer here

                        gameController.answerQuestion(game, game.currentTurn, connection, selectedQuestionIndex!!, selectedAnswerIndex!!  ) { success ->
                                if (success) {
                                    // Update the UI to reflect the new game state
                                } else {
                                    // Handle failure
                                }
                            }
                    }
                }
            }
        }




//        if (selectedQuestionIndex != null && selectedAnswerIndex != null) {
//            val correct = gameController.processAnswer(selectedQuestionIndex!!, selectedAnswerIndex!!)
//            if (correct) {
//                // Mark the questions and answers as selected
//                questionsAdapter.notifyItemChanged(selectedQuestionIndex!!)
//                answersAdapter.notifyItemChanged(selectedAnswerIndex!!)
//
//            }
//
////         Reset selected indexes
////              selectedQuestionIndex = null
////                selectedAnswerIndex = null
//        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = GameOneFragment()
    }
}
