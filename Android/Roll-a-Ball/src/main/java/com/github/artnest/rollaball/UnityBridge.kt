package com.github.artnest.rollaball

import android.os.Handler

class UnityBridge {

    companion object {
        private lateinit var messageHandler: PlayerMessageHandler
        private lateinit var unityMainThreadHandler: Handler

        @JvmStatic
        fun registerMessageHandler(handler: PlayerMessageHandler) {
            messageHandler = handler
            unityMainThreadHandler = Handler()
        }

        fun runOnUnityThread(runnable: Runnable) {
            unityMainThreadHandler.post(runnable)
        }

        fun onJoystickMoved(x: Float, y: Float) {
            runOnUnityThread(Runnable {
                if (::messageHandler.isInitialized) {
                    messageHandler.onMoved(x, y)
                }
            })
        }
    }
}