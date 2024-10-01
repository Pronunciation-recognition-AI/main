package com.example.hackathon_project
import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import android.media.AudioFormat
import android.media.MediaRecorder
import java.io.FileOutputStream
import java.io.IOException
import android.media.MediaPlayer
import android.net.Uri
import android.speech.tts.TextToSpeech  // TextToSpeech import
import android.util.Log
import java.util.Locale  // Locale import
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import com.google.firebase.storage.StorageReference
import com.google.firebase.FirebaseApp
import java.io.File

class SpeechActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var tvRecognizedSpeech: TextView
    private lateinit var btnRecord: Button
    private lateinit var btnTTS: Button
    private lateinit var btnDelete: Button
    private lateinit var btnPlay: Button
    private lateinit var textToSpeech: TextToSpeech  // TTS 객체 추가
    private var ttsInitialized = false  // TTS 초기화 상태


    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    // AudioRecord 관련 변수
    private var isRecording = false
    private lateinit var audioRecord: AudioRecord
    private lateinit var outputFile: String
    private lateinit var recordingThread: Thread
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech)
        FirebaseApp.initializeApp(this)


        // 권한 확인 및 요청
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        }

        // UI 요소 초기화
        tvRecognizedSpeech = findViewById(R.id.tvRecognizedSpeech)
        btnRecord = findViewById(R.id.btnRecord)
        btnTTS = findViewById(R.id.btnTTS)
        btnDelete = findViewById(R.id.btndelete)
        btnPlay = findViewById(R.id.btnPlay)

        // TextToSpeech 객체 초기화
        textToSpeech = TextToSpeech(this, this)

        // 녹음 파일 재생 버튼 클릭 이벤트
        btnPlay.setOnClickListener {
            val dataFolder = getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath ?: run {
                Toast.makeText(this, "파일 경로를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            outputFile = "$dataFolder/test.wav"

            if (outputFile.isNotEmpty()) {
                playRecording(outputFile) // 녹음된 파일 재생
            } else {
                Toast.makeText(this, "재생할 파일이 없습니다.", Toast.LENGTH_SHORT).show()
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

        // TTS 버튼 클릭 이벤트
        btnTTS.setOnClickListener {
            val textToSpeak = tvRecognizedSpeech.text.toString()  // TTS로 읽을 텍스트
            if (ttsInitialized) {  // TTS가 초기화 되었는지 확인
                if (textToSpeak.isNotEmpty()) {
                    // 텍스트를 음성으로 변환하여 읽어줌
                    textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    Toast.makeText(this, "읽을 텍스트가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "TTS 초기화가 완료되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // Delete 버튼 클릭 이벤트 (텍스트 초기화 기능 추가)
        btnDelete.setOnClickListener {
            tvRecognizedSpeech.text = ""  // 텍스트뷰의 내용을 빈 문자열로 초기화
            Toast.makeText(this, "텍스트가 초기화되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // TTS 초기화 시 호출되는 메서드
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // TTS 언어를 한국어로 설정
            val result = textToSpeech.setLanguage(Locale.KOREAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "한국어 TTS는 지원되지 않습니다.", Toast.LENGTH_SHORT).show()
            } else {
                ttsInitialized = true  // TTS 초기화 완료
            }
        } else {
            Toast.makeText(this, "TTS 초기화에 실패했습니다.", Toast.LENGTH_SHORT).show()
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
        Toast.makeText(this@SpeechActivity, "녹음이 시작되었습니다.", Toast.LENGTH_SHORT).show()

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

    private fun stopRecording() {
        // 녹음 중지
        isRecording = false
        audioRecord.stop()
        audioRecord.release()

        // 녹음 스레드 종료 대기
        recordingThread.join()
        btnRecord.text = "녹음 시작"
    }

    private fun uploadToFirebase(audioData: ByteArray) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (userId == null) {
            Toast.makeText(this, "사용자가 로그인되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val storageReference = FirebaseStorage.getInstance().reference
        val userAudioRef = storageReference.child("audio/$userId/speech_to_text/${System.currentTimeMillis()}.wav")

        userAudioRef.putBytes(audioData)
            .addOnSuccessListener {
                Toast.makeText(this, "오디오 파일이 Firebase에 성공적으로 업로드되었습니다.", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "오디오 파일 업로드에 실패했습니다: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    // WAV 파일 헤더 생성
    private fun createWavFileHeader(totalAudioLen: Long, totalDataLen: Long, longSampleRate: Int, channels: Int, bitRate: Int): ByteArray {
        val header = ByteArray(44)
        val byteRate = bitRate * longSampleRate * channels / 8

        header[0] = 'R'.toByte() // RIFF
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
        header[12] = 'f'.toByte() // fmt
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()
        header[16] = 16 // size of 'fmt '
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (channels * bitRate / 8).toByte() // block align
        header[33] = 0
        header[34] = bitRate.toByte() // bits per sample
        header[35] = 0
        header[36] = 'd'.toByte() // data
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
            raf.seek(0) // 파일 맨 처음으로 이동하여 헤더 작성
            raf.write(header)
            raf.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // 녹음된 파일 재생 함수
    private fun playRecording(filePath: String) {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                prepare()
                start()
                Toast.makeText(this@SpeechActivity, "음성 재생 중...", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@SpeechActivity, "재생 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // TTS 객체 해제
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()  // 미디어 플레이어 자원 해제
        mediaPlayer = null

        if (textToSpeech != null) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }
}
