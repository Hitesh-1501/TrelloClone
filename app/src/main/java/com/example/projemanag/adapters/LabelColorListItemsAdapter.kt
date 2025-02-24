package com.example.projemanag.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.databinding.ItemLabelColorBinding

class LabelColorListItemsAdapter(
    private val context: Context,
    private var list : ArrayList<String>,
    private val mSelectedColor: String
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var onClickListener: OnclickListener? = null
    class MyViewHolder(binding: ItemLabelColorBinding) : RecyclerView.ViewHolder(binding.root){
        val viewMain = binding.viewMain
        val ivSelectedColor = binding.ivSelectedColor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            ItemLabelColorBinding.inflate(LayoutInflater.from(context),parent,false)
        )
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        if(holder is MyViewHolder){
            holder.viewMain.setBackgroundColor(Color.parseColor(item))

            if(item == mSelectedColor){
                holder.ivSelectedColor.visibility = View.VISIBLE
            }else{
                holder.ivSelectedColor.visibility = View.GONE
            }
            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onClick(position,item)
                }
            }
        }
    }

    interface OnclickListener{
        fun onClick(position: Int, color: String)
    }

    fun setOnClickListener(onClickListener: OnclickListener){
        this.onClickListener = onClickListener
    }
}