package com.example.igricaslagalica.view.multiplayer

import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.igricaslagalica.R
import com.example.igricaslagalica.SharedViewModel
import com.example.igricaslagalica.controller.AsocijacijeGameController
import com.example.igricaslagalica.controller.FirebaseGameController
import com.example.igricaslagalica.data.StaticData
import com.example.igricaslagalica.databinding.FragmentAsocijacijaGameBinding
import com.example.igricaslagalica.model.Asocijacija
import com.example.igricaslagalica.model.AsocijacijaMultiplayer
import com.example.igricaslagalica.model.Game
import com.google.firebase.auth.FirebaseAuth


class AsocijacijaGameMulti : Fragment() {

    private var _binding: FragmentAsocijacijaGameBinding? = null
    private lateinit var firebaseGameController: FirebaseGameController
    private lateinit var associjacijaController: AsocijacijeGameController

    private var currentGame: Game = Game()

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
    private var asocijacije: List<AsocijacijaMultiplayer> = listOf()
    private var oduzmiBodova = 0
    private var questionToShow = 0

    private var bodovi = 0
    private var player1Score = 0
    private var player2Score = 0
    val currentUserId= FirebaseAuth.getInstance().currentUser?.uid
    private var hasStartedGame : Boolean = false
    private var hasSecondRoundStarted : Boolean = false

