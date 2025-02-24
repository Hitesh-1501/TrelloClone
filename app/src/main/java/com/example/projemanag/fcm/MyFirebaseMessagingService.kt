package com.example.projemanag.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.projemanag.R
import com.example.projemanag.activities.MainActivity
import com.example.projemanag.activities.SignInActivity
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import okhttp3.internal.notify
import kotlin.random.Random

class MyFirebaseMessagingService: FirebaseMessagingService(){

    override fun onMessageReceived(remotemessage: RemoteMessage) {
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification

        super.onMessageReceived(remotemessage)
        // Handle FCM messages here.
        Log.d(TAG,"FROM: ${remotemessage.from}")
        // Check if message contains a data payload.
        remotemessage.notification?.let {
            Log.d("FCM", "Notification Received: ${it.title} - ${it.body}")
            sendNotification(it.title ?: "No Title", it.body ?: "No Body")

        }
        // Check if message contains a notification payload.
        remotemessage.notification?.let {
            Log.d(TAG,"Message Notification Body: ${it.body}")
        }
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

    }
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG,"Refreshed Token: $token")
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?){
        // Implement this method to send token to your app server.
        // Here we have saved the token in the Shared Preferences
        val sharedPreferences =
            this.getSharedPreferences(Constants.PROJEMANAG_PREFERENCES, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(Constants.FCM_TOKEN, token)
        editor.apply()

    }
    //Create and show a simple notification containing the received FCM message.
    private fun sendNotification(title: String , message: String){
        val intent = if(FirestoreClass().getCurrentUserId().isNotEmpty()){
            Intent(this,MainActivity::class.java)
        }else{
            Intent(this,SignInActivity::class.java)
        }
        /*If set, and the activity being launched is already running in the current task,
        then instead of launching a new instance of that activity,
        all of the other activities on top of it will be closed and this Intent will be delivered to the (now on top) old activity as a new Intent.
         */
        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK
                or Intent.FLAG_ACTIVITY_CLEAR_TOP
                or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(this,0,intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        val channelId = "projemanag_notification_channel_id"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(
            this,channelId
        ).setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                channelId,
                "ProjeManag Notifications",
                NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Channel for ProjeManag App"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        //Use a random unique ID so that multiple notifications appear instead of replacing each other.
        val notificationId = Random.nextInt() // Generates a unique ID
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
    companion object{
        private const val TAG = "MyFirebaseMsgService"
    }
}