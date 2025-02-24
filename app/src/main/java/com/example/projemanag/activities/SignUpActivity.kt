package com.example.projemanag.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivitySignUpBinding
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setUpActionBar()
        binding.btnSignUp.setOnClickListener{
            registerUser()
        }
    }

    /**
     * A function to be called the user is registered successfully and entry is made in the firestore database.
     */
    fun userRegisteredSuccess(){
        showCustomToast("you have successfully registered the email address")
        hideProgressDialog()
        /**
         * Here the new user registered is automatically signed-in so we just sign-out the user from firebase
         * and send him to Intro Screen for Sign-In
         */
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun setUpActionBar(){
        setSupportActionBar(binding.toolbarSignUpActivity)
        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)
        }
        binding.toolbarSignUpActivity.setNavigationOnClickListener { onBackPressed() }

    }
    // A function to register a user to our app using the Firebase.
    private fun registerUser(){
        // Here we get the text from editText and trim the space
        val name: String = binding.etName.text.toString().trim{ it <= ' '}
        val email: String = binding.etEmail.text.toString().trim{ it <= ' '}
        val password: String = binding.etPassword.text.toString().trim{ it <= ' '}

        if(validateForm(name,email,password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->
                    // If the registration is successfully done
                    if (task.isSuccessful) {
                        // Firebase registered user
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        // Registered Email
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid,name,registeredEmail)
                        // call the registerUser function of FirestoreClass to make an entry in the database.
                        FirestoreClass().registerUser(this,user)
                    } else {
                        task.exception!!.message?.let { showCustomToast(it) }
                        hideProgressDialog()
                    }
                }
        }
    }

    /**
     * A function to validate the entries of a new user.
     */

    private fun validateForm(name:String, email:String, password:String): Boolean {
        return when{
            TextUtils.isEmpty(name)->{
                showErrorSnackBar("please enter a name")
                false
            }
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("please enter a email address")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("please enter a password")
                false
            }
            else->{
                true
            }
        }
    }
}