package com.example.projemanag.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.core.view.WindowCompat
import com.example.projemanag.databinding.ActivitySplashBinding
import com.example.projemanag.firebase.FirestoreClass

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val typeFace: Typeface = Typeface.createFromAsset(assets,"carbon bl.ttf")
        binding.tvAppName.typeface = typeFace

        Handler().postDelayed({
            val currentUserId = FirestoreClass().getCurrentUserId()
            if(currentUserId.isNotBlank()){
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        },2500)
    }
}