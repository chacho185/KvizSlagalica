package com.example.igricaslagalica.controller.auth

import android.content.ContentValues.TAG
import android.util.Log
import com.example.igricaslagalica.model.Player
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Duration

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

    fun loadPlayer(id: String, onComplete: (Player) -> Unit) {
        db.collection("players").document(id).get().addOnSuccessListener { documentSnapshot ->
            val player = documentSnapshot.toObject(Player::class.java)
            player?.let { p ->
                val currentTime = Timestamp.now()
                val lastTokenTime = p.lastTokenTime
                val diff = Duration.between(lastTokenTime.toDate().toInstant(), currentTime.toDate().toInstant())

                val diffInDays = diff.toHours().div(24)
                if (diffInDays >= 1) {
                    // Add 5 tokens for each day
                    p.tokens += 5 * diffInDays.toInt()

                    // Update the lastTokenTime to the current time
                    p.lastTokenTime = currentTime

                    // Update the player's tokens in the database
                    db.collection("players").document(id).update("tokens", p.tokens, "lastTokenTime", p.lastTokenTime)
                        .addOnSuccessListener {
                            onComplete(p)
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error updating document", e)
                        }
                } else {
                    onComplete(p)
                }
            }
        }
    }




}