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
                val x = joystick.normalizedX
                val y = joystick.normalizedY
                UnityBridge.sendMessageToUnity("movement", "$x $y")
            }
        })
    }
}
