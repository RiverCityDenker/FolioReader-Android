package com.sap_press.rheinwerk_reader.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.sap_press.rheinwerk_reader.mod.models.ebooks.Cover;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

import java.util.List;

/**
 * Created by DUY on 5/8/2018.
 */

public class BookUtil {

    private static final String TAG = BookUtil.class.getSimpleName();

    public static Ebook getEbookExistInDownloadedList(int ebookId, List<Ebook> ebooks) {
        for (Ebook ebook : ebooks) {
            if (ebook.getId() == ebookId) {
                return ebook;
            }
        }
        return null;
    }

    public static boolean isEbookExistInList(int ebookId, List<Ebook> list) {
        for (Ebook ebook : list) {
            if (ebook.getId() == ebookId) {
                return true;
            }
        }
        return false;
    }

    public static String getErrorCode(Throwable throwable) {
        return throwable.getMessage();
    }

    public static int getIndexInBookListById(int ebookId, List<Ebook> ebookList) {
        for (int i = 0; i < ebookList.size(); i++) {
            if (ebookList.get(i).getId() == ebookId) {
                return i;
            }
        }
        return -1;
    }

    public static Cover getImageCoverInBookDetailPage(Context context, List<Cover> coverList) {
        int imgHeight;
        Cover imgCover = null;

        float density = context.getResources().getDisplayMetrics().density;
        if (density >= 3) {
            imgHeight = 1050;
        } else if (density >= 2) {
            imgHeight = 700;
        } else {
            imgHeight = 350;
        }

        for (int i = 0; i < coverList.size(); i++) {
            if (coverList.get(i).getHeight() == imgHeight) {
                imgCover = coverList.get(i);
                break;
            }
        }
        if (imgCover == null) {
            imgCover = coverList.get(0);
        }
        return imgCover;
    }

    public static Cover getImageCoverInLibraryPageByTablet(Context context, List<Cover> coverList) {
        int imgHeight;
        Cover imgCover = null;

        float density = context.getResources().getDisplayMetrics().density;
        if (density >= 3) {
            imgHeight = 354;
        } else if (density >= 2) {
            imgHeight = 236;
        } else {
            imgHeight = 118;
        }

        for (int i = 0; i < coverList.size(); i++) {
            if (coverList.get(i).getHeight() == imgHeight) {
                imgCover = coverList.get(i);
                break;
            }
        }
        if (imgCover == null) {
            imgCover = coverList.get(0);
        }
        return imgCover;
    }

    public static Cover getImageCoverInLibraryPageByPhone(Context context, List<Cover> coverList) {
        int imgHeight;
        Cover imgCover = null;

        float density = context.getResources().getDisplayMetrics().density;
        if (density >= 3) {
            imgHeight = 282;
        } else if (density >= 2) {
            imgHeight = 188;
        } else {
            imgHeight = 94;
        }

        for (int i = 0; i < coverList.size(); i++) {
            if (coverList.get(i).getHeight() == imgHeight) {
                imgCover = coverList.get(i);
                break;
            }
        }
        if (imgCover == null) {
            imgCover = coverList.get(0);
        }
        return imgCover;
    }

    public static List<Ebook> updateFavoriteForBookList(Context context, List<Ebook> ebookList) {
//        List<FavoriteResponse> favoriteResponses = FileUtil.getFavoriteListFromStorage(context);
//        if (!favoriteResponses.isEmpty()) {
//            List<Integer> bookIdList = favoriteResponses.get(0).getItems();
//            for (Ebook ebook : ebookList) {
//                if (bookIdList.contains(ebook.getId())) {
//                    ebook.setFavoriten(true);
//                }
//            }
//        }
//        return ebookList;
        return null;
    }

    public static void testLayout(Context context) {

        DisplayMetrics dm = context.getResources().getDisplayMetrics();

        float screenWidth = dm.widthPixels / dm.xdpi;
        float screenHeight = dm.heightPixels / dm.ydpi;

//        Log.d(TAG, "onCreate: >>>" + dm.widthPixels + " --- " + dm.xdpi + " === " + screenWidth);
//        Log.d(TAG, "onCreate: >>>" + dm.heightPixels + " --- " + dm.ydpi + " === " + screenHeight);
//
//        Log.d(TAG, "testLayout: >>>>> Dpi = " + dm.density + "---" + dm.densityDpi);
//
//        int widthInDp = (int) (dm.widthPixels / dm.density);
//        int dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dm.widthPixels, dm);
//        Log.d(TAG, "testLayout: >>>SW = " + widthInDp + " --- " + dp);


// return 0.75 if it's LDPI
// return 1.0 if it's MDPI
// return 1.5 if it's HDPI
// return 2.0 if it's XHDPI
// return 3.0 if it's XXHDPI
// return 4.0 if it's XXXHDPI
    }
}
