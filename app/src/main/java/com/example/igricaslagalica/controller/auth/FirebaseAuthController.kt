package com.example.igricaslagalica.controller.auth

import com.google.firebase.auth.FirebaseAuth

class FirebaseAuthController {

    private val mAuth = FirebaseAuth.getInstance()

    fun signUp(email: String, password: String, listener: AuthListener) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign up success
                val user = mAuth.currentUser
                listener.onAuthSuccess()
            } else {
                // If sign up fails
                listener.onAuthFailed(task.exception?.message ?: "Authentication failed.")

            }
        }
    }

    fun signIn(email: String, password: String,  listener: AuthListener) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign up success
                listener.onAuthSuccess()
            } else {
                // If sign up fails
                listener.onAuthFailed(task.exception?.message ?: "Authentication failed.")
            }
        }
    }

    // ostale metode kao što je signOut, checkIfUserIsSignedIn, etc.
}