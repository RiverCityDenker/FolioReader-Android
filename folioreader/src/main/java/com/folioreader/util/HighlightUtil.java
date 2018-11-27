package com.folioreader.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.folioreader.datamanager.HighlightManager;
import com.folioreader.model.sqlite.HighLightTable;
import com.google.gson.Gson;
import com.sap_press.rheinwerk_reader.logging.FolioLogging;
import com.sap_press.rheinwerk_reader.mod.models.downloadinfo.DownloadInfo;
import com.sap_press.rheinwerk_reader.mod.models.highlight.Note;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by priyank on 5/12/16.
 */
public class HighlightUtil {

    private static final String TAG = "HighlightUtil";

    public static String createHighlightRangy(Context context,
                                              DownloadInfo downloadInfo,
                                              String content,
                                              String bookId,
                                              String pageId,
                                              int pageNo,
                                              String oldRangy) {
        try {
            JSONObject jObject = new JSONObject(content);
            String rangy = jObject.getString("rangy");
            postNoteToServer(context, downloadInfo, bookId, pageId, pageNo, oldRangy, jObject, rangy);
            // save highlight to database
//            long id = HighLightTable.insertHighlightItem(note);
//            if (id != -1) {
//                sendHighlightBroadcastEvent(context, note, Note.HighLightAction.NEW);
//            }
            return rangy;
        } catch (JSONException e) {
            FolioLogging.tag(TAG).e("createHighlightRangy failed", e);
        }
        return "";
    }

    private static void postNoteToServer(Context context, DownloadInfo downloadInfo, String bookId,
                                         String pageId, int pageNo, String oldRangy,
                                         JSONObject jObject, String rangy) throws JSONException {
        String textContent = jObject.getString("content");
        String color = jObject.getString("color");

        String rangyHighlightElement = getRangyString(rangy, oldRangy);
        Note note = new Note();
        note.setMarkedText(textContent);
        note.setType(color);
        note.setPageIndex(pageNo);
        note.setProductId(Integer.parseInt(bookId));
        note.setFilePath(pageId);
        note.setRange(rangyHighlightElement);
        note.setInternalId(getHighlightIdFromRangy(rangyHighlightElement));
        note.setCreatedAt(HighLightTable.getDateTimeString(Calendar.getInstance().getTime()));
        note.setTitle("Title");
        note.setNoteText("note");
        if (downloadInfo.ismIsTestMode()) {
            //post Highlight to server
            HighlightManager highlightManager = new HighlightManager(context, downloadInfo.getmBaseUrl());
            highlightManager.addHighlight(note);
        } else {
            // save highlight to database
            long id = HighLightTable.insertHighlightItem(note);
            if (id != -1) {
                sendHighlightBroadcastEvent(context, note, Note.HighLightAction.NEW);
            }
        }
    }

    /**
     * function extracts rangy element corresponding to latest highlight.
     *
     * @param rangy    new rangy string generated after adding new highlight.
     * @param oldRangy rangy string before new highlight.
     * @return rangy element corresponding to latest element.
     */
    private static String getRangyString(String rangy, String oldRangy) {
        List<String> rangyList = getRangyArray(rangy);
        for (String firs : getRangyArray(oldRangy)) {
            if (rangyList.contains(firs)) {
                rangyList.remove(firs);
            }
        }
        if (rangyList.size() >= 1) {
            return rangyList.get(0);
        } else {
            return "";
        }
    }

    /**
     * function converts Rangy text into each individual element
     * splitting with '|'.
     *
     * @param rangy rangy test with format: type:textContent|start$end$id$class$containerId
     * @return ArrayList of each rangy element corresponding to each highlight
     */
    private static List<String> getRangyArray(String rangy) {
        List<String> rangyElementList = new ArrayList<>();
        rangyElementList.addAll(Arrays.asList(rangy.split("\\|")));
        if (rangyElementList.contains("type:textContent")) {
            rangyElementList.remove("type:textContent");
        } else if (rangyElementList.contains("")) {
            return new ArrayList<>();
        }
        return rangyElementList;
    }

    /**
     * @param rangy This is rangy like format start$end$bookId_pageIndex_start_end$highlight_green$
     * @return bookId_pageIndex_start_end
     */
    private static String getHighlightIdFromRangy(String rangy) {
        List<String> rangyElementList = new ArrayList<>();
        rangyElementList.addAll(Arrays.asList(rangy.split("\\$")));
        return rangyElementList.size() >= 3 ? rangyElementList.get(2) : "";
    }

    public static String generateRangyString(String pageId) {
        List<String> rangyList = HighLightTable.getHighlightItemsForPageId(pageId);
        StringBuilder builder = new StringBuilder();
        if (!rangyList.isEmpty()) {
            builder.append("type:textContent");
            for (String rangy : rangyList) {
                builder.append('|');
                builder.append(rangy);
            }
        }
        return builder.toString();
    }

    public static void sendHighlightBroadcastEvent(Context context,
                                                   Note note,
                                                   Note.HighLightAction action) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(
                getHighlightBroadcastIntent(note, action));
    }

    public static Intent getHighlightBroadcastIntent(Note note,
                                                     Note.HighLightAction modify) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Note.INTENT, note);
        bundle.putSerializable(Note.HighLightAction.class.getName(), modify);
        return new Intent(Note.BROADCAST_EVENT).putExtras(bundle);
    }
}
