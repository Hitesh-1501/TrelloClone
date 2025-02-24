package com.example.projemanag.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.example.projemanag.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {
    private lateinit var binding:ActivityIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnSignInIntro.setOnClickListener{
            startActivity(Intent(this, SignInActivity::class.java))
        }

        binding.btnSignUpIntro.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))
        }

//        binding.btnPhoneNo.setOnClickListener{
//            startActivity(Intent(this,MobileNoActivity::class.java))
//        }
    }
}