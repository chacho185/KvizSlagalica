package com.example.igricaslagalica.view.multiplayer

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.igricaslagalica.R
import com.example.igricaslagalica.controller.FirebaseGameController
import com.example.igricaslagalica.controller.SpojnicaGameController
import com.example.igricaslagalica.controller.SpojnicaSinglePlayer
import com.example.igricaslagalica.model.Connection
import com.example.igricaslagalica.model.Game
import com.example.igricaslagalica.view.adapter.AnswersAdapter
import com.example.igricaslagalica.view.adapter.QuestionsAdapter
import com.example.igricaslagalica.view.GameOneFragment
import com.google.firebase.auth.FirebaseAuth

class SpojnicaSingleFragmentMulti : Fragment() {

    private lateinit var gameController: SpojnicaGameController
    private lateinit var firebaseGameController: FirebaseGameController

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
    private var hasStartedGame : Boolean = false
    private var showUnansweredQuestions : Boolean = false
    private var isRoundTwo: Boolean = false
    private var isGameDone : Boolean = false


    val currentUserId= FirebaseAuth.getInstance().currentUser?.uid


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the game controller
        gameController = SpojnicaGameController()
        firebaseGameController = FirebaseGameController()
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
               // switchPlayer()
                // gameController.endRound(gameId)
            }
        }

        return view // Make sure this line is outside the onCreateView block
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()

        val gameId = arguments?.getString("gameId")



        if (gameId != null) {
            firebaseGameController.listenForGameChanges(gameId) { updatedGame ->
                if (updatedGame != null && currentUserId != null) {
                        handleGameUpdate(updatedGame, currentUserId)
                        setupSwitchPlayerButton(updatedGame)
                } else {
                    // Handle failure or game not found
                   // handleGameNotFound()
                }
            }
        }
//        setupSwitchPlayerButton(gameId)





    }
    private fun setupAdapters() {
        questionsAdapter = QuestionsAdapter(emptyList(), this::onQuestionSelected)
        answersAdapter = AnswersAdapter(emptyList(), this::onAnswerSelected)
        questionsRecyclerView.adapter = questionsAdapter
        answersRecyclerView.adapter = answersAdapter
    }
    private fun handleGameUpdate(game: Game, currentPlayer: String) {
        //first time
        gameId = game.id.toString()
        if(!hasStartedGame && game.isPlayer1Done && game.isPlayer2Done) {
            hasStartedGame = true
            val currentPlayerTurn = game.currentTurn
            val filteredQuestions = game.questionInfo.filter { it.assignedToPlayer == currentPlayerTurn }
            updateDataForAdapters(filteredQuestions)
            handlePlayerTurn(game, currentPlayer)

        }
        if (currentUserId != null && hasStartedGame) {
            handlePlayerTurn(game, currentUserId)
        }

        // show question for round 2
        if(!isRoundTwo && game.currentRound == 2 ){
            game.player2?.let { it1 ->
                gameController.generateNewQuestions(game, it1) { newQuestions ->
                    game.questionInfo = newQuestions
                    Log.d("TAG", "Udjel u Transition ${game.player2} !@ $newQuestions +++ ")
                    game.id?.let { gameId ->
                        gameController.updateGameField(gameId, "questionInfo",  game.questionInfo) { success ->
                            if (success) {
                                val filteredQuestions = game.questionInfo.filter { it.assignedToPlayer == game.player2 }
                                updateDataForAdapters(filteredQuestions)
                            }

                        }
                    }
                }
            }

            isRoundTwo = true
        }
        // show unanswered question for round
        if(!showUnansweredQuestions && game.currentTurn == game.player2 && game.currentRound == 1){
            val filteredQuestions = game.questionInfo.filter { it.assignedToPlayer == game.player1 && !it.correct }
            updateDataForAdapters(filteredQuestions)
            showUnansweredQuestions = true
        }
        if(!showUnansweredQuestions && game.currentTurn == game.player1 && game.currentRound == 2){
            val filteredQuestions = game.questionInfo.filter { it.assignedToPlayer == game.player2 && !it.correct }
            updateDataForAdapters(filteredQuestions)
            showUnansweredQuestions = true
        }
        //next button on the end of game
        if(isGameDone || game.currentRound == 3) {

            switchPlayerButton.text = "Next game"
            switchPlayerButton.isEnabled = true
            currentPlayerTurn.text =  "This game is done and you need to press Next game to go to next game"

            questionsAdapter.isInteractionEnabled = false
            answersAdapter.isInteractionEnabled = false // Disable interaction for player two
            timer.start()
        }
    }
    private fun updateDataForAdapters(filteredQuestions: List<Connection>) {
        questionsAdapter.updateData(filteredQuestions)
        answersAdapter.updateData(filteredQuestions)
    }

    private fun handlePlayerTurn(game: Game, currentPlayer: String) {
        if (game.currentTurn == currentPlayer) {
            // It's the current player's turn. Enable the UI.
            enableTurnForCurrentPlayer(game, currentPlayer)
        } else {
            // It's not the current player's turn. Disable the UI.
            disableTurnForCurrentPlayer()
        }
    }

    private fun enableTurnForCurrentPlayer(game: Game, currentPlayerId: String) {
        switchPlayerButton.isEnabled = true
        getCurrentPlayer(currentPlayerId, game.currentRound)
        questionsAdapter.isInteractionEnabled = true
        answersAdapter.isInteractionEnabled = true
       // timer.start()
    }

    private fun disableTurnForCurrentPlayer() {
        switchPlayerButton.isEnabled = false
        currentPlayerTurn.text =  "Wait for your turn "
        questionsAdapter.isInteractionEnabled = false
        answersAdapter.isInteractionEnabled = false // Disable interaction for player two

        //  timer.cancel()
    }


