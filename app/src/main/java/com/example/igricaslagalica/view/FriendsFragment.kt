package com.example.igricaslagalica.view

import FriendsViewModel
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.igricaslagalica.R
import com.example.igricaslagalica.SharedViewModel
import com.example.igricaslagalica.databinding.FragmentFriendsBinding
import com.example.igricaslagalica.view.adapter.FriendsAdapter


class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var viewModel: FriendsViewModel
    private lateinit var recyclerViewMyFriends: RecyclerView
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[FriendsViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewMyFriends = binding.recyclerViewMyFriends
        friendsAdapter = FriendsAdapter(viewModel.friendsListStatic) // Prazna lista prijatelja
        recyclerViewMyFriends.adapter = friendsAdapter
        recyclerViewMyFriends.layoutManager = LinearLayoutManager(requireContext())

        binding.buttonAddFriend.setOnClickListener {
            val trazeniUsername = binding.editTextSearch.text
            if(trazeniUsername.isNotEmpty())
            {
                //viewModel.addFriend(trazeniUsername)
            }
        }

    }

}