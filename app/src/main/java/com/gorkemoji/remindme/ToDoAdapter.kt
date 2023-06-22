package com.gorkemoji.remindme

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gorkemoji.remindme.databinding.TaskLayoutBinding

class ToDoAdapter(private val toDoList: List<ToDo>) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    class ToDoViewHolder(val binding: TaskLayoutBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = TaskLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToDoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.binding.checkBox.text = toDoList[position].toDoTitle
        holder.binding.checkBox.isChecked = toDoList[position].isChecked

        holder.binding.checkBox.setOnCheckedChangeListener { _ , isChecked ->
            toDoList[position].isChecked = isChecked
        }
    }

    override fun getItemCount(): Int {
        return toDoList.size
    }
}