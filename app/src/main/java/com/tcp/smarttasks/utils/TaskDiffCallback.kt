package com.tcp.smarttasks.utils

import androidx.recyclerview.widget.DiffUtil
import com.tcp.smarttasks.data.Task

class TaskDiffCallback (private val oldList: List<Task>,
                        private val newList: List<Task>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Compare items by unique ID (or other unique identifier)
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Compare the actual content of the items
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}