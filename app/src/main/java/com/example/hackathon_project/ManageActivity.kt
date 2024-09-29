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
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        listView = findViewById(R.id.listView)

        val dataFolder = getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath ?: run {
            Toast.makeText(this, "파일 경로를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 녹음된 파일들을 불러옴
        val audioFiles = loadAudioFiles(dataFolder)
        val adapter = AudioFileAdapter(this, audioFiles)
        listView.adapter = adapter

        // 리스트 항목을 클릭하면 해당 파일을 재생
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedFile = audioFiles[position]
            playAudioFile(selectedFile)
        }

        // 리스트 항목을 길게 누르면 해당 파일을 삭제
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val selectedFile = audioFiles[position]
            selectedFile.delete()
            Toast.makeText(this, "${selectedFile.name} 파일이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            recreate() // 삭제 후 리스트를 갱신
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
        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.path)
            prepare()
            start()
        }
        Toast.makeText(this, "${file.name} 재생 중...", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release() // 미디어 플레이어 자원 해제
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