package com.example.projemanag.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.activities.TaskListActivity
import com.example.projemanag.databinding.ItemCardBinding
import com.example.projemanag.models.Board
import com.example.projemanag.models.Card
import com.example.projemanag.models.SelectedMembers

open class CardListItemAdapter(
    private val context: Context,
    private var list: ArrayList<Card>
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var onClickListener: OnclickListener? = null
    class MyViewHolder(binding: ItemCardBinding): RecyclerView.ViewHolder(binding.root){
        val cardName = binding.tvCardName
        val viewLabelColor = binding.viewLabelColor
        val rvCardSelected = binding.rvCardSelectedMembersList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            ItemCardBinding.inflate(LayoutInflater.from(context),parent,false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val model = list[position]
        if(holder is MyViewHolder){
            //As we have already have a View Item for label color so make it visible and set the selected label color
            if(model.labelColor.isNotEmpty()){
                holder.viewLabelColor.visibility = View.VISIBLE
                holder.viewLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
            }else{
                holder.viewLabelColor.visibility = View.GONE
            }
           holder.cardName.text = model.name
            // Now with use of public list of Assigned members detail List populate the recyclerView for Assigned Members.
            if((context as TaskListActivity).mAssignedMemberDetailList.size > 0 ){
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()
                // Here we got the detail list of members and add it to the selected members list as required.
                for(i in context.mAssignedMemberDetailList.indices){
                    for(j in model.assignedTo){
                        if(context.mAssignedMemberDetailList[i].id == j){
                            val selectedMembers = SelectedMembers(
                                context.mAssignedMemberDetailList[i].id,
                                context.mAssignedMemberDetailList[i].image
                            )
                            selectedMembersList.add(selectedMembers)
                        }
                    }
                }
                if(selectedMembersList.size > 0){
                    if(selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy){
                        holder.rvCardSelected.visibility = View.GONE
                    }else{
                        holder.rvCardSelected.visibility = View.VISIBLE
                        holder.rvCardSelected.layoutManager = GridLayoutManager(
                            context,4
                        )
                        val adapter = CardMembersListItemsAdapter(context,selectedMembersList,false)
                        holder.rvCardSelected.adapter = adapter
                        adapter.setOnClickListener(object: CardMembersListItemsAdapter.OnclickListener{
                            override fun onClick() {
                                if(onClickListener != null){
                                    onClickListener!!.onClick(position)
                                }
                            }
                        })
                    }
                }else{
                    holder.rvCardSelected.visibility = View.GONE
                }
            }

            //Set a click listener to the card item view.
            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onClick(position)
                }
            }
        }
    }
    /**
     * An interface for onclick items.
     */

    interface OnclickListener{
        fun onClick(position: Int)
    }
    /**
     * A function for OnClickListener where the Interface is the expected parameter..
     */

    fun setOnClickListener(onClickListener: OnclickListener){
        this.onClickListener = onClickListener
    }
}