package com.example.igricaslagalica.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.igricaslagalica.R
import com.example.igricaslagalica.controller.FirebaseGameController
import com.example.igricaslagalica.controller.auth.FirebaseAuthController
import com.example.igricaslagalica.databinding.FinalGameScoreBinding
import com.google.firebase.firestore.FirebaseFirestore

class GameEndScore : Fragment() {

        private var _binding: FinalGameScoreBinding? = null
        private val firebaseGameController: FirebaseGameController

        // This property is only valid between onCreateView and
        // onDestroyView.
        private val binding get() = _binding!!
        init {
            val db = FirebaseFirestore.getInstance()
            firebaseGameController = FirebaseGameController()
        }
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {

            _binding = FinalGameScoreBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val pointsOne = arguments?.getInt("player1Score")
            val pointsTwo = arguments?.getInt("player2Score")

            var starsOne = pointsOne?.let { calculateStars(it, pointsOne > pointsTwo!!) }
            var starsTwo = pointsTwo?.let { calculateStars(it, pointsTwo > pointsOne!!) }


            val idOne = arguments?.getString("idOne")
            val idTwo =  arguments?.getString("idTwo")
            var playerName = ""
            var playerTwoName = ""
            Log.w("testaa", "zvjezde $starsOne ++ $starsTwo")
            firebaseGameController.getPlayerName(idOne!!) { playerOneName ->
                if (playerOneName != null) {
                    playerName = playerOneName
                    binding.player1Info.text = "Player $playerOneName make result of $pointsOne points, and  $starsOne stars "

                }
            }
            firebaseGameController.getPlayerName(idTwo!!) { playerOneName ->
                if (playerOneName != null) {
                    playerTwoName = playerOneName
                    binding.player2Info.text = "Player $playerOneName make result of $pointsTwo points and $starsTwo stars"

                }
            }

           // val playerTwoName = idTwo?.let { firebaseGameController.getPlayerName(it){} }
            firebaseGameController.getCurrentStars(idOne) { stars ->
                if (starsOne != null && stars != null) {
                    if (stars != 0) {
//                        val starsToAdd = if (starsOne!! > stars) {
//                            0
//                        } else {
//                            stars - starsOne!! // Deduct the stars you already have
//                        }
//
//                        starsOne = starsOne!! + (stars - starsToAdd)
                        starsOne += stars
                        Log.w("star", "zvjez doleOne $starsOne ++  + $stars")
                        firebaseGameController.updatePlayerStars(idOne,starsOne!!){}
                    }
                }
            }
            firebaseGameController.getCurrentStars(idTwo) { stars ->
                if (starsTwo != null && stars != null) {
                        if(stars != 0){
//                            val starsToAdd =  if( starsTwo!! > stars){
//                                0
//                            } else {
//                                stars - starsTwo!!
//                            }
//                            starsTwo = starsTwo!! + (stars - starsToAdd)
                            starsTwo += stars
                            Log.w("star", "zvjez dole $starsTwo ++ + $stars")
                            firebaseGameController.updatePlayerStars(idTwo, starsTwo!!){}
                        }

                }
            }

            binding.button.setOnClickListener {
                findNavController().navigate(R.id.action_endGameScore_to_profileFragment)
            }
        }


        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    // Calculate stars earned/lost based on points
    fun calculateStars(points: Int, isWinner: Boolean): Int {
        val baseStars = 10
        val additionalStars = points / 40

        return if (isWinner) {
            baseStars + additionalStars
        } else {
            // For the loser, subtract 10 stars and add additional stars
            // Note: The additional stars are added because even though they lose, they still get stars for points earned.
            (-10) + additionalStars
        }
    }


    // Calculate tokens earned based on stars
    fun calculateTokens(stars: Int): Int {
        return stars / 50
    }

    // Update player's star count and tokens
    fun updateStarAndTokenCounts(playerId: String, points: Int) {
        // Get player's current star count from Firebase
       // val currentStars = getCurrentStarsFromFirebase(playerId)

        // Calculate stars earned
      //  val starsEarned = calculateStars(points)

        // Calculate tokens earned
    //    val tokensEarned = calculateTokens(currentStars + starsEarned)

        // Update player's star count and tokens in Firebase
       // updateStarsAndTokensInFirebase(playerId, currentStars + starsEarned, tokensEarned)
    }


    }

