package ru.albertabdullin.todooshka.presentation.screen.main_screen_container

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.SystemBarStyle
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
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.WHITE,
                Color.WHITE
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.WHITE,
                Color.WHITE
            )
        )
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            val ime = insets.getInsets(
                WindowInsetsCompat.Type.ime()
            )

            val isLandscape =
                resources.configuration.orientation == ORIENTATION_LANDSCAPE

            val right = if (isLandscape) {
                systemBars.right
            } else {
                0
            }

            val bottom = if (
                insets.isVisible(WindowInsetsCompat.Type.ime())
            ) {
                val navigationBars = insets.getInsets(
                    WindowInsetsCompat.Type.navigationBars()
                )
                (ime.bottom - navigationBars.bottom).coerceAtLeast(0)
            } else if (isLandscape) {
                systemBars.bottom
            } else {
                0
            }


            v.setPadding(systemBars.left, systemBars.top, right, bottom)
            insets
        }
        setupView()
    }

    private fun setupView() {
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
        binding.mainBottomNavigation.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->

            Log.d(
                "BNB",
                "height=${v.height}, paddingBottom=${v.paddingBottom}"
            )
        }
    }
}