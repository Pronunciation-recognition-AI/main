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
import android.media.AudioFormat
import android.media.MediaRecorder
import java.io.FileOutputStream
import java.io.IOException
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech  // TextToSpeech import
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Instant
import java.util.Locale  // Locale import


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

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("signals")

        myRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val signalData = dataSnapshot.value as? Map<*,*>
                val userId = signalData?.get("userId") as? String
                val message = signalData?.get("message") as? String
                val timestamp = signalData?.get("timestamp") as? Long


                val currentTime = Instant.now().epochSecond
                println(currentTime)
                println(timestamp)
                // 타임스탬프가 현재 시간 이후인 경우에만 처리
                if (timestamp != null && timestamp >= currentTime) {
                    if (userId != null && message != null) {
                        tvRecognizedSpeech.text = message
                        Toast.makeText(this@SpeechActivity, "신호를 받았습니다: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseSignal", "loadSignal:onCancelled", databaseError.toException())
            }
        })

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
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_user"
            val storageRef = FirebaseStorage.getInstance().reference.child("audio/$userId/test/test.wav")

            // Firebase Storage에서 파일 다운로드
            val localFile = File.createTempFile("test", "wav")
            storageRef.getFile(localFile)
                .addOnSuccessListener {
                    // 다운로드가 완료되면 파일 재생
                    playRecording(localFile.absolutePath)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "파일을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
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

        // AudioRecord 객체 초기화
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize)

        val outputStream = ByteArrayOutputStream()  // ByteArrayOutputStream 사용
        val buffer = ByteArray(bufferSize)

        // 먼저 더미 헤더를 작성하고 메모리에 기록
        val header = createWavFileHeader(0, 0, sampleRate, 1, 16)
        outputStream.write(header)

        // 녹음 시작
        audioRecord.startRecording()
        isRecording = true
        Toast.makeText(this@SpeechActivity, "음성 인식 중 입니다.", Toast.LENGTH_SHORT).show()

        // 녹음 스레드 실행
        recordingThread = Thread {
            try {
                var totalAudioLen: Long = 0
                val headerSize = 44  // WAV 헤더 크기

                while (isRecording) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        outputStream.write(buffer, 0, read)
                        totalAudioLen += read
                    }
                }

                // 총 데이터 길이와 파일 크기를 계산
                val totalDataLen = totalAudioLen + headerSize - 8
                updateWavHeader(outputStream, totalAudioLen, totalDataLen, sampleRate, 1, 16)

                // 녹음이 끝난 후 Firebase에 업로드
                uploadToFirebase(outputStream.toByteArray())

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        recordingThread.start()
    }


    // Firebase에 파일 업로드 함수
    private fun uploadToFirebase(audioData: ByteArray) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_user"
        val storageRef = FirebaseStorage.getInstance().reference.child("audio/$userId/test/test.wav")

        val uploadTask = storageRef.putBytes(audioData)  // ByteArray로 바로 업로드
        uploadTask.addOnSuccessListener {
            Toast.makeText(this, "파일 업로드 완료", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "파일 업로드 실패", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startFirebaseSignalListener() {
        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_user"  // 현재 사용자 ID 가져오기
        val userSignalRef = database.getReference("signals").child(userId)

        userSignalRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val signalData = dataSnapshot.getValue(String::class.java)
                val message = signalData ?: "No message"

                println("유저 신호 감지됨: $message")
                tvRecognizedSpeech.text = message
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseSignal", "loadSignal:onCancelled", databaseError.toException())
            }
        })
    }

    private fun stopRecording() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("signals")
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_user"
        Log.d("UserID", "User ID: $userId")
        myRef.removeValue()
        // 사용자 ID와 "study" 신호를 Firebase에 전송
        val signalData = mapOf(
            "userId" to userId,
            "action" to "speech"
        )
        myRef.push().setValue(signalData)
        isRecording = false
        audioRecord.stop()
        audioRecord.release()
        recordingThread.join()

        startFirebaseSignalListener()
        Toast.makeText(this, "음성 인식 완료", Toast.LENGTH_LONG).show()
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
    private fun updateWavHeader(outputStream: ByteArrayOutputStream, totalAudioLen: Long, totalDataLen: Long, longSampleRate: Int, channels: Int, bitRate: Int) {
        val header = createWavFileHeader(totalAudioLen, totalDataLen, longSampleRate, channels, bitRate)
        val headerBytes = header.copyOfRange(0, 44)  // WAV 헤더
        System.arraycopy(headerBytes, 0, outputStream.toByteArray(), 0, headerBytes.size)
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
