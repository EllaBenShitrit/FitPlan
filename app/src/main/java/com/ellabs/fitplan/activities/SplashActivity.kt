package com.ellabs.fitplan.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// First screen. Showing the logo until Main Activity is ready.
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}