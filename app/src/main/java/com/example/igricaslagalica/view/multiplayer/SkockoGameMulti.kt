package com.example.igricaslagalica.view.multiplayer

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.igricaslagalica.R
import com.example.igricaslagalica.SharedViewModel
import com.example.igricaslagalica.controller.AsocijacijeGameController
import com.example.igricaslagalica.controller.FirebaseGameController
import com.example.igricaslagalica.controller.SkockoGameController
import com.example.igricaslagalica.databinding.FragmentSkockoGameBinding
import com.example.igricaslagalica.model.Game
import com.example.igricaslagalica.model.Skocko
import com.google.firebase.auth.FirebaseAuth


class SkockoGameMulti : Fragment() {
    private var _binding: FragmentSkockoGameBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseGameController: FirebaseGameController
    private lateinit var skockoGameController: SkockoGameController
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var timer: CountDownTimer
//    private lateinit var skockoList: List<Skocko>
    private lateinit var skockoList: List<Skocko>
    private var currentGame: Game = Game()

    private var hintList = ArrayList<ImageView>()
    private var listaPolja = ArrayList<ImageView>()
    private var pokusaj = ArrayList<Skocko>()
    private var element = 0
    private var bodovi = 0
    var gameId: String = ""
    val currentUserId= FirebaseAuth.getInstance().currentUser?.uid
    private var hasStartedGame : Boolean = false
    private var isPlayer1Round1Answered = false
    private var isPlayer2Round1Answered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the game controller
        firebaseGameController = FirebaseGameController()
        skockoGameController = SkockoGameController()
        skockoList  = mutableListOf()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSkockoGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gameId = arguments?.getString("gameId")

        binding.finishButton.visibility = View.GONE

