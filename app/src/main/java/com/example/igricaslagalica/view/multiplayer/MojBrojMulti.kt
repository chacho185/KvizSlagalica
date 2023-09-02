package com.example.igricaslagalica.view.multiplayer

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.igricaslagalica.R
import com.example.igricaslagalica.SharedViewModel
import com.example.igricaslagalica.controller.FirebaseGameController
import com.example.igricaslagalica.controller.KoZnaZnaController
import com.example.igricaslagalica.controller.MojBrojMultiController
import com.example.igricaslagalica.databinding.FragmentMojBrojBinding
import com.example.igricaslagalica.model.Game
import com.google.firebase.auth.FirebaseAuth
import kotlin.random.Random
import net.objecthunter.exp4j.ExpressionBuilder


class MojBrojMulti : Fragment() {
    private var _binding: FragmentMojBrojBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var mojBrojController: MojBrojMultiController
    private lateinit var firebaseGameController: FirebaseGameController

    private lateinit var timer: CountDownTimer
    private var targetNumber: Int = 0
    private var daLiJeIgraPocela: Boolean = false
    private var matematickiIzraz: String = ""
    private var rezultat: Double = 0.0
    
    private lateinit var number1TextView: TextView
    private lateinit var number2TextView: TextView
    private lateinit var number3TextView: TextView
    private lateinit var number4TextView: TextView
    private lateinit var number5TextView: TextView
    private lateinit var number6TextView: TextView
    private lateinit var resultString: TextView
    private var gameId: String = ""
    val currentUserId= FirebaseAuth.getInstance().currentUser?.uid
    private var currentGame: Game = Game()
    var offeredNumbers: MutableList<Int> = mutableListOf()
    private var hasStartedGame: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the game controller
        mojBrojController = MojBrojMultiController()
        firebaseGameController = FirebaseGameController()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMojBrojBinding.inflate(inflater, container, false)
        return binding.root
    }
    private var switchTurnAndCheckGameEndRequested = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameUI()
        gameId = arguments?.getString("gameId").toString()


        if (gameId != null) {
            firebaseGameController.listenForGameChanges(gameId) { updatedGame ->
                if (updatedGame != null && currentUserId != null) {
                    handleUI(updatedGame)
                    currentGame = updatedGame
                } else {
                    // Handle failure or game not found
                    // handleGameNotFound()
                }
            }
        }

        binding.stopButton.setOnClickListener {
            if (!daLiJeIgraPocela)
                generateTrazeniBroj()
            if (daLiJeIgraPocela) {
                generatePonudjeneBrojeve()
                binding.stopButton.isEnabled = false
            }
            daLiJeIgraPocela = true
        }

        izvrsiMatematickeOperacije()
        binding.calculateAndFinishButton.setOnClickListener {
            izracunajDobijeniIzraz()
            stopGame()

             if(binding.calculateAndFinishButton.text == "Finish") {
                if (gameId != null) {
//                    val bundle = bundleOf("gameId" to gameId)
                    findNavController().navigate(R.id.action_mojBrojMulti_to_fragment_gameone)

                }
            }

          //  binding.calculateAndFinishButton.text = "Finish"
        }
    }
    private  fun handleUI(game: Game){
        if(!hasStartedGame){
            hasStartedGame = true
          //  generateNumberAfterFiveSeconds()
        }
        if(currentUserId != null){
            handlePlayerTurn(game,currentUserId)
        }
        targetNumber = game.targetNumber
        if(targetNumber != 0){
            binding.targetNumberTextView.text = game.targetNumber.toString()
        }
        if(!game.offeredNumbers.isNullOrEmpty() ) {
            offeredNumbers = game.offeredNumbers
            showOfferedNumbers()
        }
        if(game.player1AnswerBroj != 0.0){
            if(currentGame.player1 == currentUserId ) {
                resultString.text = game.player1AnswerBroj.toString()
            }
        }
        if(game.player2AnswerBroj != 0.0){
            if(currentGame.player2 == currentUserId ) {
                resultString.text = game.player2AnswerBroj.toString()
            }
        }
        if(game.isPlayer1Done && game.isPlayer2Done && game.currentRound == 1){

            calculateScores()
        //    endOfRound1()
//            switchTurnAndCheckGameEnd()
//            startTimer()
     //       resetForRound2()
        }

    }

    private fun generateTrazeniBroj() {
        // Generiraj novi traženi broj za svaku rundu
        targetNumber = Random.nextInt(100, 1000)
        mojBrojController.updateGameField(gameId,"targetNumber", targetNumber) {}

        // Prikazi traženi broj
        binding.targetNumberTextView.text = targetNumber.toString()
    }

    private fun generatePonudjeneBrojeve() {
        for (i in 1..4) {
            offeredNumbers.add(Random.nextInt(1, 10))
        }
        offeredNumbers.add(listOf(10, 15, 20).random())
        offeredNumbers.add(listOf(25, 50, 75, 100).random())

        mojBrojController.updateGameField(gameId,"offeredNumbers", offeredNumbers) {
            
        }
        showOfferedNumbers()
    }

    private fun showOfferedNumbers(){
        number1TextView.text = offeredNumbers[0].toString()
        number2TextView.text = offeredNumbers[1].toString()
        number3TextView.text = offeredNumbers[2].toString()
        number4TextView.text = offeredNumbers[3].toString()
        number5TextView.text = offeredNumbers[4].toString()
        number6TextView.text = offeredNumbers[5].toString()
    }
    var secondsLeft:Long = 0L
    private fun startTimer() {
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                 secondsLeft = millisUntilFinished / 1000
                if (secondsLeft < 55 && !daLiJeIgraPocela ) {
                    binding.stopButton.performClick()
                    binding.stopButton.performClick()

                }
                binding.timerTextView.text =
                    String.format("%2d s", secondsLeft)
            }

            override fun onFinish() {
                binding.calculateAndFinishButton.performClick()
            }
        }
        timer.start()
    }
    private fun endOfRound1() {
        timer.cancel()
      var  timerEnd = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
               val secondsLeft = millisUntilFinished / 1000
                binding.timerTextView.text =
                    String.format("New round will start in %2d s", secondsLeft)
            }

            override fun onFinish() {
                switchTurnAndCheckGameEnd()
                startTimer()

            }
        }
        timerEnd.start()
    }
    private fun izvrsiMatematickeOperacije() {
        binding.numbersLayout.findViewById<CardView>(R.id.number1CardView).setOnClickListener {
            matematickiIzraz += number1TextView.text
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.number2CardView).setOnClickListener {
            matematickiIzraz += number2TextView.text
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.number3CardView).setOnClickListener {
            matematickiIzraz += number3TextView.text
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.number4CardView).setOnClickListener {
            matematickiIzraz += number4TextView.text
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.number5CardView).setOnClickListener {
            matematickiIzraz += number5TextView.text
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.number6CardView).setOnClickListener {
            matematickiIzraz += number6TextView.text
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.plusCardView).setOnClickListener {
            matematickiIzraz += "+"
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.minusCardView).setOnClickListener {
            matematickiIzraz += "-"
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.putaCardView).setOnClickListener {
            matematickiIzraz += "*"
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.otvorenaCardView).setOnClickListener {
            matematickiIzraz += "("
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.zatvorenaCardView).setOnClickListener {
            matematickiIzraz += ")"
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.podjeljenoCardView).setOnClickListener {
            matematickiIzraz += "/"
            resultString.text = matematickiIzraz
        }
    }

    private fun izracunajDobijeniIzraz() {
        val expressionString = resultString.text.toString()
        try {
            val expression = ExpressionBuilder(expressionString).build()
            rezultat = expression.evaluate()
            if(currentGame.player1 == currentUserId) {
                mojBrojController.updateGameField(gameId, "player1AnswerBroj", rezultat){}
                mojBrojController.updateGameField(gameId, "player1Done", true){}

            } else {
                mojBrojController.updateGameField(gameId, "player2AnswerBroj", rezultat){}
                mojBrojController.updateGameField(gameId, "player2Done", true){}

            }

            resultString.text = rezultat.toString()

         //   calculateScores()

        } catch (e: Exception) {
            resultString.text = "0"
        }
    }
    private fun calculateScores(){
        // Calculate the difference between the result and the target number
        val difference = Math.abs(rezultat - targetNumber)
        var playerScore = 0
        var opponentScore = 0
        // Implement scoring rules
        if (rezultat == targetNumber.toDouble()) {
            // Player wins 20 points if the result matches the target number exactly
            playerScore += 20
        } else if (playerHasRequiredNumber(rezultat)) {
            // Player wins 20 points if they have the required number
            playerScore += 20
        } else if (opponentHasRequiredNumber(rezultat)) {
            // Opponent wins 20 points if they have the required number
            opponentScore += 20
        } else if (rezultat != 0.0 && currentGame.player2AnswerBroj != 0.0 && rezultat == currentGame.player1AnswerBroj) {
            // Both players have the same result (not equal to 0)
            // Award 5 points to the player whose turn it was
            if (currentGame.currentTurn == currentUserId) {
                playerScore += 5
            } else {
                opponentScore += 5
            }
        } else {
            // Calculate points based on how close the result is to the target number
            val points = calculatePointsForCloseness(currentGame.player1AnswerBroj, currentGame.player2AnswerBroj)
            playerScore += points
        }

        if(currentGame.player1 == currentUserId) {
            playerScore += currentGame.player1Score
            mojBrojController.saveResultPlayer1(gameId, playerScore)
        } else {
            opponentScore += currentGame.player2Score
            mojBrojController.saveResultPlayer2(gameId, opponentScore)
        }



    }
    private fun playerHasRequiredNumber(result: Double): Boolean {
        // Check if the player's result matches the target number exactly
        return result == currentGame.targetNumber.toDouble()
    }

    private fun opponentHasRequiredNumber(result: Double): Boolean {
        // Check if the opponent's result matches the target number exactly
        return result == currentGame.targetNumber.toDouble()
    }
    private fun calculatePointsForCloseness(playerResult: Double, opponentResult: Double) : Int {
        // Calculate the absolute difference between player's and opponent's results
        val playerDifference = Math.abs(playerResult - currentGame.targetNumber)
        val opponentDifference = Math.abs(opponentResult - currentGame.targetNumber)

        if (playerDifference < opponentDifference) {
            // Player is closer to the target, award 5 points
            return 5
        } else if (opponentDifference < playerDifference) {
            // Opponent is closer to the target, award 5 points to opponent
            return 5
        } else {
            // Both are equally close, no points awarded
            return 0
        }
    }
    private fun resetForRound2(){
        binding.numbersTextView.visibility = View.VISIBLE
        binding.numbersLayout.visibility = View.VISIBLE
        targetNumber = 0
        binding.targetNumberTextView.text = "Trazeni broj"

            number1TextView.text = "Broj 1"
            number2TextView.text = "Broj 2"
            number3TextView.text = "Broj 3"
            number4TextView.text = "Broj 4"
            number5TextView.text = "Broj 5"
            number6TextView.text = "Broj 6"
        binding.resultTextView.text = "Rezultat"

    }
    private fun stopGame() {
        // Zaustavi trenutnu rundu
        timer.cancel()
        binding.numbersTextView.visibility = View.GONE
        binding.numbersLayout.visibility = View.GONE
        if(currentGame.currentRound == 1 || currentGame.currentRound == 2){
            binding.calculateAndFinishButton.text = "Next round"

        } else {
            binding.calculateAndFinishButton.text = "Finish"
        }

    }
    private fun gameUI(){
        number1TextView = binding.numbersLayout.findViewById<TextView>(R.id.number1TextView)
        number2TextView = binding.numbersLayout.findViewById<TextView>(R.id.number2TextView)
        number3TextView = binding.numbersLayout.findViewById<TextView>(R.id.number3TextView)
        number4TextView = binding.numbersLayout.findViewById<TextView>(R.id.number4TextView)
        number5TextView = binding.numbersLayout.findViewById<TextView>(R.id.number5TextView)
        number6TextView = binding.numbersLayout.findViewById<TextView>(R.id.number6TextView)
        resultString = binding.resultTextView

        startTimer()
    }
    private fun handlePlayerTurn(game: Game, currentPlayerId: String) {
        if (game.currentTurn == currentPlayerId) {
            // It's the current player's turn. Enable the UI.
            enableTurnForCurrentPlayer(game, currentPlayerId)

        } else {
            // It's not the current player's turn. Disable the UI.
            disableTurnForCurrentPlayer()

        }
    }

    private fun enableTurnForCurrentPlayer(game: Game, currentPlayerId: String) {
        binding.stopButton.visibility = View.VISIBLE
        binding.stopButton.isEnabled = true
    }

    private fun disableTurnForCurrentPlayer() {
        binding.stopButton.isEnabled = false

    }
    private fun switchTurnAndCheckGameEnd() {
        mojBrojController.switchTurn(currentGame, currentGame.currentTurn) { success ->
            if (success) {
                endRound()
                handlePlayerTurn(currentGame, currentGame.currentTurn)

            } else {
                // Handle error
            }
        }
    }
    private fun endRound(){
        //endGame()
        val empty:MutableList<Int> = mutableListOf()
        currentGame.currentRound++
        mojBrojController.updateGameField(gameId, "currentRound", currentGame.currentRound) { success ->
            if (success) {

                mojBrojController.updateGameField(gameId, "player1Done", false){}
                mojBrojController.updateGameField(gameId, "player2Done", false){}
                mojBrojController.updateGameField(gameId,"offeredNumbers", empty) {}
                mojBrojController.updateGameField(gameId,"targetNumber", 0) {}
                daLiJeIgraPocela = false

            }
        }
    }
}