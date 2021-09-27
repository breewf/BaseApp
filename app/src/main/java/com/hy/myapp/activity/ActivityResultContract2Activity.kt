package com.hy.myapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hy.myapp.databinding.ActivityResultContract2Binding

class ActivityResultContract2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityResultContract2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultContract2Binding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.tv1.setOnClickListener {
            val intent = Intent()
            intent.putExtra("ResultContract", "Hello world")
            setResult(1, intent)
            finish()
        }

        binding.tv2.setOnClickListener {
            val intent = Intent()
            intent.putExtra("ResultContract", "返回结果2")
            setResult(2, intent)
            finish()
        }
    }
}