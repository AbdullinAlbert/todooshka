package ru.albertabdullin.todooshka.presentation.screen.main_screen_container

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.albertabdullin.todooshka.R
import ru.albertabdullin.todooshka.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, 0, 0)
            insets
        }
        setupView()
    }

    private fun setupView() {
        setSupportActionBar(binding.mainToolbar)
        binding.mainViewPager.apply {
            adapter = MainViewPagerAdapter(this@MainActivity)
            isUserInputEnabled = false
        }
        binding.mainBottomNavigation.setOnItemSelectedListener { item ->
            val position = when (item.itemId) {
                R.id.tasks -> 0
                R.id.notes -> 1
                R.id.settings -> 2
                else -> return@setOnItemSelectedListener false
            }
            binding.mainViewPager.currentItem = position
            return@setOnItemSelectedListener true
        }
    }
}