package com.hy.myapp.viewmodel

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.hy.basic.networkkt.BaseViewModel
import com.hy.basic.networkkt.next
import com.hy.basic.networkkt.request
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MainVm : BaseViewModel() {

    val responseText = ObservableField("Kotlin ViewMode")

    fun requestData() {
        viewModelScope.launch {
            request {
                getApiDataTest2("")
            }.catch {
                Log.e("requestData===>", "catch=====>")
            }.next {
                Log.i("requestData", "==collect=---------------=next==>${this}")
                responseText.set(content)
            }
        }
    }
}