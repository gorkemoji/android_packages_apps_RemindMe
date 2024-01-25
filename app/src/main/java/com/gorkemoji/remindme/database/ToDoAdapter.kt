package com.gorkemoji.remindme.database

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gorkemoji.remindme.databinding.TaskLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ToDoAdapter(private val toDoList: List<ToDo>, private val dao: ToDoDao, private val coroutineScope: CoroutineScope) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {
    class ToDoViewHolder(val binding: TaskLayoutBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = TaskLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToDoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.binding.text.text = toDoList[position].toDoTitle
        holder.binding.checkBox.isChecked = toDoList[position].isChecked

        if (toDoList[position].isChecked) {
            holder.binding.text.paintFlags = holder.binding.text.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.binding.checkBox.isEnabled = false
        }
        else {
            holder.binding.text.paintFlags = holder.binding.text.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.binding.checkBox.isEnabled = true
        }

        holder.binding.checkBox.setOnCheckedChangeListener { _ , isChecked ->
            if (isChecked) {
                toDoList[position].isChecked = isChecked
                coroutineScope.launch {
                    dao.update(toDoList[position])
                    holder.binding.text.paintFlags = holder.binding.text.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    holder.binding.checkBox.isEnabled = false
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return toDoList.size
    }
}