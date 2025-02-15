package com.example.android.note

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.android.note.databinding.ActivitySignInBinding
import com.example.android.note.databinding.ActivitySignUpBinding

class SignInActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySignInBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView11.setOnClickListener{
            val intent  = Intent(this,SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.passwordForgot.setOnClickListener{
            val intent  = Intent(this,ForgotPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }

    }


}