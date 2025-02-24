package com.example.projemanag.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivityMyProfileBinding
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MyProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityMyProfileBinding
    /// Add a global variable for URI of a selected image from phone storage
    private var mSelectedImageFileUri: Uri? = null
    // A global variable for a user profile image URL
    private var mProfileImageURL: String = ""
    // A global variable for user details.
    private lateinit var mUserDetails: User
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()
        //Call a function to get the current logged in user details
        FirestoreClass().loadUserData(this)
        binding.ivUserImage.setOnClickListener {
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
        binding.btnUpdate.setOnClickListener {
            // Here if the image is not selected then update the other details of user.
            if(mSelectedImageFileUri != null){
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                // Call a function to update user details in the database.
                updateUserProfileData()
            }
        }
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
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.ivUserImage);
            }catch(e : IOException){
                e.printStackTrace()
            }

        }
    }
    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarMyProfileActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back)
            actionBar.title = resources.getString(R.string.my_profile)
        }
        binding.toolbarMyProfileActivity.setNavigationOnClickListener { onBackPressed() }
    }
    // A function to set the existing details in UI.
    fun setUserDataInUi(user: User){
        // Initialize the user details variable
        mUserDetails = user
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.ivUserImage);

        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)

        if(user.mobile != 0L){
            binding.etMobile.setText(user.mobile.toString())
        }
    }
    //A function to upload the selected user image to firebase cloud storage.
    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        if(mSelectedImageFileUri != null){
            //getting the storage reference
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.
            child("USER_IMAGE"+System.currentTimeMillis()
                    +"."+Constants.getFileExtension(this,mSelectedImageFileUri))
            //adding the file to reference
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot->
                // The image upload is success
                Log.i("Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                // Get the downloadable url from the task snapshot
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->
                    Log.i("Downloadable Image URL",uri.toString())
                    // assign the image url to the variable.
                    mProfileImageURL = uri.toString()
                    // Call a function to update user details in the database.
                    updateUserProfileData()
                }.addOnFailureListener {
                    exception->
                    showCustomToast(exception.message!!)
                    hideProgressDialog()
                }
            }
        }
    }
    //A function to notify the user profile is updated successfully.
    fun profileUpdateSuccess(){
        showCustomToast("Profile updated successfully")
        hideProgressDialog()
        //Send the success result to the Base Activity
        setResult(Activity.RESULT_OK)
        finish()
    }
   // A function to update the user profile details into the database.
   private fun updateUserProfileData(){
        val userHashMap = HashMap<String, Any>()

        if(mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }
        if(binding.etName.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME] = binding.etName.text.toString()
        }
        if(binding.etMobile.text.toString() != mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE] = binding.etMobile.text.toString().toLong()
        }
       // Update the data in the database.
       FirestoreClass().updateUserProfileData(this,userHashMap)
    }
}