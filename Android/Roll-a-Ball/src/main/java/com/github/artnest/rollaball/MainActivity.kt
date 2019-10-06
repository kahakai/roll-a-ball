package com.github.artnest.rollaball

import android.os.Bundle
import android.widget.FrameLayout

class MainActivity : UnityPlayerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val unityPlayerLayout: FrameLayout = findViewById(R.id.unity_player_layout)
        unityPlayerLayout.addView(mUnityPlayer.view)
        mUnityPlayer.requestFocus()

        val joystick: JoystickView = findViewById(R.id.joystick)
        joystick.setOnMoveListener(object : JoystickView.OnMoveListener {
            override fun onMove(angle: Int, strength: Int) {
                // [-50, 50] -> [-0.5f, 0.5f]
                val x = (joystick.normalizedX - 50) / 100f
                val y = (50 - joystick.normalizedY) / 100f
                UnityBridge.onJoystickMoved(x, y)
            }
        })
    }
}
