package com.hy.myapp.demo.storage

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hy.basic.BaseActivity
import com.hy.myapp.databinding.ActivityStorageDemoBinding
import kotlinx.coroutines.launch

/**
 * desc: 本地持久化演示。MMKV 走属性委托(MmkvConfig),SP 走扩展函数备用对照。
 */
class StorageDemoActivity : BaseActivity<ActivityStorageDemoBinding, StorageDemoViewModel>() {

    override val showToolBar: Boolean get() = true

    override val toolbarTitle: CharSequence get() = "本地持久化"

    override fun createBinding(inflater: LayoutInflater) =
        ActivityStorageDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnWriteBasic.setOnClickListener { viewModel.writeBasic() }
        binding.btnWriteObject.setOnClickListener { viewModel.writeObject() }
        binding.btnAddTag.setOnClickListener { viewModel.addTag() }
        binding.btnBumpCounter.setOnClickListener { viewModel.bumpCounter() }
        binding.btnReload.setOnClickListener { viewModel.reloadFromDisk() }
        binding.btnClearMmkv.setOnClickListener { viewModel.clearMmkv() }
        binding.btnSpSave.setOnClickListener { viewModel.saveToSp(this) }
        binding.btnSpRead.setOnClickListener { viewModel.readFromSp(this) }
        binding.btnSpClear.setOnClickListener { viewModel.clearSp(this) }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.snapshot.collect { binding.tvValue.text = it } }
                launch { viewModel.logText.collect { binding.tvLog.text = it } }
            }
        }
    }
}
