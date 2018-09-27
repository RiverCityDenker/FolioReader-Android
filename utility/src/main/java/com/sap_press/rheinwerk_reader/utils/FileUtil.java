package com.sap_press.rheinwerk_reader.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;
import com.sap_press.rheinwerk_reader.mod.models.foliosupport.EpubCommon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

public class FileUtil {

    private static final String SLASH_SIGN = "/";
    private static final String EBOOK_READER = "EbookReader";
    private static final String EBOOK_READER_FILE_NAME = "ebookreader.txt";

    public static final String LIST_TOPIC = "ListTopics";
    private static final String LIST_TOPIC_FILE_NAME = "topicslist.txt";
    private static final String LIST_FAVORITE_FILE_NAME = "favoriteslist.txt";
    private static final String TAG = FileUtil.class.getSimpleName();

    public static String getEbookPath(Context context, String ebookId) {
        return getFolderEbookPath(context) + File.separator + ebookId;
    }

    private static String getFolderEbookPath(Context context) {
        return context.getFilesDir().toString() + File.separator + EBOOK_READER;
    }

    public static String writeResponseBodyToDisk(Context context, ResponseBody response, String ebookId, String href) {
        String folderPath = getEbookPath(context, ebookId);
        File file = getFile(folderPath, href);
        try {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                inputStream = response.byteStream();
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                }
                outputStream.flush();
                return file.getPath();
            } catch (NullPointerException e) {
                e.printStackTrace();
                return file.getPath();
            } catch (IOException e) {
                e.printStackTrace();
                return file.getPath();
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return file.getPath();
        }
    }

    @NonNull
    public static File getFile(String folderPath, String href) {
        String folderName;
        String fileName = href;
        if (href.contains(SLASH_SIGN)) {
            folderName = href.substring(0, href.indexOf(SLASH_SIGN));
            fileName = href.substring(href.lastIndexOf(SLASH_SIGN));
            folderPath = folderPath + File.separator + folderName;
        }
        File folder = new File(folderPath);
        folder.mkdirs();
        return new File(folder, fileName);
    }

    @NonNull
    public static String reformatHref(String href) {
        if (href.startsWith("/"))
            href = href.substring(1);
        else
            return href;
        return reformatHref(href);
    }

    public static boolean isFileExist(Context context, String ebookId, String href) {
        href = FileUtil.reformatHref(href);
        final String folderPath = getEbookPath(context, ebookId);
        File file = getFile(folderPath, href);
        return file.exists();
    }

    public static Object parseContentFileToObject(String filePath) {
        EpubCommon epubCommon = new EpubCommon(filePath);
        return epubCommon.parseBookInfo();
    }

    private static <T> void saveObjectToFile(Context context, String fileName, T t) {
        FileOutputStream fos;
        ObjectOutputStream os;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(t);
            os.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static <T> T getObjectFromFile(Context context, String fileName) {
        T object = null;
        try {
            FileInputStream fis = context.openFileInput(fileName);
            ObjectInputStream is = new ObjectInputStream(fis);
            object = (T) is.readObject();
            is.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static void deleteDownloadedEbookFromExternalStorage(Ebook ebook) {
        String path = ebook.getFilePath();
        if (path == null) {
            Log.w("FileUtil", "path to download is empty: " + path);
            return;
        }
        File file = new File(path);
        deleteDirectory(file);
    }

    public static boolean deleteAllEbook(Context context) {
        File fileExternalStorage = new File(getFolderEbookPath(context));
        return deleteDirectory(fileExternalStorage);
    }

    public static <T> void saveTopicsListIntoStorage(Context context, List<T> topicsList) {
        saveObjectToFile(context, FileUtil.LIST_TOPIC_FILE_NAME, topicsList);
    }

    public static <T> void saveFavoriteListIntoStorage(Context context, List<T> favoriteList) {
        FileUtil.saveObjectToFile(context, FileUtil.LIST_FAVORITE_FILE_NAME, favoriteList);
    }

    public static <T> List<T> getFavoriteListFromStorage(Context context) {
        List<T> favoriteList = FileUtil.getObjectFromFile(context, FileUtil.LIST_FAVORITE_FILE_NAME);
        if (favoriteList == null) {
            favoriteList = new ArrayList<>();
        }
        return favoriteList;
    }

    public static <T> List<T> getTopicsListIntoStorage(Context context) {
        List<T> topicsList = FileUtil.getObjectFromFile(context, FileUtil.LIST_TOPIC_FILE_NAME);
        if (topicsList == null) {
            topicsList = new ArrayList<>();
        }
        return topicsList;
    }

    public static boolean deleteTopicsFile() {
        File fileInternalStorage = new File(LIST_TOPIC_FILE_NAME);
        return deleteDirectory(fileInternalStorage);
    }

    public static boolean deleteFavoriteFile() {
        File fileInternalStorage = new File(LIST_FAVORITE_FILE_NAME);
        return deleteDirectory(fileInternalStorage);
    }

    private static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return (path.delete());
    }

    public static boolean deleteDirectory(String folderPath) {
        return deleteDirectory(new File(folderPath));
    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.delete();
    }

    public static boolean deleteFile(File file) {
        return file.delete();
    }

    public static long getFilesCount(String directoryPath) {
        File file = new File(directoryPath);
        File[] files = file.listFiles();
        int count = 0;
        if (files != null) {
            for (File f : files)
                if (f.isDirectory())
                    count += getFilesCount(f.getAbsolutePath());
                else
                    count++;
        }
        return count;
    }

    public static List<String> getDirectoryNameList(Context context) {
        List<String> ebookNameList = new ArrayList<>();
        File ebookDirectory = new File(getFolderEbookPath(context));
        if (ebookDirectory.listFiles() != null) {
            for (File f : ebookDirectory.listFiles()) {
                if (f.isDirectory()) {
                    ebookNameList.add(f.getName());
                }
            }
        }
        return ebookNameList;
    }
}
