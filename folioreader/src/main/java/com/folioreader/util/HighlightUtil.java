package com.folioreader.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.folioreader.model.HighLight;
import com.folioreader.model.sqlite.HighLightTable;
import com.sap_press.rheinwerk_reader.mod.models.notes.HighlightV2;
import com.sap_press.rheinwerk_reader.logging.FolioLogging;

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
                                              String content,
                                              String bookId,
                                              String pageId,
                                              int pageNo,
                                              String oldRangy) {
        try {
            JSONObject jObject = new JSONObject(content);

            String rangy = jObject.getString("rangy");
            String textContent = jObject.getString("content");
            String color = jObject.getString("color");

            String rangyHighlightElement = getRangyString(rangy, oldRangy);

//            HighlightImpl highlightImpl = new HighlightImpl();
//            highlightImpl.setContent(textContent);
//            highlightImpl.setType(color);
//            highlightImpl.setPageNumber(pageNo);
//            highlightImpl.setBookId(bookId);
//            highlightImpl.setPageId(pageId);
//            highlightImpl.setRangy(rangyHighlightElement);
//            highlightImpl.setDate(Calendar.getInstance().getTime());

            HighlightV2 highlightV2 = new HighlightV2();
            highlightV2.setMarkedText(textContent);
            highlightV2.setType(color);
            highlightV2.setPageIndex(pageNo);
            highlightV2.setProductId(Integer.parseInt(bookId));
            highlightV2.setFilePath(pageId);
            highlightV2.setRange(rangyHighlightElement);
            highlightV2.setCreatedAt(HighLightTable.getDateTimeString(Calendar.getInstance().getTime()));
            // save highlight to database
            long id = HighLightTable.insertHighlightItem(highlightV2);
            if (id != -1) {
//                highlightV2.setId((int) id);
//                sendHighlightBroadcastEvent(context, hi, HighLight.HighLightAction.NEW);
            }
            return rangy;
        } catch (JSONException e) {
            FolioLogging.tag(TAG).e("createHighlightRangy failed", e);
        }
        return "";
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
                                                   HighlightV2 highlightV2,
                                                   HighLight.HighLightAction action) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(
                getHighlightBroadcastIntent(highlightV2, action));
    }

    public static Intent getHighlightBroadcastIntent(HighlightV2 highlightV2,
                                                     HighLight.HighLightAction modify) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(HighlightV2.INTENT, highlightV2);
        bundle.putSerializable(HighlightV2.HighLightAction.class.getName(), modify);
        return new Intent(HighlightV2.BROADCAST_EVENT).putExtras(bundle);
    }
}
