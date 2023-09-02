package com.example.igricaslagalica.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.igricaslagalica.controller.FirebaseGameController
import com.example.igricaslagalica.controller.auth.FirebaseAuthController
import com.example.igricaslagalica.databinding.FragmentScoreBinding
import com.example.igricaslagalica.model.Player
import com.example.igricaslagalica.view.adapter.FriendsAdapter
import com.example.igricaslagalica.view.adapter.RangListAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// List for scores
class ScoreFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private var _binding: FragmentScoreBinding? = null
    private val firebaseGameController: FirebaseGameController

    private lateinit var recyclerViewRangList: RecyclerView
    private lateinit var friendsAdapter: RangListAdapter
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

        _binding = FragmentScoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewRangList = binding.recyclerViewRangList
        // Initialize the adapter
        friendsAdapter = RangListAdapter(emptyList()) // Pass an empty list initially
        recyclerViewRangList.adapter = friendsAdapter
        recyclerViewRangList.layoutManager = LinearLayoutManager(requireContext())


        firebaseGameController.getRankedPlayers { rankedPlayers ->
            Log.w("taaa", "tudenak $rankedPlayers")
            updateUIWithRankedPlayers(rankedPlayers)
        }
    }

    private fun updateUIWithRankedPlayers(players: List<Player>) {
        // Initialize or update your RecyclerView adapter with the ranked players list
        friendsAdapter.updateData(players)
        friendsAdapter.notifyDataSetChanged()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        @JvmStatic
        fun newInstance() = ScoreFragment()
    }

}