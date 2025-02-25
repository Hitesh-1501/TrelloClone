package com.example.projemanag.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.adapters.BoardItemsAdapter
import com.example.projemanag.databinding.ActivityMainBinding
import com.example.projemanag.databinding.AppBarMainBinding
import com.example.projemanag.databinding.MainContentBinding
import com.example.projemanag.databinding.NavHeaderMainBinding
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    companion object{
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }
    private var mUserName: String = ""
    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbarBinding: AppBarMainBinding
    private lateinit var headerBinding : NavHeaderMainBinding
    private lateinit var mainContentBinding: MainContentBinding
    private lateinit var mSharedPreferences: SharedPreferences
    private var directDownloadLink : String = "https://drive.google.com/drive/folders/1rX0BtKDkjNZ5cOc85836BsqQZ1iBkgQR?usp=sharing"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Manually bind the app_bar_main.xml
        val appBarMainView = binding.root.findViewById<android.view.View>(R.id.app_bar_main)
        toolbarBinding = AppBarMainBinding.bind(appBarMainView)

        val mainContentView = toolbarBinding.root.findViewById<android.view.View>(R.id.main_content)
        mainContentBinding = MainContentBinding.bind(mainContentView)

        setSupportActionBar()
        toolbarBinding.fabCreateBoard.setOnClickListener {
            val intent = Intent(this,CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME,mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }

        // Access the header binding
        headerBinding = NavHeaderMainBinding.bind(binding.navView.getHeaderView(0))
        // Assign the NavigationView.OnNavigationItemSelectedListener to navigation view
        binding.navView.setNavigationItemSelectedListener(this)
        // Initialize the mSharedPreferences variable
        mSharedPreferences = this.getSharedPreferences(Constants.PROJEMANAG_PREFERENCES
            ,Context.MODE_PRIVATE)
        // Variable is used get the value either token is updated in the database or not.
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED,false)
        // Here if the token is already updated than we don't need to update it every time.
        if(tokenUpdated){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this,true)
        }else{
            FirebaseMessaging.getInstance()
                .token.addOnCompleteListener {task->
                    updateFCMToken(task.result)
                }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ){
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    Constants.NOTIFICATION_REQUEST_CODE
                )
            }
        }
        //Call a function to get the current logged in user details
        FirestoreClass().loadUserData(this,true)
    }
    /**
     * A function to populate the result of BOARDS list in the UI i.e in the recyclerView.
     */
    fun populateBoardsListToUI(boardsList: ArrayList<Board>){
        hideProgressDialog()
        if(boardsList.size > 0){
            mainContentBinding.rvBoardsList.visibility = View.VISIBLE
            mainContentBinding.tvNoBoardsAvailable.visibility = View.GONE

            mainContentBinding.rvBoardsList.layoutManager = LinearLayoutManager(this)
            mainContentBinding.rvBoardsList.setHasFixedSize(true)
            // Create an instance of BoardItemsAdapter and pass the boardList to it.
            val adapter = BoardItemsAdapter(this,boardsList)
            mainContentBinding.rvBoardsList.adapter = adapter// Attach the adapter to the recyclerView.

            adapter.setOnClickListener(object : BoardItemsAdapter.OnclickListener{
                override fun onClick(position: Int, model: Board) {
                    //Pass the documentId of a board through intent
                    val intent  = Intent(this@MainActivity,TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.documentId)
                    startActivity(intent)
                }
            })
        }else{
            mainContentBinding.rvBoardsList.visibility = View.GONE
            mainContentBinding.tvNoBoardsAvailable.visibility = View.VISIBLE
        }
    }


    // A function to setup action bar
    fun setSupportActionBar(){
        setSupportActionBar(toolbarBinding.toolbarMainActivity)
        toolbarBinding.toolbarMainActivity.setNavigationIcon(R.drawable.baseline_menu_24)
        toolbarBinding.toolbarMainActivity.setNavigationOnClickListener {
                toogleDrawer()
        }
    }
    // A function for opening and closing the Navigation Drawer.
    private fun toogleDrawer(){
        if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    // Add a onBackPressed function and check if the navigation drawer is open or closed.
    override fun onBackPressed() {
        if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            // A double back press function is added in Base Activity.
            doubleBackToExit()
        }
    }
    // Add the onActivityResult function and check the result of the activity for which we expect the result.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }else if(resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE){
            // Get the latest boards list.
            FirestoreClass().getBoardsList(this)
        }else{
            Log.e("Cancelled","Cancelled")
        }
    }

    //Implement members of NavigationView.OnNavigationItemSelectedListener
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile ->{
                startActivityForResult(Intent(this,MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out ->{

                mSharedPreferences.edit().clear().apply()
                // Here sign outs the user from firebase in this device.
                FirebaseAuth.getInstance().signOut()
                // Send the user to the intro screen of the application.
                val intent = Intent(this,IntroActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            R.id.nav_share ->{
                shareApp()

            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        // Reset menu item selection to remove the highlight effect
        Handler(Looper.getMainLooper()).postDelayed({
            binding.navView.menu.findItem(item.itemId).isChecked = false
        }, 200)
        return true
    }
    //A function to get the current user details from firebase.
    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean){
        hideProgressDialog()
        mUserName = user.name
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(headerBinding.navUserImage);
        headerBinding.tvUsername.text = user.name
        //Here if the ReadBoardList is TRUE then get the list of boards.
        if(readBoardsList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
    }
    /**
     * A function to notify the token is updated successfully in the database.
     */

    @SuppressLint("CommitPrefEdits")
    fun tokenUpdateSuccess(){
        hideProgressDialog()
        // Here we have added a another value in shared preference that the token is updated in the database successfully.
        // So we don't need to update it every time.
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED,true)
        editor.apply()
        // Get the current logged in user details.
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this,true)
    }
    /**
     * A function to update the user's FCM token into the database.
     */
    private fun updateFCMToken(token: String){
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        // Update the data in the database.
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this,userHashMap)
    }
    private fun shareApp(){
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain")
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this app!")
        var shareMessage = "ProjeManag,Download the app Now: "
        shareMessage = shareMessage + directDownloadLink + "\n\n"
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.NOTIFICATION_REQUEST_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.e("Permission","Permission already granted")
            }else{
                showCustomToast("Oops, you just denied the permission for storage. you can also allow from it settings.")
            }
        }
    }

    fun boardDeletedSuccessfully(){
        hideProgressDialog()
        FirestoreClass().getBoardsList(this)
    }

    fun deleteBoard(position: Int,boardList: ArrayList<Board>){
        val board = boardList[position]
        boardList.removeAt(position)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().deleteBoards(this,board)
        showCustomToast("board deleted successfully")
    }
}