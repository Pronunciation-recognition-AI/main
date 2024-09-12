package com.example.hackathon_project

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

    // 낱말 리스트
    private val words = listOf("안녕하세요", "감사합니다", "네", "아니요")
    private var currentWordIndex = 0

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learning)

        // UI 요소 초기화
        wordTextView = findViewById(R.id.wordTextView)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnRecord = findViewById(R.id.btnRecord)

        // 현재 낱말 표시
        wordTextView.text = words[currentWordIndex]

        // 이전 버튼 클릭 시
        btnPrevious.setOnClickListener {
            if (currentWordIndex > 0) {
                currentWordIndex--
                wordTextView.text = words[currentWordIndex]
            } else {
                Toast.makeText(this, "첫 번째 단어입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 다음 버튼 클릭 시
        btnNext.setOnClickListener {
            if (currentWordIndex < words.size - 1) {
                currentWordIndex++
                wordTextView.text = words[currentWordIndex]
            } else {
                Toast.makeText(this, "마지막 단어입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 녹음 버튼 클릭 시
        btnRecord.setOnClickListener {
            if (isRecording) {
                stopRecording()
                Toast.makeText(this, "녹음이 완료되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                startRecording()
                Toast.makeText(this, "녹음 중...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 녹음 시작 함수
    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile("${externalCacheDir?.absolutePath}/learning_recording.3gp")
            prepare()
            start()
        }
        isRecording = true
    }

    // 녹음 종료 함수
    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaRecorder = null
    }
}