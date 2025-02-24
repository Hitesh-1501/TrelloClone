package com.example.projemanag.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.projemanag.activities.CardDetailsActivity
import com.example.projemanag.activities.CreateBoardActivity
import com.example.projemanag.activities.MainActivity
import com.example.projemanag.activities.MembersActivity
import com.example.projemanag.activities.MyProfileActivity
import com.example.projemanag.activities.SignInActivity
import com.example.projemanag.activities.SignUpActivity
import com.example.projemanag.activities.TaskListActivity
import com.example.projemanag.models.Board
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.FieldPosition

class FirestoreClass{
    // Create a instance of Firebase Firestore
    private val mFireStore = FirebaseFirestore.getInstance()
    /**
     * A function to make an entry of the registered user in the firestore database.
     */
    fun registerUser(activity: SignUpActivity , userInfo: User){
        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(getCurrentUserId())
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener {
                activity.hideProgressDialog()
            }
    }
    /**
     * A function to get the Board Details.
     */
    fun getBoardDetails(activity: TaskListActivity, documentId: String){
        // The collection name for BOARDS
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get() // Will get the documents snapshots.
            .addOnSuccessListener {
                document->
                Log.i(activity.javaClass.simpleName,document.toString())
                //Assign the board document id to the Board Detail object
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }
    /**
     * A function to create a task list in the board detail.
     */

    fun addUpdateTaskList(activity: Activity, board: Board){
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated.")
                if(activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                }else if(activity is CardDetailsActivity){
                    activity.addUpdateTaskListSuccess()
                }
            } .addOnFailureListener { e ->
                if(activity is TaskListActivity)
                     activity.hideProgressDialog()
                else if(activity is CardDetailsActivity)
                    activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    /**
     * A function for creating a board and making an entry in the database.
     */

    fun createBoard(activity: CreateBoardActivity, board: Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                exception->
                Log.e(
                    activity.javaClass.simpleName,
                    "error while creating a board.", exception
                )
            }
    }


    fun deleteBoards(activity: MainActivity, board:Board){
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .delete()
            .addOnSuccessListener {
                activity.boardDeletedSuccessfully()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "error while deleting a board.",it
                )

            }

    }
    fun getBoardsList(activity: MainActivity){
        // The collection name for BOARDS
        mFireStore.collection(Constants.BOARDS)
            // A where array query as we want the list of the board in which the user is assigned.
            // So here you can pass the current user id.
            .whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserId())
            .get() // Will get the documents snapshots.
            .addOnSuccessListener {
                document->
                // Here we get the list of boards in the form of documents.
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                // Here we have created a new instance for Boards ArrayList.
                val boardList: ArrayList<Board> = ArrayList()
                // A for loop as per the list of documents to convert them into Boards ArrayList.
                for(i in document.documents){
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }
                // Here pass the result to the base activity.
                activity.populateBoardsListToUI(boardList)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }
    //A function to update the user profile data into the database.
    fun updateUserProfileData(activity: Activity,userHashMap: HashMap<String,Any>){
        mFireStore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserId())
            .update(userHashMap)// A hashmap of fields which are to be updated.
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile data updated")
                when(activity){
                    is MainActivity ->{
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity->{
                        // Notify the success result.
                        activity.profileUpdateSuccess()
                    }
                }
            }.addOnFailureListener {e->
                when(activity){
                    is MainActivity ->{
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity->{
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "error while creating a board.", e
                )
                Toast.makeText(activity,"Error when updating the profile!",Toast.LENGTH_SHORT).show()
            }
    }

    // A function to SignIn using firebase and get the user details from Firestore Database.
    // Add a parameter to check whether to read the boards list or not.
    fun loadUserData(activity: Activity, readBoardsList: Boolean = false){
        mFireStore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener {document->
                // Here we have received the document snapshot which is converted into the User Data model object.
                val loggedInUser = document.toObject(User::class.java)!!
                // Here call a function of base activity for transferring the result to it.
                when(activity){
                    is SignInActivity->{
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity ->{
                        activity.updateNavigationUserDetails(loggedInUser,readBoardsList)
                    }
                    is MyProfileActivity ->{
                        activity.setUserDataInUi(loggedInUser)
                    }
                }
            }
            .addOnFailureListener {
                when(activity){
                    is SignInActivity->{
                        activity.hideProgressDialog()
                    }
                    is MainActivity ->{
                        activity.hideProgressDialog()
                    }
                }
                Log.e("SignInUser","Error Writing document",it)
            }
    }
    /**
     * A function to get the list of user details which is assigned to the board.
     */
    fun getAssignedMembersListDetails(activity: Activity , assignedTo: ArrayList<String>) {
        mFireStore.collection(Constants.USERS)// Collection Name
            .whereIn(Constants.ID, assignedTo)// Here the database field name and the id's of the members.
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                val userList: ArrayList<User> = ArrayList()

                for (i in document.documents) {
                    // Convert all the document snapshot to the object using the data model class.
                    val user = i.toObject(User::class.java)!!
                    userList.add(user)
                }
                if(activity is MembersActivity) {
                    activity.setupMembersList(userList)
                }else if(activity is TaskListActivity) {
                    activity.boardMembersDetails(userList)
                }
            }.addOnFailureListener { e ->
                if(activity is MembersActivity) {
                    activity.hideProgressDialog()
                }else if(activity is TaskListActivity) {
                    activity.hideProgressDialog()
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "error while creating a board.", e
                )
            }
    }

    /**
     * A function to get the user details from Firestore Database using the email address.
     */

    fun getMemberDetails(activity:MembersActivity, email: String){
        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL,email)
            .get()
            .addOnSuccessListener {
                document->
                if(document.documents.size > 0){
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }else{
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such Member Found")
                }
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "error while getting a user details.", e
                )
            }
    }
    /**
     * A function to assign a updated members list to board.
     */

    fun assignMemberTo(activity: MembersActivity,board:Board,user:User){
        val assignedToHashMap = HashMap<String,Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                 activity.memberAssignSuccess(user)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "error while creating a board.", e
                )
            }
    }

    //  A function for getting the user id of current logged user.
    fun getCurrentUserId(): String{
        var currentUser = FirebaseAuth.getInstance().currentUser
        // A variable to assign the currentUserId if it is not null or else it will be blank.
        var currentUserId = ""
        if(currentUser != null){
            currentUserId = currentUser.uid
        }
        return currentUserId
    }
}