package com.sap_press.rheinwerk_reader.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sap_press.rheinwerk_reader.R;

import static android.content.Context.MODE_PRIVATE;
import static com.sap_press.rheinwerk_reader.utils.Util.isMyServiceRunning;

/**
 * Created by Dung Hoang on 3/27/2018.
 */

public final class DialogCreator {

    public enum TypeDownload {
        DOWNLOAD, LIVE_READING
    }

    public static void showNotEnoughSpaceDialog(Context context) {
        createMessageDialog(context, context.getResources()
                        .getString(R.string.not_enough_space_dialog_title),
                context.getResources().getString(R.string.not_enough_space_dialog_mesage));
    }

    public interface DialogCallback {
        void deleteEbook();

    }

    public interface LogoutCallback {
        void logout(boolean isDownloading);
    }

    public interface DownLoadCallback {
        void download(TypeDownload typeDownload, boolean isSkip);
    }

    public interface MessageDialogCallback {
        void onClick();
    }

    public interface PausedDialogCallback {
        void onAbort();

        void onResume();
    }

    public static AlertDialog.Builder getDialogBuilder(Context context) {
        return new AlertDialog.Builder(context, R.style.AlertDialogStyle);
    }

    public static void createLogoutDialog(final Context context, String title, String msg, LogoutCallback callback, Class<?> serviceClass) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View vi = inflater.inflate(R.layout.dialog_one_message_two_button, null);
        TextView messageText = vi.findViewById(R.id.dialog_delete_message);
        Button logOut = vi.findViewById(R.id.dialog_delete_delete);
        Button cancel = vi.findViewById(R.id.dialog_delete_cancel);
        messageText.setText(msg);
        logOut.setText(R.string.ok);
        cancel.setText(R.string.cancel);

