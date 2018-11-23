package com.folioreader.ui.folio.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.folioreader.ui.custom.CustomLink;
import com.folioreader.ui.folio.activity.FolioActivity;
import com.folioreader.ui.folio.fragment.FolioPageFragment;
import com.sap_press.rheinwerk_reader.logging.FolioLogging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author mahavir on 4/2/16.
 */
public class FolioPageFragmentAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = FolioPageFragmentAdapter.class.getSimpleName();
    private List<CustomLink> mSpineReferences;
    private String mEpubFileName;
    private String mBookId;
    private String contentKey;
    private String userKey;
    private String mEbookFilePath;
    private FolioActivity.EpubSourceType mEpubSourceType;
    private ArrayList<Fragment> fragments;
    private final FolioPageAdapterListener mListener;

    public interface FolioPageAdapterListener {
        void updatePositionList(int position);
    }


    public FolioPageFragmentAdapter(FragmentManager fragmentManager, List<CustomLink> spineReferences,
                                    String epubFileName, String bookId, FolioActivity.EpubSourceType epubSourceType) {
        super(fragmentManager);
        this.mListener = null;
        this.mSpineReferences = spineReferences;
        this.mEpubFileName = epubFileName;
        this.mBookId = bookId;
        this.mEpubSourceType = epubSourceType;
        fragments = new ArrayList<>(Arrays.asList(new Fragment[mSpineReferences.size()]));
    }

    public FolioPageFragmentAdapter(FragmentManager fm, List<CustomLink> spineReferences,
                                    String ebookFilePath, String bookId, String epubFileName,
                                    String contentKey, String userKey,
                                    FolioActivity.EpubSourceType epubSourceType,
                                    FolioPageAdapterListener listener) {
        super(fm);
        this.mListener = listener;
        this.mSpineReferences = spineReferences;
        this.mEbookFilePath = ebookFilePath;
        this.mBookId = bookId;
        this.contentKey = contentKey;
        this.userKey = userKey;
        this.mEpubFileName = epubFileName;
        this.mEpubSourceType = epubSourceType;
        fragments = new ArrayList<>(Arrays.asList(new Fragment[mSpineReferences.size()]));
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        fragments.set(position, null);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        FolioLogging.tag(TAG).d("instantiateItem: >>>" + position);
        mListener.updatePositionList(position);
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragments.set(position, fragment);
        return fragment;
    }

    @Override
    public Fragment getItem(int position) {

        if (mSpineReferences.size() == 0 || position < 0 || position >= mSpineReferences.size())
            return null;
        FolioLogging.tag(TAG).d("getItem: >>>position = " + position);
        Fragment fragment = fragments.get(position);
        if (fragment == null) {
            if (mEpubSourceType.equals(FolioActivity.EpubSourceType.ENCRYPTED_FILE)) {
                FolioLogging.tag(TAG).d("getItem: ENCRYPTED_FILE>>>" + mSpineReferences.get(position));
                fragment = FolioPageFragment.newInstance(position, mEbookFilePath,
                        mSpineReferences.get(position), mBookId, mEpubFileName,
                        contentKey, userKey, mEpubSourceType);
            } else {
                FolioLogging.tag(TAG).d("getItem: NORMAL>>>" + mSpineReferences.get(position));
                fragment = FolioPageFragment.newInstance(position,
                        mEpubFileName, mSpineReferences.get(position), mBookId, mEpubSourceType);
            }
            fragments.set(position, fragment);
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return mSpineReferences.size();
    }

    public void setContentKey(String contentKey) {
        if (contentKey != null)
            this.contentKey = contentKey;
    }
}
