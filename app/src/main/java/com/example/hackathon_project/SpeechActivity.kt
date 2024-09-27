package com.example.hackathon_project

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

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

            // 폴더 경로 정의 (핸드폰 Music 폴더에서 음성 파일 가져오기)
            val dataFolder = getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath ?: run {
                Toast.makeText(this, "파일 경로를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            println(dataFolder)

            // Chaquopy Python 플랫폼 시작 (안드로이드 플랫폼으로 설정)
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))  // 'this'는 현재 Activity의 context
            }

            // Chaquopy로 Python 스크립트 실행
            val python = Python.getInstance()
            val pythonCode = python.getModule("predict")

            // Python 코드 실행
            val result = pythonCode.callAttr("main", dataFolder)

            Handler(Looper.getMainLooper()).postDelayed({
                tvRecognizedSpeech.text = result.toString()
            }, 3000)
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
