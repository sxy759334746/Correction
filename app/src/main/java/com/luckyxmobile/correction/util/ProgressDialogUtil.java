package com.luckyxmobile.correction.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.luckyxmobile.correction.R;

/**
 * @author ChangHao
 * 耗时工具类util
 */
public class ProgressDialogUtil {
    private static AlertDialog mAlertDialog;

    /**
     * 弹出耗时对话框
     *
     * @param context
     */
    public static void showProgressDialog(Context context) {
        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(context, R.style.progress_dialog_style).create();
        }

        View loadView = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null);
        mAlertDialog.setView(loadView, 0, 0, 0, 0);
        mAlertDialog.setCanceledOnTouchOutside(false);

        TextView tvTip = loadView.findViewById(R.id.tvTip);
        tvTip.setText(R.string.loading);

        if(!((Activity)context).isFinishing())
            mAlertDialog.show();
    }

    public static void showProgressDialog(Context context, String tip) {
        if (TextUtils.isEmpty(tip)) {
            tip = context.getString(R.string.loading);
        }

        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(context, R.style.progress_dialog_style).create();
        }

        View loadView = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null);
        mAlertDialog.setView(loadView, 0, 0, 0, 0);
        mAlertDialog.setCanceledOnTouchOutside(false);

        TextView tvTip = loadView.findViewById(R.id.tvTip);
        tvTip.setText(tip);

        mAlertDialog.show();
    }

    /**
     * 隐藏耗时对话框
     */
    public static void dismiss() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
    }
}
