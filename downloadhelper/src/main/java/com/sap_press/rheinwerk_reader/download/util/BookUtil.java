package com.sap_press.rheinwerk_reader.download.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.sap_press.rheinwerk_reader.download.DownloadService;
import com.sap_press.rheinwerk_reader.download.datamanager.tables.LibraryTable;
import com.sap_press.rheinwerk_reader.download.events.UnableDownloadEvent;
import com.sap_press.rheinwerk_reader.download.models.ebooks.Cover;
import com.sap_press.rheinwerk_reader.download.models.ebooks.Ebook;

import org.greenrobot.eventbus.EventBus;

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


    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        } else
            return false;
    }

    public static String getErrorCode(Throwable throwable) {
        return throwable.getMessage();
    }

    public static long getCurrentTimeStamp() {
        return System.currentTimeMillis();
    }


    public static int getIndexInBookListById(int ebookId, List<Ebook> ebookList) {
        for (int i = 0; i < ebookList.size(); i++) {
            if (ebookList.get(i).getId() == ebookId) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        if (context != null) {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (manager != null) {
                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (serviceClass.getName().equals(service.service.getClassName())) {
                        return true;
                    }
                }
            }
        }
        return false;
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

    public static void stopDownloadServiceIfNeeded(Context context, UnableDownloadEvent.DownloadErrorType errorType) {
        Intent serviceIntent = new Intent(context, DownloadService.class);
        if (context != null && Util.isMyServiceRunning(context, DownloadService.class)) {
            context.stopService(serviceIntent);
        }
        List<Ebook> downloadedEbookList = LibraryTable.getDownloadingEbooks();
        if (!downloadedEbookList.isEmpty()) {
            for (int i = 0; i < downloadedEbookList.size(); i++) {
                Ebook ebook = downloadedEbookList.get(i);
                if (!ebook.isDownloaded()) {
                    if (ebook.getDownloadProgress() > 0) {
                        FileUtil.deleteDirectory(FileUtil.getEbookPath(context, String.valueOf(ebook.getId())));
                    }
                    ebook.resetInfo();
                    LibraryTable.updateEbook(ebook);
                }
            }
            EventBus.getDefault().post(new UnableDownloadEvent(errorType));
        }
    }

    public static void testLayout(Context context) {

        DisplayMetrics dm = context.getResources().getDisplayMetrics();

        float screenWidth = dm.widthPixels / dm.xdpi;
        float screenHeight = dm.heightPixels / dm.ydpi;

        Log.e(TAG, "onCreate: >>>" + dm.widthPixels + " --- " + dm.xdpi + " === " + screenWidth);
        Log.e(TAG, "onCreate: >>>" + dm.heightPixels + " --- " + dm.ydpi + " === " + screenHeight);

        Log.e(TAG, "testLayout: >>>>> Dpi = " + dm.density + "---" + dm.densityDpi);

        int widthInDp = (int) (dm.widthPixels / dm.density);
        int dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dm.widthPixels, dm);
        Log.e(TAG, "testLayout: >>>SW = " + widthInDp + " --- " + dp);


// return 0.75 if it's LDPI
// return 1.0 if it's MDPI
// return 1.5 if it's HDPI
// return 2.0 if it's XHDPI
// return 3.0 if it's XXHDPI
// return 4.0 if it's XXXHDPI
    }
}
