package com.example.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivitySignInBinding
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SignInActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialize Firebase Auth
        auth = Firebase.auth
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setUpActionBar()
        binding.btnSignIn.setOnClickListener{
            registerSignInUser()
        }

    }
    private fun setUpActionBar(){
        setSupportActionBar(binding.toolbarSignInActivity)
        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)
        }
        binding.toolbarSignInActivity.setNavigationOnClickListener { onBackPressed() }

    }
    // A function to get the user details from the firestore database after authentication.
    fun signInSuccess(user:User){
        showCustomToast("you have successfully signIn")
        hideProgressDialog()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    private fun registerSignInUser(){
        val email = binding.etEmail.text.toString().trim { it <= ' ' }
        val password =  binding.etPassword.text.toString().trim{ it <= ' '}

        if(validateSignInForm(email , password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        FirestoreClass().loadUserData(this)
                    } else {
                        // If sign in fails, display a message to the user.
                        task.exception!!.message?.let { showCustomToast(it) }
                    }
                }

        }
    }

   private fun validateSignInForm(email:String, password:String): Boolean{
        return when{
            TextUtils.isEmpty(email)->{
                    showErrorSnackBar("Please Enter a Email Address")
                    false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please Enter a Password")
                false
            }
            else->{
                true
            }
        }
    }
}