package com.folioreader.view;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.model.event.ReloadDataEvent;
import com.folioreader.ui.folio.activity.FolioActivityCallback;
import com.folioreader.util.AppUtil;
import com.folioreader.util.UiUtil;

import org.greenrobot.eventbus.EventBus;

import com.sap_press.rheinwerk_reader.googleanalytics.AnalyticViewName;
import com.sap_press.rheinwerk_reader.googleanalytics.GoogleAnalyticManager;

public class ConfigDialogFragment extends AppCompatDialogFragment implements View.OnClickListener {

    public static final int DAY_BUTTON = 30;
    public static final int NIGHT_BUTTON = 31;
    private static final int FADE_DAY_NIGHT_MODE = 500;
    private static final String TAG = ConfigDialogFragment.class.getSimpleName();

    private int FONT_ANDADA = 1;
    private int FONT_LATO = 2;
    private int FONT_LORA = 3;
    private int FONT_RALEWAY = 4;

    private boolean mIsNightMode = false;


    private RelativeLayout mContainer;
    private ImageView mDayButton;
    private ImageView mNightButton;
    private LinearLayout dayContainer;
    private LinearLayout nightContainer;
    private TextView dayText;
    private TextView nightText;
    private SeekBar mFontSizeSeekBar;
    private View mDialogView;
    private ImageView smallFont;
    private ImageView bigFont;
    private StyleableTextView verticalText;
    private StyleableTextView horizontalText;

    private FolioActivityCallback activityCallback;
    private Config mConfig;
    private GoogleAnalyticManager googleAnalyticManager;
    private Config.Direction mCurrentDirection;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            window.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
            if (tabletSize) {
                dialog.getWindow().setLayout(width / 3 * 2, WindowManager.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setGravity(Gravity.TOP | Gravity.RIGHT);
            } else {
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setGravity(Gravity.TOP);
            }
            setStyle(DialogFragment.STYLE_NORMAL, R.style.MyDialogActivity);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(STYLE_NO_TITLE);
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.view_config_dialog, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDialogView = view;
        mConfig = AppUtil.getSavedConfig(getActivity());
        googleAnalyticManager = new GoogleAnalyticManager(getContext());
        initViews();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDialogView.getViewTreeObserver().addOnGlobalLayoutListener(null);
    }

