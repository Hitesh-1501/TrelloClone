package com.example.projemanag.fcm

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

object FirebaseAuthTokenGenerator {
    fun getAccessToken(context: Context): String?{
        return try{
            Log.d("FirebaseAuthTokenGenerator", "Attempting to generate access token...")
            val assetManager = context.assets
            val inputStream: InputStream = assetManager.open("service_account.json")
            Log.d("FirebaseAuthTokenGenerator", "service.json file found and opened.")
            val googleCredentials =
                GoogleCredentials
                    .fromStream(inputStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            Log.d("FirebaseAuthTokenGenerator", "GoogleCredentials created.")
            Log.d("FirebaseAuthTokenGenerator", "GoogleCredentials created: $googleCredentials")
            googleCredentials.refreshIfExpired()
            Log.d("FirebaseAuthTokenGenerator", "Credentials refreshed if expired.")
            val token = googleCredentials.accessToken.tokenValue
            if (token == null) {
                Log.e("FirebaseAuthTokenGenerator", "Access token is null after refresh.")
            } else {
                Log.d("FirebaseAuthTokenGenerator", "Access token generated: $token")
            }
            return token
        }catch(e:Exception){
            Log.e("FirebaseAuthTokenGenerator", "Error generating access token: ${e.message}")
            Log.e("FirebaseAuthTokenGenerator", "Exception details: ${e.printStackTrace()}")
            return null
        }catch (e: IOException) {
            Log.e("FirebaseAuthTokenGenerator", "IOException: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
}