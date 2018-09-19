package com.sap_press.rheinwerk_reader.utils;

import android.content.Context;

import com.sap_press.rheinwerk_reader.mod.models.topics.SubTopics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hale on 6/18/2018.
 */
public class TopicsMapHolderUtil {
    private Context context;
    private Map<Integer, String> topicsMap = new HashMap<>();

    public TopicsMapHolderUtil(Context context) {
        this.context = context;
    }

    public Map<Integer, String> getTopicsMapIntoStorage() {
        List<SubTopics> topicsList = FileUtil.getTopicsListIntoStorage(context);
        if (topicsList.size() > 0) {
            for (int i = 0; i < topicsList.size(); i++) {
                SubTopics subTopics = topicsList.get(i);
                topicsMap.put(subTopics.getId(), subTopics.getTitle());
                if (subTopics.getListTopic().size() > 0) {
                    setTopicsMap(subTopics.getListTopic(), topicsMap);
                }
            }
        }

        return topicsMap;
    }

    private void setTopicsMap(List<SubTopics> topicsList, Map<Integer, String> topicsMap) {
        for (int i = 0; i < topicsList.size(); i++) {
            SubTopics subTopics = topicsList.get(i);
            topicsMap.put(subTopics.getId(), subTopics.getTitle());
            if (subTopics.getListTopic().size() > 0) {
                setTopicsMap(subTopics.getListTopic(), topicsMap);
            }
        }

    }
}
