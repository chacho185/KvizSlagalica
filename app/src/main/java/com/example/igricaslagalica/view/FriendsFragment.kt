package com.example.igricaslagalica.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.igricaslagalica.controller.FriendsController
import com.example.igricaslagalica.databinding.FragmentFriendsBinding
import com.example.igricaslagalica.view.adapter.FriendsAdapter


class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private lateinit var friendsController: FriendsController

    private val binding get() = _binding!!

    private lateinit var recyclerViewMyFriends: RecyclerView
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        friendsController = FriendsController()

        recyclerViewMyFriends = binding.recyclerViewMyFriends
        friendsAdapter = FriendsAdapter(friendsController.friendsList) // Use the list from FriendsController
        recyclerViewMyFriends.adapter = friendsAdapter
        recyclerViewMyFriends.layoutManager = LinearLayoutManager(requireContext())

        binding.buttonAddFriend.setOnClickListener {
            val trazeniUsername = binding.editTextSearch.text.toString()
            if (trazeniUsername.isNotEmpty()) {
                friendsController.addFriend(trazeniUsername,
                    {
                        // This code will be executed if the friend is added successfully
                        // Here, you can refresh your friend list
                        friendsController.getFriends { friends ->
                            friendsAdapter.updateData(friends)
                            Toast.makeText(context, "You added new friend: $trazeniUsername", Toast.LENGTH_SHORT).show()

                        }

                    },
                    { e ->
                        // This code will be executed if there is an error while adding the friend
                        // Here, you can show an error message to the user
                        Toast.makeText(context, "Error adding friend: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                )

            }
        }

        friendsController.getFriends { friends ->
            // Update the adapter with the list of friends
            friendsAdapter.updateData(friends)
        }
    }

}