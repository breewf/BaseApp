package com.hy.basic;

import android.os.Bundle;

import com.hy.basic.networkkt.BaseViewModel;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;

/**
 * @author hy
 * @date 2021/9/28
 * desc: BaseActivity
 **/
public abstract class BaseActivity<VDB extends ViewDataBinding, VM extends BaseViewModel> extends AppCompatActivity {

    private VM vm;
    private VDB vdb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handlerVdb();
        handlerVm();
    }

    private void handlerVdb() {
        vdb = DataBindingUtil.setContentView(this, getLayoutId());
        if (vdb == null) {
            return;
        }
        vdb.setLifecycleOwner(this);
    }

    @SuppressWarnings("unchecked")
    private void handlerVm() {
        Class<BaseViewModel> viewModelClass;
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            viewModelClass = (Class<BaseViewModel>) ((ParameterizedType) type).getActualTypeArguments()[1];
        } else {
            viewModelClass = BaseViewModel.class;
        }
        vm = (VM) new ViewModelProvider(this).get(viewModelClass);
        if (getVariableId() > 0) {
            getLifecycle().addObserver(vm);
            vdb.setVariable(getVariableId(), vm);
        }
    }

    public VM getViewModel() {
        return vm;
    }

    public VDB getViewDataBinding() {
        return vdb;
    }

    /**
     * 布局 id
     *
     * @return 布局 id
     */
    public abstract int getLayoutId();

    /**
     * 初始化ViewModel的id
     *
     * @return BR的id
     */
    public abstract int getVariableId();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vdb != null) {
            vdb.unbind();
        }
        vdb = null;
    }
}
