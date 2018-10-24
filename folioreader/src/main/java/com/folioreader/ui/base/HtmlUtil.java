package com.folioreader.ui.base;

import android.content.Context;
import android.text.TextUtils;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.R;

import static com.sap_press.rheinwerk_reader.utils.FileUtil.readFileFromAssets;

/**
 * @author gautam chibde on 14/6/17.
 */

public final class HtmlUtil {

    public static final String HTML_CODE_TAG = "code";
    public static final String HTML_SPAN_TAG = "span";

    /**
     * Function modifies input html string by adding extra css,js and font information.
     *
     * @param context     Activity Context
     * @param htmlContent input html raw data
     * @return modified raw html string
     */
    public static String getHtmlContent(Context context, String htmlContent, Config config) {

        String cssPath =
                String.format(context.getString(R.string.css_tag), "file:///android_asset/css/Style.css");

        String jsPath = String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/jsface.min.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/jquery-3.1.1.min.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-core.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-highlighter.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-classapplier.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-serializer.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/Bridge.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/android.selection.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag_method_call),
                "setMediaOverlayStyleColors('#C0ED72','#C0ED72')") + "\n";

        jsPath = jsPath
                + "<meta name=\"viewport\" content=\"height=device-height, user-scalable=no\" />";

        String toInject = "<head>" + "\n" + cssPath + "\n" + jsPath + "\n";
        htmlContent = htmlContent.replace("<head>", toInject);

        String classes = "";
        switch (config.getFont()) {
            case Constants.FONT_ANDADA:
                classes = "andada";
                break;
            case Constants.FONT_LATO:
                classes = "lato";
                break;
            case Constants.FONT_LORA:
                classes = "lora";
                break;
            case Constants.FONT_RALEWAY:
                classes = "raleway";
                break;
            default:
                break;
        }

        if (config.isNightMode()) {
            classes += " nightMode";
        }

        switch (config.getFontSize()) {
            case 0:
                classes += " textSizeOne";
                break;
            case 1:
                classes += " textSizeTwo";
                break;
            case 2:
                classes += " textSizeThree";
                break;
            case 3:
                classes += " textSizeFour";
                break;
            case 4:
                classes += " textSizeFive";
                break;
            default:
                break;
        }
        htmlContent = htmlContent.replace("<html ", "<html class=\"" + classes + "\" ");

        htmlContent = getUpdatedStyle(htmlContent, classes);
        return htmlContent;
    }
    public static String getErrorHtml(Context context, Config config, String title, String message) {

        final String errorImageForDay = "file:///android_asset/image/error_ic.png";
        final String errorImageForNight = "file:///android_asset/image/error_ic_night_mode.png";
        final String errorImagePath = config.isNightMode() ? errorImageForNight : errorImageForDay;

        String html = readFileFromAssets("html/empty_page.html", context);

        final String htmlWithTitle = html.replace(">Title<", ">" + title +"<");
        final String htmlWithMessage = htmlWithTitle.replace(">Message<", ">" + message + "<");

        return htmlWithMessage;
    }

    private static String getUpdatedStyle(String htmlContent, String classes) {
        if (TextUtils.isEmpty(htmlContent)) {
            return htmlContent;
        }
        int startBodyTagIndex = htmlContent.indexOf("<body");
        int endBodyTagIndex = -1;
        for (int i = startBodyTagIndex; i < htmlContent.length(); i++) {
            if (htmlContent.charAt(i) == '>') {
                endBodyTagIndex = i;
                break;
            }
        }
        String bodyTag = htmlContent.substring(startBodyTagIndex, endBodyTagIndex + 1);
        if (bodyTag.contains(">")) {
            if (bodyTag.contains("class")) {
                StringBuilder className = new StringBuilder();
                for (int j = bodyTag.indexOf("class=") + "class=".length() + 1; j < bodyTag.length(); j++) {
                    if (bodyTag.charAt(j) == '"') {
                        break;
                    } else {
                        className.append(bodyTag.charAt(j));
                    }
                }
                System.out.println("className = " + className);
                String updatedClass = className.toString() + " " + classes;
                String bodyTag2 = bodyTag.replace(className.toString(), updatedClass);
                htmlContent = htmlContent.replace(bodyTag, bodyTag2);
            } else {
                String bodyTag2 = bodyTag.replace(">", " class=\"" + classes + "\">");
                htmlContent = htmlContent.replace(bodyTag, bodyTag2);
            }
        }
        return htmlContent;
    }

    public static String reformatHtml(Context context, String htmlString, Config mConfig) {
        final String formatedHtml = htmlString.replace(HTML_CODE_TAG, HTML_SPAN_TAG).replace("../", "");
        return HtmlUtil.getHtmlContent(context, formatedHtml, mConfig);
    }
}
