package com.example.frame_extractions

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Создаем и запускаем сервис
        val intent = Intent(this, CameraService::class.java)
        startService(intent)
    }
}
