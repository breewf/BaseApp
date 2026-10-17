package com.hy.myapp.demo.event

import com.hy.basic.BaseViewModel

/**
 * desc: 演示页占位 ViewModel。
 *
 * 事件总线本身是全局单例,不依赖 ViewModel;这里仅为满足 [com.hy.basic.BaseFragment] /
 * [com.hy.basic.BaseActivity] 的 VM 泛型而存在(演示页没有需要长期持有的业务状态)。
 * 真实业务里若某页确有数据,应在自己的 VM 里用 StateFlow 承载,而不是塞进总线。
 */
class BusDemoViewModel : BaseViewModel()
