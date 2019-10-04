using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class PlayerController : MonoBehaviour
{
    private Rigidbody rb;
    public float speed;

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
                float moveHorizontal = -Input.gyro.attitude.z * 2;
                float moveVertical = Input.gyro.attitude.x * 2;

                movement = new Vector3(moveHorizontal, 0.0f, moveVertical);
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
}
