package com.example.igricaslagalica.view.multiplayer

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.igricaslagalica.R
import com.example.igricaslagalica.SharedViewModel
import com.example.igricaslagalica.controller.FirebaseGameController
import com.example.igricaslagalica.controller.KorakPoKorakController
import com.example.igricaslagalica.controller.SkockoGameController
import com.example.igricaslagalica.data.StaticData
import com.example.igricaslagalica.model.KorakPoKorak
import com.example.igricaslagalica.databinding.FragmentKorakPoKorakBinding
import com.example.igricaslagalica.model.Game
import com.google.firebase.auth.FirebaseAuth
import java.util.Random

class KorakPoKorakMulti : Fragment() {
    private var _binding: FragmentKorakPoKorakBinding? = null
    private lateinit var firebaseGameController: FirebaseGameController
    private lateinit var korakPoKorakController: KorakPoKorakController
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var currentGame: Game = Game()

    private lateinit var timer: CountDownTimer
    private var remainingTime: Long = 0
    private var brojPojma = 0
    private lateinit var pitanje: KorakPoKorak
    private var pojmoviTextView = ArrayList<TextView>()
    private var gameId: String = ""
    val currentUserId= FirebaseAuth.getInstance().currentUser?.uid
    private var hasStartedGame : Boolean = false
    private var hasSecondRoundStarted: Boolean = false
    private val randomNum = Random()

    private var previousRandomIndex = -1 // Initialize with a value that's not valid
    private fun generateRandomIndex(listSize: Int): Int {
        var randomIndex: Int
        do {
            randomIndex = randomNum.nextInt(listSize)
        } while (randomIndex == previousRandomIndex)

        previousRandomIndex = randomIndex
        return randomIndex
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the game controller
        firebaseGameController = FirebaseGameController()
        korakPoKorakController = KorakPoKorakController()
        pitanje  = KorakPoKorak()
        startTimer()
        timer.cancel()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKorakPoKorakBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gameId = arguments?.getString("gameId")

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

        binding.provjeriButton.setOnClickListener {
            handleAnswer()
            if (binding.provjeriButton.text == "Next game" ) {
               // if (gameId != null) {
               // currentGame.currentRound++
                if (gameId != null) {
                    korakPoKorakController.updateGameField(gameId, "currentRound", 1) { success ->
                        if (success) {
                            // Continue Round 1 for Player 2
                        }

                    }
                    korakPoKorakController.updateGameField(gameId,"player1Done", false){}
                    korakPoKorakController.updateGameField(gameId,"player2Done", false){}

                }
                    val bundle = bundleOf("gameId" to gameId)
                    findNavController().navigate(
                        R.id.action_korakPoKorakMulti_to_mojBrojMulti,
                        bundle
                    )
            }
            if(binding.provjeriButton.text == "Next round"){
                switchTurnAndCheckGameEnd()
            }


        }

    }
    private fun handleUI(game: Game){
        gameId = game.id.toString()
        if(!hasStartedGame){
            hasStartedGame = true
            handleUIGame()
            if(game.korakPoKorakQuestions.isEmpty()){
                generateQuestions(game)
            } else {
                pitanje = game.korakPoKorakQuestions[0]
                prikaziPojam()
            }

        }
        if(currentUserId != null && hasStartedGame){
            handlePlayerTurn(game,currentUserId)
        }
        if(game.opponentAnswer.equals(pitanje.odgovor, ignoreCase = true)){
            setAndShowCorrectAnswer()
            brojPojma = 0
        }
        if(game.currentRound == 2 && !hasSecondRoundStarted) {
            resetujPojam()
            pitanje = game.korakPoKorakQuestions[1]
            prikaziPojam()
            hasSecondRoundStarted = true

        }
        if(currentGame.currentRound >= 3){
            binding.tacanOdgovorTextView.text = pitanje.odgovor
            binding.provjeriButton.visibility = View.VISIBLE
        }


    }
    private fun handleAnswer():Boolean{
        var odgovor = binding.odgovorEditText.text.toString()
        odgovor = odgovor.trim()

        if (odgovor.equals(pitanje.odgovor, ignoreCase = true)) {

            binding.provjeriButton.text = "Next round"
//            timer.cancel()
            var brojBodova = 20 - ((brojPojma - 1) * 2)
            pitanje.bodovi = brojBodova
            binding.timerTextView.text = "$brojBodova bodova"
            synchronizeAnswer(pitanje.odgovor)
            binding.odgovorEditText.setText("")
            if(currentGame.player1 == currentUserId) {
                brojBodova += currentGame.player1Score
                Log.w("bodovi", "bodovi su tu $brojBodova nakon")
                korakPoKorakController.saveResultPlayer1(gameId, brojBodova)
            } else {
                brojBodova += currentGame.player2Score
                korakPoKorakController.saveResultPlayer2(gameId, brojBodova)
            }
        //    switchTurnAndCheckGameEnd()
            return true
        } else
            binding.odgovorEditText.setText("")
            synchronizeAnswer(odgovor)
            return false
    }

