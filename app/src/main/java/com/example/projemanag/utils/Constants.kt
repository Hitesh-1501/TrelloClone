package com.example.projemanag.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.example.projemanag.activities.MyProfileActivity

object Constants {
   // Firebase Constants
   // This  is used for the collection name for USERS.
   const val USERS: String = "users"
   // Firebase database field names
   const val IMAGE: String = "image"
   const val NAME:  String  = "name"
   const val MOBILE:String = "mobile"
   const val ASSIGNED_TO: String = "assignedTo"
   const val DOCUMENT_ID: String = "documentId"
   //A unique code for asking the Read Storage Permission using this we will be check and identify in the method onRequestPermissionsResult
   const val READ_STORAGE_PERMISSION_CODE = 1
   const val  REQUEST_NOTIFICATION_PERMISSION = 101
   //Add a constant for image selection from phone storage
   const val PICK_IMAGE_REQUEST_CODE = 2
   // This  is used for the collection name for BOARDS.
   const val BOARDS: String = "boards"
   const val TASK_LIST: String = "taskList"
   const val BOARD_DETAILS: String = "boardDetails"
   const val ID: String = "id"
   const val EMAIL: String = "email"

   const val BOARD_MEMBERS_LIST: String = "board_members_list"
   const val SELECT: String = "Select"
   const val UN_SELECT: String = "Unselect"
   const val TASK_LIST_ITEM_POSITION: String = "task_list_item_position"
   const val CARD_LIST_ITEM_POSITION: String = "card_list_item_position"


   const val PROJEMANAG_PREFERENCES = "ProjemanagPrefs"
   const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"
   const val FCM_TOKEN = "fcmToken"

   const val NOTIFICATION_REQUEST_CODE: Int = 101

   // A function for user profile image selection from phone storage.
    fun showImageChooser(activity: Activity){
      val galleryIntent = Intent(
         Intent.ACTION_PICK,
         MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
      // Launches the image selection of phone storage using the constant code.
      activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
   }
   fun getFileExtension(activity: Activity,uri: Uri?): String? {
      return MimeTypeMap.getSingleton().
      getMimeTypeFromExtension(activity.contentResolver.getType(uri!!))
   }
}