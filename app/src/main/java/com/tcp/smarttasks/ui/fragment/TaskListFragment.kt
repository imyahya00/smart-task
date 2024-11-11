package com.tcp.smarttasks.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.tcp.smarttasks.adapter.MyTaskRecyclerViewAdapter
import com.tcp.smarttasks.R
import com.tcp.smarttasks.data.Task
import com.tcp.smarttasks.databinding.FragmentTaskListBinding
import com.tcp.smarttasks.db.AppDatabase
import com.tcp.smarttasks.network.Status
import com.tcp.smarttasks.utils.Utils
import com.tcp.smarttasks.vm.TasksViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TasksViewModel
    private lateinit var appDatabase: AppDatabase
    private var taskList: List<Task> = emptyList()
    private var currentDate: LocalDate = LocalDate.now()
    private lateinit var taskAdapter: MyTaskRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        appDatabase = AppDatabase.getDatabase(requireContext())
        viewModel = ViewModelProvider(this)[TasksViewModel::class.java]

        if (Utils.isNetworkAvailable(requireContext()) && taskList.isEmpty())
            viewModel.getAllTasks(appDatabase.taskDao())
        else
            viewModel.loadTasksFromDb(appDatabase.taskDao())

        setupAdapter()
        observeTasks()

        binding.navigateBack.setOnClickListener {
            // Define the limit of 6 months before the current date
            val sixMonthsBefore = LocalDate.now().minusMonths(6)
            currentDate = currentDate.minusDays(1)
            if (currentDate.isBefore(sixMonthsBefore)) {
                Toast.makeText(requireContext(), "No tasks found for previous dates.", Toast.LENGTH_SHORT).show()
            } else {
                handleTaskDateWise()
            }
        }

        binding.navigateForward.setOnClickListener {
            val sixMonthsAhead = LocalDate.now().plusMonths(6)
            currentDate = currentDate.plusDays(1)
            if (currentDate.isAfter(sixMonthsAhead)) {
                Toast.makeText(requireContext(), "No tasks found for upcoming dates.", Toast.LENGTH_SHORT).show()
            } else {
                handleTaskDateWise()
            }
        }

        binding.tvDate.setOnClickListener {
            currentDate = LocalDate.now()
            handleTaskDateWise()
        }

        return binding.root
    }

    private fun setupAdapter() {
        taskAdapter = MyTaskRecyclerViewAdapter(
            listOf(),
            object : MyTaskRecyclerViewAdapter.OnItemClickListener {
                override fun onItemClick(task: Task) {
                    val action = TaskListFragmentDirections.actionTaskListFragmentToTaskDetailFragment(task)
                    val navController = activity?.findNavController(R.id.nav_host_fragment)
                    navController?.navigate(action)
                }
            })

        binding.list.adapter = taskAdapter
    }

    private fun observeTasks() {
        lifecycleScope.launch {
            viewModel.taskState.collect { it ->
                when (it.status) {

                    Status.LOADING -> {
                          binding.progressCircular.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.progressCircular.visibility = View.GONE
                        it.data?.let { response ->
                            taskList = response.tasks!!
                            handleTaskDateWise()
                        }
                    }
                    // In case of error, show some data to user
                    else -> {
                        binding.progressCircular.visibility = View.GONE
                        Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()
                        Log.d("mytag", "getAllTasks: ${it.message}")
                    }
                }
            }
        }
    }

    private fun handleTaskDateWise() {

        val tasksForCurrentDate = taskList.filter {
            val targetDate = LocalDate.parse(it.TargetDate)
            val dueDate = it.DueDate?.let { LocalDate.parse(it) }

            // Show task if the TargetDate is the current date OR if the DueDate is after or on the current date
            (targetDate == currentDate) || (dueDate != null && !dueDate.isBefore(currentDate))
        }

        if (tasksForCurrentDate.isNotEmpty()) {
            binding.noData.visibility = View.GONE
            binding.list.visibility = View.VISIBLE

            val sortedTaskList = tasksForCurrentDate.sortedWith(compareByDescending<Task> { task ->
                task.Priority ?: 0
            }.thenBy { it.TargetDate })

            taskAdapter.updateTasks(sortedTaskList)

        } else {
            binding.noData.visibility = View.VISIBLE
            binding.noData.text = "No tasks found for ${Utils.formatDate(currentDate)}"
            binding.list.visibility = View.GONE
        }

        binding.tvDate.text = Utils.formatDate(currentDate)
    }

}