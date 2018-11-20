package com.sap_press.rheinwerk_reader.download;

import android.content.Context;

import com.sap_press.rheinwerk_reader.mod.models.apiinfo.ApiInfo;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

import java.lang.ref.WeakReference;

public class DownloadFileTask extends ParallelExecutorTask<String, Integer, Ebook> {
    private final Context context;
    private final boolean isBasicData;
    private final String folderPath;
    private final ApiInfo apiInfo;
    private final Ebook ebook;
    private static final String TAG = DownloadFileTask.class.getSimpleName();

    public DownloadFileTask(Context context, Ebook ebook, ApiInfo apiInfo, String folderPath,
                            boolean isBasicData) {
        super(ParallelExecutorTask.createPool());
        this.context = context;
        this.isBasicData = isBasicData;
        this.folderPath = folderPath;
        this.apiInfo = apiInfo;
        this.ebook = ebook;
    }

    @Override
    protected Ebook doInBackground(String... originalHrefs) {
        return new DownloadFileTaskSync(context, ebook, apiInfo, folderPath, isBasicData).downloadSync(originalHrefs);
    }

}