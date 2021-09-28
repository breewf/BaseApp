package com.hy.myapp.activity

import android.os.Bundle
import com.hy.basic.BaseActivity
import com.hy.myapp.BR
import com.hy.myapp.R
import com.hy.myapp.databinding.ActivityKotlinRetrofitBinding
import com.hy.myapp.viewmodel.MainViewMode

class KotlinRetrofitActivity : BaseActivity<ActivityKotlinRetrofitBinding, MainViewMode>() {

    //private lateinit var binding: ActivityKotlinRetrofitBinding

    //override fun onCreate(savedInstanceState: Bundle?) {
    //    super.onCreate(savedInstanceState)
    //    //setContentView(R.layout.activity_kotlin_retrofit)
    //
    //    binding = ActivityKotlinRetrofitBinding.inflate(layoutInflater)
    //    setContentView(binding.root)
    //}

    override fun getLayoutId(): Int {
        return R.layout.activity_kotlin_retrofit
    }

    override fun getVariableId(): Int {
        return BR.mainVm
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.requestData()
    }
}