    private val interactionButtonMap = mutableMapOf<Pair<Int, Int>, Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the game controller
        firebaseGameController = FirebaseGameController()
        associjacijaController = AsocijacijeGameController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAsocijacijaGameBinding.inflate(inflater, container, false)
        return binding.root
    }
    private var asocijacijeFetched: List<AsocijacijaMultiplayer> = listOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gameId = arguments?.getString("gameId")
        startTimer()
        Log.d("idd","Game id $gameId")

        if (gameId != null) {
            firebaseGameController.listenForGameChanges(gameId) { updatedGame ->
                if (updatedGame != null && currentUserId != null) {
                    handleUI(updatedGame)
                    currentGame = updatedGame
                    //   setupSwitchPlayerButton(updatedGame)
                } else {
                    // Handle failure or game not found
                    // handleGameNotFound()
                }
            }

        }


    }

    private fun handleUI(game: Game){
//        val gameId = arguments?.getString("gameId")
        gameUIHandled()


        if(!hasStartedGame) {
            currentGame.currentTurn = game.player1.toString()
            associjacijaController.updateGameField(game.id!!, "currentTurn", currentGame.currentTurn) { success ->
                if (success) {
                    // Generate questions for both players
                        associjacijaController.generateNewQuestions(game, currentGame.player1!!) { newQuestions ->
                            // Update the game with the new questions for both players
                            currentGame.asocijacijaQuestions = newQuestions
                            associjacijaController.updateQuestionAssocijacija(currentGame) { success ->
                                if(success){
                                    asocijacije = newQuestions.filter { it.assignedToPlayer == currentGame.player1 }
                                    startGame()
                                }

                            }

                        }


                } else {
                    // Handle update failure
                }
            }
            hasStartedGame = true
       }

        prikaziOdabranaPolja()

        val currentPlayerId = currentGame.currentTurn
        if (currentUserId != null && hasStartedGame) {
            handlePlayerTurn(currentGame, currentUserId)
        }
        Log.w("id","current round ${currentGame.currentRound} or ++ ${game.currentRound}")
        if(currentGame.currentRound == 2 && !hasSecondRoundStarted) {
          //  questionToShow = 1
            asocijacije = currentGame.asocijacijaQuestions.filter { it.assignedToPlayer == currentGame.player2 } //listOf(currentGame.asocijacijaQuestions[1])
            val emptyInteractionsList: List<Map<String, Int>> = emptyList()
            currentGame.id?.let {it
                associjacijaController.updateGameField(it,"interactionsAsocijacija", emptyInteractionsList){ success ->
                    if( success ){
                        resetujOdabranaPolja()
                        startGame()
                    }
                }
            }

            hasSecondRoundStarted = true
        }


//        val currentPlayerId = currentGame.currentTurn
        val isPlayer1Turn = currentPlayerId == currentGame.player1
        Log.w(TAG, "questions from bodovi igre $bodovi")
        if (isPlayer1Turn) {
            // Omogućite interakciju prvom igraču (asocijacija1Buttons, asocijacija2Buttons, itd.)
            // Onemogućite interakciju drugom igraču
            player1Score = bodovi
           // binding.finishButton.isEnabled = true

        } else {
            // Omogućite interakciju drugom igraču
            // Onemogućite interakciju prvom igraču
            player2Score = bodovi
        //    binding.finishButton.isEnabled = true
        }

        binding.finishButton.visibility = View.VISIBLE
      //  binding.finishButton.isEnabled = true


        if(game.currentRound == 3 || !game.isPlayer1Done){
            binding.finishButton.text = "Next game"
            binding.finishButton.isEnabled = true
            binding.finishButton.visibility = View.VISIBLE
//            prikaziRjesenje()
        }

        binding.finishButton.setOnClickListener {
            if (game.id != null) {
                //  switchTurnAndCheckGameEnd(currentGame, gameId)
                //here I can reinitizalize position for asocijacije[pozicija]
                if(binding.finishButton.text == "Next round" ){
                    switchTurnAndCheckGameEnd(game)
                } else {

                    associjacijaController.updateGameField(game.id!!, "currentRound", 1) {
                            success ->
                        if (success) {
                            associjacijaController.updateGameField(game.id!!, "isPlayer1Done", false) {
                            }

                        }
                    }
                    val bundle = bundleOf("gameId" to game.id)
                    findNavController().navigate(R.id.action_asocijacijaGameMulti_to_skockoGameMulti, bundle)

                }
            }

        }
    }


    private fun endRound() {
        timer.cancel()
        val currentPlayerId = currentGame.currentTurn
        val isPlayer1Turn = currentPlayerId == currentGame.player1

        // Switch player turns
        currentGame.currentTurn = if (isPlayer1Turn) currentGame.player2.toString() else currentGame.player1.toString()

        // Provjeri točnost odgovora igrača
        val tacanOdgovor =  asocijacije[questionToShow].asocijacijaKonacnoRjesenje.trim()
        val odgovor = binding.odgovorEditText.text.toString().trim()
        val isOdgovorTocan = odgovor.equals(tacanOdgovor, ignoreCase = true)

        // Dodijeli bodove odgovarajućem igraču

        if (isPlayer1Turn) {
            if (isOdgovorTocan) {
                player1Score += 7 + (6 * neodgovorenihAsocijacija) - oduzmiBodova
                currentGame.currentTurn = currentGame.player2.toString()
                // Spremi rezultate u bazu
                currentGame.id?.let {
                    associjacijaController.saveResultPlayer1(it, player1Score, currentGame.currentTurn)
                }
            }
        } else {
            if (isOdgovorTocan) {
                Log.w(TAG,"bodovi tacni 2 $bodovi")
                player2Score += 7 + (6 * neodgovorenihAsocijacija) - oduzmiBodova
                currentGame.currentTurn = currentGame.player1.toString()
                // Spremi rezultate u bazu
                currentGame.id?.let {
                    associjacijaController.saveResultPlayer2(it, player2Score, currentGame.currentTurn)
                }
            }
        }
        // Increment the round number
        currentGame.currentRound++

        // Update the game object in the database
        currentGame.id?.let { game ->
            associjacijaController.updateGameField(game, "currentRound", currentGame.currentRound) { success ->
                if (success) {
                    val emptyInteractionsList: List<Map<String, Int>> = emptyList()
                    associjacijaController.updateGameField(game,"interactionsAsocijacija", emptyInteractionsList){ success ->
                        if( success ){
                            resetujOdabranaPolja()
                            asocijacije = currentGame.asocijacijaQuestions.filter { it.assignedToPlayer == currentGame.player2 } //listOf(currentGame.asocijacijaQuestions[1])
                            startGame()

                        binding.finishButton.isEnabled = true
                        }
                    }

//                    asocijacije = currentGame.asocijacijaQuestions.filter { it.assignedToPlayer == currentGame.player2 } //listOf(currentGame.asocijacijaQuestions[1])
//                    resetujOdabranaPolja()
//                    startGame()
                } else {
                    // Handle update failure
                }
            }
        }
    }
    private fun switchTurnAndCheckGameEnd(game: Game) {
        associjacijaController.switchTurn(game, game.currentTurn) { success ->
            if (success) {
                endRound()
                handlePlayerTurn(currentGame, currentGame.currentTurn)


            } else {
                // Handle error
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
        }
    }

    private fun enableTurnForCurrentPlayer(game: Game, currentPlayerId: String) {

        binding.finishButton.isEnabled = true
        binding.odgovorEditText.visibility = View.VISIBLE
        for (i in 0 until 4) {
            asocijacija1Buttons[i].isClickable = true
            asocijacija2Buttons[i].isClickable = true
            asocijacija3Buttons[i].isClickable = true
            asocijacija4Buttons[i].isClickable = true
        }
        binding.odgovorEditText.isEnabled = true

//        timer.start()
    }

    private fun disableTurnForCurrentPlayer() {
        binding.finishButton.isEnabled = false
        binding.odgovorEditText.visibility = View.GONE
        for (i in 0 until 4) {
            asocijacija1Buttons[i].isClickable = false
            asocijacija2Buttons[i].isClickable = false
            asocijacija3Buttons[i].isClickable = false
            asocijacija4Buttons[i].isClickable = false
        }
        binding.odgovorEditText.isEnabled = false

        //     timer.cancel()
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
        Log.w("poljaa" , "punjena polja ${asocijacije[questionToShow]}")
        for (i in 0 until 4)
        {
            asocijacija1Buttons[i].text = asocijacije[questionToShow].asocijacijaList[i] //asocijacije.asocijacije[0].asocijacijaList[i]
            asocijacija2Buttons[i].text =  asocijacije[questionToShow].asocijacijaListOne[i]
            asocijacija3Buttons[i].text =   asocijacije[questionToShow].asocijacijaTwo[i] //asocijacije.asocijacije[2].asocijacijaList[i]
            asocijacija4Buttons[i].text =   asocijacije[questionToShow].asocijacijaThree[i] // asocijacije.asocijacije[3].asocijacijaList[i]
        }
        asocijacija1Buttons[4].text =  asocijacije[questionToShow].asocijacijaList[4]

        asocijacija2Buttons[4].text =  asocijacije[questionToShow].asocijacijaListOne[4] // asocijacije.asocijacije[1].asocijacijaRjesenje

        asocijacija3Buttons[4].text =  asocijacije[questionToShow].asocijacijaTwo[4] //asocijacije.asocijacije[2].asocijacijaRjesenje

        asocijacija4Buttons[4].text =  asocijacije[questionToShow].asocijacijaThree[4] // asocijacije.asocijacije[3].asocijacijaRjesenje

    }
    private fun initializeInteractionButtonMap() {
        for (i in 0 until 5) {
            interactionButtonMap[0 to i] = asocijacija1Buttons[i]
            interactionButtonMap[1 to i] = asocijacija2Buttons[i]
            interactionButtonMap[2 to i] = asocijacija3Buttons[i]
            interactionButtonMap[3 to i] = asocijacija4Buttons[i]
            interactionButtonMap[4 to i] = asocijacija4Buttons[i]

        }
    }
    private fun prikaziOdabranaPolja() {
        initializeInteractionButtonMap()
        for (interaction in currentGame.interactionsAsocijacija) {
            val row = interaction["row"] as? Int
            val buttonIndex = interaction["buttonIndex"] as? Int
            interactionButtonMap[row to buttonIndex]?.setTextColor(Color.WHITE)
            interactionButtonMap[row to buttonIndex]?.visibility = View.VISIBLE

            // Check if it's a final solution interaction
            val isFinalSolution = interaction["isFinalSolution"] as? Int
            if (isFinalSolution == 1) {
              //  val finalSolution = asocijacije[0].asocijacijaKonacnoRjesenje.trim()
             //   binding.finalAnswerField.text = finalSolution

                prikaziRjesenje()
                binding.finishButton.visibility = View.VISIBLE
                binding.finishButton.isEnabled = true
            }
        }

    }
    private fun resetujOdabranaPolja() {
        initializeInteractionButtonMap()

        // Clear color and visibility of interaction buttons
        for ((row, buttonIndex) in interactionButtonMap.keys) {
            interactionButtonMap[row to buttonIndex]?.setTextColor(Color.TRANSPARENT) // Or the default color you want
             //interactionButtonMap[row to buttonIndex]?.visibility = View.INVISIBLE
        }

        binding.finalAnswerField.setTextColor(Color.TRANSPARENT)
        binding.finishButton.visibility = View.GONE

    }
    private fun prikaziRjesenje() {
        timer.cancel()
        val tacanOdgovor = asocijacije[questionToShow].asocijacijaKonacnoRjesenje.trim()  //asocijacije.asocijacijaKonacnoRjesenje.trim()
        binding.odgovorEditText.visibility = View.GONE
        binding.finalAnswerField.text = tacanOdgovor
        binding.finalAnswerField.setTextColor(Color.WHITE)
        binding.timerTextView.text = "Vasi bodovi su $bodovi"
        if(currentGame.currentRound == 1 || currentGame.currentRound == 2) {
            binding.finishButton.text = "Next round"
        } else {
            binding.finishButton.text = "Next game"
           // binding.finishButton.isEnabled = true

        }
        binding.finishButton.visibility = View.VISIBLE
        binding.finishButton.isEnabled = true
    }
    private fun startGame() {
        napuniPolja()
        startTimer()
    }
//    private val interactions = mutableListOf<Map<String, Int>>()

    private fun trackInteraction(row: Int, buttonIndex: Int) {
        val interaction = mapOf("row" to row, "buttonIndex" to buttonIndex)
        val updatedInteractions = currentGame.interactionsAsocijacija.toMutableList()
        updatedInteractions.add(interaction)
        currentGame.interactionsAsocijacija = updatedInteractions
//        associjacijaController.updateInteractions(currentGame.id!!, currentGame.interactionsAsocijacija)
        associjacijaController.updateGameField(currentGame.id!!,"interactionsAsocijacija", currentGame.interactionsAsocijacija){

        }
    }
    private fun gameUIHandled(){
        buttonsUI()
        for (i in 0 until 4) {
            asocijacija1Buttons[i].setOnClickListener {
                asocijacija1Buttons[i].setTextColor(Color.WHITE)
                zatvorenoPolja1--
                oduzmiBodova++
                trackInteraction(0, i)
            }
            asocijacija2Buttons[i].setOnClickListener {
                asocijacija2Buttons[i].setTextColor(Color.WHITE)
                zatvorenoPolja2--
                oduzmiBodova++
                trackInteraction(1, i)

            }
            asocijacija3Buttons[i].setOnClickListener {
                asocijacija3Buttons[i].setTextColor(Color.WHITE)
                zatvorenoPolja3--
                oduzmiBodova++
                trackInteraction(2, i)

            }
            asocijacija4Buttons[i].setOnClickListener {
                asocijacija4Buttons[i].setTextColor(Color.WHITE)
                zatvorenoPolja4--
                oduzmiBodova++
                trackInteraction(3, i)

            }
        }
        asocijacija1Buttons[4].setOnClickListener {
            handleAnswerClick(0, asocijacija1Buttons[4].text.toString().trim())
        }

        asocijacija2Buttons[4].setOnClickListener {
            handleAnswerClick(1, asocijacija2Buttons[4].text.toString().trim())
        }

        asocijacija3Buttons[4].setOnClickListener {
            handleAnswerClick(2, asocijacija3Buttons[4].text.toString().trim())
        }

        asocijacija4Buttons[4].setOnClickListener {
            handleAnswerClick(3, asocijacija4Buttons[4].text.toString().trim())
        }

        binding.finalAnswerField.setOnClickListener {
           // handleFinalAnswerClick(asocijacije[0].asocijacijaKonacnoRjesenje.trim())
            handleFinalSolutionClick()

        }

        binding.finishButton.isEnabled = true

    }
    private fun handleAnswerClick(row: Int, tacanOdgovor: String) {
        val odgovor = binding.odgovorEditText.text.toString().trim()
        if (odgovor.equals(tacanOdgovor, ignoreCase = true)) {
            val button = interactionButtonMap[row to 4]
            button?.setTextColor(Color.WHITE)
            // Track the interaction
            trackInteraction(row, 4)
            when (row) {
                0 -> {
                    bodovi += 2 + zatvorenoPolja1
                    neodgovorenihAsocijacija--
                    oduzmiBodova -= zatvorenoPolja1
                }
                1 -> {
                    bodovi += 2 + zatvorenoPolja2
                    neodgovorenihAsocijacija--
                    oduzmiBodova -= zatvorenoPolja2
                }
                2 -> {
                    bodovi += 2 + zatvorenoPolja3
                    neodgovorenihAsocijacija--
                    oduzmiBodova -= zatvorenoPolja3
                }
                3 -> {
                    bodovi += 2 + zatvorenoPolja4
                    neodgovorenihAsocijacija--
                    oduzmiBodova -= zatvorenoPolja4
                }
            }

            binding.odgovorEditText.setText("")
        }
    }
    private fun handleFinalSolutionClick() {
        val finalSolution = asocijacije[questionToShow].asocijacijaKonacnoRjesenje
        val odgovor = binding.odgovorEditText.text.toString().trim()

        if (odgovor.equals(finalSolution, ignoreCase = true)) {
            // Update UI or perform any relevant actions

            // Track the interaction for the final solution
            trackInteractionFinalSolution()
            prikaziRjesenje()
        }

        binding.odgovorEditText.setText("")
    }
    private fun trackInteractionFinalSolution() {
        val interaction = mapOf("isFinalSolution" to 1)
        val updatedInteractions = currentGame.interactionsAsocijacija.toMutableList()
        updatedInteractions.add(interaction)
        associjacijaController.updateInteractions(currentGame.id!!, updatedInteractions)
    }

    private fun buttonsUI(){
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
    }

}