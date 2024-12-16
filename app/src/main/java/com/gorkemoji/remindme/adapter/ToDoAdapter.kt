package com.gorkemoji.remindme.adapter

import android.content.Context
import android.graphics.Paint
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.gorkemoji.remindme.MainActivity
import com.gorkemoji.remindme.R
import com.gorkemoji.remindme.data.dao.ToDoDao
import com.gorkemoji.remindme.data.model.ToDo
import com.gorkemoji.remindme.databinding.TaskLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale


class ToDoAdapter(
    private val context: Context,
    private val toDoList: ArrayList<ToDo>,
    private val dao: ToDoDao,
    private val coroutineScope: CoroutineScope,
    private var player: MediaPlayer
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    class ToDoViewHolder(val binding: TaskLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    val initialToDoList = ArrayList<ToDo>().apply { addAll(toDoList) }
    private var isPlayingSound = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = TaskLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToDoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val currentToDo = toDoList[position]

        if (currentToDo.font == "pacifico") holder.binding.text.typeface =
            ResourcesCompat.getFont(context, R.font.pacifico)

        holder.binding.text.text =
            if (currentToDo.isLocked) "*".repeat(currentToDo.toDoTitle.length)
            else currentToDo.toDoTitle

        holder.binding.checkBox.setOnCheckedChangeListener(null)
        holder.binding.checkBox.isChecked = currentToDo.isChecked

        updateTextAppearance(holder.binding, currentToDo.isChecked)

        holder.binding.alarmIcon.visibility =
            if (currentToDo.isReminderOn) View.VISIBLE else View.GONE
        holder.binding.lockIcon.visibility = if (currentToDo.isLocked) View.VISIBLE else View.GONE

        holder.binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isPlayingSound) {
                currentToDo.isChecked = true
                (context as MainActivity).incrementTaskDone()

                isPlayingSound = true

                updateTextAppearance(holder.binding, true)

                try {
                    if (player.isPlaying) {
                        player.stop()
                        player.start()
                    }
                    player = MediaPlayer.create(context, R.raw.pencil_done)
                    player.start()
                    player.setOnCompletionListener {
                        isPlayingSound = false
                        it.reset()
                        it.release()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    isPlayingSound = false
                }

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
        binding.text.invalidate()
    }

    override fun getItemCount(): Int = toDoList.size

    fun updateList(newList: List<ToDo>) {
        toDoList.clear()
        toDoList.addAll(newList)
        initialToDoList.clear()
        initialToDoList.addAll(newList)
        notifyDataSetChanged()
    }

    fun getFilter(): Filter {
        return toDoFilter
    }

    private val toDoFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: ArrayList<ToDo> = ArrayList()

            if (constraint.isNullOrEmpty()) {
                initialToDoList.let { filteredList.addAll(it) }
            } else {
                val query = constraint.toString().trim().lowercase()
                initialToDoList.forEach {
                    if (it.toDoTitle.lowercase(Locale.ROOT).contains(query)) {
                        filteredList.add(it)
                    }
                }
            }

            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results?.values is ArrayList<*>) {
                toDoList.clear()
                toDoList.addAll(results.values as ArrayList<ToDo>)
                notifyDataSetChanged()
            }
        }
    }
}
