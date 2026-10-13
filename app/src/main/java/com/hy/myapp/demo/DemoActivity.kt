package com.hy.myapp.demo

import android.view.LayoutInflater
import androidx.lifecycle.viewModelScope
import com.hy.basic.BaseActivity
import com.hy.basic.BaseViewModel
import com.hy.basic.network.launchRequest
import com.hy.myapp.bean.TestResultDataKt
import com.hy.myapp.databinding.ActivityDemoBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DemoActivity : BaseActivity<ActivityDemoBinding, DemoViewModel>() {
    override fun createBinding(inflater: LayoutInflater) = ActivityDemoBinding.inflate(inflater)
    override fun initData() {
        viewModel.loadData()
    }
}

class DemoViewModel : BaseViewModel() {
    private val _data = MutableStateFlow<List<TestResultDataKt?>>(emptyList())
    val data = _data.asStateFlow()
    fun loadData() = viewModelScope.launch {
        launchRequest(block = { getTestResult("28654780") }) { _data.value = listOf(it) }
    }
}