package com.example.igricaslagalica.controller.auth

import android.util.Log
import com.example.igricaslagalica.model.Player
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseAuthController(private val db: FirebaseFirestore) {

    private val mAuth = FirebaseAuth.getInstance()



    fun signUp(email: String, password: String, username: String, listener: AuthListener) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign up success
                val user = mAuth.currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username).build()

                user?.updateProfile(profileUpdates)
                    ?.addOnCompleteListener { profileUpdateTask ->
                        if (profileUpdateTask.isSuccessful) {
                            // Add new player to Firestore after successful registration
                            val player = Player(id = user.uid, email = email, username = username, tokens = 5)
                            db.collection("players").document(user.uid).set(player)
                                .addOnSuccessListener {
                                    listener.onAuthSuccess()
                                }.addOnFailureListener { exception ->
                                    listener.onAuthFailed(exception.message ?: "Failed to add player to Firestore.")
                                }
                        } else {
                            listener.onAuthFailed(profileUpdateTask.exception?.message ?: "Failed to update profile.")
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

    fun loadPlayer(uid: String, onComplete: (Player) -> Unit) {
        db.collection("players").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val player = document.toObject(Player::class.java)
                    if (player != null) {
                        onComplete(player)
                    }
                } else {
                    Log.d("FirebaseAuthController", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("FirebaseAuthController", "get failed with ", exception)
            }
    }


}