package ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.albertabdullin.todooshka.databinding.DailyRepresentationPageFragmentBinding
import java.time.LocalDate

class DailyTasksPageFragment : Fragment() {

    private var _binding: DailyRepresentationPageFragmentBinding? = null

    private val binding get() = _binding!!

    companion object {
        fun getInstance(date: LocalDate): DailyTasksPageFragment {
            val args = Bundle().apply {
                putLong(DATE_KEY, date.toEpochDay())
            }
            return DailyTasksPageFragment().apply {
                arguments = args
            }
        }

        private const val DATE_KEY: String = "DATE_KEY"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DailyRepresentationPageFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val date = requireArguments().getLong(DATE_KEY)
    }
}