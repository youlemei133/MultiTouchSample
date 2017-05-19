package com.hudawei.multitouchsample;

import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hudawei on 2017/5/19.
 *
 */

public class PathsBean {
    List<Path> mPaths;
    List<Path> mRecyclePaths;
    float mOffsetX;
    float mOffsetY;

    public PathsBean(float offsetX, float offsetY) {
        mOffsetX = offsetX;
        mOffsetY = offsetY;
        mPaths = new ArrayList<>();
        mRecyclePaths = new ArrayList<>();
    }
}
