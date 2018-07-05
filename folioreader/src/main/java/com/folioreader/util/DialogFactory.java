package com.folioreader.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.folioreader.R;

public class DialogFactory {


    public interface DialogCallback {
        void exitReader();
    }

    private static AlertDialog.Builder getDialogBuilder(Context context) {
        return new AlertDialog.Builder(context, R.style.AlertDialogStyle);
    }

    public static void createEbookErrorDialog(final Context context, String msg, final DialogFactory.DialogCallback listener) {
        final AlertDialog dialog = getDialogBuilder(context)
                .setTitle(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        listener.exitReader();
                    }
                }).create();
        dialog.show();
    }
}
