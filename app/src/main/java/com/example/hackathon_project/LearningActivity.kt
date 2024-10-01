package com.example.hackathon_project

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import java.io.FileOutputStream
import java.io.IOException
import com.chaquo.python.android.AndroidPlatform
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File

class LearningActivity : AppCompatActivity() {

    private lateinit var wordTextView: TextView
    private lateinit var fileCountTextView: TextView
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button
    private lateinit var btnRecord: Button
    private lateinit var btnLearn: Button

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    // 낱말 리스트
    private val words = listOf("안녕하세요", "감사합니다", "네", "아니요")
    private var currentWordIndex = 0

    // AudioRecord 관련 변수
    private var isRecording = false
    private lateinit var audioRecord: AudioRecord
    private lateinit var outputFile: String
    private lateinit var recordingThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learning)

        // 권한 확인 및 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        }

        // UI 요소 초기화
        wordTextView = findViewById(R.id.wordTextView)
        fileCountTextView = findViewById(R.id.fileCountTextView)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnRecord = findViewById(R.id.btnRecord)
        btnLearn = findViewById(R.id.btnLearn)

        // 현재 낱말 표시
        wordTextView.text = words[currentWordIndex]
        updateFileCount()  // 처음에 파일 개수 표시

        // 이전 버튼 클릭 이벤트
        btnPrevious.setOnClickListener {
            if (currentWordIndex > 0) {
                currentWordIndex--
                wordTextView.text = words[currentWordIndex]
                updateFileCount()  // 단어가 변경된 후 파일 개수 업데이트
            } else {
                Toast.makeText(this, "첫 번째 단어입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 다음 버튼 클릭 이벤트
        btnNext.setOnClickListener {
            if (currentWordIndex < words.size - 1) {
                currentWordIndex++
                wordTextView.text = words[currentWordIndex]
                updateFileCount()  // 단어가 변경된 후 파일 개수 업데이트
            } else {
                Toast.makeText(this, "마지막 단어입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 녹음 버튼 클릭 이벤트
        btnRecord.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                // 권한 확인 후 녹음 시작
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    startRecording()
                } else {
                    Toast.makeText(this, "녹음 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

//        if (!Python.isStarted()) {
//            Python.start(AndroidPlatform(this))
//        }

        btnLearn.setOnClickListener {
            // 폴더 경로 정의 (핸드폰 Music 폴더에서 음성 파일 가져오기)
            val dataFolder = getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath ?: run {
                Toast.makeText(this, "파일 경로를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid // 사용자의 고유 ID



//            // Chaquopy로 Python 스크립트 실행
//            val python = Python.getInstance()
//            val pythonCode = python.getModule("prepare_and_extract")
//            val result = pythonCode.callAttr("run_feature_extraction", dataFolder)

//            Toast.makeText(this, result.toString(), Toast.LENGTH_LONG).show()
        }
    }
    private fun startRecording() {
        val sampleRate = 44100  // 44.1kHz 샘플 레이트
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize)

        val outputStream = ByteArrayOutputStream()  // 파일 대신 메모리 스트림 사용
        val buffer = ByteArray(bufferSize)

        audioRecord.startRecording()
        isRecording = true
        Toast.makeText(this@LearningActivity, "녹음이 시작되었습니다.", Toast.LENGTH_SHORT).show()

        recordingThread = Thread {
            try {
                while (isRecording) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        outputStream.write(buffer, 0, read)  // 데이터를 메모리에 저장
                    }
                }

                // UI 스레드에서 Firebase Storage 업로드 실행
                runOnUiThread {
                    uploadToFirebase(outputStream.toByteArray())
                }

            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                outputStream.close()
            }
        }
        recordingThread.start()
    }

    private fun uploadToFirebase(audioData: ByteArray) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (userId == null) {
            Toast.makeText(this, "사용자가 로그인되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val storageReference = FirebaseStorage.getInstance().reference
        val userAudioRef = storageReference.child("audio/$userId/${System.currentTimeMillis()}.wav")

        userAudioRef.putBytes(audioData)
            .addOnSuccessListener {
                Toast.makeText(this, "오디오 파일이 Firebase에 성공적으로 업로드되었습니다.", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "오디오 파일 업로드에 실패했습니다: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun stopRecording() {
        // 녹음 중지
        isRecording = false
        audioRecord.stop()
        audioRecord.release()

        // 녹음 스레드 종료 대기
        recordingThread.join()
        btnRecord.text = "녹음 시작"
    }

    // WAV 파일 헤더 생성
    private fun createWavFileHeader(totalAudioLen: Long, totalDataLen: Long, longSampleRate: Int, channels: Int, bitRate: Int): ByteArray {
        val header = ByteArray(44)
        val sampleRate = longSampleRate
        val byteRate = bitRate * sampleRate * channels / 8

        header[0] = 'R'.toByte() // RIFF/WAVE header
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.toByte() // WAVE
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        header[12] = 'f'.toByte() // 'fmt ' chunk
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1 (PCM)
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = (sampleRate shr 8 and 0xff).toByte()
        header[26] = (sampleRate shr 16 and 0xff).toByte()
        header[27] = (sampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (channels * bitRate / 8).toByte() // block align
        header[33] = 0
        header[34] = bitRate.toByte() // bits per sample
        header[35] = 0
        header[36] = 'd'.toByte() // data chunk identifier
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()
        return header
    }

    // WAV 파일 헤더 업데이트
    private fun updateWavHeader(filePath: String, totalAudioLen: Long, totalDataLen: Long, longSampleRate: Int, channels: Int, bitRate: Int) {
        val header = createWavFileHeader(totalAudioLen, totalDataLen, longSampleRate, channels, bitRate)
        try {
            val raf = java.io.RandomAccessFile(filePath, "rw")
            raf.seek(0) // 파일의 처음으로 이동하여 헤더 작성
            raf.write(header)
            raf.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // 녹음된 현재 단어의 파일 개수를 업데이트하는 함수
    private fun updateFileCount() {
        val dataFolder = getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath ?: return

        // 현재 단어에 해당하는 파일 필터링
        val recordedFiles = File(dataFolder).listFiles { file ->
            file.extension == "wav" && file.name.contains(words[currentWordIndex])
        } ?: emptyArray()

        // 파일 개수 업데이트
        fileCountTextView.text = "현재 녹음된 파일: ${recordedFiles.size} / 100 개"
    }
}
