package com.hy.basic.networkkt

import androidx.lifecycle.MutableLiveData


/**
 * @author hy
 * @date 2021/9/27
 * desc:
 **/
open class BaseViewModel : LifeViewModel() {

    var apiExceptionEvent = MutableLiveData<ApiException>()

}