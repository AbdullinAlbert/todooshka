package ru.albertabdullin.todooshka.presentation.screen.main_screen

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.albertabdullin.todooshka.presentation.screen.notes.NotesFragment
import ru.albertabdullin.todooshka.presentation.screen.settings.SettingsFragment
import ru.albertabdullin.todooshka.presentation.screen.tasks.TaskContainerFragment

class MainViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> TaskContainerFragment()
            1 -> NotesFragment()
            2 -> SettingsFragment()
            else -> throw RuntimeException("incorrect position: $position")
        }
    }

    override fun getItemCount() = 3
}