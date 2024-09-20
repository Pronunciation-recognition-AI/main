package com.example.hackathon_project

import android.content.Intent
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
    private lateinit var btnHome: Button
    private lateinit var btnDelete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech)

        // UI 요소 초기화
        tvRecognizedSpeech = findViewById(R.id.tvRecognizedSpeech)
        btnRecord = findViewById(R.id.btnRecord)
        btnTTS = findViewById(R.id.btnTTS)
        btnHome = findViewById(R.id.btnHome)
        btnDelete = findViewById(R.id.btndelete) // btnDelete ID 수정

        // 녹음 버튼 클릭 이벤트
        btnRecord.setOnClickListener {
            Toast.makeText(this, "녹음 중...", Toast.LENGTH_SHORT).show()
            tvRecognizedSpeech.text = "텍스트 인식 중..."

            // 3초 후에 "인식 완료!"로 텍스트 변경
            Handler(Looper.getMainLooper()).postDelayed({
                tvRecognizedSpeech.text = "인식 완료!"
            }, 3000) // 3초 지연
        }

        // TTS 버튼 클릭 이벤트 (미구현)
        btnTTS.setOnClickListener {
            Toast.makeText(this, "TTS 기능은 구현 예정입니다.", Toast.LENGTH_SHORT).show()
        }

        // 홈으로 돌아가는 버튼 클릭 이벤트
        btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()  // 현재 액티비티를 종료하여 홈 화면으로 복귀
        }

        // Delete 버튼 클릭 이벤트 (텍스트 초기화 기능 추가)
        btnDelete.setOnClickListener {
            tvRecognizedSpeech.text = ""  // 텍스트뷰의 내용을 빈 문자열로 초기화
            Toast.makeText(this, "텍스트가 초기화되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