    private void initViews() {
        inflateView();
        configFonts();
        mFontSizeSeekBar.setProgress(mConfig.getFontSize());
        configSeekBar();
        selectFont(mConfig.getFont(), false);
        mIsNightMode = mConfig.isNightMode();
        if (mIsNightMode) {
            mContainer.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.night));
            mDayButton.setSelected(false);
            mNightButton.setSelected(true);
            initColorNight();
        } else {
            mContainer.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
            mDayButton.setSelected(true);
            mNightButton.setSelected(false);
            initColorDay();
        }

        activityCallback = (FolioActivityCallback) getActivity();

        if (activityCallback.getDirection() == Config.Direction.HORIZONTAL) {
            horizontalText.setSelected(true);
        } else if (activityCallback.getDirection() == Config.Direction.VERTICAL) {
            verticalText.setSelected(true);
        }

    }

    private void inflateView() {
        mContainer = mDialogView.findViewById(R.id.container);
        mFontSizeSeekBar = mDialogView.findViewById(R.id.seekbar_font_size);
        mDayButton = mDialogView.findViewById(R.id.day_button);
        mNightButton = mDialogView.findViewById(R.id.night_button);
        dayContainer = mDialogView.findViewById(R.id.day_container);
        nightContainer = mDialogView.findViewById(R.id.night_container);
        dayText = mDialogView.findViewById(R.id.day_text);
        nightText = mDialogView.findViewById(R.id.night_text);
        smallFont = mDialogView.findViewById(R.id.small_font);
        bigFont = mDialogView.findViewById(R.id.big_font);
        verticalText = mDialogView.findViewById(R.id.btn_vertical_orentation);
        horizontalText = mDialogView.findViewById(R.id.btn_horizontal_orentation);

        dayContainer.setTag(DAY_BUTTON);
        mDayButton.setTag(DAY_BUTTON);
        nightContainer.setTag(NIGHT_BUTTON);
        mNightButton.setTag(NIGHT_BUTTON);
        dayContainer.setOnClickListener(this);
        mDayButton.setOnClickListener(this);
        nightContainer.setOnClickListener(this);
        mNightButton.setOnClickListener(this);


        if (mConfig.getDirection().equals(Config.Direction.VERTICAL)) {
            mDialogView.findViewById(R.id.btn_vertical_orentation).setSelected(true);
            googleAnalyticManager.sendEvent(AnalyticViewName.select_reading_direction, AnalyticViewName.reading_direction_select, "Vertical");
        } else {
            mDialogView.findViewById(R.id.btn_horizontal_orentation).setSelected(false);
            googleAnalyticManager.sendEvent(AnalyticViewName.select_reading_direction, AnalyticViewName.reading_direction_select, "Horizontal");
        }

    }


    private void configFonts() {
        mDialogView.findViewById(R.id.btn_font_andada).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFont(Constants.FONT_ANDADA, true);
                googleAnalyticManager.sendEvent(AnalyticViewName.select_font, AnalyticViewName.font_select, "FONT_ANDADA");
            }
        });

        mDialogView.findViewById(R.id.btn_font_lato).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFont(Constants.FONT_LATO, true);
                googleAnalyticManager.sendEvent(AnalyticViewName.select_font, AnalyticViewName.font_select, "FONT_LATO");
            }
        });

        mDialogView.findViewById(R.id.btn_font_lora).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFont(Constants.FONT_LORA, true);
                googleAnalyticManager.sendEvent(AnalyticViewName.select_font, AnalyticViewName.font_select, "FONT_LORA");
            }
        });

        mDialogView.findViewById(R.id.btn_font_raleway).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFont(Constants.FONT_RALEWAY, true);
                googleAnalyticManager.sendEvent(AnalyticViewName.select_font, AnalyticViewName.font_select, "FONT_RALEWAY");
            }
        });


        mDialogView.findViewById(R.id.btn_horizontal_orentation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConfig = AppUtil.getSavedConfig(getActivity());
                if (mConfig == null
                        || mConfig.getDirection() == Config.Direction.HORIZONTAL
                        || !mConfig.isEnableDirection() || mCurrentDirection == Config.Direction.HORIZONTAL) return;
                mCurrentDirection = Config.Direction.HORIZONTAL;
                mConfig.setEnableDirection(false);
                mConfig.setDirection(Config.Direction.HORIZONTAL);
                AppUtil.saveConfig(getActivity(), mConfig);
                activityCallback.onDirectionChange(Config.Direction.HORIZONTAL);
                horizontalText.setSelected(true);
                verticalText.setSelected(false);
                verticalText.setTextColor(getActivity().getResources().getColor(mIsNightMode ? R.color.app_gray : R.color.black));
                horizontalText.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
            }
        });

        mDialogView.findViewById(R.id.btn_vertical_orentation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConfig = AppUtil.getSavedConfig(getActivity());
                if (mConfig == null
                        || mConfig.getDirection() == Config.Direction.VERTICAL
                        || !mConfig.isEnableDirection() || mCurrentDirection == Config.Direction.VERTICAL) return;
                mCurrentDirection = Config.Direction.VERTICAL;
                mConfig.setEnableDirection(false);
                mConfig.setDirection(Config.Direction.VERTICAL);
                AppUtil.saveConfig(getActivity(), mConfig);
                activityCallback.onDirectionChange(Config.Direction.VERTICAL);

                horizontalText.setSelected(false);
                verticalText.setSelected(true);
                verticalText.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                horizontalText.setTextColor(getActivity().getResources().getColor(mIsNightMode ? R.color.app_gray : R.color.black));
            }
        });
    }

    private void selectFont(int selectedFont, boolean isReloadNeeded) {
        mConfig = AppUtil.getSavedConfig(getActivity());
        if (mConfig.getFont() == selectedFont) return;
        disableDirection();

        Resources resources = getActivity().getResources();
        int colorUnselected = mIsNightMode ? R.color.grey_color : R.color.black;

        ((StyleableTextView) mDialogView.findViewById(R.id.btn_font_andada)).setTextColor(resources.getColor(FONT_ANDADA == selectedFont ? R.color.colorPrimary : colorUnselected));
        ((StyleableTextView) mDialogView.findViewById(R.id.btn_font_lato)).setTextColor(resources.getColor(FONT_LATO == selectedFont ? R.color.colorPrimary : colorUnselected));
        ((StyleableTextView) mDialogView.findViewById(R.id.btn_font_lora)).setTextColor(resources.getColor(FONT_LORA == selectedFont ? R.color.colorPrimary : colorUnselected));
        ((StyleableTextView) mDialogView.findViewById(R.id.btn_font_raleway)).setTextColor(resources.getColor(FONT_RALEWAY == selectedFont ? R.color.colorPrimary : colorUnselected));

        mConfig.setFont(selectedFont);
        //if (mConfigDialogCallback != null) mConfigDialogCallback.onConfigChange();
        if (isAdded() && isReloadNeeded) {
            AppUtil.saveConfig(getActivity(), mConfig);
            EventBus.getDefault().post(new ReloadDataEvent());
        }
    }

    private void toggleBlackTheme() {
        mConfig = AppUtil.getSavedConfig(getActivity());
        disableDirection();

        int day = getResources().getColor(R.color.white);
        int night = getResources().getColor(R.color.night);
        int darkNight = getResources().getColor(R.color.dark_night);
        final int diffNightDark = night - darkNight;

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                mIsNightMode ? night : day, mIsNightMode ? day : night);
        colorAnimation.setDuration(FADE_DAY_NIGHT_MODE);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int value = (int) animator.getAnimatedValue();
                mContainer.setBackgroundColor(value);
            }
        });

        colorAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mIsNightMode = !mIsNightMode;
                mConfig = AppUtil.getSavedConfig(getActivity());
                mConfig.setNightMode(mIsNightMode);
                AppUtil.saveConfig(getActivity(), mConfig);
                EventBus.getDefault().post(new ReloadDataEvent());
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });

        colorAnimation.setDuration(FADE_DAY_NIGHT_MODE);
        colorAnimation.start();
    }

    private void disableDirection() {
        if (mConfig.isEnableDirection()) {
            mConfig.setEnableDirection(false);
            AppUtil.saveConfig(getActivity(), mConfig);
        }
    }

    private void configSeekBar() {
        Drawable thumbDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.seekbar_thumb);
        UiUtil.setColorToImage(getActivity(), R.color.colorPrimary, (thumbDrawable));
        UiUtil.setColorToImage(getActivity(), R.color.grey_color, mFontSizeSeekBar.getProgressDrawable());
        mFontSizeSeekBar.setThumb(thumbDrawable);

        mFontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mConfig = AppUtil.getSavedConfig(getActivity());
                mConfig.setFontSize(progress);
                mConfig.setEnableDirection(false);
                AppUtil.saveConfig(getActivity(), mConfig);
                EventBus.getDefault().post(new ReloadDataEvent());
                googleAnalyticManager.sendEvent(AnalyticViewName.select_font_size, AnalyticViewName.fontsize_select, "" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (((Integer) v.getTag())) {
            case DAY_BUTTON:
                if (mIsNightMode) {
                    toggleBlackTheme();
                    mDayButton.setSelected(true);
                    dayText.setSelected(true);
                    mNightButton.setSelected(false);
                    nightText.setSelected(false);
                    setToolBarColor();
                    setAudioPlayerBackground();
                    initColorDay();
                    googleAnalyticManager.sendEvent(AnalyticViewName.select_font, AnalyticViewName.font_select, "Day");
                }
                break;
            case NIGHT_BUTTON:
                if (!mIsNightMode) {
                    toggleBlackTheme();
                    mDayButton.setSelected(false);
                    dayText.setSelected(false);
                    mNightButton.setSelected(true);
                    nightText.setSelected(true);
                    setToolBarColor();
                    setAudioPlayerBackground();
                    initColorNight();
                    googleAnalyticManager.sendEvent(AnalyticViewName.choose_topic, AnalyticViewName.theme_select, "Night");
                }
                break;
            default:
                break;
        }
    }

    private void initColorNight() {
        Resources resources = getActivity().getResources();
        UiUtil.setColorToImage(getActivity(), R.color.colorPrimary, mNightButton.getDrawable());
        UiUtil.setColorToImage(getActivity(), R.color.app_gray, mDayButton.getDrawable());
        UiUtil.setColorToImage(getActivity(), R.color.app_gray, smallFont.getDrawable());
        UiUtil.setColorToImage(getActivity(), R.color.app_gray, bigFont.getDrawable());

        ((StyleableTextView) mDialogView.findViewById(R.id.btn_font_andada)).setTextColor(resources.getColor(FONT_ANDADA == mConfig.getFont() ? R.color.colorPrimary : R.color.grey_color));
        ((StyleableTextView) mDialogView.findViewById(R.id.btn_font_lato)).setTextColor(resources.getColor(FONT_LATO == mConfig.getFont() ? R.color.colorPrimary : R.color.grey_color));
        ((StyleableTextView) mDialogView.findViewById(R.id.btn_font_lora)).setTextColor(resources.getColor(FONT_LORA == mConfig.getFont() ? R.color.colorPrimary : R.color.grey_color));
        ((StyleableTextView) mDialogView.findViewById(R.id.btn_font_raleway)).setTextColor(resources.getColor(FONT_RALEWAY == mConfig.getFont() ? R.color.colorPrimary : R.color.grey_color));

        dayText.setTextColor(resources.getColor(R.color.grey_color));
        nightText.setTextColor(resources.getColor(R.color.colorPrimary));

        if (mConfig.getDirection().equals(Config.Direction.VERTICAL)) {
            verticalText.setTextColor(resources.getColor(R.color.colorPrimary));
            horizontalText.setTextColor(resources.getColor(R.color.app_gray));
        } else {
            verticalText.setTextColor(resources.getColor(R.color.app_gray));
            horizontalText.setTextColor(resources.getColor(R.color.colorPrimary));
        }
    }

    private void initColorDay() {
        Resources resources = getActivity().getResources();
        UiUtil.setColorToImage(getActivity(), R.color.black, mNightButton.getDrawable());
        UiUtil.setColorToImage(getActivity(), R.color.colorPrimary, mDayButton.getDrawable());
        UiUtil.setColorToImage(getActivity(), R.color.black, smallFont.getDrawable());
        UiUtil.setColorToImage(getActivity(), R.color.black, bigFont.getDrawable());

        ((StyleableTextView) mDialogView.findViewById(R.id.btn_font_andada)).setTextColor(resources.getColor(FONT_ANDADA == mConfig.getFont() ? R.color.colorPrimary : R.color.black));
        ((StyleableTextView) mDialogView.findViewById(R.id.btn_font_lato)).setTextColor(resources.getColor(FONT_LATO == mConfig.getFont() ? R.color.colorPrimary : R.color.black));
        ((StyleableTextView) mDialogView.findViewById(R.id.btn_font_lora)).setTextColor(resources.getColor(FONT_LORA == mConfig.getFont() ? R.color.colorPrimary : R.color.black));
        ((StyleableTextView) mDialogView.findViewById(R.id.btn_font_raleway)).setTextColor(resources.getColor(FONT_RALEWAY == mConfig.getFont() ? R.color.colorPrimary : R.color.black));

        dayText.setTextColor(resources.getColor(R.color.colorPrimary));
        nightText.setTextColor(resources.getColor(R.color.black));

        if (mConfig.getDirection().equals(Config.Direction.VERTICAL)) {
            verticalText.setTextColor(resources.getColor(R.color.colorPrimary));
            horizontalText.setTextColor(resources.getColor(R.color.black));
        } else {
            verticalText.setTextColor(resources.getColor(R.color.black));
            horizontalText.setTextColor(resources.getColor(R.color.colorPrimary));
        }
    }

    private void setToolBarColor() {
        if (mIsNightMode) {
            ((FolioToolbar) ((Activity) getContext()).findViewById(R.id.toolbar)).setDayModeConfig();
        } else {
            ((FolioToolbar) ((Activity) getContext()).findViewById(R.id.toolbar)).setNightModeConfig();
        }
    }

    private void setAudioPlayerBackground() {
        if (mIsNightMode) {
            ((Activity) getContext()).
                    findViewById(R.id.container).
                    setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        } else {
            ((Activity) getContext()).
                    findViewById(R.id.container).
                    setBackgroundColor(ContextCompat.getColor(getContext(), R.color.night));
        }
    }
}
