package com.hy.basic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hy.basic.networkkt.BaseViewModel;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

/**
 * @author hy
 * @date 2021/9/28
 * desc: BaseFragment
 **/
public abstract class BaseFragment<VDB extends ViewDataBinding, VM extends BaseViewModel> extends Fragment {

    private VM vm;
    private VDB vdb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        vdb = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        if (vdb != null) {
            vdb.setLifecycleOwner(this);
            return vdb.getRoot();
        } else {
            return inflater.inflate(getLayoutId(), container, false);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handlerVm();
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
    public void onDestroy() {
        super.onDestroy();
        if (vdb != null) {
            vdb.unbind();
        }
        vdb = null;
    }
}
