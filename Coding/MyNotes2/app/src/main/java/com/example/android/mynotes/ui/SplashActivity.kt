package com.example.android.mynotes

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.android.mynotes.databinding.SplashScreenBinding

class SplashScreen : AppCompatActivity() {

    private lateinit var binding: SplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the binding object
        binding = SplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Delay of 2 seconds before navigating to HomeActivity
        Handler(Looper.getMainLooper()).postDelayed({
            // Navigate to the next screen (HomeActivity)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finish the SplashActivity so that it doesn't stay in the back stack
        }, 2000) // 2000 ms = 2 seconds
    }
}
