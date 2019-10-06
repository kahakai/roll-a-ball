package com.github.artnest.rollaball

interface MessageHandler {
    fun onMessage(message: String, data: String)
}
