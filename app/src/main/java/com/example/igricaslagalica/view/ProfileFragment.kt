package com.example.igricaslagalica.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.igricaslagalica.R
import com.example.igricaslagalica.controller.auth.FirebaseAuthController
import com.example.igricaslagalica.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val authController: FirebaseAuthController
    private lateinit var content: ActivityResultLauncher<String>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    init {
        val db = FirebaseFirestore.getInstance()
        authController = FirebaseAuthController(db)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        content = registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri: Uri? ->
            // Handle the returned Uri
            val user = FirebaseAuth.getInstance().currentUser
            val storageReference = FirebaseStorage.getInstance().getReference("profilepics/${user?.uid}")

            imageUri?.let {
                storageReference.putFile(it).addOnSuccessListener {
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        val sharedPreferences = activity?.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                        sharedPreferences?.edit()?.putString("profilePicUrl", uri.toString())?.apply()
                        Log.d("ProfileFragment", "Download URL: $uri")

                        // Load the image into the ImageView
                        Glide.with(this)
                            .load(uri)
                            .into(binding.imageViewProfile)
                    }.addOnFailureListener { exception ->
                        // Handle the error
                        Log.e("ProfileFragment", "Could not get download URL: $exception")

                    }
                }.addOnFailureListener { exception ->
                    // Handle the error
                    Log.e("ProfileFragment", "Could not upload image: $exception")
                }

            }

        }

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        binding.textViewEmail.text = user?.email
        binding.textViewUsername.text = user?.displayName

        binding.imageViewProfile.setOnClickListener {
            content.launch("image/*")

        }
        // Load the profile picture from shared preferences
        val sharedPreferences = activity?.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val profilePicUrl = sharedPreferences?.getString("profilePicUrl", null)
        if (profilePicUrl != null) {
            Glide.with(this)
                .load(profilePicUrl)
                .into(binding.imageViewProfile)
        }

        binding.buttonLogout.setOnClickListener{
            authController.signOut()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }


        //...

        if (user != null) {
            authController.loadPlayer(user.uid) { player ->
                // Show the player's tokens somewhere
                binding.textViewTokeni.text = player.tokens.toString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}