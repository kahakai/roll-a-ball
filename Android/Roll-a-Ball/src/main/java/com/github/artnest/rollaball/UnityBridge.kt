package com.github.artnest.rollaball

import android.os.Handler

class UnityBridge {

    companion object {
        private lateinit var container: UnityPlayerContainer
        private lateinit var controlModeHandler: ControlModeHandler
        private lateinit var playerMessageHandler: PlayerMessageHandler
        private lateinit var unityMainThreadHandler: Handler

        fun registerContainer(container: UnityPlayerContainer) {
            this.container = container
        }

        @JvmStatic
        fun registerControlModeHandler(handler: ControlModeHandler) {
            require(::container.isInitialized)
            controlModeHandler = handler
            unityMainThreadHandler = Handler()
            container.onInitialized()
        }

        @JvmStatic
        fun registerPlayerMessageHandler(handler: PlayerMessageHandler) {
            require(::container.isInitialized)
            playerMessageHandler = handler
        }

        fun runOnUnityThread(runnable: Runnable) {
            if (::unityMainThreadHandler.isInitialized) {
                unityMainThreadHandler.post(runnable)
            }
        }

        fun selectControlMode(mode: Int) {
            runOnUnityThread(Runnable {
                if (::controlModeHandler.isInitialized) {
                    controlModeHandler.onControlModeSelected(mode)
                }
            })
        }

        fun onJoystickMoved(x: Float, y: Float) {
            runOnUnityThread(Runnable {
                if (::playerMessageHandler.isInitialized) {
                    playerMessageHandler.onMoved(x, y)
                }
            })
        }
    }
}