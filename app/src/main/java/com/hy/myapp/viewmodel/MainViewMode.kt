package com.hy.myapp.viewmodel

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.hy.basic.networkkt.BaseViewModel
import com.hy.basic.networkkt.next
import com.hy.basic.networkkt.request
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MainViewMode : BaseViewModel() {

    val responseText = ObservableField("Kotlin ViewMode")

    fun requestData() {
        viewModelScope.launch {
            request {
                getApiTest2("")
            }.catch {
                Log.e("requestData===>", "catch=====>")
            }.next {
                Log.i("requestData", "==collect=---------------=next==>${this}")
                responseText.set(content)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("MainVm", "lifecycle==onCreate")
    }

    override fun onStart() {
        super.onStart()
        Log.i("MainVm", "lifecycle==onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.i("MainVm", "lifecycle==onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i("MainVm", "lifecycle==onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("MainVm", "lifecycle==onDestroy")
    }
}