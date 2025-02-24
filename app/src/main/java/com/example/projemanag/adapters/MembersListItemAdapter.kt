package com.example.projemanag.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.databinding.ItemMemberBinding
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants

class MembersListItemAdapter(
    private val context: Context,
    private var list : ArrayList<User>
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var onClickListener: OnClickListener? = null
    class MyViewHolder(binding: ItemMemberBinding): RecyclerView.ViewHolder(binding.root){
        val ivMemberImage = binding.ivMemberImage
        val ivMemberName = binding.tvMemberName
        val ivMemberEmail = binding.tvMemberEmail
        val ivSelectedMember = binding.ivSelectedMember
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            ItemMemberBinding.inflate(LayoutInflater.from(context),parent,false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if(holder is MyViewHolder){
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.ivMemberImage)

            holder.ivMemberName.text = model.name
            holder.ivMemberEmail.text = model.email

            if(model.selected){
                holder.ivSelectedMember.visibility = View.VISIBLE
            }else{
                holder.ivSelectedMember.visibility = View.INVISIBLE
            }
            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    // Pass the constants here according to the selection.
                    if(model.selected){
                        onClickListener!!.onClick(position,model,Constants.UN_SELECT)
                    }else{
                        onClickListener!!.onClick(position,model,Constants.SELECT)
                    }
                }
            }
        }
    }

    interface OnClickListener{
        fun onClick(position: Int, user: User, action: String)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }
}