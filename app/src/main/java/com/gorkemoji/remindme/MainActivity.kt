package com.gorkemoji.remindme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.gorkemoji.remindme.database.ToDo
import com.gorkemoji.remindme.database.ToDoAdapter
import com.gorkemoji.remindme.database.ToDoDatabase
import com.gorkemoji.remindme.databinding.ActivityMainBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var database: ToDoDatabase
    private lateinit var binding: ActivityMainBinding
    private val list = arrayListOf<ToDo>()
    private lateinit var adapter: ToDoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        when (loadMode("theme")) {
            "dark" ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else ->
                saveMode("light", "theme")
        }

        super.onCreate(savedInstanceState)

        database = ToDoDatabase.getDatabase(this)
        adapter = ToDoAdapter(list, database.getDao(), MainScope())

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.selectedItemId = R.id.tasks

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.settings) {
                startActivity(Intent(applicationContext, SettingsActivity::class.java))
                overridePendingTransition(0, 0)
                true
            } else item.itemId == R.id.tasks
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            itemAnimator = DefaultItemAnimator()
        }

        database.getDao().getAll().observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                list.clear()
                list.addAll(it)
                list.reverse()
                adapter.notifyDataSetChanged()
            }
            else {
                list.clear()
                adapter.notifyDataSetChanged()
            }
        })

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val position = viewHolder.adapterPosition

                when (swipeDir) {
                    ItemTouchHelper.LEFT -> {
                        MainScope().launch {
                            database.getDao().delete(list[position])
                        }
                        adapter.notifyItemRemoved(position)
                    }
                    ItemTouchHelper.RIGHT -> {
                        val intent = Intent(this@MainActivity, TaskActivity::class.java)
                        intent.putExtra("mode", 2)
                        intent.putExtra("id", list[position].id)
                        intent.putExtra("taskName", list[position].toDoTitle)
                        intent.putExtra("cbState", list[position].isChecked)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_in_bottom)
                        adapter.notifyDataSetChanged()
                    }
                }
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return 0.5f
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    if (dX > 0)
                        setUpdateIcon(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    else
                        setDeleteIcon(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }).attachToRecyclerView(binding.recyclerView)

        binding.addBtn.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            intent.putExtra("mode", 1)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_in_bottom)
        }
    }

    private fun setDeleteIcon(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val mClearPaint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }

        var itemView : View = viewHolder.itemView

        val mBackground = ColorDrawable().apply {
            color = Color.parseColor("#b80f0a")
            setBounds((itemView.right + dX).toInt(), itemView.top, itemView.right, itemView.bottom)
            draw(c)
        }

        val deleteDrawable: Drawable? = ContextCompat.getDrawable(this, R.drawable.ic_delete)
        val width: Int = deleteDrawable?.intrinsicWidth ?: 0
        val height: Int = deleteDrawable?.intrinsicHeight ?: 0

        val itemHeight: Int = itemView.height
        val deleteIconTop: Int = itemView.top + (itemHeight - height) / 2
        val deleteIconMargin: Int = (itemHeight - height) / 2
        val deleteIconLeft: Int = itemView.right - deleteIconMargin - width
        val deleteIconRight: Int = itemView.right - deleteIconMargin
        val deleteIconBottom: Int = deleteIconTop + height

        deleteDrawable?.bounds = Rect(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        deleteDrawable?.draw(c)
    }

    private fun setUpdateIcon(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val mClearPaint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }

        var itemView : View = viewHolder.itemView

        val mBackground = ColorDrawable().apply {
            color = Color.parseColor("#e88f2c")
            setBounds(itemView.left, itemView.top, (itemView.left + dX).toInt(), itemView.bottom)
            draw(c)
        }

        val updateDrawable: Drawable? = ContextCompat.getDrawable(this, R.drawable.ic_edit)
        val width: Int = updateDrawable?.intrinsicWidth ?: 0
        val height: Int = updateDrawable?.intrinsicHeight ?: 0

        val itemHeight: Int = itemView.height
        val updateIconTop: Int = itemView.top + (itemHeight - height) / 2
        val updateIconMargin: Int = (itemHeight - height) / 2
        val updateIconLeft: Int = itemView.left + updateIconMargin
        val updateIconRight: Int = itemView.left + updateIconMargin + width
        val updateIconBottom: Int = updateIconTop + height

        updateDrawable?.bounds = Rect(updateIconLeft, updateIconTop, updateIconRight, updateIconBottom)
        updateDrawable?.draw(c)
    }

    private fun loadMode(type: String): String? {
        val pref : SharedPreferences = applicationContext.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        return pref.getString("theme", type)
    }

    private fun saveMode(data: String, type: String) {
        val pref : SharedPreferences = applicationContext.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = pref.edit()

        editor.putString("theme", data)
        editor.apply()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finishAffinity()"))
    override fun onBackPressed() {
        finishAffinity()
    }
}