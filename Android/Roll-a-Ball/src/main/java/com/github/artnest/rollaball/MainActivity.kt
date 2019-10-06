package com.github.artnest.rollaball

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.constraintlayout.widget.Group

class MainActivity : UnityPlayerActivity(), UnityPlayerContainer {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        UnityBridge.registerContainer(this)

        val unityPlayerLayout: FrameLayout = findViewById(R.id.unity_player_layout)
        unityPlayerLayout.addView(mUnityPlayer.view)
        mUnityPlayer.requestFocus()

        val accelerometerButton: Button = findViewById(R.id.accelerometer_button)
        accelerometerButton.setOnClickListener {
            hideWelcomeGroup()
            UnityBridge.selectControlMode(0)
        }

        val gyroscopeButton: Button = findViewById(R.id.accelerometer_button)
        gyroscopeButton.setOnClickListener {
            hideWelcomeGroup()
            UnityBridge.selectControlMode(1)
        }

        val joystickButton: Button = findViewById(R.id.accelerometer_button)
        joystickButton.setOnClickListener {
            hideWelcomeGroup()
            val joystick: JoystickView = findViewById(R.id.joystick)
            joystick.visibility = View.VISIBLE
            UnityBridge.selectControlMode(2)
        }

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

    override fun onInitialized() {
        val welcomeGroup: Group = findViewById(R.id.welcome_group)
        welcomeGroup.visibility = View.VISIBLE
    }

    private fun hideWelcomeGroup() {
        val welcomeGroup: Group = findViewById(R.id.welcome_group)
        welcomeGroup.visibility = View.GONE
    }
}
