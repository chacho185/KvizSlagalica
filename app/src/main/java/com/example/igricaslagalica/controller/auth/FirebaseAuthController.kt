package com.example.igricaslagalica.controller.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class FirebaseAuthController {

    private val mAuth = FirebaseAuth.getInstance()

    fun signUp(email: String, password: String, username: String, listener: AuthListener) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign up success
                val user = mAuth.currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username).build()

                user?.updateProfile(profileUpdates)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            listener.onAuthSuccess()
                        }
                    }
            } else {
                // If sign up fails
                listener.onAuthFailed(task.exception?.message ?: "Authentication failed.")
            }
        }
    }


    fun signIn(email: String, password: String, listener: AuthListener) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign in success
                listener.onAuthSuccess()
            } else {
                // If sign in fails
                listener.onAuthFailed(task.exception?.message ?: "Authentication failed.")
            }
        }
    }
    fun signOut() {
        mAuth.signOut()
    }

    // ostale metode checkIfUserIsSignedIn, etc.
}