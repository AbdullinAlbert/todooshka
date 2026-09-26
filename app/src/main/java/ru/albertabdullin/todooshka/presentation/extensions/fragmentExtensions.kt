package ru.albertabdullin.todooshka.presentation.extensions

import androidx.fragment.app.Fragment
import ru.albertabdullin.todooshka.App
import ru.albertabdullin.todooshka.infrastructure.di.DiContainer

fun Fragment.diContainer(): DiContainer {
    return (requireActivity().application as App).diContainer
}