    private fun synchronizeAnswer(answer: String) {
            // Update the answer for the other player
            korakPoKorakController.updateGameField(gameId, "opponentAnswer", answer) { success ->
                if (success) {
                    // Handle success
                } else {
                    // Handle failure
                }
            }
    }
    private fun handlePlayerTurn(game: Game, currentPlayerId: String) {
        if (game.currentTurn == currentPlayerId) {
            // It's the current player's turn. Enable the UI.
            enableTurnForCurrentPlayer(game, currentPlayerId)

        } else {
            // It's not the current player's turn. Disable the UI.
            disableTurnForCurrentPlayer()
//            val opponentAnswer = game.opponentAnswer
//            if (!opponentAnswer.isNullOrEmpty()) {
//                if(opponentAnswer == pitanje.odgovor){
//                    binding.odgovorEditText.visibility = View.GONE
//                    binding.tacanOdgovorCardView.visibility = View.VISIBLE
//
//                } else
//                // Display the opponent's answer on the screen
//                binding.odgovorEditText.setText(opponentAnswer)
//                binding.odgovorEditText.visibility = View.VISIBLE
//                binding.odgovorEditText.isClickable = false
//                binding.odgovorEditText.isEnabled = false
//            }
        }
    }

    private fun enableTurnForCurrentPlayer(game: Game, currentPlayerId: String) {
            binding.provjeriButton.visibility = View.VISIBLE
            binding.odgovorEditText.visibility = View.VISIBLE
            binding.odgovorEditText.isClickable = true
            binding.odgovorEditText.isEnabled = true

    }

    private fun disableTurnForCurrentPlayer() {
//        binding.provjeriButton.visibility = View.VISIBLE
        binding.provjeriButton.visibility = View.GONE
        binding.odgovorEditText.isClickable = false
        binding.odgovorEditText.isEnabled = false
        if(binding.provjeriButton.text == "Next game"){
            binding.provjeriButton.visibility = View.VISIBLE
        }


    }
    private fun switchTurnAndCheckGameEnd() {
        korakPoKorakController.switchTurn(currentGame, currentGame.currentTurn) { success ->
            if (success) {
                endRound()
                handlePlayerTurn(currentGame, currentGame.currentTurn)

            } else {
                // Handle error
            }
        }


    }
    private fun handleUIGame() {
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam1TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam2TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam3TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam4TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam5TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam6TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam7TextView))
    }
    private fun generateQuestions(game: Game) {

            korakPoKorakController.generateNewQuestions(game) { newQuestions ->
                game.korakPoKorakQuestions = newQuestions
                pitanje = newQuestions[0]
                game.id?.let { gameId ->
                    korakPoKorakController.updateGameField(gameId, "korakPoKorakQuestions",  game.korakPoKorakQuestions) { success ->
                        if (success) {
//                            val filteredQuestions = game.korakPoKorakQuestions.filter { it.assignedToPlayer == game.player1 }
//                            pitanje = filteredQuestions[0]
                            prikaziPojam()
                        }

                    }
                }
            }

    }
    private fun startTimer() {
        timer = object : CountDownTimer(10000, 10) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                val secondsLeft = millisUntilFinished / 1000
                val millisecondsLeft = millisUntilFinished % 1000
                binding.timerTextView.text =
                    String.format("%2d s: %s ms", secondsLeft, millisecondsLeft)
            }

            override fun onFinish() {
                prikaziPojam()
            }
        }
        timer.start()
    }

    private fun prikaziPojam() {
        Log.w("tes","borj $brojPojma")
        if (brojPojma < 7) {
            startTimer()
            pojmoviTextView[brojPojma].text = pitanje.pojmovi[brojPojma]
            brojPojma++
        } else
            endGame()
         //   switchTurnAndCheckGameEnd()

    }
    private fun resetujPojam() {
        timer.cancel()
        for (i in 0 until pojmoviTextView.size) {
            pojmoviTextView[i].text = "Pojam ${i + 1}"
        }
        binding.tacanOdgovorCardView.visibility = View.GONE
    }

    private fun endGame() {
//        binding.timerTextView.text = "0 bodova"
//        setAndShowCorrectAnswer()
        if(currentGame.currentRound == 1){
            binding.provjeriButton.text = "Next round"
        } else {
            binding.provjeriButton.text = "Next game"

        }
    }

    private fun setAndShowCorrectAnswer() {
      //  timer.cancel()
        binding.odgovorEditText.visibility = View.VISIBLE
        binding.tacanOdgovorCardView.visibility = View.VISIBLE
        binding.tacanOdgovorTextView.text = pitanje.odgovor
        endGame()
    }

    private fun endRound(){
        endGame()
        currentGame.currentRound++
        korakPoKorakController.updateGameField(gameId, "currentRound", currentGame.currentRound) { success ->
            if (success) {
            }
        }
    }
}