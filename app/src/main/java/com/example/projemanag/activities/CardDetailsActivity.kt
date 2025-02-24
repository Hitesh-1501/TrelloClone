package com.example.projemanag.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.example.projemanag.R
import com.example.projemanag.adapters.CardMembersListItemsAdapter
import com.example.projemanag.databinding.ActivityCardDetailsBinding
import com.example.projemanag.dialogs.LabelColorListDialog
import com.example.projemanag.dialogs.MembersListDialog
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.models.Card
import com.example.projemanag.models.SelectedMembers
import com.example.projemanag.models.Task
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CardDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityCardDetailsBinding
    private lateinit var mBoardDetails: Board
    private var mTaskLisPosition = -1
    private var mCardLisPosition = -1
    private var mSelectedColor = ""
    private lateinit var mMembersDetailList: ArrayList<User>
    private var mSelectedDueDateMilliSeconds: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getIntentData()
        setUpActionBar()
        binding.etNameCardDetails.setText(mBoardDetails.taskList[mTaskLisPosition].cards[mCardLisPosition].name)
        binding.etNameCardDetails.setSelection(binding.etNameCardDetails.text.toString().length)
        //Get the already selected label color and set it to the TextView background.
        mSelectedColor = mBoardDetails.taskList[mTaskLisPosition].cards[mCardLisPosition].labelColor

        if(mSelectedColor.isNotEmpty()){
            setColor()
        }

        // Add a click event for update button and also call the function to update the card details
        binding.btnUpdateCardDetails.setOnClickListener {
            if(binding.etNameCardDetails.text.toString().isNotEmpty()){
                updateCardDetails()
            }else{
                showCustomToast("Enter a Card Name.")
            }
        }
        binding.tvSelectLabelColor.setOnClickListener {
            labelColorsListDialog()
        }
       // Add the click event to launch the members list dialog.
        binding.tvSelectMembers.setOnClickListener {
            membersListDialog()
        }
        // Call the function to setup the recyclerView for assigned members
        setUpMembersSelectedList()
        // Set the due to if it is already selected before.
        mSelectedDueDateMilliSeconds = mBoardDetails.taskList[mTaskLisPosition].cards[mCardLisPosition].dueDate
        if(mSelectedDueDateMilliSeconds > 0 ){
            val simpleDateFormat = SimpleDateFormat("dd//MM//yyyy",Locale.getDefault())
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            binding.tvSelectDueDate.text = selectedDate
        }
        // Add click event for selecting the due date
        binding.tvSelectDueDate.setOnClickListener {
            showDataPicker()
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarCardDetailsActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back)
            actionBar.title = mBoardDetails.taskList[mTaskLisPosition].cards[mCardLisPosition].name
        }
        binding.toolbarCardDetailsActivity.setNavigationOnClickListener { onBackPressed() }
    }
    // A function to get all the data that is sent through intent.
    private fun getIntentData(){
        if(intent.hasExtra(Constants.BOARD_DETAILS)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAILS)!!
        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskLisPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION,-1)
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardLisPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION,-1)
        }
        if(intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteCard( mBoardDetails.taskList[mTaskLisPosition].cards[mCardLisPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)

    }
    /**
     * A function to get the result of add or updating the task list.
     */

    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    /**
     * A function to update card details.
     */
    private fun updateCardDetails(){
        // Here we have updated the card name using the data model class.
        val card = Card(
            binding.etNameCardDetails.text.toString(),
            mBoardDetails.taskList[mTaskLisPosition].cards[mCardLisPosition].createdBy,
            mBoardDetails.taskList[mTaskLisPosition].cards[mCardLisPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliSeconds

        )
        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        // Here we have assigned the update card details to the task list using the card position.
        mBoardDetails.taskList[mTaskLisPosition].cards[mCardLisPosition] = card
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity,mBoardDetails)
    }
    /**
     * A function to delete the card from the task list.
     */
    private fun deleteCard(){
        // Here we have got the cards list from the task item list using the task list position.
        val cardList:ArrayList<Card> = mBoardDetails.taskList[mTaskLisPosition].cards
        // Here we will remove the item from cards list using the card position.
        cardList.removeAt(mCardLisPosition)

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        mBoardDetails.taskList[mTaskLisPosition].cards = cardList

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity,mBoardDetails)
    }

    /**
     * A function to show an alert dialog for the confirmation to delete the card.
     */
    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle(resources.getString(R.string.alert))
        //set message for alert dialog
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_card,
                cardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
            // TODO Call the function to delete the card.
            deleteCard()

        }
        //performing negative action
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }
    /**
     * A function to add some static label colors in the list.
     */

    private fun colorList(): ArrayList<String>{
        var colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")//Green
        colorsList.add("#F44336") // Red
        colorsList.add("#673AB7") // Purple
        colorsList.add("#2196F3") // Blue
        colorsList.add("#FFC107") // Amber
        colorsList.add("#03A9F4") // Light Blue
        colorsList.add("#8BC34A") // Light Green
        colorsList.add("#FF9800") // Orange
        colorsList.add("#9E9E9E") // Gray
        colorsList.add("#795548") // Brown
        colorsList.add("#607D8B") // Blue Gray
        colorsList.add("#E91E63") // Pink
        colorsList.add("#00BCD4") // Cyan
        colorsList.add("#FF5722") // Deep Orange
        colorsList.add("#4CAF50") // Green
        colorsList.add("#CDDC39") // Lime
        colorsList.add("#9C27B0") // Deep Purple
        colorsList.add("#3F51B5") // Indigo
        colorsList.add("#FFC0CB") // Light Pink
        colorsList.add("#FFA07A") // Light Salmon
        colorsList.add("#D32F2F") // Dark Red
        colorsList.add("#C2185B") // Dark Pink
        colorsList.add("#7B1FA2") // Dark Purple
        colorsList.add("#512DA8") // Dark Indigo
        colorsList.add("#303F9F") // Dark Blue
        colorsList.add("#0288D1") // Dark Cyan
        colorsList.add("#00796B") // Teal
        colorsList.add("#388E3C") // Dark Green
        colorsList.add("#689F38") // Olive Green
        colorsList.add("#FBC02D") // Yellow
        colorsList.add("#FFA000") // Mustard
        colorsList.add("#F57C00") // Burnt Orange
        colorsList.add("#5D4037") // Dark Brown
        colorsList.add("#616161") // Dark Gray
        colorsList.add("#455A64") // Dark Blue Gray
        return colorsList
    }
    /**
     * A function to remove the text and set the label color to the TextView.
     */

    private fun setColor(){
        binding.tvSelectLabelColor.text = ""
        binding.tvSelectLabelColor.setBackgroundColor(Color.parseColor(mSelectedColor))
    }
    /**
     * A function to launch the label color list dialog.
     */

    private fun labelColorsListDialog(){
        val colorList: ArrayList<String> = colorList()

        val listDialog = object: LabelColorListDialog(
            this,
            colorList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor){
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }
    /**
     * A function to launch and setup assigned members detail list into recyclerview.
     */

    private fun membersListDialog(){
        // Here we get the updated assigned members list
        var cardAssignedMembersList = mBoardDetails
            .taskList[mTaskLisPosition]
            .cards[mCardLisPosition].assignedTo
        if(cardAssignedMembersList.size > 0){
            //Here we got the details of assigned members list from the global members list which is passed from the Task List screen.
            for(i in mMembersDetailList.indices){
                for(j in cardAssignedMembersList){
                    if(mMembersDetailList[i].id == j){
                        mMembersDetailList[i].selected = true
                    }
                }
            }
        }else{
            for(i in mMembersDetailList.indices) {
                mMembersDetailList[i].selected = false
            }
        }
        val listDialog = object: MembersListDialog(
            this,
            mMembersDetailList,
            resources.getString(R.string.str_select_member)){
            override fun onItemSelected(user: User, action: String) {
                //Here based on the action in the members list dialog update the list.
                if(action == Constants.SELECT){
                    if(!mBoardDetails.taskList[mTaskLisPosition].
                        cards[mCardLisPosition].assignedTo.contains(user.id)){
                        mBoardDetails.taskList[mTaskLisPosition].
                        cards[mCardLisPosition].assignedTo.add(user.id)
                    }
                }else{
                    mBoardDetails.taskList[mTaskLisPosition].
                    cards[mCardLisPosition].assignedTo.remove(user.id)

                    for(i in mMembersDetailList.indices){
                        if(mMembersDetailList[i].id == user.id){
                            mMembersDetailList[i].selected = false
                        }
                    }
                }
                setUpMembersSelectedList()
            }
        }
        listDialog.show()
    }
    /**
     * A function to setup the recyclerView for card assigned members.
     */
    private fun setUpMembersSelectedList(){
        // Assigned members of the Card.
        val cardAssignedMembersList = mBoardDetails
            .taskList[mTaskLisPosition]
            .cards[mCardLisPosition].assignedTo
        // A instance of selected members list.
        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()
        // Here we got the detail list of members and add it to the selected members list as required.
        for(i in mMembersDetailList.indices){
                for(j in cardAssignedMembersList){
                    if(mMembersDetailList[i].id == j){
                        val selectedMember = SelectedMembers(
                            mMembersDetailList[i].id,
                            mMembersDetailList[i].image
                        )
                        selectedMembersList.add(selectedMember)
                    }
                }
            }

        if(selectedMembersList.size > 0 ){
            // This is for the last item to show.
            selectedMembersList.add(SelectedMembers("",""))
            binding.tvSelectMembers.visibility = View.GONE
            binding.rvSelectedMembersList.visibility =  View.VISIBLE
            binding.rvSelectedMembersList.layoutManager = GridLayoutManager(
                this,6
            )
            val adapter = CardMembersListItemsAdapter(this,selectedMembersList,true)
            binding.rvSelectedMembersList.adapter = adapter
            adapter.setOnClickListener(object: CardMembersListItemsAdapter.OnclickListener{
                override fun onClick() {
                    membersListDialog()
                }
            })
        }else{
            binding.tvSelectMembers.visibility = View.VISIBLE
            binding.rvSelectedMembersList.visibility =  View.GONE
        }
    }
    /**
     * The function to show the DatePicker Dialog and select the due date.
     */
    private fun showDataPicker() {
        /**
         * This Gets a calendar using the default time zone and locale.
         * The calender returned is based on the current time
         * in the default time zone with the default.
         */
        val c = Calendar.getInstance()
        val year =
            c.get(Calendar.YEAR) // Returns the value of the given calendar field. This indicates YEAR
        val month = c.get(Calendar.MONTH) // This indicates the Month
        val day = c.get(Calendar.DAY_OF_MONTH) // This indicates the Day

        /**
         * Creates a new date picker dialog for the specified date using the parent
         * context's default date picker dialog theme.
         */
        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                /*
                  The listener used to indicate the user has finished selecting a date.
                 Here the selected date is set into format i.e : day/Month/Year
                 And the month is counted in java is 0 to 11 so we need to add +1 so it can be as selected.
                 */

                // Here we have appended 0 if the selected day is smaller than 10 to make it double digit value.
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                // Here we have appended 0 if the selected month is smaller than 10 to make it double digit value.
                val sMonthOfYear =
                    if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                // Selected date it set to the TextView to make it visible to user.
                binding.tvSelectDueDate.text = selectedDate
                /**
                 * Here we have taken an instance of Date Formatter as it will format our
                 * selected date in the format which we pass it as an parameter and Locale.
                 * Here I have passed the format as dd/MM/yyyy.
                 */
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                // The formatter will parse the selected date in to Date object
                // so we can simply get date in to milliseconds.
                val theDate = sdf.parse(selectedDate)

                /** Here we have get the time in milliSeconds from Date object
                 */

                /** Here we have get the time in milliSeconds from Date object
                 */
                mSelectedDueDateMilliSeconds = theDate!!.time
            },
            year,
            month,
            day
        )
        dpd.show() // It is used to show the datePicker Dialog.
    }

}