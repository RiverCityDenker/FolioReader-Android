package com.folioreader.ui.base;

import android.os.AsyncTask;

import com.folioreader.model.ReadPosition;
import com.folioreader.model.sqlite.ReadPositionTable;

public class SaveReadPositionTask extends AsyncTask<Void, Void, Void> {

    private OnSaveReadPosition onSaveReadPosition;
    private ReadPosition readPosition;

    public SaveReadPositionTask(ReadPosition readPosition, OnSaveReadPosition onSaveReadPosition) {
        this.onSaveReadPosition = onSaveReadPosition;
        this.readPosition = readPosition;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        ReadPositionTable.saveReadPosition(this.readPosition);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        onSaveReadPosition.onFinished();
    }
}