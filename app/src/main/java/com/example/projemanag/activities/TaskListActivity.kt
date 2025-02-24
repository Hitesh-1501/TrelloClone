package com.example.projemanag.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projemanag.R
import com.example.projemanag.adapters.TaskListItemAdapter
import com.example.projemanag.databinding.ActivityTaskListBinding
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.models.Card
import com.example.projemanag.models.Task
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants

class TaskListActivity : BaseActivity() {
    private lateinit var binding: ActivityTaskListBinding
    // A global variable for Board Details.
    private lateinit var mBoardDetails: Board
    // A global variable for board document id as mBoardDocumentId
    private lateinit var mBoardDocumentId: String
    // A global variable for Assigned Members List.
    lateinit var mAssignedMemberDetailList: ArrayList<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Get the board documentId through intent.
        if(intent.hasExtra(Constants.DOCUMENT_ID)){
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }
        //Call the function to get the Board Details
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this,mBoardDocumentId)
    }
    //Add the onActivityResult function add based on the requested document get the updated board details.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MEMBERS_REQUEST_CODE || requestCode == CARD_DETAILS_REQUEST_CODE){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardDetails(this,mBoardDocumentId)
        }else{
            Log.e("Cancelled","cancelled")
        }
    }

    /**
     * A function to setup action bar
     */
    private fun setSupportActionBar(){
        setSupportActionBar(binding.toolbarTaskList)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back)
            actionBar.title = mBoardDetails.name
        }
        binding.toolbarTaskList.setNavigationOnClickListener { onBackPressed() }
    }
    /**
     * A function to get the result of Board Detail.
     */
    fun boardDetails(board: Board){
        mBoardDetails = board
        hideProgressDialog()
        // Call the function to setup action bar.
        setSupportActionBar()
        // Get all the members detail list which are assigned to the board.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)

    }
    /**
     * A function to get the result of add or updating the task list.
     */
    fun  addUpdateTaskListSuccess(){
        hideProgressDialog()
        // Here get the updated board details.
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this,mBoardDetails.documentId)
    }
    /**
     * A function to get the task list name from the adapter class which we will be using to create a new task list in the database.
     */
    fun createTaskList(taskListName: String){
        // Create and Assign the task details
        val task = Task(taskListName,FirestoreClass().getCurrentUserId())
        mBoardDetails.taskList.add(0,task)/// Add task to the first position of ArrayList
        // Remove the last position as we have added the item manually for adding the TaskList.
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    /**
     * A function to update the taskList
     */
    fun updateTaskList(position: Int, listName: String, model: Task){
        val task = Task(listName,model.createdBy)
        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    /**
     * A function to delete the task list from database.
     */
    fun deleteTaskList(position:Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    /**
     * A function to create a card and update it in the task list.
     */

    fun addCardToTaskList(position:Int , cardName: String){
        // Remove the last item
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        val cardAssignedUserList: ArrayList<String> = ArrayList()
        cardAssignedUserList.add(FirestoreClass().getCurrentUserId())

        val card = Card(cardName,FirestoreClass().getCurrentUserId(),cardAssignedUserList)

        val cardList = mBoardDetails.taskList[position].cards
        cardList.add(card)

        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardList
        )
        mBoardDetails.taskList[position] = task
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members ,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_member ->{
                val intent = Intent(this,MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAILS,mBoardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    /**
     * A function for viewing and updating card details.
     */

    fun cardDetails(taskListPosition: Int , cardPosition: Int){
        //Send all the required details to CardDetailsActivity through intent
        val intent = Intent(this,CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAILS,mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST,mAssignedMemberDetailList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }
    /**
     * A function to get assigned members detail list.
     */

    fun boardMembersDetails(list: ArrayList<User>){
        mAssignedMemberDetailList = list
        hideProgressDialog()
        // Here we are appending an item view for adding a list task list for the board.
        val taskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(taskList)

        binding.rvTaskList.layoutManager = LinearLayoutManager(
            this,LinearLayoutManager.HORIZONTAL,false
        )
        binding.rvTaskList.setHasFixedSize(true)
        // Create an instance of TaskListItemsAdapter and pass the task list to it.
        val adapter = TaskListItemAdapter(this,mBoardDetails.taskList)
        binding.rvTaskList.adapter = adapter
    }
    /**
     * A function to update the card list in the particular task list.
     */
    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>){

        // Remove the last item
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        mBoardDetails.taskList[taskListPosition].cards = cards

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }


    companion object{
        //A unique code for starting the activity for result
        const val MEMBERS_REQUEST_CODE = 13
        const val CARD_DETAILS_REQUEST_CODE = 14
    }
}