package com.example.android.mynotes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.mynotes.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // ProgressBar initialization
        progressBar = binding.progressBar // Assuming you have added ProgressBar in your layout XML

        // Check if the user is already logged in
        val user = auth.currentUser
        if (user != null && user.isEmailVerified) {
            // If user is logged in and email is verified, go to MainActivity
            navigateToMainActivity()
        }

        binding.signUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.signIn.setOnClickListener {
            val email = binding.email.editText?.text.toString().trim()
            val password = binding.password.editText?.text.toString().trim()

            // Validate inputs
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        startLoading()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                stopLoading()

                if (task.isSuccessful) {
                    // Check if email is verified
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        // Navigate to MainActivity if the email is verified
                        navigateToMainActivity()
                    } else {
                        // Show message if email is not verified
                        Toast.makeText(this, "Please verify your email.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Show failure message
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun startLoading() {
        progressBar.visibility = View.VISIBLE
    }

    private fun stopLoading() {
        progressBar.visibility = View.GONE
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Prevent going back to login screen
    }

    override fun onStart() {
        super.onStart()

        // Check if user is already authenticated and email is verified
        val user = auth.currentUser
        if (user != null && user.isEmailVerified) {
            // Skip the login screen and go to MainActivity
            navigateToMainActivity()
        }
    }
}
