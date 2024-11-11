package com.tcp.smarttasks.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.tcp.smarttasks.R
import com.tcp.smarttasks.data.Task
import com.tcp.smarttasks.databinding.ItemListBinding
import com.tcp.smarttasks.utils.TaskDiffCallback
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit


class MyTaskRecyclerViewAdapter(
    private var tasksList: List<Task>,
    private val onItemClickListener: OnItemClickListener,
) : RecyclerView.Adapter<MyTaskRecyclerViewAdapter.ViewHolder>() {

    fun updateTasks(newTasks: List<Task>) {
        val diffCallback = TaskDiffCallback(tasksList, newTasks)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        tasksList = newTasks
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            ItemListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val item = tasksList[position]
        holder.taskTitle.text = item.Title
        holder.dueDate.text = item.DueDate ?: "--"

        item.DueDate?.let { dueDateStr ->
            try {
                val dueDate = dateFormat.parse(dueDateStr)
                val today = Calendar.getInstance().time

                // Calculate the difference in milliseconds
                val diffInMillis = dueDate!!.time - today.time
                // Convert milliseconds to days
                val daysLeft = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()

                // Set daysLeft text based on the result
                holder.daysLeft.text = if (daysLeft >= 0) "$daysLeft" else "0"
            } catch (e: Exception) {
                holder.daysLeft.text = "--" // If parsing fails, use a placeholder
            }
        } ?: run {
            // If DueDate is null, show a default value
            holder.daysLeft.text = "--"
        }

        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(item)
        }

       if (item.taskStatus == "Resolved") {
           holder.statusIcon.setImageResource(R.drawable.btn_resolved)
           holder.statusIcon.visibility = View.VISIBLE
       } else if (item.taskStatus == "Not Resolved") {
           holder.statusIcon.setImageResource(R.drawable.btn_unresolved)
           holder.statusIcon.visibility = View.VISIBLE
       } else holder.statusIcon.visibility = View.GONE
    }

    override fun getItemCount(): Int = tasksList.size

    inner class ViewHolder(binding: ItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val daysLeft: TextView = binding.taskDaysLeft
        val taskTitle: TextView = binding.taskTitle
        val dueDate: TextView = binding.taskDueDate
        val statusIcon: ImageView = binding.statusIcon
    }

    interface OnItemClickListener {
        fun onItemClick(task: Task)
    }
}