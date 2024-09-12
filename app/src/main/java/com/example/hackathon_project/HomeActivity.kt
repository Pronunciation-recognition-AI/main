package com.example.hackathon_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 음성 인식 버튼 초기화
        val btnSpeechRecognition = findViewById<Button>(R.id.btnSpeechRecognition)

        // 학습하기 버튼 초기화
        val btnLearning = findViewById<Button>(R.id.btnLearning)

        // 음성 인식 버튼 클릭 시 SpeechActivity로 이동
        btnSpeechRecognition.setOnClickListener {
            val intent = Intent(this, SpeechActivity::class.java)
            startActivity(intent)
        }

        // 학습하기 버튼 클릭 시 LearningActivity로 이동
        btnLearning.setOnClickListener {
            val intent = Intent(this, LearningActivity::class.java)
            startActivity(intent)
        }
    }
}