package com.folioreader.util;

import com.folioreader.model.ReadPosition;

/**
 * Created by hale on 11/9/2018.
 */
public class ReadPositionUtil {
    private static ReadPosition readPosition;

    public static ReadPosition getReadPosition() {
        return readPosition;
    }

    public static void setReadPosition(ReadPosition readPosition) {
        ReadPositionUtil.readPosition = readPosition;
    }
}
