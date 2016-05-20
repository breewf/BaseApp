package com.ghy.baseapp.helper;


import android.support.v4.app.FragmentManager;

import com.company.myapp.R;
import com.ghy.baseapp.global.TitleInfo;
import com.rey.material.app.DatePickerDialog;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.TimePickerDialog;

import java.text.SimpleDateFormat;

/**
 * Created by GHY on 2016/5/9.
 * 日期、时间选择
 */
public class DateTimeHelper {

    public interface OnDateConfirmClickListener {
        void onConfirmClick(String date);
    }

    public interface OnTimeConfirmClickListener {
        void onConfirmClick(String date);
    }

    /**
     * 显示日期选择对话框
     *
     * @param manager
     * @param listener
     */
    public static void showDateDialogLight(FragmentManager manager, final OnDateConfirmClickListener listener) {
        Dialog.Builder builder;
        builder = new DatePickerDialog.Builder(R.style.Material_App_Dialog_DatePicker_Light) {
            @Override
            public void onPositiveActionClicked(DialogFragment fragment) {
                DatePickerDialog dialog = (DatePickerDialog) fragment.getDialog();
                String date = dialog.getFormattedDate(SimpleDateFormat.getDateInstance());
                if (listener != null) listener.onConfirmClick(date);
                super.onPositiveActionClicked(fragment);
            }

            @Override
            public void onNegativeActionClicked(DialogFragment fragment) {
                super.onNegativeActionClicked(fragment);
            }
        };
        builder.positiveAction(TitleInfo.BTN_CONFIRM).negativeAction(TitleInfo.BTN_CANCEL);
        DialogFragment fragment = DialogFragment.newInstance(builder);
        fragment.show(manager, null);
    }

    /**
     * 显示日期选择对话框
     *
     * @param manager
     * @param confirm
     * @param cancel
     * @param listener
     */
    public static void showDateDialogLight(FragmentManager manager, String confirm, String cancel, final OnDateConfirmClickListener listener) {
        Dialog.Builder builder;
        builder = new DatePickerDialog.Builder(R.style.Material_App_Dialog_DatePicker_Light) {
            @Override
            public void onPositiveActionClicked(DialogFragment fragment) {
                DatePickerDialog dialog = (DatePickerDialog) fragment.getDialog();
                String date = dialog.getFormattedDate(SimpleDateFormat.getDateInstance());
                if (listener != null) listener.onConfirmClick(date);
                super.onPositiveActionClicked(fragment);
            }

            @Override
            public void onNegativeActionClicked(DialogFragment fragment) {
                super.onNegativeActionClicked(fragment);
            }
        };
        builder.positiveAction(confirm).negativeAction(cancel);
        DialogFragment fragment = DialogFragment.newInstance(builder);
        fragment.show(manager, null);
    }

    /**
     * 显示日期选择对话框
     *
     * @param manager
     * @param listener
     */
    public static void showDateDialogDark(FragmentManager manager, final OnDateConfirmClickListener listener) {
        Dialog.Builder builder;
        builder = new DatePickerDialog.Builder(R.style.Material_App_Dialog_DatePicker) {
            @Override
            public void onPositiveActionClicked(DialogFragment fragment) {
                DatePickerDialog dialog = (DatePickerDialog) fragment.getDialog();
                String date = dialog.getFormattedDate(SimpleDateFormat.getDateInstance());
                if (listener != null) listener.onConfirmClick(date);
                super.onPositiveActionClicked(fragment);
            }

            @Override
            public void onNegativeActionClicked(DialogFragment fragment) {
                super.onNegativeActionClicked(fragment);
            }
        };
        builder.positiveAction(TitleInfo.BTN_CONFIRM).negativeAction(TitleInfo.BTN_CANCEL);
        DialogFragment fragment = DialogFragment.newInstance(builder);
        fragment.show(manager, null);
    }

    /**
     * 显示日期选择对话框
     *
     * @param manager
     * @param confirm
     * @param cancel
     * @param listener
     */
    public static void showDateDialogDark(FragmentManager manager, String confirm, String cancel, final OnDateConfirmClickListener listener) {
        Dialog.Builder builder;
        builder = new DatePickerDialog.Builder(R.style.Material_App_Dialog_DatePicker) {
            @Override
            public void onPositiveActionClicked(DialogFragment fragment) {
                DatePickerDialog dialog = (DatePickerDialog) fragment.getDialog();
                String date = dialog.getFormattedDate(SimpleDateFormat.getDateInstance());
                if (listener != null) listener.onConfirmClick(date);
                super.onPositiveActionClicked(fragment);
            }

            @Override
            public void onNegativeActionClicked(DialogFragment fragment) {
                super.onNegativeActionClicked(fragment);
            }
        };
        builder.positiveAction(confirm).negativeAction(cancel);
        DialogFragment fragment = DialogFragment.newInstance(builder);
        fragment.show(manager, null);
    }

