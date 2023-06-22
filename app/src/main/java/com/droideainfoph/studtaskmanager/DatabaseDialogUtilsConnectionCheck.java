package com.droideainfoph.studtaskmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;


public class DatabaseDialogUtilsConnectionCheck {

    private static Dialog loadingDialog;

    public static void showLoadingDialog(Context context) {
        dismissLoadingDialog();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.activity_database_dialog_utils_connection_check, null);
        builder.setView(dialogView);

        // Disable the dialog from being canceled
        builder.setCancelable(false);

        loadingDialog = builder.create();
        loadingDialog.show();
    }

    public static void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    public static void showDialogBox(Context context, String title, String message,
                                     DialogInterface.OnClickListener positiveClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Try Again", positiveClickListener)
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}