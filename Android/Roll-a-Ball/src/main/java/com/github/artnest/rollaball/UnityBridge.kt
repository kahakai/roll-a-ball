package com.github.artnest.rollaball

import android.os.Handler

class UnityBridge {

    companion object {
        private lateinit var messageHandler: MessageHandler
        private lateinit var unityMainThreadHandler: Handler

        fun registerMessageHandler(handler: MessageHandler) {
            messageHandler = handler
            unityMainThreadHandler = Handler()
        }

        fun runOnUnityThread(runnable: Runnable) {
            unityMainThreadHandler.post(runnable)
        }

        fun sendMessageToUnity(message: String, data: String) {
            runOnUnityThread(Runnable {
                if (::messageHandler.isInitialized) {
                    messageHandler.onMessage(message, data)
                }
            })
        }
    }
}