    /**
     * 显示时间选择对话框
     *
     * @param manager
     * @param listener
     */
    public static void showTimeDialogLight(FragmentManager manager, final OnTimeConfirmClickListener listener) {
        Dialog.Builder builder;
        builder = new TimePickerDialog.Builder(R.style.Material_App_Dialog_TimePicker_Light, 24, 0) {
            @Override
            public void onPositiveActionClicked(DialogFragment fragment) {
                TimePickerDialog dialog = (TimePickerDialog) fragment.getDialog();
                String time = dialog.getFormattedTime(SimpleDateFormat.getTimeInstance());
                if (listener != null) listener.onConfirmClick(time);
                super.onPositiveActionClicked(fragment);
            }

            @Override
            public void onNegativeActionClicked(DialogFragment fragment) {
                super.onNegativeActionClicked(fragment);
            }
        };
        builder.positiveAction(TitleInfo.BTN_CONFIRM).negativeAction(TitleInfo.BTN_CANCEL);
        DialogFragment fragment = DialogFragment.newInstance(builder);
        fragment.show(manager, null);
    }

    /**
     * 显示时间选择对话框
     *
     * @param manager
     * @param confirm
     * @param cancel
     * @param listener
     */
    public static void showTimeDialogLight(FragmentManager manager, String confirm, String cancel, final OnTimeConfirmClickListener listener) {
        Dialog.Builder builder;
        builder = new TimePickerDialog.Builder(R.style.Material_App_Dialog_TimePicker_Light, 24, 0) {
            @Override
            public void onPositiveActionClicked(DialogFragment fragment) {
                TimePickerDialog dialog = (TimePickerDialog) fragment.getDialog();
                String time = dialog.getFormattedTime(SimpleDateFormat.getTimeInstance());
                if (listener != null) listener.onConfirmClick(time);
                super.onPositiveActionClicked(fragment);
            }

            @Override
            public void onNegativeActionClicked(DialogFragment fragment) {
                super.onNegativeActionClicked(fragment);
            }
        };
        builder.positiveAction(confirm).negativeAction(cancel);
        DialogFragment fragment = DialogFragment.newInstance(builder);
        fragment.show(manager, null);
    }

    /**
     * 显示时间选择对话框
     *
     * @param manager
     * @param listener
     */
    public static void showTimeDialogDark(FragmentManager manager, final OnTimeConfirmClickListener listener) {
        Dialog.Builder builder;
        builder = new TimePickerDialog.Builder(R.style.Material_App_Dialog_TimePicker, 24, 0) {
            @Override
            public void onPositiveActionClicked(DialogFragment fragment) {
                TimePickerDialog dialog = (TimePickerDialog) fragment.getDialog();
                String time = dialog.getFormattedTime(SimpleDateFormat.getTimeInstance());
                if (listener != null) listener.onConfirmClick(time);
                super.onPositiveActionClicked(fragment);
            }

            @Override
            public void onNegativeActionClicked(DialogFragment fragment) {
                super.onNegativeActionClicked(fragment);
            }
        };
        builder.positiveAction(TitleInfo.BTN_CONFIRM).negativeAction(TitleInfo.BTN_CANCEL);
        DialogFragment fragment = DialogFragment.newInstance(builder);
        fragment.show(manager, null);
    }

    /**
     * 显示时间选择对话框
     *
     * @param manager
     * @param confirm
     * @param cancel
     * @param listener
     */
    public static void showTimeDialogDark(FragmentManager manager, String confirm, String cancel, final OnTimeConfirmClickListener listener) {
        Dialog.Builder builder;
        builder = new TimePickerDialog.Builder(R.style.Material_App_Dialog_TimePicker, 24, 0) {
            @Override
            public void onPositiveActionClicked(DialogFragment fragment) {
                TimePickerDialog dialog = (TimePickerDialog) fragment.getDialog();
                String time = dialog.getFormattedTime(SimpleDateFormat.getTimeInstance());
                if (listener != null) listener.onConfirmClick(time);
                super.onPositiveActionClicked(fragment);
            }

            @Override
            public void onNegativeActionClicked(DialogFragment fragment) {
                super.onNegativeActionClicked(fragment);
            }
        };
        builder.positiveAction(confirm).negativeAction(cancel);
        DialogFragment fragment = DialogFragment.newInstance(builder);
        fragment.show(manager, null);
    }

}
