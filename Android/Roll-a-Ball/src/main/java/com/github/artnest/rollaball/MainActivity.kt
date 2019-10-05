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
    }
}
