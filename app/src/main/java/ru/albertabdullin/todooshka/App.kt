package ru.albertabdullin.todooshka

import android.app.Application
import ru.albertabdullin.todooshka.infrastructure.di.DiContainer

class App : Application() {

    val diContainer: DiContainer by lazy { DiContainer() }

    override fun onCreate() {
        super.onCreate()
    }

}