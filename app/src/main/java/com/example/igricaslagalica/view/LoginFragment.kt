package com.example.igricaslagalica.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.igricaslagalica.controller.auth.AuthListener
import com.example.igricaslagalica.controller.auth.FirebaseAuthController
import androidx.navigation.fragment.findNavController
import com.example.igricaslagalica.MainActivity
import com.example.igricaslagalica.R
import com.example.igricaslagalica.SharedViewModel
import com.example.igricaslagalica.databinding.FragmentLoginBinding
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class LoginFragment : Fragment(), AuthListener {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()


    // Dodajte instancu FirebaseAuthController-a
    private lateinit var authController: FirebaseAuthController
    init {
        val db = FirebaseFirestore.getInstance()
        authController = FirebaseAuthController(db)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLogin.setOnClickListener {
            loginUser()
        }
        binding.buttonRegister.setOnClickListener{
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
        }

        binding.buttonPlay.setOnClickListener {
           findNavController().navigate(R.id.action_loginFragment_to_koZnaZnaGame)
        }
    }

    private fun loginUser() {
        val email = binding.editTextEmail.text.toString()
        val password = binding.editTextPassword.text.toString()


        // TODO Prosiriti funkciju singIn da moze primiti i username a ne samo email
        if(email.isNotEmpty() && password.isNotEmpty())
            authController.signIn(email, password, this)
        else
            Toast.makeText(context, "Morate unijeti email/username i password!", Toast.LENGTH_SHORT).show()

    }
    override fun onAuthSuccess() {
        // Handle login success (navigate to another fragment, show a success message, etc.)
        findNavController().navigate(R.id.action_loginFragment_to_profileFragment)

    }

    override fun onAuthFailed(message: String) {
        // Handle login failure (show an error message, etc.)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}