package com.example.android.mynotes

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.mynotes.databinding.ActivitySignupBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class  SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()



        // Set up the sign-in redirect
        binding.signIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Handle the sign-up button click
        binding.buttonSignUp.setOnClickListener {
            validateAndSignUp()
        }

        // Set up focus change listeners for input validation
        setUpFocusChangeListeners()
        setUpPasswordTextWatcher() // Call this function to add password validation
    }

    private fun setUpFocusChangeListeners() {
        // Full Name validation (focus change)
        binding.fullName.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val fullName = binding.fullName.editText?.text.toString().trim()
                if (fullName.isEmpty()) {
                    binding.errorTextFullName.visibility = View.VISIBLE
                    binding.errorTextFullName.text = "Full name is required!"
                } else if (fullName.split(" ").size < 2) {
                    binding.errorTextFullName.visibility = View.VISIBLE
                    binding.errorTextFullName.text = "Enter both firstname and lastname"
                } else {
                    binding.errorTextFullName.visibility = View.INVISIBLE
                }
            }
        }

        // Email validation (focus change)
        binding.email.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val email = binding.email.editText?.text.toString().trim()
                if (email.isEmpty()) {
                    binding.errorTextEmail.visibility = View.VISIBLE
                    binding.errorTextEmail.text = "Email is required!"
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.errorTextEmail.visibility = View.VISIBLE
                    binding.errorTextEmail.text = "Enter a valid email!"
                } else {
                    binding.errorTextEmail.visibility = View.INVISIBLE
                }
            }
        }

        // Confirm Password validation (focus change)
        binding.confirmPassword.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val confirmPassword = binding.confirmPassword.editText?.text.toString().trim()
                val password = binding.password.editText?.text.toString().trim()
                if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                    binding.errorTextConfirmPassword.visibility = View.VISIBLE
                    binding.errorTextConfirmPassword.text = "Passwords do not match!"
                } else {
                    binding.errorTextConfirmPassword.visibility = View.INVISIBLE
                }
            }
        }
    }

    // Set up text watcher for password field to validate password strength in real-time
    private fun setUpPasswordTextWatcher() {
        binding.password.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                val password = editable.toString().trim()

                // Hide error message as user starts typing
                if (password.isNotEmpty()) {
                    binding.errorTextPassword.visibility = View.INVISIBLE
                }
            }
        })

        // Password validation (focus change)
        binding.password.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = binding.password.editText?.text.toString().trim()
                if (password.isEmpty()) {
                    binding.errorTextPassword.visibility = View.VISIBLE
                    binding.errorTextPassword.text = "Password is required!"
                } else {
                    val missingCriteria = checkPasswordStrength(password)
                    if (missingCriteria.isNotEmpty()) {
                        binding.errorTextPassword.visibility = View.VISIBLE
                        binding.errorTextPassword.text = "Password must include: $missingCriteria"
                    } else {
                        binding.errorTextPassword.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    // Function to check what criteria is missing from the password
    private fun checkPasswordStrength(password: String): String {
        var missingCriteria = ""
        if (!password.contains(Regex("[A-Z]"))) {
            missingCriteria += "uppercase letter, "
        }
        if (!password.contains(Regex("[a-z]"))) {
            missingCriteria += "lowercase letter, "
        }
        if (!password.contains(Regex("[*\\-#]"))) {
            missingCriteria += "special character, "
        }
        if (!password.contains(Regex("[0-9]"))) {
            missingCriteria += "number, "
        }

        return if (missingCriteria.isNotEmpty()) {
            // Remove the last comma and space
            missingCriteria.dropLast(2)
        } else {
            ""
        }
    }

    private fun validateAndSignUp() {
        val fullName = binding.fullName.editText?.text.toString().trim()
        val email = binding.email.editText?.text.toString().trim()
        val password = binding.password.editText?.text.toString().trim()
        val confirmPassword = binding.confirmPassword.editText?.text.toString().trim()

        var isValid = true

        // Full name validation (click validation, in case the user didn't type before)
        if (fullName.isEmpty()) {
            binding.errorTextFullName.text = "Full name is required!"
            binding.errorTextFullName.visibility = View.VISIBLE
            isValid = false
        } else if (fullName.split(" ").size < 2) {
            binding.errorTextFullName.text = "Full name must contain at least two words!"
            binding.errorTextFullName.visibility = View.VISIBLE
            isValid = false
        }

        // Email validation (already handled by focus listener)
        // Password validation (already handled by focus listener)
        // Confirm password validation (click validation)

        if (confirmPassword.isEmpty()) {
            binding.errorTextConfirmPassword.text = "Confirm your password!"
            binding.errorTextConfirmPassword.visibility = View.VISIBLE
            isValid = false
        } else if (confirmPassword != password) {
            binding.errorTextConfirmPassword.text = "Passwords do not match!"
            binding.errorTextConfirmPassword.visibility = View.VISIBLE
            isValid = false
        }

        // If everything is valid, proceed with Firebase sign-up
        if (isValid) {
            startLoading()
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    stopLoading()
                    if (task.isSuccessful) {
                        // Sign-up success
                        val user: FirebaseUser? = firebaseAuth.currentUser

                        // Send email verification
                        user?.sendEmailVerification()
                            ?.addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    // Inform the user that the email has been sent for verification
                                    Toast.makeText(
                                        this,
                                        "Account created successfully! Please check your email to verify.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Optionally, log out the user so they can log in after verification
                                    firebaseAuth.signOut()

                                    // Redirect the user to the login activity
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                } else {
                                    // Handle error in sending verification email
                                    Toast.makeText(
                                        this,
                                        "Failed to send verification email: ${verificationTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        // Sign-up failed
                        Toast.makeText(this, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }



    private fun startLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun stopLoading() {
        binding.progressBar.visibility = View.GONE
    }
}
