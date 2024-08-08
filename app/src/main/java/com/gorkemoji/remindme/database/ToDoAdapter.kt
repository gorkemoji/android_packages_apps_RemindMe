package com.gorkemoji.remindme.database

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gorkemoji.remindme.databinding.TaskLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ToDoAdapter(private val toDoList: List<ToDo>, private val dao: ToDoDao, private val coroutineScope: CoroutineScope) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {
    class ToDoViewHolder(val binding: TaskLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = TaskLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToDoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val currentToDo = toDoList[position]

        holder.binding.text.text = currentToDo.toDoTitle
        holder.binding.checkBox.isChecked = currentToDo.isChecked

        updateTextAppearance(holder.binding, currentToDo.isChecked)

        holder.binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentToDo.isChecked = true
                updateTextAppearance(holder.binding, true)
                coroutineScope.launch { dao.update(currentToDo) }
            } else holder.binding.checkBox.isChecked = true
        }
    }

    private fun updateTextAppearance(binding: TaskLayoutBinding, isChecked: Boolean) {
        if (isChecked) {
            binding.text.paintFlags = binding.text.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            binding.checkBox.isEnabled = false
        } else {
            binding.text.paintFlags = binding.text.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            binding.checkBox.isEnabled = true
        }
    }

    override fun getItemCount(): Int = toDoList.size
}
