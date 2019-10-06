package com.github.artnest.rollaball

interface PluginCallback {
    fun onSuccess(data: String)
    fun onError(message: String)
}