            binding.finishButton.setOnClickListener {
                if(gameId != null){
                    if(binding.finishButton.text == "Next round"){
                        switchTurnAndCheckGameEnd(currentGame)
                    } else {
                        skockoGameController.updateGameField(gameId, "currentRound", 1) { success ->
                            if (success) { }
                        }
                        val bundle = bundleOf("gameId" to gameId)
                        findNavController().navigate(R.id.action_skockoGameMulti_to_korakPoKorakMulti, bundle)

                    }
                }
        }
        if (gameId != null) {
            firebaseGameController.listenForGameChanges(gameId) { updatedGame ->
                if (updatedGame != null && currentUserId != null) {
                    handleUI(updatedGame)
                    skockoList = updatedGame.skockoList
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
        gameId = game.id.toString()
        napuniListuPolja()
        napuniHintListu()
        if(!hasStartedGame){
            hasStartedGame = true
            game.id?.let { startGame(it) }
        }
        if(currentUserId != null && hasStartedGame){
            handlePlayerTurn(game,currentUserId)
        }
        // Fetch player1Attempts from the updated game and update the UI
        val player1Attempts = game.player1Attempts
        for ((index, skocko) in player1Attempts.withIndex()) {
            listaPolja[index].setImageResource(vratiImageResourceZaVrijednost(skocko))
        }
        if(game.player1Attempts.isEmpty() ){
            listaPolja.forEach { imageView -> imageView.setImageResource(R.drawable.polje) }
            binding.pokusaji.visibility = View.VISIBLE
            binding.tacanOdgovor.visibility = View.GONE
        }
        if(game.currentRound == 3 ){
            endGame()
        }

    }
    private fun switchTurnAndCheckGameEnd(game: Game) {
        skockoGameController.switchTurn(game, game.currentTurn) { success ->
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
            if (game.currentTurn == game.player1) {
                updateUIForPlayer(game)
            }
        }
    }
    private fun updateUIForPlayer(game: Game) {
        val player1Attempts = game.player1Attempts
        for ((index, skocko) in player1Attempts.withIndex()) {
            listaPolja[index].setImageResource(vratiImageResourceZaVrijednost(skocko))
        }
    }

    private fun enableTurnForCurrentPlayer(game: Game, currentPlayerId: String) {
        if(binding.finishButton.isVisible){
            binding.opcije.visibility = View.GONE
        } else {
            binding.opcije.visibility = View.VISIBLE // Show the options for selecting Skocko values
        }
    }

    private fun disableTurnForCurrentPlayer() {
        binding.opcije.visibility = View.GONE // Hide the options for selecting Skocko values
    }


    private fun startGame(gameId: String) {
       // makeRandomCombination()
        skockoGameController.createSkockoGame(gameId)
        startTimer()
        pokusaj()
    }
    private fun handleUserAttempt(attemptedValue: Skocko, gameId: String) {
        currentGame.player1Attempts += attemptedValue // Add the attempted value to the list

        // Update the game document in the database
        skockoGameController.updateGameField(gameId, "player1Attempts", currentGame.player1Attempts) { success ->
            if (success) {
                // Handle successful update
            }
        }
    }

    private fun pokusaj() {
        binding.opcije.findViewById<ImageView>(R.id.skocko).setOnClickListener {
            listaPolja[element].setImageResource(R.drawable.skocko)
            pokusaj.add(Skocko.SKOCKO)
            element++
            if (element % 4 == 0)
                provjeriPokusaj()
            handleUserAttempt(Skocko.SKOCKO, gameId)
        }
        binding.opcije.findViewById<ImageView>(R.id.kvadrat).setOnClickListener {
            listaPolja[element].setImageResource(R.drawable.kvadrat)
            pokusaj.add(Skocko.KVADRAT)
            element++
            if (element % 4 == 0)
                provjeriPokusaj()
            handleUserAttempt(Skocko.KVADRAT, gameId)
        }
        binding.opcije.findViewById<ImageView>(R.id.krug).setOnClickListener {
            listaPolja[element].setImageResource(R.drawable.crveni_krug)
            pokusaj.add(Skocko.KRUG)
            element++
            if (element % 4 == 0)
                provjeriPokusaj()
            handleUserAttempt(Skocko.KRUG, gameId)
        }
        binding.opcije.findViewById<ImageView>(R.id.srce).setOnClickListener {
            listaPolja[element].setImageResource(R.drawable.srce)
            pokusaj.add(Skocko.SRCE)
            element++
            if (element % 4 == 0)
                provjeriPokusaj()
            handleUserAttempt(Skocko.SRCE, gameId)
        }
        binding.opcije.findViewById<ImageView>(R.id.trougao).setOnClickListener {
            listaPolja[element].setImageResource(R.drawable.trougao)
            pokusaj.add(Skocko.TROUGAO)
            element++
            if (element % 4 == 0)
                provjeriPokusaj()
            handleUserAttempt(Skocko.TROUGAO, gameId)
        }
        binding.opcije.findViewById<ImageView>(R.id.zvezda).setOnClickListener {
            listaPolja[element].setImageResource(R.drawable.zvezda)
            pokusaj.add(Skocko.ZVEZDA)
            element++
            if (element % 4 == 0)
                provjeriPokusaj()
            handleUserAttempt(Skocko.ZVEZDA, gameId)
        }
        // Update the hint images after processing the attempt
        updateHintImages()
    }
    private fun updateHintImages() {
        val correctPosition = vratiBrojTacnihMjesta()
        val incorrectPosition = vratiBrojNaPogresnomMjestu()

        // Update hint images for correct positions
        for (i in 0 until correctPosition) {
            hintList[element - 4 + i].setImageResource(R.drawable.crveni_krug)
        }

        // Update hint images for incorrect positions
        for (i in 0 until incorrectPosition) {
            hintList[element - 4 + correctPosition + i].setImageResource(R.drawable.zuti_krug)
        }
    }

    private fun provjeriPokusaj() {
        if (pokusaj == skockoList) {
            bodovi = vratiBrojBodova()
            Log.w("bodovi", "bodovi su tu $bodovi")
            if(currentGame.player1 == currentUserId) {
                bodovi += currentGame.player1Score
                Log.w("bodovi", "bodovi su tu $bodovi nakon")
                skockoGameController.saveResultPlayer1(gameId, bodovi)
            } else {
                bodovi += currentGame.player2Score
                Log.w("bodovi", "bodovi 2 su tu $bodovi nakon")
                skockoGameController.saveResultPlayer2(gameId, bodovi)
            }


            endGame()
        } else {
            if (element >= 24)
                endGame()
            var hintNo = element - 4
            for (i in 0 until vratiBrojTacnihMjesta()) {
                hintList[hintNo].setImageResource(R.drawable.crveni_krug)
                hintNo++
            }
            for (i in 0 until vratiBrojNaPogresnomMjestu()) {
                hintList[hintNo].setImageResource(R.drawable.zuti_krug)
                hintNo++
            }
            pokusaj = ArrayList()
        }
    }

    private fun vratiBrojTacnihMjesta(): Int {
        return pokusaj.zip(skockoList).count { (element1, element2) ->
            element1 == element2
        }
    }

    private fun vratiBrojNaPogresnomMjestu(): Int {
        val brojIstihElemenata = pokusaj.intersect(skockoList).sumBy { element ->
            minOf(pokusaj.count { it == element }, skockoList.count { it == element })
        }
        return brojIstihElemenata - vratiBrojTacnihMjesta()
    }

    private fun endGame() {
        if(currentGame.currentRound == 1 ){
            binding.finishButton.text = "Next round"
            binding.opcije.visibility = View.GONE

        } else {
            binding.finishButton.text = "Next game"
        }

        timer.cancel()
        napuniTacanOdgovor()
        binding.finishButton.visibility = View.VISIBLE
        binding.skor.text = "Bodovi u ovoj igri su : $bodovi"
        binding.pokusaji.visibility = View.GONE
        binding.tacanOdgovor.visibility = View.VISIBLE
        binding.opcije.visibility = View.GONE
       handleCurrentRound()
    }
    private fun handleCurrentRound(){
        currentGame.currentRound++
        skockoGameController.updateGameField(gameId, "currentRound", currentGame.currentRound) { success ->
            if (success) {
                // Start a new round
                //  startNewRound()
            }
        }
    }
    private fun endRound(){
        // here you can check flag for round 1 is answered properly
     //   if(currentGame.currentRound == )
//
//        skockoGameController.updateGameField(gameId, "currentRound", currentGame.currentRound) { success ->
//            if (success) {
//                // Start a new round
//              //  startNewRound()
//            }
//        }
        currentGame.player1Attempts = mutableListOf()
        skockoGameController.updateGameField(gameId, "player1Attempts", currentGame.player1Attempts) { success ->
            if (success) {
                // Start a new round
                startNewRound()
                binding.finishButton.visibility = View.GONE
                binding.pokusaji.visibility = View.VISIBLE
                binding.tacanOdgovor.visibility = View.GONE

            }
        }
//        currentGame.player1Attempts = mutableListOf()
//        skockoGameController.updateGameField(gameId, "player1Attempts", currentGame.player1Attempts) { success ->
//            if (success) {
//                // Start a new round
//                startNewRound()
//            }
//        }
    }
//    private fun endRound() {
//        if (currentGame.currentRound == 1) {
//            if (currentUserId == currentGame.player1) {
//                isPlayer1Round1Answered = true
//            } else if (currentUserId == currentGame.player2) {
//                isPlayer2Round1Answered = true
//            }
//        }
//
//        if (isPlayer1Round1Answered && isPlayer2Round1Answered) {
//            currentGame.currentRound++
//            isPlayer1Round1Answered = false
//            isPlayer2Round1Answered = false
//            skockoGameController.updateCurrentRound(gameId, currentGame.currentRound) { success ->
//                if (success) {
//                    startNewRound()
//                }
//            }
//        } else {
//            // Update the UI for the current round and player turns
//            updateUIForPlayer(currentGame)
//            handlePlayerTurn(currentGame, currentUserId)
//        }
//    }

    private fun startNewRound() {
        skockoGameController.createSkockoGame(gameId) // Start a new game (or round) by resetting the game data
        startTimer()
        pokusaj()
    }
    private fun vratiImageResourceZaVrijednost(s: Skocko): Int {
        return when (s) {
            Skocko.SKOCKO -> R.drawable.skocko
            Skocko.ZVEZDA -> R.drawable.zvezda
            Skocko.TROUGAO -> R.drawable.trougao
            Skocko.SRCE -> R.drawable.srce
            Skocko.KRUG -> R.drawable.crveni_krug
            else -> R.drawable.kvadrat
        }
    }
    private fun vratiBrojBodova(): Int
    {
        return if(element <= 8)
            20
        else if(element <= 16)
            15
        else
            10
    }

    private fun napuniTacanOdgovor() {
        binding.tacanOdgovor.findViewById<ImageView>(R.id.kolona1Polje).setImageResource(
            vratiImageResourceZaVrijednost(
                skockoList[0]
            )
        )
        binding.tacanOdgovor.findViewById<ImageView>(R.id.kolona2Polje).setImageResource(
            vratiImageResourceZaVrijednost(
                skockoList[1]
            )
        )
        binding.tacanOdgovor.findViewById<ImageView>(R.id.kolona3Polje).setImageResource(
            vratiImageResourceZaVrijednost(
                skockoList[2]
            )
        )
        binding.tacanOdgovor.findViewById<ImageView>(R.id.kolona4Polje).setImageResource(
            vratiImageResourceZaVrijednost(
                skockoList[3]
            )
        )
    }
    private fun napuniListuPolja() {
        listaPolja.add(binding.vrsta1.findViewById(R.id.kolona1Polje))
        listaPolja.add(binding.vrsta1.findViewById(R.id.kolona2Polje))
        listaPolja.add(binding.vrsta1.findViewById(R.id.kolona3Polje))
        listaPolja.add(binding.vrsta1.findViewById(R.id.kolona4Polje))
        listaPolja.add(binding.vrsta2.findViewById(R.id.kolona1Polje))
        listaPolja.add(binding.vrsta2.findViewById(R.id.kolona2Polje))
        listaPolja.add(binding.vrsta2.findViewById(R.id.kolona3Polje))
        listaPolja.add(binding.vrsta2.findViewById(R.id.kolona4Polje))
        listaPolja.add(binding.vrsta3.findViewById(R.id.kolona1Polje))
        listaPolja.add(binding.vrsta3.findViewById(R.id.kolona2Polje))
        listaPolja.add(binding.vrsta3.findViewById(R.id.kolona3Polje))
        listaPolja.add(binding.vrsta3.findViewById(R.id.kolona4Polje))
        listaPolja.add(binding.vrsta4.findViewById(R.id.kolona1Polje))
        listaPolja.add(binding.vrsta4.findViewById(R.id.kolona2Polje))
        listaPolja.add(binding.vrsta4.findViewById(R.id.kolona3Polje))
        listaPolja.add(binding.vrsta4.findViewById(R.id.kolona4Polje))
        listaPolja.add(binding.vrsta5.findViewById(R.id.kolona1Polje))
        listaPolja.add(binding.vrsta5.findViewById(R.id.kolona2Polje))
        listaPolja.add(binding.vrsta5.findViewById(R.id.kolona3Polje))
        listaPolja.add(binding.vrsta5.findViewById(R.id.kolona4Polje))
        listaPolja.add(binding.vrsta6.findViewById(R.id.kolona1Polje))
        listaPolja.add(binding.vrsta6.findViewById(R.id.kolona2Polje))
        listaPolja.add(binding.vrsta6.findViewById(R.id.kolona3Polje))
        listaPolja.add(binding.vrsta6.findViewById(R.id.kolona4Polje))
    }

    private fun napuniHintListu() {
        var hint = binding.vrsta1.findViewById<LinearLayout>(R.id.kolonaHint)
        hintList.add(hint.findViewById(R.id.image1))
        hintList.add(hint.findViewById(R.id.image2))
        hintList.add(hint.findViewById(R.id.image3))
        hintList.add(hint.findViewById(R.id.image4))
        hint = binding.vrsta2.findViewById(R.id.kolonaHint)
        hintList.add(hint.findViewById(R.id.image1))
        hintList.add(hint.findViewById(R.id.image2))
        hintList.add(hint.findViewById(R.id.image3))
        hintList.add(hint.findViewById(R.id.image4))
        hint = binding.vrsta3.findViewById(R.id.kolonaHint)
        hintList.add(hint.findViewById(R.id.image1))
        hintList.add(hint.findViewById(R.id.image2))
        hintList.add(hint.findViewById(R.id.image3))
        hintList.add(hint.findViewById(R.id.image4))
        hint = binding.vrsta4.findViewById(R.id.kolonaHint)
        hintList.add(hint.findViewById(R.id.image1))
        hintList.add(hint.findViewById(R.id.image2))
        hintList.add(hint.findViewById(R.id.image3))
        hintList.add(hint.findViewById(R.id.image4))
        hint = binding.vrsta5.findViewById(R.id.kolonaHint)
        hintList.add(hint.findViewById(R.id.image1))
        hintList.add(hint.findViewById(R.id.image2))
        hintList.add(hint.findViewById(R.id.image3))
        hintList.add(hint.findViewById(R.id.image4))
        hint = binding.vrsta6.findViewById(R.id.kolonaHint)
        hintList.add(hint.findViewById(R.id.image1))
        hintList.add(hint.findViewById(R.id.image2))
        hintList.add(hint.findViewById(R.id.image3))
        hintList.add(hint.findViewById(R.id.image4))

    }

    private fun startTimer() {
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                binding.timerTextView.text =
                    String.format("%2d s", secondsLeft)
            }

            override fun onFinish() {
                endGame()
            }
        }
        timer.start()
    }


}