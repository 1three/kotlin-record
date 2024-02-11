package fastcampus.part2.record

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fastcampus.part2.record.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException

/**
 * 녹음 앱
 *
 * 1) 권한 요청
 * 2) 파형 나타내기
 * */

/**
 * MediaPlayer
 * MediaRecorder
 * Permission Request
 * Canvas
 * Handler
 * */

class MainActivity : AppCompatActivity(), OnTimerTickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var timer: Timer
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var fileName: String = ""
    private var state: State = State.RELEASE

    companion object {
        private const val REQUEST_RECORD_AUDIO_CODE = 200 // 녹음 권한 요청 코드
    }

    private enum class State {
        RELEASE, RECORDING, PLAYING
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        timer = Timer(this)
        fileName = "${externalCacheDir?.absolutePath}/audioRecord.3gp"

        binding.recordButton.setOnClickListener {
            when (state) {
                State.RELEASE -> handleRecordPermission()
                State.RECORDING -> onRecord(false)
                State.PLAYING -> {
                    Toast.makeText(this, "재생 중에는 녹음할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.playButton.setOnClickListener {
            when (state) {
                State.RELEASE -> onPlay(true)
                State.RECORDING -> Toast.makeText(this, "녹음 중입니다.", Toast.LENGTH_SHORT).show()
                State.PLAYING -> Toast.makeText(this, "이미 재생 중입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.stopButton.setOnClickListener {
            when (state) {
                State.RECORDING -> onRecord(false)
                State.PLAYING -> onPlay(false)
                State.RELEASE -> {
                    Toast.makeText(this, "재생 중인 파일이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** 녹음 권한 처리 */
    private fun handleRecordPermission() {
        when {
            hasRecordAudioPermission() -> onRecord(true)
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> showPermissionRationalDialog()
            else -> requestRecordAudioPermission()
        }
    }

    /** 녹음 권한 확인 */
    private fun hasRecordAudioPermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    /** 녹음 권한 요청 */
    private fun requestRecordAudioPermission() = ActivityCompat.requestPermissions(
        this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_CODE
    )

    private fun onRecord(isRecord: Boolean) = if (isRecord) {
        val file = File(fileName)
        if (file.exists()) {
            showOverwriteDialog()
        } else {
            startRecording()
        }
    } else {
        stopRecording()
    }

    private fun onPlay(isPlay: Boolean) = if (isPlay) startPlaying() else stopPlaying()

    private fun startRecording() {
        state = State.RECORDING

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e("record APP (recording)", "prepare() failed: $e")
                Toast.makeText(
                    this@MainActivity, "녹음을 시작할 수 없습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT
                ).show()
            }

            start()
        }

        binding.waveformView.clearData()
        timer.start()

        binding.recordButton.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.round_stop_24)
        )
        binding.playButton.isEnabled = false
        binding.stopButton.isEnabled = false
        binding.playButton.alpha = 0.4f
        binding.stopButton.alpha = 0.4f
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null

        timer.stop()

        state = State.RELEASE

        binding.recordButton.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.round_fiber_manual_record_24)
        )
        binding.playButton.isEnabled = true
        binding.stopButton.isEnabled = true
        binding.playButton.alpha = 1.0f
        binding.stopButton.alpha = 1.0f
    }

    private fun startPlaying() {
        state = State.PLAYING

        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
            } catch (e: IOException) {
                Log.e("record APP (playing)", "prepare() failed: $e")
                Toast.makeText(
                    this@MainActivity, "녹음 파일을 재생할 수 없습니다. 다시 녹음을 진행해주세요.", Toast.LENGTH_SHORT
                ).show()
            }

            start()
        }

        binding.waveformView.clearWave()
        timer.start()

        player?.setOnCompletionListener { stopPlaying() }

        binding.recordButton.isEnabled = false
        binding.recordButton.alpha = 0.4f
    }

    private fun stopPlaying() {
        state = State.RELEASE

        player?.release()
        player = null

        timer.stop()

        binding.recordButton.isEnabled = true
        binding.recordButton.alpha = 1.0f
    }

    /** 권한 요청 설명 다이얼로그 */
    private fun showPermissionRationalDialog() {
        AlertDialog.Builder(this).setTitle("녹음 권한 허용 안내")
            .setMessage("녹음기를 사용하기 위해서는 녹음 권한이 필요합니다. 권한을 허용할까요?")
            .setPositiveButton("허용") { _, _ -> requestRecordAudioPermission() }
            .setNegativeButton("취소") { dialogInterface, _ -> dialogInterface.cancel() }.show()
    }

    /** 설정 화면 안내 다이얼로그 */
    private fun showPermissionSettingDialog() {
        AlertDialog.Builder(this).setTitle("서비스 이용 안내")
            .setMessage("녹음기를 사용하기 위해서는 녹음 권한이 반드시 필요합니다. 설정 화면으로 이동할까요?\n(권한 → 마이크 → 권한 설정)")
            .setPositiveButton("설정으로 이동") { _, _ -> navigateToAppSetting() }
            .setNegativeButton("취소") { dialogInterface, _ -> dialogInterface.cancel() }.show()
    }

    /** 녹음 덮어쓰기 확인 다이얼로그 */
    private fun showOverwriteDialog() {
        AlertDialog.Builder(this).setTitle("녹음 덮어쓰기").setMessage("이미 녹음된 파일이 있습니다. 덮어쓸까요?")
            .setPositiveButton("네") { _, _ -> startRecording() }.setNegativeButton("아니요") { _, _ ->
                Toast.makeText(this, "기존 녹음을 유지합니다.", Toast.LENGTH_SHORT).show()
            }.show()
    }

    /** 설정 화면 이동 */
    private fun navigateToAppSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    /** 권한 요청 결과 처리 */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_CODE) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                onRecord(true)
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    showPermissionRationalDialog()
                } else {
                    showPermissionSettingDialog()
                }
            }
        }
    }

    override fun onTick(duration: Long) {
        val milliSeconds = duration % 1000 / 10
        val seconds = (duration / 1000) % 60
        val minutes = (duration / 1000) / 60

        binding.timeTextView.text = String.format("%02d:%02d.%02d", minutes, seconds, milliSeconds)

        if (state == State.PLAYING) {
            binding.waveformView.replayAmplitude()
        } else if (state == State.RECORDING) {
            binding.waveformView.addAmplitude(recorder?.maxAmplitude?.toFloat() ?: 0f)
        }
    }
}