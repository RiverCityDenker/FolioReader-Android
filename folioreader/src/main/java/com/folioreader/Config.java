package com.folioreader;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

/**
 * Configuration class for FolioReader.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class Config implements Parcelable {

    private static final String LOG_TAG = Config.class.getSimpleName();
    public static final String INTENT_CONFIG = "config";
    public static final String EXTRA_OVERRIDE_CONFIG = "com.folioreader.extra.OVERRIDE_CONFIG";
    public static final String CONFIG_FONT = "font";
    public static final String CONFIG_FONT_SIZE = "font_size";
    public static final String CONFIG_IS_NIGHT_MODE = "is_night_mode";
    public static final String CONFIG_IS_THEME_COLOR = "theme_color";
    public static final String CONFIG_IS_TTS = "is_tts";
    public static final String CONFIG_ALLOWED_DIRECTION = "allowed_direction";
    public static final String CONFIG_DIRECTION = "direction";
    public static final String INTENT_PORT = "port";
    private static final AllowedDirection DEFAULT_ALLOWED_DIRECTION = AllowedDirection.ONLY_VERTICAL;
    private static final Direction DEFAULT_DIRECTION = Direction.VERTICAL;
    public static final String CONFIG_DIRECTION_ENABLE = "direction_enable";

    private int font = 3;
    private int fontSize = 2;
    private boolean nightMode;
    private int themeColor = R.color.app_green;
    private boolean showTts = true;
    private AllowedDirection allowedDirection = DEFAULT_ALLOWED_DIRECTION;
    private Direction direction = DEFAULT_DIRECTION;
    private boolean mEnableDirection;

    public Config setEnableDirection(boolean enableDirection) {
        this.mEnableDirection = enableDirection;
        return this;
    }

    public boolean isEnableDirection() {
        return mEnableDirection;
    }

    /**
     * Reading modes available
     */
    public enum AllowedDirection {
        ONLY_VERTICAL, ONLY_HORIZONTAL, VERTICAL_AND_HORIZONTAL
    }

    /**
     * Reading directions
     */
    public enum Direction {
        VERTICAL, HORIZONTAL
    }

    public static final Creator<Config> CREATOR = new Creator<Config>() {
        @Override
        public Config createFromParcel(Parcel in) {
            return new Config(in);
        }

        @Override
        public Config[] newArray(int size) {
            return new Config[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(font);
        dest.writeInt(fontSize);
        dest.writeByte((byte) (nightMode ? 1 : 0));
        dest.writeByte((byte) (mEnableDirection ? 1 : 0));
        dest.writeInt(themeColor);
        dest.writeByte((byte) (showTts ? 1 : 0));
        dest.writeString(allowedDirection.toString());
        dest.writeString(direction.toString());
    }

    protected Config(Parcel in) {
        font = in.readInt();
        fontSize = in.readInt();
        nightMode = in.readByte() != 0;
        mEnableDirection = in.readByte() != 0;
        themeColor = in.readInt();
        showTts = in.readByte() != 0;
        allowedDirection = getAllowedDirectionFromString(LOG_TAG, in.readString());
        direction = getDirectionFromString(LOG_TAG, in.readString());
    }

    public Config() {
    }

    public Config(int font, int fontSize, boolean nightMode, int themeColor, boolean showTts,
                  AllowedDirection allowedDirection, boolean enableDirection, Direction direction) {
        this.font = font;
        this.fontSize = fontSize;
        this.nightMode = nightMode;
        this.mEnableDirection = enableDirection;
        this.themeColor = themeColor;
        this.showTts = showTts;
        setAllowedDirection(allowedDirection);
        setDirection(direction);
    }

    public Config(JSONObject jsonObject) {
        font = jsonObject.optInt(CONFIG_FONT);
        fontSize = jsonObject.optInt(CONFIG_FONT_SIZE);
        nightMode = jsonObject.optBoolean(CONFIG_IS_NIGHT_MODE);
        themeColor = jsonObject.optInt(CONFIG_IS_THEME_COLOR);
        showTts = jsonObject.optBoolean(CONFIG_IS_TTS);
        allowedDirection = getAllowedDirectionFromString(LOG_TAG,
                jsonObject.optString(CONFIG_ALLOWED_DIRECTION));
        direction = getDirectionFromString(LOG_TAG, jsonObject.optString(CONFIG_DIRECTION));
        mEnableDirection = jsonObject.optBoolean(CONFIG_DIRECTION_ENABLE);
    }

    public static Direction getDirectionFromString(String LOG_TAG, String directionString) {

        switch (directionString) {
            case "VERTICAL":
                return Direction.VERTICAL;
            case "HORIZONTAL":
                return Direction.HORIZONTAL;
            default:
                Log.w(LOG_TAG, "-> Illegal argument directionString = `" + directionString
                        + "`, defaulting direction to " + DEFAULT_DIRECTION);
                return DEFAULT_DIRECTION;
        }
    }

    public static AllowedDirection getAllowedDirectionFromString(String LOG_TAG,
                                                                 String allowedDirectionString) {

        switch (allowedDirectionString) {
            case "ONLY_VERTICAL":
                return AllowedDirection.ONLY_VERTICAL;
            case "ONLY_HORIZONTAL":
                return AllowedDirection.ONLY_HORIZONTAL;
            case "VERTICAL_AND_HORIZONTAL":
                return AllowedDirection.VERTICAL_AND_HORIZONTAL;
            default:
                Log.w(LOG_TAG, "-> Illegal argument allowedDirectionString = `"
                        + allowedDirectionString + "`, defaulting allowedDirection to "
                        + DEFAULT_ALLOWED_DIRECTION);
                return DEFAULT_ALLOWED_DIRECTION;
        }
    }

    public int getFont() {
        return font;
    }

    public Config setFont(int font) {
        this.font = font;
        return this;
    }

    public int getFontSize() {
        return fontSize;
    }

    public Config setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public boolean isNightMode() {
        return nightMode;
    }

    public Config setNightMode(boolean nightMode) {
        this.nightMode = nightMode;
        return this;
    }

    public int getThemeColor() {
        return themeColor;
    }

    public Config setThemeColor(int themeColor) {
        this.themeColor = themeColor;
        return this;
    }

    public boolean isShowTts() {
        return showTts;
    }

    public Config setShowTts(boolean showTts) {
        this.showTts = showTts;
        return this;
    }

    public AllowedDirection getAllowedDirection() {
        return allowedDirection;
    }

    /**
     * Set reading direction mode options for users. This method should be called before
     * {@link #setDirection(Direction)} as it has higher preference.
     *
     * @param allowedDirection reading direction mode options for users. Setting to
     *                         {@link AllowedDirection#VERTICAL_AND_HORIZONTAL} users will have
     *                         choice to select the reading direction at runtime.
     */
    public Config setAllowedDirection(AllowedDirection allowedDirection) {

        this.allowedDirection = allowedDirection;

        if (allowedDirection == null) {
            this.allowedDirection = DEFAULT_ALLOWED_DIRECTION;
            direction = DEFAULT_DIRECTION;
            Log.w(LOG_TAG, "-> allowedDirection cannot be null, defaulting " +
                    "allowedDirection to " + DEFAULT_ALLOWED_DIRECTION + " and direction to " +
                    DEFAULT_DIRECTION);

        } else if (allowedDirection == AllowedDirection.ONLY_VERTICAL && direction != Direction.VERTICAL) {
            direction = Direction.VERTICAL;
            Log.w(LOG_TAG, "-> allowedDirection is " + allowedDirection +
                    ", defaulting direction to " + direction);

        } else if (allowedDirection == AllowedDirection.ONLY_HORIZONTAL && direction != Direction.HORIZONTAL) {
            direction = Direction.HORIZONTAL;
            Log.w(LOG_TAG, "-> allowedDirection is " + allowedDirection
                    + ", defaulting direction to " + direction);
        }

        return this;
    }

    public Direction getDirection() {
        return direction;
    }

    /**
     * Set reading direction. This method should be called after
     * {@link #setAllowedDirection(AllowedDirection)} as it has lower preference.
     *
     * @param direction reading direction.
     */
    public Config setDirection(Direction direction) {

        if (allowedDirection == AllowedDirection.VERTICAL_AND_HORIZONTAL && direction == null) {
            this.direction = DEFAULT_DIRECTION;
            Log.w(LOG_TAG, "-> direction cannot be `null` when allowedDirection is " +
                    allowedDirection + ", defaulting direction to " + this.direction);

        } else if (allowedDirection == AllowedDirection.ONLY_VERTICAL && direction != Direction.VERTICAL) {
            this.direction = Direction.VERTICAL;
            Log.w(LOG_TAG, "-> direction cannot be `" + direction + "` when allowedDirection is " +
                    allowedDirection + ", defaulting direction to " + this.direction);

        } else if (allowedDirection == AllowedDirection.ONLY_HORIZONTAL && direction != Direction.HORIZONTAL) {
            this.direction = Direction.HORIZONTAL;
            Log.w(LOG_TAG, "-> direction cannot be `" + direction + "` when allowedDirection is " +
                    allowedDirection + ", defaulting direction to " + this.direction);

        } else {
            this.direction = direction;
        }

        return this;
    }

    @Override
    public String toString() {
        return "Config{" +
                "font=" + font +
                ", fontSize=" + fontSize +
                ", nightMode=" + nightMode +
                ", themeColor=" + themeColor +
                ", showTts=" + showTts +
                ", allowedDirection=" + allowedDirection +
                ", direction=" + direction +
                ", enableDirection=" + mEnableDirection +
                '}';
    }
}


