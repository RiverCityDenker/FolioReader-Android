package com.folioreader.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.folioreader.R;

public class ImageViewerFragment extends DialogFragment {
    private static final String KEY_EXTRA = "key_extra";
    private SubsamplingScaleImageView imageView;
    private String imagePart;

    public static void startShowImage(String extra, FragmentManager supportFragmentManager) {
        ImageViewerFragment imageViewerFragment = new ImageViewerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ImageViewerFragment.KEY_EXTRA, extra);
        imageViewerFragment.setArguments(bundle);
        imageViewerFragment.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialogStyle);
        imageViewerFragment.show(supportFragmentManager, "");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imagePart = getArguments().getString(KEY_EXTRA);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog().getWindow() != null)
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.layout_image_viewer, container, false);
        imageView = view.findViewById(R.id.imageView);
        imageView.setImage(ImageSource.uri(imagePart));
        imageView.setMinimumDpi(50);
        imageView.setMaximumDpi(250);
        return view;
    }
}
