package com.example.hackathon_project

import android.content.Intent
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LearningActivity : AppCompatActivity() {

    private lateinit var wordTextView: TextView
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button
    private lateinit var btnRecord: Button
    private lateinit var btnHome: Button

    // 낱말 리스트
    private val words = listOf("안녕하세요", "감사합니다", "네", "아니요")
    private var currentWordIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learning)

        // UI 요소 초기화
        wordTextView = findViewById(R.id.wordTextView)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnRecord = findViewById(R.id.btnRecord)
        btnHome = findViewById(R.id.btnHome)

        // 현재 낱말 표시
        wordTextView.text = words[currentWordIndex]

        // 이전 버튼 클릭 이벤트
        btnPrevious.setOnClickListener {
            if (currentWordIndex > 0) {
                currentWordIndex--
                wordTextView.text = words[currentWordIndex]
            } else {
                Toast.makeText(this, "첫 번째 단어입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 다음 버튼 클릭 이벤트
        btnNext.setOnClickListener {
            if (currentWordIndex < words.size - 1) {
                currentWordIndex++
                wordTextView.text = words[currentWordIndex]
            } else {
                Toast.makeText(this, "마지막 단어입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 녹음 버튼 클릭 이벤트 (녹음 기능 구현 예시)
        btnRecord.setOnClickListener {
            Toast.makeText(this, "녹음 기능은 구현 예정입니다.", Toast.LENGTH_SHORT).show()
        }

        // 홈 버튼 클릭 이벤트
        btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()  // 현재 액티비티 종료
        }
    }
}