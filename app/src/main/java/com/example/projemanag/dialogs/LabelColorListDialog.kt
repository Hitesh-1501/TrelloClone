package com.example.projemanag.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projemanag.adapters.LabelColorListItemsAdapter
import com.example.projemanag.databinding.DialogListBinding
import com.example.projemanag.databinding.ItemLabelColorBinding

abstract class LabelColorListDialog(
    private val context: Context,
    private var list : ArrayList<String>,
    private val title:  String,
    private var mSelectedColor: String = ""
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
    private fun setUpRecycleView(binding:DialogListBinding){
        binding.tvTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(context)

        val adapter = LabelColorListItemsAdapter(context,list,mSelectedColor)
        binding.rvList.adapter = adapter

        adapter.setOnClickListener(object : LabelColorListItemsAdapter.OnclickListener{
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        })
    }

    protected abstract fun onItemSelected(color:String)
}