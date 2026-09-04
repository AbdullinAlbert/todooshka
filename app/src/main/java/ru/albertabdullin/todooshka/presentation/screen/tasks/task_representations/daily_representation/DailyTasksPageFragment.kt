package ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import ru.albertabdullin.todooshka.R
import java.time.LocalDate

class DailyTasksPageFragment : Fragment() {
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
    ): View? {
        return inflater.inflate(R.layout.daily_representation_page_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val date = requireArguments().getLong(DATE_KEY)
        val backgroundColor = if (date % 2 == 0L) R.color.green else R.color.red
        view.findViewById<View>(R.id.daily_page_root).background =
            ResourcesCompat.getDrawable(resources, backgroundColor, null)
    }
}