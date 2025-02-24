package com.example.projemanag.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.activities.TaskListActivity
import com.example.projemanag.databinding.ItemTaskBinding
import com.example.projemanag.models.Task
import com.google.rpc.context.AttributeContext.Resource
import java.util.Collections

open class TaskListItemAdapter(
    private val context: Context,
    private val list: ArrayList<Task>
):RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    // A global variable for position dragged FROM
    private var mPositionDraggedFrom = -1
    // A global variable for position dragged TO.
    private var mPositionDraggedTo = -1

    class MyViewHolder(binding: ItemTaskBinding): RecyclerView.ViewHolder(binding.root){
        val tvAddTaskList = binding.tvAddTaskList
        val tvTaskListTitle = binding.tvTaskListTitle
        val llTaskItem = binding.llTaskItem
        val cvAddTaskListName = binding.cvAddTaskListName
        val ibCloseListName = binding.ibCloseListName
        val ibDoneListName = binding.ibDoneListName
        val etTaskListName = binding.etTaskListName
        val ibEditList = binding.ibEditListName
        val ibDeleteList = binding.ibDeleteList
        val llTitleView = binding.llTitleView
        val ibClosedEditableView = binding.ibCloseEditableView
        val cvEditTaskListName = binding.cvEditTaskListName
        val etEditTaskListName = binding.etEditTaskListName
        val ibDoneEditListName = binding.ibDoneEditListName
        val cvAddCard = binding.cvAddCard
        val tvAddCard = binding.tvAddCard
        val ibClosedCardName = binding.ibCloseCardName
        val etCardName = binding.etCardName
        val ibDoneCardName = binding.ibDoneCardName
        val rvCardList = binding.rvCardList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Here we have done some additional changes to display the item of the task list item in 70% of the screen size
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(context),parent,false)
        // Here the layout params are converted dynamically according to the screen size as width is 70% and height is wrap_content.
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT
        )
        // Here the dynamic margins are applied to the view.
        layoutParams.setMargins(
            (15.toDp().toPx()),0,(40.toDp()).toPx(),0)

        binding.root.layoutParams = layoutParams

        return MyViewHolder(binding)
    }
    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }
    @SuppressLint("RecyclerView")
    // Bind each item in the ArrayList to a view
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if(holder is MyViewHolder){
            if(position == list.size-1){
                holder.tvAddTaskList.visibility = View.VISIBLE
                holder.llTaskItem.visibility = View.GONE
            }else{
                holder.tvAddTaskList.visibility = View.GONE
                holder.llTaskItem.visibility = View.VISIBLE
            }
            holder.tvTaskListTitle.text = model.title

            holder.tvAddTaskList.setOnClickListener {
                holder.tvAddTaskList.visibility = View.GONE
                holder.cvAddTaskListName.visibility = View.VISIBLE
            }
            holder.ibCloseListName.setOnClickListener {
                holder.tvAddTaskList.visibility = View.VISIBLE
                holder.cvAddTaskListName.visibility = View.GONE
            }
           // Add a click event for passing the task list name to the base activity function. To create a task list.)
            holder.ibDoneListName.setOnClickListener {
                val listName = holder.etTaskListName.text.toString()
                if(listName.isNotEmpty()) {
                    // Here we check the context is an instance of the TaskListActivity.
                    if (context is TaskListActivity) {
                        context.createTaskList(listName)
                    }
                }else{
                    Toast.makeText(context,"Please Enter List Name",Toast.LENGTH_SHORT).show()
                }
            }
            //Add a click event for iv_edit_list for showing the editable view.
            holder.ibEditList.setOnClickListener {
                holder.etEditTaskListName.setText(model.title)// // Set the existing title
                holder.llTitleView.visibility = View.GONE
                holder.cvEditTaskListName.visibility = View.VISIBLE
            }
            //Add a click event for iv_close_editable_view for hiding the editable view.
            holder.ibClosedEditableView.setOnClickListener {
                holder.llTitleView.visibility = View.VISIBLE
                holder.cvEditTaskListName.visibility = View.GONE
            }
           // Add a click event for iv_edit_list for showing thr editable view
            holder.ibDoneEditListName.setOnClickListener {
                val listName = holder.etEditTaskListName.text.toString()
                if(listName.isNotEmpty()) {
                    // Here we check the context is an instance of the TaskListActivity.
                    if (context is TaskListActivity) {
                        context.updateTaskList(position,listName,model)
                    }
                }else{
                    Toast.makeText(context,"Please Enter List Name",Toast.LENGTH_SHORT).show()
                }
            }
            // Add a click event for ib_delete_list for deleting the task list.
            holder.ibDeleteList.setOnClickListener {
                alertDialogForDeleteList(position,model.title)
            }
            //Add a click event for adding a card in the task list.
            holder.tvAddCard.setOnClickListener {
                holder.tvAddCard.visibility = View.GONE
                holder.cvAddCard.visibility = View.VISIBLE
            }
            //Add a click event for closing the view for card add in the task list.
            holder.ibClosedCardName.setOnClickListener {
                holder.tvAddCard.visibility = View.VISIBLE
                holder.cvAddCard.visibility = View.GONE
            }
            // Add a click event for adding a card in the task list.
            holder.ibDoneCardName.setOnClickListener {
                val cardName = holder.etCardName.text.toString()
                if(cardName.isNotEmpty()) {
                    // Here we check the context is an instance of the TaskListActivity.
                    if (context is TaskListActivity) {
                        context.addCardToTaskList(position, cardName)
                    }
                }else{
                    Toast.makeText(context,"Please Enter Card Name",Toast.LENGTH_SHORT).show()
                }
            }

            holder.rvCardList.layoutManager = LinearLayoutManager(context)
            holder.rvCardList.setHasFixedSize(true)
            val adapter = CardListItemAdapter(context,model.cards)
            holder.rvCardList.adapter = adapter
            //Add a click event on card items for card details
            adapter.setOnClickListener(object : CardListItemAdapter.OnclickListener{
                override fun onClick(cardposition: Int) {
                    if(context is TaskListActivity){
                        context.cardDetails(position, cardPosition = cardposition)
                    }
                }
            })

            // TODO ( Add a feature to drap and drop the card items.)
            // START
            /**
             * Creates a divider {@link RecyclerView.ItemDecoration} that can be used with a
             * {@link LinearLayoutManager}.
             *
             * @param context Current context, it will be used to access resources.
             * @param orientation Divider orientation. Should be {@link #HORIZONTAL} or {@link #VERTICAL}.
             */

            val dividerItemDecoration = DividerItemDecoration(context,DividerItemDecoration.VERTICAL)
            holder.rvCardList.addItemDecoration(dividerItemDecoration)
            //  Creates an ItemTouchHelper that will work with the given Callback.
            val helper = ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN,0
                ){
                    /*Called when ItemTouchHelper wants to move the dragged item from its old position to
              the new position.*/
                    override fun onMove(
                        recyclerView: RecyclerView,
                        dragged: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        val draggedPosition = dragged.adapterPosition
                        val targetPosition = target.adapterPosition
                        // Assign the global variable with updated values.
                        if(mPositionDraggedFrom == -1){
                            mPositionDraggedFrom = draggedPosition
                        }
                        mPositionDraggedTo = targetPosition
                        /**
                         * Swaps the elements at the specified positions in the specified list.
                         */
                        Collections.swap(list[position].cards,draggedPosition,targetPosition)
                        // move item in `draggedPosition` to `targetPosition` in adapter.
                        adapter.notifyItemMoved(draggedPosition,targetPosition)
                        return false//// true if moved, false otherwise
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        TODO("Not yet implemented")
                    }
                    //Finally when the dragging is completed than call the function to update the cards in the database and reset the global variables.
                    /*Called by the ItemTouchHelper when the user interaction with an element is over and it
                     also completed its animation.*/
                    override fun clearView(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder
                    ) {
                        super.clearView(recyclerView, viewHolder)
                        if(mPositionDraggedFrom!=-1 && mPositionDraggedTo != 1 &&
                            mPositionDraggedFrom != mPositionDraggedTo){
                            (context as TaskListActivity).updateCardsInTaskList(
                                position,
                                list[position].cards
                            )
                        }
                        mPositionDraggedFrom = -1
                        mPositionDraggedTo = -1
                    }
                }
            )
            /*Attaches the ItemTouchHelper to the provided RecyclerView. If TouchHelper is already
           attached to a RecyclerView, it will first detach from the previous one.*/
            helper.attachToRecyclerView(holder.rvCardList)
        }
    }
    /**
     * Method is used to show the Alert Dialog for deleting the task list.
     */
    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle("Alert")
        //set message for alert dialog
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed

            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }

        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }
    // END

    /**
     * A function to get density pixel from pixel
     */
    private fun Int.toDp(): Int =
        (this / Resources.getSystem().displayMetrics.density).toInt()
    /**
     * A function to get pixel from density pixel
     */

    private fun Int.toPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()
}