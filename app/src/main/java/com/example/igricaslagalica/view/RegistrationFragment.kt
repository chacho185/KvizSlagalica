package com.example.igricaslagalica.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.igricaslagalica.R
import com.example.igricaslagalica.controller.auth.AuthListener
import com.example.igricaslagalica.controller.auth.FirebaseAuthController
import com.example.igricaslagalica.databinding.FragmentRegistrationBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRegister.setOnClickListener {
            registerUser()
           // findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
        }
    }

    private fun registerUser() {
        val email = binding.editTextEmail.text.toString()
        val username = binding.editTextUsername.text.toString()
        val password = binding.editTextPassword.text.toString()
        val confirmPassword = binding.editTextConfirmPassword.text.toString()

        if (password == confirmPassword) {
            //Add the firebase logic
            FirebaseAuthController().signUp(email, password, username, object : AuthListener {
                override fun onAuthSuccess() {
                    findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
                }
                override fun onAuthFailed(message: String) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
        }
}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}