package com.sap_press.rheinwerk_reader.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sap_press.rheinwerk_reader.logging.FolioLogging;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;
import com.sap_press.rheinwerk_reader.mod.models.foliosupport.EpubBook;
import com.sap_press.rheinwerk_reader.mod.models.foliosupport.EpubCommon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public interface ContentParserResult {
        void onContentParserResult(EpubBook epubBook);
    }

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
        if (href.startsWith("/") || href.startsWith("."))
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

    public static class ContentParserAsyn extends AsyncTask<String, Void, EpubBook> {

        private ContentParserResult listener;

        public ContentParserAsyn(ContentParserResult listener) {
            this.listener = listener;
        }

        @Override
        protected EpubBook doInBackground(String... strings) {
            final String filePath = strings[0];
            return (EpubBook) parseContentFileToObject(filePath);
        }

        @Override
        protected void onPostExecute(EpubBook epubBook) {
            this.listener.onContentParserResult(epubBook);
        }
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

    public static void deleteDownloadedEbookFromExternalStorage(Context context, Ebook ebook, boolean isFullDelete) {
        String path = ebook.getFilePath();
        if (path == null) {
            FolioLogging.tag(TAG).w("path to download is empty: " + path);
            return;
        }
        FolioLogging.tag(TAG).d("deleteDownloadedEbookFromExternalStorage: >>>" + path);
        File file = new File(path);
        if (isFullDelete) {
            deleteDirectory(file);
            if (isFileExist(context, String.valueOf(ebook.getId()), "content.opf")) {
                deleteFile(FileUtil.getFile(path, "content.opf"));
            }
        } else {
            deleteAlmostFilesInReader(file);
        }
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

    private static void deleteAlmostFilesInReader(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteAlmostFilesInReader(file);
                    } else {
                        final String name = file.getName();
                        if (name.contains("content.opf")
                                || name.contains("toc.ncx")
                                || name.contains("styles.css"))
                            continue;
                        file.delete();
                    }
                }
            }
        }
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
        int count = 0;
        try {
            File file = new File(directoryPath);
            File[] files = file.listFiles();

            if (files != null) {
                for (File f : files)
                    if (f.isDirectory())
                        count += getFilesCount(f.getAbsolutePath());
                    else
                        count++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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

    public String readFromFile(String rootPath, String relativePath) {
        String filePath = rootPath.concat(relativePath);
        File epubFile = new File(filePath);

        if (epubFile.exists()) {
            System.out.println(TAG + relativePath + " File exists at given path");

            try {
                InputStream is = new FileInputStream(epubFile);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (!epubFile.exists()) {
            System.out.println(TAG + " No such file exists at given path: " + relativePath);
        }
        return null;
    }

    public static String readFileFromAssets(String fileName, Context context) {
        StringBuilder returnString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {
            fIn = context.getResources().getAssets()
                    .open(fileName);
            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);
            String line = "";
            while ((line = input.readLine()) != null) {
                returnString.append(line);
            }
        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                if (isr != null)
                    isr.close();
                if (fIn != null)
                    fIn.close();
                if (input != null)
                    input.close();
            } catch (Exception e2) {
                e2.getMessage();
            }
        }
        return returnString.toString();
    }

}
