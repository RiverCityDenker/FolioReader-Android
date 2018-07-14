package com.folioreader.ui.folio.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.folioreader.Constants;
import com.folioreader.FolioReader;
import com.folioreader.R;
import com.folioreader.ui.folio.activity.FolioActivity;
import com.folioreader.ui.tableofcontents.view.TableOfContentFragment;

import java.util.ArrayList;
import java.util.List;

import static com.folioreader.Constants.CHAPTER_SELECTED;

public class ContentHighlightTabletFragment extends DialogFragment {

    private String mChapterSelected;
    private String mBookId;
    private String mBookTitle;
    private String mBookFilePath;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mChapterSelected = getArguments().getString(CHAPTER_SELECTED);
            mBookId = getArguments().getString(FolioReader.INTENT_BOOK_ID);
            mBookTitle = getArguments().getString(Constants.BOOK_TITLE);
            mBookFilePath = getArguments().getString(Constants.BOOK_FILE_PATH);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog().getWindow() != null)
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (getActivity() != null)
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        boolean tabletSize = getContext().getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            getDialog().getWindow().setGravity(Gravity.TOP | Gravity.START | Gravity.BOTTOM);
            WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
            layoutParams.width = width * 2 / 3;
            getDialog().getWindow().setAttributes(layoutParams);
        }
        return inflater.inflate(R.layout.layout_content_hightlight_tablet, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view) {
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            tabLayout.setTabMode(TabLayout.MODE_FIXED);

        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        TableOfContentFragment contentFragment
                = TableOfContentFragment.newInstance(mChapterSelected,
                mBookTitle, mBookFilePath, new FolioActivity.ItemSelectedListener() {
                    @Override
                    public void onItemSelected() {
                        dismiss();
                    }
                });
        HighlightFragment highlightFragment = HighlightFragment.newInstance(mBookId, mBookTitle, new FolioActivity.ItemSelectedListener() {
            @Override
            public void onItemSelected() {
                dismiss();
            }
        });

        adapter.addFragment(contentFragment, "CONTENTS");
        adapter.addFragment(highlightFragment, "HIGHLIGHTS");
        viewPager.setAdapter(adapter);
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
