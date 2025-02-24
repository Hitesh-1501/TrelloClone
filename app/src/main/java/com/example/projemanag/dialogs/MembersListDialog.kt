package com.example.projemanag.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projemanag.adapters.LabelColorListItemsAdapter
import com.example.projemanag.adapters.MembersListItemAdapter
import com.example.projemanag.databinding.DialogListBinding
import com.example.projemanag.models.User

abstract class MembersListDialog(
    context: Context,
    private val list: ArrayList<User>,
    private val title: String = ""
): Dialog(context){
    private lateinit var dialogBinding: DialogListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogBinding = DialogListBinding.inflate(layoutInflater)
        setContentView(dialogBinding.root)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecycleView(dialogBinding)
    }
    private fun setUpRecycleView(binding: DialogListBinding){
        binding.tvTitle.text = title
        if(list.size > 0) {
            binding.rvList.layoutManager = LinearLayoutManager(context)
            val adapter = MembersListItemAdapter(context, list)
            binding.rvList.adapter = adapter

            adapter.setOnClickListener(object :MembersListItemAdapter.OnClickListener{
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user,action)
                }
            })
        }
    }

    protected abstract fun onItemSelected(user: User,action: String)
}