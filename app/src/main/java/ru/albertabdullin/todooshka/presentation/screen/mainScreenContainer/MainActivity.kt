package ru.albertabdullin.todooshka.presentation.screen.mainScreenContainer

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
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
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
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
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainBottomNavigation) { _, insets ->
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.navigationBarCard) { v, insets ->
            val lp = v.layoutParams as ViewGroup.MarginLayoutParams
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            var bottom = systemBars.bottom + resources.getDimension(R.dimen.bottom_navigation_bar_margin_bottom)
            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                bottom += ime.bottom
            }
            lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, bottom.toInt())
            v.layoutParams = lp
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
                R.id.assistant -> 1
                R.id.notes -> 2
                R.id.settings -> 3
                else -> return@setOnItemSelectedListener false
            }
            binding.mainViewPager.currentItem = position
            return@setOnItemSelectedListener true
        }
    }
}