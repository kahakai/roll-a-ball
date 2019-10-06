using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class PlayerController : MonoBehaviour
{
    private Rigidbody rb;
    public float speed;

    public static float x;
    public static float y;

    private int count;
    public Text countText;
    public Text winText;

    void Start()
    {
        rb = GetComponent<Rigidbody>();
        count = 0;
        SetCountText();
        winText.text = "";

        if (SystemInfo.deviceType == DeviceType.Handheld)
        {
            Input.gyro.enabled = true;
        }
    }

    void FixedUpdate()
    {
        Vector3 movement = Vector3.zero;
        switch (SystemInfo.deviceType)
        {
            case DeviceType.Desktop:
            {
                float moveHorizontal = Input.GetAxis("Horizontal");
                float moveVertical = Input.GetAxis("Vertical");

                movement = new Vector3(moveHorizontal, 0.0f, moveVertical);
                break;
            }
            case DeviceType.Handheld:
            {
                /*float moveHorizontal = Input.acceleration.x;
                float moveVertical = Input.acceleration.y;

                movement = new Vector3(moveHorizontal, 0.0f, moveVertical);*/

                // We use unbiased rotation rate to get more accurate values.
                // float orientationX = Input.gyro.rotationRateUnbiased.x;
                // float orientationY = Input.gyro.rotationRateUnbiased.y;

                // movement = new Vector3(orientationY, 0.0f, -orientationX);
                movement = new Vector3(x, 0.0f, y);
                break;
            }
        }

        rb.AddForce(movement * speed);
    }

    void OnTriggerEnter(Collider other)
    {
        if (other.gameObject.CompareTag("Pick Up"))
        {
            other.gameObject.SetActive(false);
            count++;
            SetCountText();
        }
    }

    private void SetCountText()
    {
        countText.text = "Count: " + count.ToString();
        if (count >= 12)
        {
            winText.text = "You Win!";
        }
    }

    private class PlayerMessageHandler : AndroidJavaProxy
    {
        internal PlayerMessageHandler() : base("com.github.artnest.rollaball.PlayerMessageHandler")
        {
        }

        public void onMoved(float x, float y)
        {
            PlayerController.x = x;
            PlayerController.y = y;
        }
    }

    [RuntimeInitializeOnLoadMethod]
    private static void Initialize()
    {
        #if !UNITY_EDITOR
        AndroidJavaClass unityBridge = new AndroidJavaClass("com.github.artnest.rollaball.UnityBridge");
        unityBridge.CallStatic("registerMessageHandler", new PlayerMessageHandler());
        #endif
    }
}
