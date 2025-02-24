package com.example.projemanag.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivityBaseBinding
import com.example.projemanag.databinding.CustomToastBinding
import com.example.projemanag.databinding.DialogProgressBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false
    private lateinit var mProgressDialog: Dialog
    private lateinit var binding: ActivityBaseBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
    /**
     * This function is used to show the progress dialog with the title and message to user.
     */
    fun showProgressDialog(text:String){
        mProgressDialog = Dialog(this)
        var dialogBinding = DialogProgressBinding.inflate(layoutInflater)
        mProgressDialog.setContentView(dialogBinding.root)
        dialogBinding.tvProgressText.text = text
        mProgressDialog.show()
    }

    fun hideProgressDialog(){
        mProgressDialog.dismiss()
    }

    fun getCurrentUserId(): String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
    fun doubleBackToExit(){
        if(doubleBackToExitPressedOnce){
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        showCustomToast(resources.getString(R.string.please_click_back_again_to_exit))
        Handler().postDelayed({doubleBackToExitPressedOnce = false},2000)
    }
    fun showErrorSnackBar(message: String){
        val snackBar =
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(
            ContextCompat.getColor(
                this@BaseActivity,
                R.color.snackbar_error_color
            )
        )
        snackBar.show()
    }

    fun showCustomToast(text: String){
        var binding = CustomToastBinding.inflate(layoutInflater)
        binding.customToast.text = text
        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_LONG
        toast.view = binding.root
        toast.show()
    }
}