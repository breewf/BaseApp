package com.hy.myapp.activity

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.hy.myapp.databinding.ActivityResultContractBinding

class ActivityResultContractActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultContractBinding

    private lateinit var startForResult: ActivityResultLauncher<Intent?>
    private lateinit var takePicture: ActivityResultLauncher<Void?>
    private lateinit var pickContact: ActivityResultLauncher<Void?>

    private lateinit var cameraPermission: ActivityResultLauncher<String?>
    private lateinit var cameraPermission2: ActivityResultLauncher<Array<String>?>
    private lateinit var contactPermission: ActivityResultLauncher<String?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultContractBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == 1) {
                    val msg = it.data?.getStringExtra("ResultContract") ?: "没有返回intent"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }

                if (it.resultCode == 2) {
                    val msg = it.data?.getStringExtra("ResultContract") ?: "没有返回intent"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            }

        takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            binding.ivImage.setImageBitmap(it)
        }

        // 拍摄视频
        registerForActivityResult(ActivityResultContracts.TakeVideo()) {

        }

        cameraPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                Toast.makeText(this, "请求相机权限结果:$it", Toast.LENGTH_SHORT).show()
            }

        cameraPermission2 =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                var result = ""
                it.forEach { gain ->
                    result += "获取:${gain.key} 权限 ${if (gain.value) "成功" else "失败"}"
                }
                Toast.makeText(this, result, Toast.LENGTH_LONG).show()
            }

        binding.tv1.setOnClickListener {
            // 启动activity，参数传intent
            startForResult.launch(Intent(this, ActivityResultContract2Activity::class.java))
        }

        binding.tvPick.setOnClickListener {
            takePicture.launch(null)
        }

        binding.tvCamera.setOnClickListener {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }

        binding.tvCamera2.setOnClickListener {
            cameraPermission2.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }

        contactPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    // 获取联系人权限
                    pickContact.launch(null)
                }
            }

        // 联系人
        pickContact = registerForActivityResult(ActivityResultContracts.PickContact()) {
            it?.toString()
        }

        binding.tvContact.setOnClickListener {
            contactPermission.launch(Manifest.permission.READ_CONTACTS)
        }
    }
}