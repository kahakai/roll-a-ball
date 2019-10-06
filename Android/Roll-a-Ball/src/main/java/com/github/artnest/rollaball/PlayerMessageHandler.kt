package com.github.artnest.rollaball

interface ControlModeHandler {
    fun onControlModeSelected(mode: Int)
}

interface PlayerMessageHandler {
    fun onMoved(x: Float, y: Float)
}
