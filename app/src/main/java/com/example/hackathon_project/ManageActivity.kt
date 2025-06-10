package com.example.hackathon_project

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ManageActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioFiles: MutableList<StorageReference>
    private lateinit var adapter: AudioFileAdapter

    // Firebase Storage에서 파일을 가져오기 위한 참조
    private val storageReference = FirebaseStorage.getInstance().reference.child("audio/WCfGDgr3mWaBaoFdVNBcaHAzJ1P2/train")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        listView = findViewById(R.id.listView)

        // Firebase에서 파일 목록을 가져와서 목록을 표시
        loadAudioFilesFromFirebase()

        // 리스트 항목을 클릭하면 해당 파일을 재생
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedFile = audioFiles[position]
            playAudioFileFromFirebase(selectedFile)
        }

        // 리스트 항목을 길게 누르면 해당 파일을 Firebase에서 삭제
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val selectedFile = audioFiles[position]
            deleteAudioFileFromFirebase(selectedFile) { success ->
                if (success) {
                    Toast.makeText(this, "${selectedFile.name} 파일이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    audioFiles.removeAt(position)
                    adapter.notifyDataSetChanged()
                    stopAudioPlayback()
                } else {
                    Toast.makeText(this, "파일 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
    }

    // Firebase Storage에서 파일 목록을 가져오는 함수
    private fun loadAudioFilesFromFirebase() {
        storageReference.listAll()
            .addOnSuccessListener { listResult ->
                audioFiles = mutableListOf()

                listResult.items.forEach { fileRef ->
                    audioFiles.add(fileRef)
                }

                adapter = AudioFileAdapter(this, audioFiles)
                listView.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Firebase에서 파일 목록을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    // Firebase Storage에서 음성 파일을 삭제하는 함수
    private fun deleteAudioFileFromFirebase(fileRef: StorageReference, callback: (Boolean) -> Unit) {
        fileRef.delete()
            .addOnSuccessListener {
                callback(true)  // 삭제 성공 시
            }
            .addOnFailureListener {
                callback(false)  // 삭제 실패 시
            }
    }

    // Firebase Storage에서 음성 파일을 재생하는 함수
    private fun playAudioFileFromFirebase(fileRef: StorageReference) {
        stopAudioPlayback()  // 기존 재생 중인 파일이 있으면 중지

        // Firebase Storage에서 파일을 스트리밍 방식으로 재생
        fileRef.downloadUrl.addOnSuccessListener { uri ->
            mediaPlayer = MediaPlayer().apply {
                setDataSource(uri.toString())
                prepare()
                start()
                setOnCompletionListener {
                    stopAudioPlayback()
                }
            }
            Toast.makeText(this, "${fileRef.name} 재생 중...", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "파일 재생에 실패했습니다: ${fileRef.name}", Toast.LENGTH_SHORT).show()
        }
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
        stopAudioPlayback()
    }

    // Firebase Storage 파일 목록을 보여주는 Adapter 클래스
    class AudioFileAdapter(context: Context, private val audioFiles: List<StorageReference>) :
        ArrayAdapter<StorageReference>(context, 0, audioFiles) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(
                android.R.layout.simple_list_item_1, parent, false
            )

            val textView = view.findViewById<TextView>(android.R.id.text1)
            val audioFile = audioFiles[position]
            textView.text = audioFile.name  // Firebase에 업로드된 파일명을 그대로 표시

            return view
        }
    }
}