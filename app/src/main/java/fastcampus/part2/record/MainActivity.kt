package fastcampus.part2.record

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fastcampus.part2.record.databinding.ActivityMainBinding

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

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        // 녹음 권한 요청 코드
        private const val REQUEST_RECORD_AUDIO_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 녹음 버튼 리스너 설정
        binding.recordButton.setOnClickListener {
            handleRecordPermission()
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

    /** 녹음 시작 처리 (권한 부여 시) */
    private fun onRecord(isGranted: Boolean) {
        // TODO: Implement recording functionality
    }

    /** 권한 요청 설명 다이얼로그 */
    private fun showPermissionRationalDialog() {
        AlertDialog.Builder(this)
            .setTitle("녹음 권한 허용 안내")
            .setMessage("녹음기를 사용하기 위해서는 녹음 권한이 필요합니다. 권한을 허용할까요?")
            .setPositiveButton("허용") { _, _ -> requestRecordAudioPermission() }
            .setNegativeButton("취소") { dialogInterface, _ -> dialogInterface.cancel() }
            .show()
    }

    /** 설정 화면 안내 다이얼로그 */
    private fun showPermissionSettingDialog() {
        AlertDialog.Builder(this)
            .setTitle("서비스 이용 안내")
            .setMessage("녹음기를 사용하기 위해서는 녹음 권한이 반드시 필요합니다. 설정 화면으로 이동할까요?")
            .setPositiveButton("설정으로 이동") { _, _ -> navigateToAppSetting() }
            .setNegativeButton("취소") { dialogInterface, _ -> dialogInterface.cancel() }
            .show()
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
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
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
}