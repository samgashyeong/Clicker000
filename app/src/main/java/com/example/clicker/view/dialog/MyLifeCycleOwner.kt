package com.example.clicker.view.dialog

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class MyLifeCycleOwner() : LifecycleOwner {
    private val mLifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }
    override var lifecycle: Lifecycle
    init {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycle = mLifecycleRegistry
    }

    fun stop() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    fun start() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }
}