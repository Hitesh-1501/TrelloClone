package com.example.projemanag.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.icu.text.Transliterator.Position
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.activities.MainActivity
import com.example.projemanag.activities.TaskListActivity
import com.example.projemanag.databinding.ItemBoardBinding
import com.example.projemanag.models.Board

open class BoardItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Board>
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var onClickListener: OnclickListener? = null
    class MyViewHolder(binding: ItemBoardBinding): RecyclerView.ViewHolder(binding.root){
        val boardImage = binding.ivBoardImage
        val tvName = binding.tvName
        val tvCreatedBy = binding.tvCreatedBy
        val deleteBoard = binding.deleteBoard
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            ItemBoardBinding.inflate(LayoutInflater.from(context),parent,false)
        )
    }

    override fun getItemCount(): Int {
       return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val model = list[position]
        if(holder is MyViewHolder){
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.boardImage)
            holder.tvName.text = model.name
            holder.tvCreatedBy.text = "Created by: ${model.createdBy}"

            holder.itemView.setOnClickListener {
                if(onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
            holder.deleteBoard.setOnClickListener {
                alertDialogForDeleteBoard(position,model.name,list)
            }
        }
    }
    /**
     * An interface for onclick items.
     */

    interface OnclickListener{
        fun onClick(position: Int, model: Board)
    }
    /**
     * A function for OnClickListener where the Interface is the expected parameter..
     */

    fun setOnClickListener(onClickListener: OnclickListener){
        this.onClickListener = onClickListener
    }

    private fun alertDialogForDeleteBoard(position: Int, title: String,boardList:ArrayList<Board>) {
        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle("Alert")
        //set message for alert dialog
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed

            if (context is MainActivity) {
                context.deleteBoard(position,boardList)
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

}