private fun setupSwitchPlayerButton(game: Game) {
    switchPlayerButton.setOnClickListener {
        game.id?.let { gameId ->
            if (game.currentRound == 1 && game.currentTurn == game.player2) {
                // Case 2: Update only round

                game.currentRound++
                gameController.updateGameField(gameId, "currentRound", game.currentRound) { success ->
                    if (success) {
                        // Continue Round 1 for Player 2
                    }

                }

            } else if (game.currentRound == 2 && game.currentTurn == game.player1) {
                // Case 4: End of game
                isGameDone = true
                game.currentRound++
                gameController.updateGameField(gameId, "currentRound", game.currentRound) { success ->
                    if (success) {
                        // Continue Round 1 for Player 2
                    }

                }
            } else {
                // Inside your main game flow
                if (currentUserId == game.player1) {
                    gameController.handleRound1(game) { success ->
                        if (success) {
                            // Player 2 starts Round 2
                        } else {
                            // Continue Round 1 for Player 2
                        }
                    }
                } else if (currentUserId == game.player2 && game.currentRound == 2) {

                    gameController.handleRound2(game) { success ->
                        if (success) {
                            // Game ends
                            isGameDone = true

                        } else {
                            // Continue Round 2 for Player 1
                        }
                    }
                }

                handleGameAfterAnswer(game, gameId)
            }
            if(switchPlayerButton.text == "Next game"){
                gameController.updateGameField(gameId, "currentRound", 1) { success ->
                    if (success) {
                        // Continue Round 1 for Player 2
                    }

                }
                val bundle = bundleOf("gameId" to gameId)
                findNavController().navigate(R.id.action_spojnicaSingleFragmentMulti_to_asocijacijaGameMulti, bundle)
            }
        }

    }
}

    private fun handleGameAfterAnswer(game: Game, gameId: String) {
        val connection = game.questionInfo[selectedQuestionIndex!!]
        gameController.updateGameAfterAnswer(game, game.currentTurn, connection) { success ->
            if (success) {
                updateScore(game,game.player1Score, game.player2Score)
                switchTurnAndCheckGameEnd(game, gameId)
            } else {
                // Handle error
            }
        }
    }

    private fun switchTurnAndCheckGameEnd(game: Game, gameId: String) {
        if (currentUserId != null) {
            gameController.switchTurn(game, currentUserId) { success ->
                if (success) {
                    getCurrentPlayer(game.currentTurn, game.currentRound)
                    // switchPlayer(game)
                    if(game.currentRound == 2 && game.currentTurn == game.player1 ){
                        val bundle = bundleOf("gameId" to gameId)
                        // findNavController().navigate(R.id.action_loginFragment_to_multiPlayerAsocijacije, bundle)

                    }
                    if(game.currentRound == 1 && game.currentTurn != game.player1 ){
                        val bundle = bundleOf("gameId" to gameId)
                        //findNavController().navigate(R.id.action_loginFragment_to_multiPlayerAsocijacije, bundle)

                    }
                } else {
                    // Handle error
                }
            }
        }
    }

    private fun updateScore(game: Game, scorePlayer1: Int, scorePlayer2: Int){
        player1Score.text = "Player 1: $scorePlayer1"
        player2Score.text = "Player 2: $scorePlayer2"
        var player1FinalScore = scorePlayer1
        var player2FinalScore = scorePlayer2

        player1FinalScore += game.player1Score
        player2FinalScore += game.player2Score

        gameController.saveResultToDatabase(gameId, player1FinalScore, player2FinalScore)

    }

    private fun getCurrentPlayer(currentPlayerId: String, round: Int){
        gameController.getPlayerName(currentPlayerId) { playerName ->
            currentPlayerTurn.text =  "Round $round: $playerName playing" ?: "Unknown Player"
        }
    }
    private fun switchPlayer(game:Game) {
        // stop the timer if it's still counting down
        timer.cancel()
        // Switch the current turn between player 1 and player 2
      //  val newTurn = if (currentPlayerId == game.currentTurn) game.player2 else game.player1

        if (currentUserId != null) {
            gameController.switchTurn(game,currentUserId) { success ->
                if (success) {
                    // Restart the timer for the new player's turn
                    timer.start()
                } else {
                    // Handle error
                }
            }
        }

        //  gameController.switchPlayer()
//        val unansweredQuestions = gameController.getUnansweredConnections()
//        questionsAdapter.updateData(unansweredQuestions)
//        val correspondingUnansweredAnswers = gameController.getUnansweredConnections()
//        answersAdapter.updateData(correspondingUnansweredAnswers)
//        // TODO: check if game is over, if not, restart the timer
        timer.start()
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
                        Log.d("trest", "Connection abi $connection")
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

    }
    companion object {
        @JvmStatic
        fun newInstance() = GameOneFragment()
    }
}
