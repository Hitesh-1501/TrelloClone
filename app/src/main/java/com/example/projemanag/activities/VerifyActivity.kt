package com.example.projemanag.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivityVerifyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class VerifyActivity : BaseActivity() {
    lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityVerifyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val storedVerificationId=intent.getStringExtra("storedVerificationId")

        binding.verifyBtn.setOnClickListener{
            var otp = binding.idOtp.text.toString().trim{ it <= ' '}
            if(!otp.isEmpty()){
                showProgressDialog(resources.getString(R.string.please_wait))
                val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    storedVerificationId.toString(), otp)
                signInWithPhoneAuthCredential(credential)

            }else{
                showCustomToast("Enter Otp")
            }
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                hideProgressDialog()
                if (task.isSuccessful) {
                    val user = task.result?.user
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                }else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        showCustomToast("Invalid otp")
                    }
                }
            }

    }
}