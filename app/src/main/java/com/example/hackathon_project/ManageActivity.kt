package com.example.hackathon_project

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.io.File

class ManageActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private var mediaPlayer: MediaPlayer? = null  // 미디어 플레이어는 nullable로 관리
    private lateinit var audioFiles: MutableList<File>
    private lateinit var adapter: AudioFileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        listView = findViewById(R.id.listView)

        val dataFolder = getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath ?: run {
            Toast.makeText(this, "파일 경로를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 녹음된 파일들을 불러옴
        audioFiles = loadAudioFiles(dataFolder).toMutableList()
        adapter = AudioFileAdapter(this, audioFiles)
        listView.adapter = adapter

        // 리스트 항목을 클릭하면 해당 파일을 재생
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedFile = audioFiles[position]
            playAudioFile(selectedFile)
        }

        // 리스트 항목을 길게 누르면 해당 파일을 삭제
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val selectedFile = audioFiles[position]
            if (selectedFile.delete()) {
                // 파일이 삭제되었을 경우
                Toast.makeText(this, "${selectedFile.name} 파일이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                audioFiles.removeAt(position)
                adapter.notifyDataSetChanged()
                stopAudioPlayback()  // 삭제 후 재생 중인 파일이 있을 경우 재생 중지
            } else {
                // 파일 삭제에 실패한 경우
                Toast.makeText(this, "파일 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    // 녹음된 음성 파일을 불러오는 함수
    private fun loadAudioFiles(folderPath: String): List<File> {
        val directory = File(folderPath)
        return directory.listFiles { file -> file.extension == "wav" }?.toList() ?: emptyList()
    }

    // 선택된 파일을 재생하는 함수
    private fun playAudioFile(file: File) {
        stopAudioPlayback()  // 기존 재생 중인 파일이 있으면 중지

        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.path)
            prepare()
            start()
            setOnCompletionListener {
                stopAudioPlayback()  // 재생이 완료되면 플레이어 중지
            }
        }

        Toast.makeText(this, "${file.name} 재생 중...", Toast.LENGTH_SHORT).show()
    }

    // 미디어 플레이어 정지 및 해제 함수
    private fun stopAudioPlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAudioPlayback()  // 액티비티 종료 시 재생 중지
    }

    // AudioFileAdapter 클래스 추가
    class AudioFileAdapter(context: Context, private val audioFiles: List<File>) :
        ArrayAdapter<File>(context, 0, audioFiles) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(
                android.R.layout.simple_list_item_1, parent, false
            )

            val textView = view.findViewById<TextView>(android.R.id.text1)
            val audioFile = audioFiles[position]
            textView.text = audioFile.name

            return view
        }
    }
}