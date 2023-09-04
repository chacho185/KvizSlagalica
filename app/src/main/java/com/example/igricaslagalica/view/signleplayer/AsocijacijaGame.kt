package com.example.igricaslagalica.view.signleplayer

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
import com.example.igricaslagalica.model.Game


class AsocijacijaGame : Fragment() {

    private var _binding: FragmentAsocijacijaGameBinding? = null
    private lateinit var gameController: FirebaseGameController
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
    private val asocijacije = StaticData.dajAsocijaciju()
    private var oduzmiBodova = 0
    private var gameId: String = ""

    private var bodovi = 0
    private var player1Score = 0
    private var player2Score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the game controller
        gameController = FirebaseGameController()
        associjacijaController = AsocijacijeGameController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAsocijacijaGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameId = arguments?.getString("gameId").toString()

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

        for (i in 0 until 4) {
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
            if (odgovor.equals(tacanOdgovor, ignoreCase = true)) {
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
            if (odgovor.equals(tacanOdgovor, ignoreCase = true)) {
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
            if (odgovor.equals(tacanOdgovor, ignoreCase = true)) {
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
            if (odgovor.equals(tacanOdgovor, ignoreCase = true)) {
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
            if (odgovor.equals(tacanOdgovor, ignoreCase = true)) {
                bodovi += 7 + (6 * neodgovorenihAsocijacija) - oduzmiBodova

                prikaziRjesenje()
                endRound()
            }
            binding.odgovorEditText.setText("")
        }

        val currentPlayerId = currentGame.currentTurn
        binding.finishButton.isEnabled = true
        gameController.getGame(gameId) { gameCurrent ->
            if (gameCurrent != null) {
                currentGame = gameCurrent
               // switchTurnAndCheckGameEnd(currentGame,gameId)

            }
        }
        binding.finishButton.setOnClickListener {

                findNavController().navigate(R.id.action_singlePlayer_to_online_skockoGame)

        }
        startGame()
        handlePlayerTurn(currentGame, currentGame.currentTurn)

//        val currentPlayerId = currentGame.currentTurn
        val isPlayer1Turn = currentPlayerId == currentGame.player1
        Log.w(TAG, "questions from bodovi igre $bodovi")
        if (isPlayer1Turn) {
            // Omogućite interakciju prvom igraču (asocijacija1Buttons, asocijacija2Buttons, itd.)
            // Onemogućite interakciju drugom igraču
            player1Score = bodovi
        } else {
            // Omogućite interakciju drugom igraču
            // Onemogućite interakciju prvom igraču
            player2Score = bodovi
        }

//        // Spremanje rezultata prvog igrača u bazu
//        gameController.saveResultToDatabase(currentGame.gameId, currentGame.playerId1, player1Score)
//
//// Ažuriranje Game objekta u bazi sa novim potezom
//        gameController.updateGameInDatabase(currentGame.gameId, currentGame)
    }

    private fun startGame() {
        napuniPolja()
        startTimer()
    }

    private fun endRound() {
        timer.cancel()
        val currentPlayerId = currentGame.currentTurn
        val isPlayer1Turn = currentPlayerId == currentGame.player1

        // Provjeri točnost odgovora igrača
        val tacanOdgovor = asocijacije.asocijacijaKonacnoRjesenje.trim()
        val odgovor = binding.odgovorEditText.text.toString().trim()
        val isOdgovorTocan = odgovor.equals(tacanOdgovor, ignoreCase = true)

        // Dodijeli bodove odgovarajućem igraču

        if (isPlayer1Turn) {
            if (isOdgovorTocan) {
                player1Score += 7 + (6 * neodgovorenihAsocijacija) - oduzmiBodova
                currentGame.currentTurn = currentGame.player2.toString()
                // Spremi rezultate u bazu
                currentGame.id?.let {
                    associjacijaController.saveResultPlayer1(it, player1Score)
                }
            }
        } else {
            if (isOdgovorTocan) {
                Log.w(TAG,"bodovi tacni 2 $bodovi")
                player2Score += 7 + (6 * neodgovorenihAsocijacija) - oduzmiBodova
                currentGame.currentTurn = currentGame.player1.toString()
                // Spremi rezultate u bazu
                currentGame.id?.let {
                    associjacijaController.saveResultPlayer2(it, player1Score)
                }
            }
        }
        // Ažuriraj Game objekt s novim potezom
        Log.w(TAG,"bodovi tacni 2 $bodovi ++ $player1Score iii $player2Score")



        // Navigiraj na sljedeću rundu ili završi igru
        // implementacija ovisi o vašoj navigaciji
    }
    private fun switchTurnAndCheckGameEnd(game: Game, gameId: String) {
        associjacijaController.switchTurn(game, game.currentTurn) { success ->
            if (success) {
               // associjacijaController.endRound(gameId)
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

        timer.start()
    }

    private fun disableTurnForCurrentPlayer() {
        binding.finishButton.isEnabled = false
        binding.odgovorEditText.visibility = View.GONE
        timer.cancel()
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