        final AlertDialog.Builder builder = getDialogBuilder(context);
        builder.setView(vi)
                .setTitle(title);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        logOut.setOnClickListener(view -> {

            messageText.setText("Please wait..");
            logOut.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);

            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (isMyServiceRunning(context, serviceClass)) {
                    callback.logout(true);
                    dialog.dismiss();
                } else {
                    callback.logout(false);
                    dialog.dismiss();
                }
            }, 300);
        });
        cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }

    public static void create1Message1ButtonWithCallback(Context context, String msg, String buttonText, MessageDialogCallback callback) {
        final AlertDialog dialog = getDialogBuilder(context)
                .setMessage(msg)
                .setPositiveButton(buttonText, (dialog1, id) -> {
                    callback.onClick();
                    dialog1.dismiss();
                }).create();
        dialog.show();
    }

    public static void createDownLoadFailDialog(final Context context, String title, String msg, String okBtnText) {
        SharedPreferences prefs = context.getSharedPreferences("NotifyDialog", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        boolean isShowing = prefs.getBoolean("IsShowDownLoadFail", false);
        if (!isShowing) {
            final AlertDialog dialog = getDialogBuilder(context)
                    .setTitle(title)
                    .setMessage(msg)
                    .setPositiveButton(okBtnText, (dialog1, id) -> {
                        editor.putBoolean("IsShowDownLoadFail", false);
                        editor.apply();
                        dialog1.dismiss();
                    }).create();
            dialog.show();
            dialog.setCancelable(false);
            editor.putBoolean("IsShowDownLoadFail", true);
            editor.apply();
        }
    }

    public static void createMessageDialog(final Context context, String title) {
        createMessageDialog(context, title, null, null, null);
    }

    public static void createMessageDialog(final Context context, String title, String message) {
        createMessageDialog(context, title, message, null, null);
    }

    public static void createMessageDialog(final Context context, String title, String message, String actionText) {
        createMessageDialog(context, title, message, actionText, null);
    }

    public static void createMessageDialog(final Context context, String title, String message, String actionText, MessageDialogCallback callback) {
        final AlertDialog.Builder builder = getDialogBuilder(context);
        if (actionText == null) {
            actionText = "OK";
        }
        builder.setTitle(title)
                .setPositiveButton(actionText, (dialog1, id) -> {
                    if (callback != null) {
                        callback.onClick();
                    }
                    dialog1.dismiss();
                });
        if (message != null) {
            builder.setMessage(message);
        }
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void createMessage2ButtonDialog(final Context context, String title, String message, String btnYesText, String btnNoText, MessageDialogCallback callback) {
        final AlertDialog.Builder builder = getDialogBuilder(context);
        builder.setTitle(title)
                .setPositiveButton(btnYesText, (dialog1, id) -> {
                    if (callback != null) {
                        callback.onClick();
                    }
                    dialog1.dismiss();
                })
                .setNegativeButton(btnNoText, (dialog2, id) -> dialog2.dismiss());
        if (message != null) {
            builder.setMessage(message);
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void createDeleteEbookDialog(final Context context,
                                               String bookTitle,
                                               boolean isDownloading,
                                               MessageDialogCallback callback) {

        LayoutInflater inflater = LayoutInflater.from(context);
        final View vi = inflater.inflate(R.layout.dialog_one_message_two_button, null);
        TextView messageText = vi.findViewById(R.id.dialog_delete_message);
        Button delete = vi.findViewById(R.id.dialog_delete_delete);
        Button cancel = vi.findViewById(R.id.dialog_delete_cancel);
        delete.setText(R.string.delete);
        cancel.setText(R.string.cancel);

        messageText.setText(String.format(context.getString(R.string.text_delete_dialog_message), bookTitle));

        final AlertDialog.Builder builder = getDialogBuilder(context);
        builder.setView(vi).setTitle(R.string.text_delete_dialog_title);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        delete.setOnClickListener(view -> {
            if (isDownloading) {
                messageText.setText("Please wait..");
                delete.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);

                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    callback.onClick();
                    dialog.dismiss();
                }, 300);
            } else {
                callback.onClick();
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }

    public static void createDeleteEbookInReaderDialog(final Context context,
                                                       String bookTitle,
                                                       boolean isDownloading,
                                                       MessageDialogCallback callback) {

        LayoutInflater inflater = LayoutInflater.from(context);
        final View vi = inflater.inflate(R.layout.dialog_one_message_two_button, null);
        TextView messageText = vi.findViewById(R.id.dialog_delete_message);
        Button delete = vi.findViewById(R.id.dialog_delete_delete);
        Button cancel = vi.findViewById(R.id.dialog_delete_cancel);
        delete.setText(R.string.delete);
        cancel.setText(R.string.cancel);

        messageText.setText(String.format(context.getString(R.string.text_delete_in_reader_dialog_message), bookTitle));

        final AlertDialog.Builder builder = getDialogBuilder(context);
        builder.setView(vi).setTitle(R.string.text_delete_dialog_title);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        delete.setOnClickListener(view -> {
            if (isDownloading) {
                messageText.setText("Please wait..");
                delete.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);

                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    callback.onClick();
                    dialog.dismiss();
                }, 300);
            } else {
                callback.onClick();
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }

    public static void createPausedDownloadDialog(final Context context, PausedDialogCallback callback) {

        LayoutInflater inflater = LayoutInflater.from(context);
        final View vi = inflater.inflate(R.layout.dialog_pause_download, null);
        TextView tvMessage = vi.findViewById(R.id.tv_dialog_message);
        Button btnAbort = vi.findViewById(R.id.btn_abort);
        Button btnResume = vi.findViewById(R.id.btn_resume);
        Button btnBack = vi.findViewById(R.id.btn_back);

        tvMessage.setText(context.getString(R.string.text_paused_dialog_message));

        final AlertDialog.Builder builder = getDialogBuilder(context);
        builder.setView(vi).setTitle(R.string.text_paused_dialog_title);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        btnAbort.setOnClickListener(view -> {
            if (callback != null)
                callback.onAbort();
            dialog.dismiss();
        });
        btnResume.setOnClickListener(view -> {
            if (callback != null)
                callback.onResume();
            dialog.dismiss();
        });
        btnBack.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }
}
