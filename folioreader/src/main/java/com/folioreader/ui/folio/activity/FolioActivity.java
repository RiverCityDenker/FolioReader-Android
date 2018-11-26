/*
 * Copyright (C) 2016 Pedro Paulo de Amorim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.folioreader.ui.folio.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.FolioReader;
import com.folioreader.R;
import com.folioreader.datamanager.FolioDataManager;
import com.folioreader.model.ReadPosition;
import com.folioreader.model.event.HighlightClickedEvent;
import com.folioreader.model.event.MediaOverlayPlayPauseEvent;
import com.folioreader.model.event.TOCClickedEvent;
import com.folioreader.ui.custom.CustomLink;
import com.folioreader.ui.custom.EpubParser;
import com.folioreader.ui.custom.EpubPublicationCustom;
import com.folioreader.ui.folio.adapter.FolioPageFragmentAdapter;
import com.folioreader.ui.folio.fragment.ContentHighlightTabletFragment;
import com.folioreader.ui.folio.fragment.FolioPageFragment;
import com.folioreader.ui.folio.presenter.MainPresenter;
import com.folioreader.ui.folio.views.MainMvpView;
import com.folioreader.util.AppUtil;
import com.folioreader.util.DialogFactory;
import com.folioreader.util.FileUtil;
import com.folioreader.util.SharedPreferenceUtil;
import com.folioreader.view.ConfigDialogFragment;
import com.folioreader.view.DirectionalViewpager;
import com.folioreader.view.FolioToolbar;
import com.folioreader.view.FolioToolbarCallback;
import com.folioreader.view.FolioWebView;
import com.folioreader.view.ImageViewerFragment;
import com.folioreader.view.LoadingView;
import com.folioreader.view.MediaControllerCallback;
import com.folioreader.view.MediaControllerView;
import com.sap_press.rheinwerk_reader.dialog.DialogCreator;
import com.sap_press.rheinwerk_reader.download.datamanager.tables.LibraryTable;
import com.sap_press.rheinwerk_reader.download.events.DownloadBasicFileErrorEvent;
import com.sap_press.rheinwerk_reader.download.events.FinishDownloadContentEvent;
import com.sap_press.rheinwerk_reader.download.events.OnDownloadInterruptedBookEvent;
import com.sap_press.rheinwerk_reader.download.events.UnableDownloadEvent;
import com.sap_press.rheinwerk_reader.googleanalytics.AnalyticViewName;
import com.sap_press.rheinwerk_reader.googleanalytics.GoogleAnalyticManager;
import com.sap_press.rheinwerk_reader.mod.models.downloadinfo.DownloadInfo;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;
import com.sap_press.rheinwerk_reader.mod.models.notes.HighlightV2;
import com.sap_press.rheinwerk_reader.utils.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.readium.r2_streamer.model.container.Container;
import org.readium.r2_streamer.model.container.DirectoryContainer;
import org.readium.r2_streamer.model.container.EpubContainer;
import org.readium.r2_streamer.model.publication.link.Link;
import org.readium.r2_streamer.server.EpubServer;
import org.readium.r2_streamer.server.EpubServerSingleton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.folioreader.Constants.CHAPTER_SELECTED;
import static com.folioreader.view.DirectionalViewpager.SCROLL_STATE_DRAGGING;
import static com.sap_press.rheinwerk_reader.dialog.DialogCreator.createPausedDownloadDialog;

public class FolioActivity
        extends AppCompatActivity
        implements FolioActivityCallback,
        FolioWebView.ToolBarListener,
        MainMvpView,
        MediaControllerCallback,
        FolioToolbarCallback,
        DialogInterface.OnDismissListener, FolioPageFragmentAdapter.FolioPageAdapterListener {

    private static final String LOG_TAG = "FolioActivity";

    public static final String INTENT_EPUB_SOURCE_PATH = "com.folioreader.epub_asset_path";
    public static final String INTENT_EPUB_SOURCE_TYPE = "epub_source_type";
    public static final String INTENT_HIGHLIGHTS_LIST = "highlight_list";
    public static final String EXTRA_READ_POSITION = "com.folioreader.extra.READ_POSITION";
    private static final String BUNDLE_READ_POSITION_CONFIG_CHANGE = "BUNDLE_READ_POSITION_CONFIG_CHANGE";
    private static final String BUNDLE_TOOLBAR_IS_VISIBLE = "BUNDLE_TOOLBAR_IS_VISIBLE";
    public static final String INTENT_EBOOK_ID = "ebook_id";
    public static final String INTENT_EBOOK_FILE_PATH = "ebook_file_path";
    public static final String INTENT_EBOOK_CONTENT_KEY = "content_key";
    public static final String INTENT_EBOOK_USER_KEY = "user_key";
    public static final String INTENT_EBOOK_TITLE_NAME = "title_ebook";
    private static final String TAG = FolioActivity.class.getSimpleName();
    public static final String INTENT_EBOOK = "ebook";
    public static final String INTENT_DOWNLOAD_INFO = "download_info";
    public static final String INTENT_READING_TYPE = "reading_type";
    public static boolean mIsDirectionChanged = false;
    private Ebook mEbook;
    private FolioDataManager dataManager;
    private MainPresenter mMainPresenter;
    private DownloadInfo mDownloadInfo;
    private boolean mIsOnlineReading;
    private LoadingView loadingView;
    private boolean mImageClicked;
    private String mSelectedChapterHref;
    private boolean wasScrollLeft;
    private boolean isScrolling;
    private ArrayList<Integer> mPositionList = new ArrayList<>();


    public boolean isOnlineReading() {
        return mIsOnlineReading;
    }

    private boolean isDownloadingBook() {
        final FolioDataManager folioDataManager = FolioDataManager.getInstance();
        return folioDataManager.checkEbookDownload(this.mEbook.getId());
    }

    private MainPresenter getPresenter() {
        return mMainPresenter;
    }

    private void sendEventActionGoogleAnalytics(String action) {
        googleAnalytic.sendEvent(AnalyticViewName.Product_Page, action, "Screen Product");
    }

    @Override
    public void downloadOrDelete() {
        getPresenter().handleClickOnDownloadingView(this, mEbook);

    }

    private void startDownloadBook(Ebook currentEbook) {
        sendEventActionGoogleAnalytics("Selected download book");
        toolbar.setEbook(currentEbook);
        getPresenter().downloadEbook(this, currentEbook);
    }

    @Override
    public void updateUIAfterDelete(Ebook ebook) {
        runOnUiThread(() -> {
            toolbar.updateDownloadView(ebook);
        });
    }

    @Override
    public void updateDownloadProgress(Ebook ebook) {
        runOnUiThread(() -> {
            toolbar.updateDownloadView(ebook);
        });
    }

    @Override
    public void showDownloadError(String message) {

    }

    @Override
    public void showLoading() {
        loadingView.visible();
    }

    @Override
    public void hideLoading() {
        loadingView.invisible();
    }

    @Override
    public void exitReader() {
        this.finish();
    }

    @Override
    public void setOnlineReadingStatus(boolean isOnlineReading) {
        mIsOnlineReading = true;
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return mDownloadInfo;
    }

    @Override
    public boolean wasScrollLeft() {
        return wasScrollLeft;
    }

    @Override
    public boolean isScrolling() {
        return isScrolling;
    }

    @Override
    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

    public Ebook getEbook() {
        return mEbook;
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        mImageClicked = false;
    }

    public String getSelectedChapterHref() {
        return mSelectedChapterHref;
    }

    @Override
    public void updatePositionList(int position) {
        if (!mPositionList.contains(Integer.valueOf(position))) {
            mPositionList.add(position);
            Log.d(TAG, "updatePositionList: >>>" + position);
        }
    }

    public void doShouldHideLoading(int mPosition) {
        runOnUiThread(() -> {
            refreshPositionList();
            if (mPositionList.contains(mPosition)) {
                Log.d(TAG, "run: >>>remove " + mPosition);
                mPositionList.remove(Integer.valueOf(mPosition));
            }
            Log.d(TAG, "doShouldHideLoading: >>>>>>>>>>" + mPositionList.size());
            if (mPositionList.isEmpty()) {
                hideLoading();
            }
        });
    }

    private void refreshPositionList() {
        if (mPositionList.size() > 3) {
            mPositionList.remove(0);
            refreshPositionList();
        }
    }

    public enum EpubSourceType {
        RAW,
        ASSETS,
        SD_CARD,
        ENCRYPTED_FILE
    }

    public static final int ACTION_CONTENT_HIGHLIGHT = 77;
    private String bookFileName;
    private static final String HIGHLIGHT_ITEM = "highlight_item";
    private DirectionalViewpager mFolioPageViewPager;
    private FolioToolbar toolbar;
    private int mChapterPosition;
    private FolioPageFragmentAdapter mFolioPageFragmentAdapter;
    private ReadPosition entryReadPosition;
    private ReadPosition lastReadPosition;
    private Bundle outState;
    private Bundle savedInstanceState;
    private List<CustomLink> mSpineReferenceList = new ArrayList<>();
    private EpubServer mEpubServer;
    private String mBookId;
    private String mEpubFilePath;
    private EpubSourceType mEpubSourceType;
    int mEpubRawId = 0;
    private String ebookFilePath;
    private String contentKey;
    private String userKey;
    private String titleEbook;
    private GoogleAnalyticManager googleAnalytic;
    private MediaControllerView mediaControllerView;
    private Config.Direction direction = Config.Direction.VERTICAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        dataManager = FolioDataManager.getInstance();

        setConfig(savedInstanceState);
        setContentView(R.layout.folio_activity);
        loadingView = (LoadingView) findViewById(R.id.loadingView);
        this.savedInstanceState = savedInstanceState;
        mEpubSourceType = (EpubSourceType)
                getIntent().getExtras().getSerializable(FolioActivity.INTENT_EPUB_SOURCE_TYPE);
        mIsOnlineReading = getIntent().getExtras().getBoolean(FolioActivity.INTENT_READING_TYPE);

        if (mEpubSourceType.equals(EpubSourceType.ENCRYPTED_FILE)) {
            mEbook = getIntent().getParcelableExtra(INTENT_EBOOK);
            mDownloadInfo = getIntent().getParcelableExtra(INTENT_DOWNLOAD_INFO);
            if (!mIsOnlineReading || !Util.isOnline(this)) {
                if (mEbook != null) {
                    mBookId = String.valueOf(mEbook.getId());
                    ebookFilePath = mEbook.getFilePath();
                    contentKey = mEbook.getContentKey();
                    titleEbook = mEbook.getTitle();
                } else {
                    mBookId = String.valueOf(getIntent().getExtras().getInt(FolioActivity.INTENT_EBOOK_ID));
                    ebookFilePath = getIntent().getExtras().getString(FolioActivity.INTENT_EBOOK_FILE_PATH);
                    contentKey = getIntent().getExtras().getString(FolioActivity.INTENT_EBOOK_CONTENT_KEY);
                    titleEbook = getIntent().getExtras().getString(FolioActivity.INTENT_EBOOK_TITLE_NAME);
                }
            } else {
                // Reading ONLINE
                initAskingDialog();
                if (mEbook != null) {
                    mBookId = String.valueOf(mEbook.getId());
                    titleEbook = mEbook.getTitle();
                    contentKey = mEbook.getContentKey();
                }
            }

            if (mDownloadInfo != null) {
                userKey = mDownloadInfo.getmApiKey();
            }
            SharedPreferenceUtil.putSharedPreferencesLong(this, titleEbook, FileUtil.getCurrentTimeStamp());
            googleAnalytic = new GoogleAnalyticManager(this);
            mMainPresenter = new MainPresenter(this, mEbook, mDownloadInfo, googleAnalytic);
        } else {
            mBookId = getIntent().getStringExtra(FolioReader.INTENT_BOOK_ID);
            if (mEpubSourceType.equals(EpubSourceType.RAW)) {
                mEpubRawId = getIntent().getExtras().getInt(FolioActivity.INTENT_EPUB_SOURCE_PATH);
            } else {
                mEpubFilePath = getIntent().getExtras()
                        .getString(FolioActivity.INTENT_EPUB_SOURCE_PATH);
            }
        }

        mediaControllerView = (MediaControllerView) findViewById(R.id.media_controller_view);
        mediaControllerView.setListeners(this);

        initToolbar(savedInstanceState);

        if (ContextCompat.checkSelfPermission(FolioActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FolioActivity.this, Constants.getWriteExternalStoragePerms(), Constants.WRITE_EXTERNAL_STORAGE_REQUEST);
        } else {
            setupBook();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleAnalytic.sendScreen(AnalyticViewName.Reader);
        googleAnalytic.sendEvent(AnalyticViewName.start_eBook_reading, AnalyticViewName.reading_start, titleEbook);
    }

    private void initAskingDialog() {
        if (!isDownloadingBook())
            DialogFactory.createDownloadDialog(this, getResources().getString(R.string.download_asking), (typeDownload, isSkip) -> {
                SharedPreferenceUtil.putSharedPreferencesBoolean(getApplicationContext(), SharedPreferenceUtil.PREF_KEY_DIALOG_SKIP, isSkip);
                if (typeDownload == DialogFactory.TypeDownload.DOWNLOAD) {
                    //Handle function download here
                    Ebook currentEbook = dataManager.getCurrentBook(mEbook.getId());
                    startDownloadBook(currentEbook);
                }
            });
    }


    private void initToolbar(Bundle savedInstanceState) {
        toolbar = (FolioToolbar) findViewById(R.id.toolbar);
        toolbar.setListeners(this);
        if (savedInstanceState != null) {
            toolbar.setVisible(savedInstanceState.getBoolean(BUNDLE_TOOLBAR_IS_VISIBLE));
            if (toolbar.getVisible()) {
                toolbar.show();
            } else {
                toolbar.hide();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Config config = AppUtil.getSavedConfig(getApplicationContext());
            int color;
            if (config.isNightMode()) {
                color = ContextCompat.getColor(this, R.color.black);
            } else {
                int[] attrs = {android.R.attr.navigationBarColor};
                TypedArray typedArray = getTheme().obtainStyledAttributes(attrs);
                color = typedArray.getColor(0, ContextCompat.getColor(this, R.color.white));
            }
            getWindow().setNavigationBarColor(color);
        }
        toolbar.setEbook(mEbook);
        toolbar.setTitle(titleEbook);
        updateDownloadProgress(mEbook);
    }

    @Override
    public void showMediaController() {
        mediaControllerView.show();
    }

    @Override
    public void startContentHighlightActivity() {
        if (mSpineReferenceList.isEmpty()) {
            Log.d(TAG, "startContentHighlightActivity: >>>");
            showErrorDialog();
            return;
        }
        if (getResources().getBoolean(R.bool.isTablet)) {
            Bundle bundle = new Bundle();
            bundle.putString(CHAPTER_SELECTED, mSpineReferenceList.get(mChapterPosition).href);
            bundle.putString(FolioReader.INTENT_BOOK_ID, mBookId);
            bundle.putString(Constants.BOOK_TITLE, bookFileName);
            bundle.putString(Constants.BOOK_FILE_PATH, ebookFilePath);
            ContentHighlightTabletFragment contentHighlightTabletFragment = new ContentHighlightTabletFragment();
            contentHighlightTabletFragment.setArguments(bundle);
            contentHighlightTabletFragment.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialog);
            contentHighlightTabletFragment.show(getSupportFragmentManager(), "");
        } else {
            Intent intent = new Intent(FolioActivity.this, ContentHighlightActivity.class);
            intent.putExtra(CHAPTER_SELECTED, mSpineReferenceList.get(mChapterPosition).href);
            intent.putExtra(FolioReader.INTENT_BOOK_ID, mBookId);
            intent.putExtra(Constants.BOOK_TITLE, bookFileName);
            intent.putExtra(Constants.BOOK_FILE_PATH, ebookFilePath);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTOCClickedEvent(TOCClickedEvent event) {
        String selectedChapterHref = event.getHref();
        for (Link spine : mSpineReferenceList) {
            if (selectedChapterHref.contains(spine.href)) {
                Log.d(TAG, "onTOCClickedEvent: >>>" + selectedChapterHref);
                mChapterPosition = mSpineReferenceList.indexOf(spine);
                mFolioPageViewPager.setCurrentItem(mChapterPosition);
                FolioPageFragment folioPageFragment = (FolioPageFragment)
                        mFolioPageFragmentAdapter.getItem(mChapterPosition);
                folioPageFragment.showLoading();

                if (folioPageFragment.isCurrentFragment()) {
                    mSelectedChapterHref = selectedChapterHref;
                    folioPageFragment.scrollToAnchorId(selectedChapterHref);
                } else {
                    mSelectedChapterHref = selectedChapterHref;
                }
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHighlightClickedEvent(HighlightClickedEvent event) {
        HighlightV2 highlightV2 = event.getHighlight();
        mFolioPageViewPager.setCurrentItem(highlightV2.getPageIndex());
        FolioPageFragment folioPageFragment = (FolioPageFragment)
                mFolioPageFragmentAdapter.getItem(highlightV2.getPageIndex());
        folioPageFragment.scrollToHighlightId(highlightV2.getRange());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadInterruptedBookEvent(OnDownloadInterruptedBookEvent event) {
        if (event.getEbook().isDownloadFailed()) {
            Log.d(TAG, "onDownloadInterruptedBookEvent: >>>>>>");
            createPausedDownloadDialog(this,
                    getResources().getString(R.string.text_failed_dialog_title),
                    getResources().getString(R.string.text_failed_dialog_message),
                    getResources().getString(R.string.cancel),
                    getResources().getString(R.string.delete),
                    getResources().getString(R.string.try_again),
                    new DialogCreator.PausedDialogCallback() {
                        @Override
                        public void onAbort() {
                            getPresenter().handleAbortDownload(FolioActivity.this, event.getEbook());
                        }

                        @Override
                        public void onResume() {
                            getPresenter().resumeEbook(FolioActivity.this, event.getEbook());
                        }
                    });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnableDownloadEvent(UnableDownloadEvent event) {
        if (event.getErrorType().equals(UnableDownloadEvent.DownloadErrorType.DISCONNECTED)) {
            final Ebook ebook = LibraryTable.getEbook(Integer.valueOf(mBookId));
            toolbar.setEbook(ebook);
            toolbar.updateDownloadView(ebook);
        }
    }

    private void initBook(String mEpubFileName, int mEpubRawId, String mEpubFilePath, EpubSourceType mEpubSourceType) {
        try {
            int portNumber = getIntent().getIntExtra(Config.INTENT_PORT, Constants.PORT_NUMBER);
            mEpubServer = EpubServerSingleton.getEpubServerInstance(portNumber);
            mEpubServer.start();
            String path = FileUtil.saveEpubFileAndLoadLazyBook(FolioActivity.this, mEpubSourceType, mEpubFilePath,
                    mEpubRawId, mEpubFileName);
            addEpub(path);

            String urlString = Constants.LOCALHOST + bookFileName + "/manifest";
            mMainPresenter.parseManifest(urlString);

        } catch (IOException e) {
            Log.e(LOG_TAG, "initBook failed", e);
        }
    }

    private void addEpub(String path) throws IOException {
        Container epubContainer = new EpubContainer(path);
        mEpubServer.addEpub(epubContainer, "/" + bookFileName);
        getEpubResource();
    }

    private void addEbook(String path) {
        if (path == null) {
            showErrorDialog();
            return;
        }
        Container epubContainer = new DirectoryContainer(path);
        EpubParser parser = new EpubParser(epubContainer);
        onLoadPublicationCustom(parser.parseEpubFile("/" + bookFileName));
    }


    private void showErrorDialog() {
        DialogFactory.createEbookErrorDialog(this, getResources().getString(R.string.ebook_error_msg), new DialogFactory.DialogCallback() {
            @Override
            public void exitReader() {
                finish();
            }
        });
    }

    private void getEpubResource() {
    }

    @Override
    public void onDirectionChange(@NonNull Config.Direction newDirection) {
        Log.v(LOG_TAG, "-> onDirectionChange");

        FolioPageFragment folioPageFragment = getCurrentFragment();
        if (folioPageFragment == null) return;

        entryReadPosition = folioPageFragment.getLastReadPosition();

        direction = newDirection;
        mIsDirectionChanged = true;

        mFolioPageViewPager.setDirection(newDirection);
        if (mEpubSourceType.equals(EpubSourceType.ENCRYPTED_FILE)) {
            mFolioPageFragmentAdapter = new FolioPageFragmentAdapter(getSupportFragmentManager(),
                    mSpineReferenceList, ebookFilePath, mBookId, bookFileName, contentKey, userKey,
                    mEpubSourceType, this);
        } else {
            mFolioPageFragmentAdapter = new FolioPageFragmentAdapter(getSupportFragmentManager(),
                    mSpineReferenceList, bookFileName, mBookId, mEpubSourceType);
        }
        mFolioPageViewPager.setAdapter(mFolioPageFragmentAdapter);
        mFolioPageViewPager.setCurrentItem(mChapterPosition);
        mFolioPageFragmentAdapter.notifyDataSetChanged();

    }

    @Override
    public void showConfigBottomSheetDialogFragment() {
        new ConfigDialogFragment().show(getSupportFragmentManager(),
                ConfigDialogFragment.class.getSimpleName());
    }

    @Override
    public void hideOrShowToolBar() {
        toolbar.showOrHideIfVisible();
    }

    @Override
    public void hideToolBarIfVisible() {

    }

    @Override
    public void setPagerToPosition(String href) {
        String anchor = "";
        if (href.contains("#")) {
            anchor = href.substring(href.lastIndexOf("#") + 1);
            href = href.substring(0, href.indexOf("#"));
        }

        href = href.substring(href.indexOf(bookFileName + "/") + bookFileName.length() + 1);

        String selectedChapterHref = "/" + href + "#" + anchor;
        for (Link spine : mSpineReferenceList) {
            if (selectedChapterHref.contains(spine.href)) {
                mChapterPosition = mSpineReferenceList.indexOf(spine);
                mFolioPageViewPager.setCurrentItem(mChapterPosition);

                FolioPageFragment folioPageFragment = (FolioPageFragment)
                        mFolioPageFragmentAdapter.getItem(mChapterPosition);
                if (!folioPageFragment.isCurrentFragment())
                    folioPageFragment.showLoading();
                mSelectedChapterHref = selectedChapterHref;
                break;
            }
        }
    }

    @Override
    public ReadPosition getEntryReadPosition() {
        if (entryReadPosition != null) {
            ReadPosition tempReadPosition = entryReadPosition;
            entryReadPosition = null;
            return tempReadPosition;
        }
        return null;
    }

    @Override
    public void goToChapter(String href) {
        href = href.substring(href.indexOf(bookFileName + "/") + bookFileName.length() + 1);
        for (Link spine : mSpineReferenceList) {
            if (spine.href.contains(href)) {
                mChapterPosition = mSpineReferenceList.indexOf(spine);
                mFolioPageViewPager.setCurrentItem(mChapterPosition);
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (outState != null)
            outState.putParcelable(BUNDLE_READ_POSITION_CONFIG_CHANGE, lastReadPosition);

        if (mEpubServer != null) {
            mEpubServer.stop();
        }
        Log.d(TAG, "onDestroy: >>>");
        if (isOnlineReading() && !isDownloadingBook()) {
            getPresenter().deleteCacheData(getApplicationContext(), mEbook);
        }
        long timeRead = TimeUnit.SECONDS.toSeconds(FileUtil.getCurrentTimeStamp() - SharedPreferenceUtil.getSharedPreferencesLong(this, titleEbook, 1526620402));
        googleAnalytic.sendEvent(AnalyticViewName.eBook_reading_duration, AnalyticViewName.reading_duration, titleEbook, timeRead);
    }

    @Override
    public int getChapterPosition() {
        return mChapterPosition;
    }

    @Override
    public void onLoadPublication(EpubPublicationCustom publication) {
        mSpineReferenceList.addAll(publication.spines);

        if (mBookId == null) {
            if (publication.metadata.identifier != null) {
                mBookId = publication.metadata.identifier;
            } else {
                if (publication.metadata.title != null) {
                    mBookId = String.valueOf(publication.metadata.title.hashCode());
                } else {
                    mBookId = String.valueOf(bookFileName.hashCode());
                }
            }
        }
        configFolio();
    }

    public void onLoadPublicationCustom(EpubPublicationCustom publication) {
        mSpineReferenceList.addAll(publication.spines);

        if (mBookId == null) {
            if (publication.metadata.identifier != null) {
                mBookId = publication.metadata.identifier;
            } else {
                if (publication.metadata.title != null) {
                    mBookId = String.valueOf(publication.metadata.title.hashCode());
                } else {
                    mBookId = String.valueOf(bookFileName.hashCode());
                }
            }
        }
        configFolio();
    }

    private void configFolio() {
        mFolioPageViewPager = (DirectionalViewpager) findViewById(R.id.folioPageViewPager);
        // Replacing with addOnPageChangeListener(), onPageSelected() is not invoked
        mFolioPageViewPager.setOnPageChangeListener(new DirectionalViewpager.OnPageChangeListener() {
            int lastPage = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (lastPage > position) {
                    //User Move to left
                    wasScrollLeft = true;
                } else if (lastPage < position) {
                    //User Move to right
                    wasScrollLeft = false;
                }
                lastPage = position;
            }

            @Override
            public void onPageSelected(int position) {
                if (position != 0 && mSpineReferenceList != null && position != mSpineReferenceList.size() - 1)
                    showLoading();
                EventBus.getDefault().post(new MediaOverlayPlayPauseEvent(
                        mSpineReferenceList.get(mChapterPosition).href, false, true));
                mediaControllerView.setPlayButtonDrawable();
                mChapterPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == SCROLL_STATE_DRAGGING) {
                    isScrolling = true;
                }
            }
        });

        if (mSpineReferenceList != null) {

            mFolioPageViewPager.setDirection(direction);
            if (mEpubSourceType.equals(EpubSourceType.ENCRYPTED_FILE)) {
                mFolioPageFragmentAdapter = new FolioPageFragmentAdapter(getSupportFragmentManager(),
                        mSpineReferenceList, ebookFilePath, mBookId, bookFileName, contentKey, userKey,
                        mEpubSourceType, this);
            } else {
                mFolioPageFragmentAdapter = new FolioPageFragmentAdapter(getSupportFragmentManager(),
                        mSpineReferenceList, bookFileName, mBookId, mEpubSourceType);
            }
            mFolioPageViewPager.setAdapter(mFolioPageFragmentAdapter);

            ReadPosition readPosition;
            if (savedInstanceState == null) {
                readPosition = getIntent().getParcelableExtra(FolioActivity.EXTRA_READ_POSITION);
                entryReadPosition = readPosition;
            } else {
                readPosition = savedInstanceState.getParcelable(BUNDLE_READ_POSITION_CONFIG_CHANGE);
                lastReadPosition = readPosition;
            }
            mFolioPageViewPager.setCurrentItem(getChapterIndex(readPosition));
        }
    }

    /**
     * Returns the index of the chapter by following priority -
     * 1. id
     * 2. href
     * 3. index
     *
     * @param readPosition Last read position
     * @return index of the chapter
     */
    private int getChapterIndex(ReadPosition readPosition) {
        if (readPosition == null) {
            return 0;

        } else if (!TextUtils.isEmpty(readPosition.getChapterId())) {
            return getChapterIndex("id", readPosition.getChapterId());

        } else if (!TextUtils.isEmpty(readPosition.getChapterHref())) {
            return getChapterIndex("href", readPosition.getChapterHref());

        } else if (readPosition.getChapterIndex() > -1
                && readPosition.getChapterIndex() < mSpineReferenceList.size()) {
            return readPosition.getChapterIndex();
        }

        return 0;
    }

    private int getChapterIndex(String caseString, String value) {
        for (int i = 0; i < mSpineReferenceList.size(); i++) {
            switch (caseString) {
                case "id":
                    if (mSpineReferenceList.get(i).getId().equals(value))
                        return i;
                case "href":
                    if (mSpineReferenceList.get(i).getOriginalHref().equals(value))
                        return i;
            }
        }
        return 0;
    }

    /**
     * If called, this method will occur after onStop() for applications targeting platforms
     * starting with Build.VERSION_CODES.P. For applications targeting earlier platform versions
     * this method will occur before onStop() and there are no guarantees about whether it will
     * occur before or after onPause()
     *
     * @see Activity#onSaveInstanceState(Bundle) of Build.VERSION_CODES.P
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v(LOG_TAG, "-> onSaveInstanceState");
        this.outState = outState;

        outState.putBoolean(BUNDLE_TOOLBAR_IS_VISIBLE, toolbar.getVisible());
    }

    @Override
    public void storeLastReadPosition(ReadPosition lastReadPosition) {
        Log.v(LOG_TAG, "-> storeLastReadPosition");
        this.lastReadPosition = lastReadPosition;
    }

    @Override
    public ReadPosition getLastReadPosition() {
        return this.lastReadPosition;
    }

    @Override
    public void showSinglePage(String href) {
        if (mImageClicked) return;
        mImageClicked = true;
        String idref = href.substring(href.indexOf(bookFileName + "/") + bookFileName.length() + 1, href.lastIndexOf("."));
        Log.d(TAG, "showSinglePage: >>>" + idref);
        for (CustomLink spine : mSpineReferenceList) {
            if (spine.href.contains(idref) && spine.linear.equalsIgnoreCase("no")) {

                String mimeType;
                if (spine.typeLink.equalsIgnoreCase(getString(R.string.xhtml_mime_type))) {
                    mimeType = getString(R.string.xhtml_mime_type);
                } else {
                    mimeType = getString(R.string.html_mime_type);
                }
                ImageViewerFragment.startShowImage(href,
                        mBookId,
                        mimeType,
                        contentKey,
                        getDownloadInfo(),
                        getSupportFragmentManager());
                break;
            }
        }
    }

    @Override
    public LoadingView getLoadingView() {
        return loadingView;
    }

    @Override
    public void updateEbook(Ebook ebook) {
        contentKey = ebook.getContentKey();
        mEbook = ebook;
        if (mFolioPageFragmentAdapter != null) {
            mFolioPageFragmentAdapter.setContentKey(ebook.getContentKey());
        }
    }

    private void setConfig(Bundle savedInstanceState) {

        Config config;
        Config intentConfig = getIntent().getParcelableExtra(Config.INTENT_CONFIG);
        boolean overrideConfig = getIntent().getBooleanExtra(Config.EXTRA_OVERRIDE_CONFIG, false);
        Config savedConfig = AppUtil.getSavedConfig(this);

        if (savedInstanceState != null) {
            config = savedConfig;

        } else if (savedConfig == null) {
            if (intentConfig == null) {
                config = new Config();
            } else {
                config = intentConfig;
            }

        } else {
            if (intentConfig != null && overrideConfig) {
                config = intentConfig;
            } else {
                config = savedConfig;
            }
        }

        // Code would never enter this if, just added for any unexpected error
        // and to avoid lint warning
        if (config == null)
            config = new Config();

        AppUtil.saveConfig(this, config);
        direction = config.getDirection();
    }

    @Override
    public void play() {
        EventBus.getDefault().post(new MediaOverlayPlayPauseEvent(mSpineReferenceList.get(mChapterPosition).href, true, false));
    }

    @Override
    public void pause() {
        EventBus.getDefault().post(new MediaOverlayPlayPauseEvent(mSpineReferenceList.get(mChapterPosition).href, false, false));
    }

    @Override
    public void onError() {
    }

    private void setupBook() {
        if (!mIsOnlineReading || !Util.isOnline(this)) {
            if (mEpubSourceType.equals(EpubSourceType.ENCRYPTED_FILE)) {
                bookFileName = mBookId;
                initBook();
            } else {
                bookFileName = FileUtil.getEpubFilename(this, mEpubSourceType, mEpubFilePath, mEpubRawId);
                initBook(bookFileName, mEpubRawId, mEpubFilePath, mEpubSourceType);
            }
        } else if (mEpubSourceType.equals(EpubSourceType.ENCRYPTED_FILE)) {
            bookFileName = mBookId;
            // Reading ONLINE
            downloadContent();
        }
    }

    private void downloadContent() {
        getPresenter().downloadContent(this.getApplicationContext(), mEbook, mDownloadInfo);
    }

    private void initBook() {
        addEbook(ebookFilePath);
    }

    private FolioPageFragment getCurrentFragment() {

        if (mFolioPageFragmentAdapter != null && mFolioPageViewPager != null) {
            return (FolioPageFragment) mFolioPageFragmentAdapter
                    .getItem(mFolioPageViewPager.getCurrentItem());
        } else {
            return null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFinishedDownloadContent(FinishDownloadContentEvent event) {
        Log.d(TAG, "onFinishedDownloadContent: >>>");
        if (mEbook.getId() == event.getEbook().getId()) {
            mEbook = event.getEbook();
            ebookFilePath = mEbook.getFilePath();
            initBook();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadBasicFileErrorEvent(DownloadBasicFileErrorEvent event) {
        final boolean isHttpError = event.isHttpError();
        final String title;
        final String message;
        if (!isHttpError) {
            title = getResources().getString(R.string.download_error_from_offline_title);
            message = getResources().getString(R.string.download_error_from_offline_message);
        } else {
            title = getResources().getString(R.string.download_error_from_file_title);
            message = getResources().getString(R.string.download_error_from_file_message);
        }
        DialogCreator.createDownLoadFailDialog(this,
                title,
                message,
                getResources().getString(R.string.btn_ok),
                new DialogCreator.MessageDialogCallback() {
                    @Override
                    public void onClick() {
                        finish();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.WRITE_EXTERNAL_STORAGE_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupBook();
                } else {
                    Toast.makeText(this, getString(R.string.cannot_access_epub_message), Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

    @Override
    public Config.Direction getDirection() {
        return direction;
    }

    public interface ItemSelectedListener {
        void onItemSelected();
    }
}