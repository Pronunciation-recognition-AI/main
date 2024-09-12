package com.example.hackathon_project

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SpeechActivity : AppCompatActivity() {

    private lateinit var tvRecognizedSpeech: TextView
    private lateinit var btnRecord: Button
    private lateinit var btnTTS: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech)

        // UI 요소 초기화
        tvRecognizedSpeech = findViewById(R.id.tvRecognizedSpeech)
        btnRecord = findViewById(R.id.btnRecord)
        btnTTS = findViewById(R.id.btnTTS)

        // 녹음 버튼 클릭 이벤트
        btnRecord.setOnClickListener {
            // Toast 메시지로 "녹음 중..." 표시
            Toast.makeText(this, "녹음 중...", Toast.LENGTH_SHORT).show()

            // 텍스트 뷰에 "텍스트 인식 중..." 표시
            tvRecognizedSpeech.text = "텍스트 인식 중..."

            // 3초 후에 "인식 완료!"로 텍스트 변경
            Handler(Looper.getMainLooper()).postDelayed({
                tvRecognizedSpeech.text = "안녕하세요"
            }, 3000) // 3000ms = 3초
        }

        // TTS 버튼 클릭 이벤트 (미구현)
        btnTTS.setOnClickListener {
            Toast.makeText(this, "TTS 기능은 구현 예정입니다.", Toast.LENGTH_SHORT).show()
        }
    }
}