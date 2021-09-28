package com.hy.baseapp.base;

import android.os.Bundle;

import com.hy.basic.networkkt.BaseViewModel;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import androidx.annotation.NonNull;
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

    private VM VM;
    private VDB VDB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handlerVDB();
        handlerVM();
        receiveLiveData();
    }

    private void handlerVDB() {
        VDB = DataBindingUtil.setContentView(this, getLayoutId());
        if (VDB == null) {
            return;
        }
        VDB.setLifecycleOwner(this);
    }

    @SuppressWarnings("unchecked")
    private void handlerVM() {
        Class<BaseViewModel> viewModelClass;
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            viewModelClass = (Class<BaseViewModel>) ((ParameterizedType) type).getActualTypeArguments()[1];
        } else {
            //使用父类的类型
            viewModelClass = BaseViewModel.class;
        }
        VM = (VM) new ViewModelProvider(this).get(viewModelClass);
        if (getVariableId() > 0) {
            getLifecycle().addObserver(VM);
            VDB.setVariable(getVariableId(), VM);
        }
    }

    private void receiveLiveData() {

    }

    public @NonNull
    VM getVM() {
        return VM;
    }

    public @NonNull
    VDB getVDB() {
        return VDB;
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
        if (VDB != null) {
            VDB.unbind();
        }
        VDB = null;
    }
}
