package com.tcp.smarttasks.ui.fragment

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.tcp.smarttasks.R
import com.tcp.smarttasks.data.Task
import com.tcp.smarttasks.databinding.FragmentTaskDetailBinding
import com.tcp.smarttasks.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit


class TaskDetailFragment : Fragment() {

    private lateinit var task: Task
    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val args = TaskDetailFragmentArgs.fromBundle(it)
            task = args.task
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        appDatabase = AppDatabase.getDatabase(requireContext())
        setDetailTask(task)

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.resolveBtn.setOnClickListener {
            showCommentDialog("Resolved")
        }

        binding.notResolveBtn.setOnClickListener {
            showCommentDialog("Not Resolved")
        }

        return binding.root
    }

    private fun showCommentDialog(status: String) {
        MaterialAlertDialogBuilder(ContextThemeWrapper(requireContext(), R.style.MyAlertDialogTheme))
            .setTitle("Do you want to leave a comment?")
            .setPositiveButton("Yes") { _, _ ->
                showCommentInputDialog(status)
            }
            .setNegativeButton("No") { _, _ ->
                updateTaskStatus(status, null)
            }
            .show()
    }

    private fun showCommentInputDialog(status: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.comment_edit_text, null)
        val inputEditText = dialogView.findViewById<TextInputEditText>(R.id.comment_field)

        MaterialAlertDialogBuilder(ContextThemeWrapper(requireContext(), R.style.MyAlertDialogTheme))
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val comment = inputEditText.text.toString()
                updateTaskStatus(status, comment)
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .show()
    }

    private fun updateTaskStatus(status: String, comment: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            task.taskStatus = status
            task.comments = comment
            appDatabase.taskDao().updateTask(task)

            withContext(Dispatchers.Main) {
                if (!comment.isNullOrEmpty()) {
                    updatesComment(task)
                }
                when (status) {
                    "Resolved" -> {
                        updateUI("Resolved", R.color.green)
                    }

                    "Not Resolved" -> {
                        updateUI("Unresolved", R.color.red)
                    }
                }
            }
        }
    }

    private fun updateUI(status: String, color: Int) {
        binding.taskStatus.text = status
        binding.taskTitle.setTextColor(ContextCompat.getColor(requireContext(), color))
        binding.taskDueDate.setTextColor(ContextCompat.getColor(requireContext(), color))
        binding.taskDaysLeft.setTextColor(ContextCompat.getColor(requireContext(), color))
        binding.taskStatus.setTextColor(ContextCompat.getColor(requireContext(), color))

        when (status) {
            "Resolved" -> {
                binding.resolveBtn.visibility = View.GONE
                binding.notResolveBtn.visibility = View.GONE
                binding.ivResolved.visibility = View.VISIBLE
            }

            "Unresolved" -> {
                binding.resolveBtn.visibility = View.GONE
                binding.notResolveBtn.visibility = View.GONE
                binding.ivNotResolved.visibility = View.VISIBLE
            }
        }

    }

    private fun setDetailTask(task: Task) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        binding.taskTitle.text = task.Title
        binding.taskDetail.text = task.Description
        binding.taskDueDate.text = task.DueDate ?: "--"

        updatesComment(task)

        when (task.taskStatus) {
            "Resolved" -> {
                binding.resolveBtn.visibility = View.GONE
                binding.notResolveBtn.visibility = View.GONE
                binding.ivResolved.visibility = View.VISIBLE
                updateUI("Resolved", R.color.green)
            }
            "Not Resolved" -> {
                binding.resolveBtn.visibility = View.GONE
                binding.notResolveBtn.visibility = View.GONE
                binding.ivNotResolved.visibility = View.VISIBLE
                updateUI("Unresolved", R.color.red)
            }
            else -> {
                binding.resolveBtn.visibility = View.VISIBLE
                binding.notResolveBtn.visibility = View.VISIBLE
                binding.ivResolved.visibility = View.GONE
                binding.ivNotResolved.visibility = View.GONE
            }
        }

        task.DueDate?.let { dueDateStr ->
            try {
                val dueDate = dateFormat.parse(dueDateStr)
                val today = Calendar.getInstance().time

                // Calculate the difference in milliseconds
                val diffInMillis = dueDate!!.time - today.time
                // Convert milliseconds to days
                val daysLeft = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()

                // Set daysLeft text based on the result
                binding.taskDaysLeft.text = if (daysLeft >= 0) "$daysLeft" else "0"
            } catch (e: Exception) {
                binding.taskDaysLeft.text = "--" // If parsing fails, use a placeholder
            }
        } ?: run {
            // If DueDate is null, show a default value
            binding.taskDaysLeft.text = "--"
        }
    }

    private fun updatesComment(task: Task) {
        if (!task.comments.isNullOrEmpty()) {
            binding.taskComments.visibility = View.VISIBLE
            binding.view4.visibility = View.VISIBLE
            binding.taskComments.text = task.comments
        } else {
            binding.taskComments.visibility = View.GONE
        }
    }
}