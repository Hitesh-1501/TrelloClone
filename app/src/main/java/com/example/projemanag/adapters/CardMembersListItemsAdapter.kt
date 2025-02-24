package com.example.projemanag.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.databinding.ItemCardSelectedMemberBinding
import com.example.projemanag.models.SelectedMembers

open class CardMembersListItemsAdapter(
    private val context: Context,
    private val list: ArrayList<SelectedMembers>,
    private val assignMembers: Boolean
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var onClickListener: OnclickListener? = null
    class MyViewHolder(binding: ItemCardSelectedMemberBinding): RecyclerView.ViewHolder(binding.root){
        val ivAddMember = binding.ivAddMember
        val ivSelectedMember = binding.ivSelectedMemberImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            ItemCardSelectedMemberBinding.inflate(LayoutInflater.from(context),parent,false)
        )
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){
            if(position == list.size-1 && assignMembers){
                holder.ivAddMember.visibility = View.VISIBLE
                holder.ivSelectedMember.visibility = View.GONE
            }else{
                holder.ivAddMember.visibility = View.GONE
                holder.ivSelectedMember.visibility = View.VISIBLE

                Glide
                    .with(context)
                    .load(model.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(holder.ivSelectedMember)
            }
            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onClick()
                }
            }
        }
    }

    interface OnclickListener{
        fun onClick()
    }

    fun setOnClickListener(onClickListener:OnclickListener){
        this.onClickListener = onClickListener
    }

}