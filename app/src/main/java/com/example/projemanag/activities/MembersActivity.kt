package com.example.projemanag.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.whenResumed
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projemanag.R
import com.example.projemanag.adapters.MembersListItemAdapter
import com.example.projemanag.databinding.ActivityMembersBinding
import com.example.projemanag.databinding.DialogSearchMemberBinding
import com.example.projemanag.fcm.FirebaseAuthTokenGenerator
import com.example.projemanag.fcm.FirebaseUtils
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MembersActivity : BaseActivity() {
    private lateinit var binding: ActivityMembersBinding
    private lateinit var mBoardDetails: Board
    private lateinit var mAssignedMembersList: ArrayList<User>

    // A global variable for notifying any changes done or not in the assigned members list.
    private var anyChangeMade: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allow network calls on the main thread (not recommended for production)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (intent.hasExtra(Constants.BOARD_DETAILS)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAILS)!!
        }
        setUpActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        // Get the members list details from the database.
        FirestoreClass().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
        FirebaseUtils.copyServiceAccountJsonToFiles(this)

    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarMembersActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back)
            actionBar.title = resources.getString(R.string.members)
        }
        binding.toolbarMembersActivity.setNavigationOnClickListener { onBackPressed() }
    }

    /**
     * A function to setup assigned members list into recyclerview.
     */
    fun setupMembersList(list: ArrayList<User>) {
        mAssignedMembersList = list
        hideProgressDialog()
        binding.rvMembersList.layoutManager = LinearLayoutManager(this)
        binding.rvMembersList.setHasFixedSize(true)
        val adapter = MembersListItemAdapter(this, list)
        binding.rvMembersList.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_member -> {
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        var dialogBinding = DialogSearchMemberBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.setCanceledOnTouchOutside(false)
        dialogBinding.tvAdd.setOnClickListener {
            val email = dialogBinding.etEmailSearchMember.text.toString()
            if (email.isNotEmpty()) {
                dialog.dismiss()
                // Get the member details from the database
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getMemberDetails(this, email)
            } else {
                showCustomToast("Please enter members email address.")
            }
        }
        dialogBinding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    //Here we will get the result of the member if it found in the database.
    fun memberDetails(user: User) {
        //Here add the user id to the existing assigned members list of the board.
        mBoardDetails.assignedTo.add(user.id)
        //Finally assign the member to the board
        FirestoreClass().assignMemberTo(this, mBoardDetails, user)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (anyChangeMade) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }


    /**
     * A function to get the result of assigning the members.
     */
    fun memberAssignSuccess(user: User) {
        hideProgressDialog()
        mAssignedMembersList.add(user)
        // Here the list is updated so change the global variable which we have declared for notifying changes.
        anyChangeMade = true
        setupMembersList(mAssignedMembersList)

        // Call the AsyncTask class when the board is assigned to the user and based on the users detail send them the notification using the FCM token
        val accessToken = FirebaseAuthTokenGenerator.getAccessToken(this)
        Log.d("FCM", "User FCM Token: ${user.fcmToken}")
        if (accessToken != null) {
            sendNotificationToUserAsynkTask(
                mBoardDetails.name,
                user.fcmToken,
                accessToken
            ).execute()
        } else {
            showCustomToast("Fail to generate 0AuthToken")
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class sendNotificationToUserAsynkTask(
        val boardName: String,
        val fcmToken: String, //Device FCM Token
        val accessToken: String // Access Token for HTTP v1 API
    ) : AsyncTask<Any, Void, String>() {
        @Deprecated("Deprecated in Java")
        override fun onPreExecute() {
            super.onPreExecute()
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))
        }

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Any?): String {
            try {
                val jsonObject = JSONObject()
                jsonObject.put("message", JSONObject().apply {
                    put("token", fcmToken)
                    put("notification", JSONObject().apply {
                        put("title", "Assigned to the Board $boardName")
                        put(
                            "body",
                            "You have been assigned to the new board by ${mAssignedMembersList[0].name}"
                        )
                    })
                })
                Log.d("FCM", "Notification Payload: $jsonObject")
                val url =
                    URL("https://fcm.googleapis.com/v1/projects/projemanag-6a101/messages:send")
                //Opens a connection to the specified URL. It's cast to HttpURLConnection for additional methods specific to HTTP.
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true//indicates that data will be sent to the server.
                connection.doInput = true//Indicates that data will be received from the server.
                connection.instanceFollowRedirects = false// Disables automatic redirection.
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty(
                    "Authorization",
                    "Bearer $accessToken"
                ) // Use Access Token

                val outputStream = DataOutputStream(connection.outputStream)//Prepares to send data to the server
                //Converts the jsonObject to a JSON string and writes it to the output stream.
                outputStream.writeBytes(jsonObject.toString())
                outputStream.flush()//Ensures all data is sent.
                outputStream.close()
                //Retrieves the HTTP response code.
                val responseCode = connection.responseCode
                return if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Reads the server's response.
                    val responseStream = BufferedReader(InputStreamReader(connection.inputStream))
                    //Reads the response as a string.
                    val responseString = responseStream.use { it.readText() }
                    Log.d("FCM", "Notification sent successfully: $responseString")
                    responseString
                } else {
                    val errorStream = BufferedReader(InputStreamReader(connection.errorStream))
                    val errorResponse = errorStream.use { it.readText() }
                    Log.e("FCM", "Failed to send notification. Response: $errorResponse")
                    "Error: $errorResponse"
                }
            }catch (e: Exception) {
                Log.e("FCM", "Error sending notification", e)
                return "Exception: ${e.message}"
            }
        }
        /**
         * This function will be executed after the background execution is completed.
         */
        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            hideProgressDialog()
            // JSON result is printed in the log.
            Log.e("FCM","result $result")
        }
    }
}