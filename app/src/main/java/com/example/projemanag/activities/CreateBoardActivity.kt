package com.example.projemanag.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivityCreateBoardBinding
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class CreateBoardActivity : BaseActivity() {
    private lateinit var binding: ActivityCreateBoardBinding
    /// Add a global variable for URI of a selected image from phone storage
    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserName: String
    // A global variable for a board image URL
    private var mBoardImageURL: String = ""
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar()

        if(intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }

        binding.ivBoardImage.setOnClickListener {
            if(ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }else{
                /*Requests permissions to be granted to this application. These permissions
                 must be requested in your manifest, they should not be granted to your app,
                 and they should have protection level*/
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf( Manifest.permission.READ_MEDIA_IMAGES),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        binding.btnCreate.setOnClickListener {
            // Here if the image is not selected then update the other details of user.
            if(mSelectedImageFileUri != null){
                uploadBoardImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                // Call a function to update create a board.
                createBoard()
            }
        }
    }
    /**
     * A function to make an entry of a board in the database.
     */

    private fun createBoard(){
        //  A list is created to add the assigned members.
        //  This can be modified later on as of now the user itself will be the member of the board.
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserId())// adding the current user id.
        val createdBoardText = binding.etBoardName.text.toString()
        if(createdBoardText.isEmpty()) {
            showErrorSnackBar("Text is not empty")
            hideProgressDialog()
        }else{
            var board = Board(
                createdBoardText,
                mBoardImageURL,
                mUserName,
                assignedUsersArrayList
            )
            FirestoreClass().createBoard(this,board)
        }


    }
    /**
     * A function to upload the Board Image to storage and getting the downloadable URL of the image.
     */
    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        //getting the storage reference
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.
        child("BOARD_IMAGE"+System.currentTimeMillis()
                +"."+Constants.getFileExtension(this,mSelectedImageFileUri))
        //adding the file to reference
        sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot->
            // The image upload is success
            Log.i("Firebase Board Image URL",
                taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
            )

            // Get the downloadable url from the task snapshot
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->
                Log.i("Downloadable Image URL",uri.toString())
                // assign the image url to the variable.
                mBoardImageURL = uri.toString()
                // Call a function to create the board.
                createBoard()
            }.addOnFailureListener {
                    exception->
                showCustomToast(exception.message!!)
                hideProgressDialog()
            }
        }
    }
    /**
     * A function for notifying the board is created successfully.
     */
    fun boardCreatedSuccessfully(){
        showCustomToast("Board created successfully")
        hideProgressDialog()
        setResult(RESULT_OK)
        finish()
    }


    private fun setSupportActionBar(){
        setSupportActionBar(binding.toolbarCreateBoardActivity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back)
            actionBar.title = resources.getString(R.string.create_board_title)
        }
        binding.toolbarCreateBoardActivity.setNavigationOnClickListener { onBackPressed() }
    }
    /* This function will identify the result of runtime
      permission after the user allows or deny permission based on the unique code.
   */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }else{
                showCustomToast("Oops, you just denied the permission for storage. you can also allow from it settings.")
            }
        }
    }

    //Get the result of the image selection based on the constant code
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null){
            // The uri of selection image from phone storage.
            mSelectedImageFileUri = data.data
            try {
                // Load the user image in the ImageView.
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(binding.ivBoardImage);
            }catch(e : IOException){
                e.printStackTrace()
            }

        }
    }

}