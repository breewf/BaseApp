package com.hy.baseapp.base;

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

    private VM VM;
    private VDB VDB;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        VDB = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        if (VDB != null) {
            VDB.setLifecycleOwner(this);
            return VDB.getRoot();
        } else {
            return inflater.inflate(getLayoutId(), container, false);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handlerVM();
        receiveLiveData();
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
        VM = (VM) new ViewModelProvider(requireActivity()).get(viewModelClass);
        if (VM == null) {
            VM = (VM) new ViewModelProvider(this).get(viewModelClass);
        }
        if (getVariableId() > 0) {
            getLifecycle().addObserver(VM);
            VDB.setVariable(getVariableId(), VM);
        }

    }

    private void receiveLiveData() {

    }

    public VM getViewModel() {
        return VM;
    }

    public VDB getViewDataBinding() {
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
    public void onDestroy() {
        super.onDestroy();
        if (VDB != null) {
            VDB.unbind();
        }
        VDB = null;
    }
}
