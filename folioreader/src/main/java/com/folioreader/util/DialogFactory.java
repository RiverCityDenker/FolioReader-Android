package com.folioreader.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.folioreader.R;

public class DialogFactory {

    public enum TypeDownload {
        DOWNLOAD, LIVE_READING
    }

    public interface DialogCallback {
        void exitReader();
    }

    public interface DownLoadCallback {
        void download(TypeDownload typeDownload, boolean isSkip);
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

    public static void createDownloadDialog(final Context context, String msg, final DownLoadCallback callback) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View vi = inflater.inflate(R.layout.check_box, null);
        final CheckBox dontShowAgain = vi.findViewById(R.id.check_box_skip);

        final AlertDialog dialog = getDialogBuilder(context)
                .setView(vi)
                .setTitle("Reading online")
                .setMessage(msg)
                .setPositiveButton("READ ONLINE", (dialog1, which) -> {
                    callback.download(TypeDownload.LIVE_READING, dontShowAgain.isChecked());
                    dialog1.dismiss();
                }).setNegativeButton("START DOWNLOAD", (dialog12, which) -> {
                    callback.download(TypeDownload.DOWNLOAD, dontShowAgain.isChecked());
                    dialog12.dismiss();
                }).create();
        boolean isSkip = SharedPreferenceUtil.getSharedPreferencesBoolean(context, SharedPreferenceUtil.PREF_KEY_DIALOG_SKIP, false);
        if (!isSkip) {
            dialog.setCancelable(false);
            dialog.show();
        }
    }
}
