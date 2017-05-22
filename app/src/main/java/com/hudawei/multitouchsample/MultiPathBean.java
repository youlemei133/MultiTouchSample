package com.hudawei.multitouchsample;

import android.graphics.Path;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hudawei on 2017/5/19.
 */

public class MultiPathBean {
    List<PathsBean> mPathsBeanList;
    int mLayerIndex;

    public MultiPathBean() {
        mPathsBeanList = new ArrayList<>();
        mPathsBeanList.add(new PathsBean(0, 0));
    }

    /**
     * 添加一个路径,画草稿时调用
     *
     * @param path
     */
    public void addPath(@NonNull Path path) {
        PathsBean lastPathsBean = getLastPathsBean();
        if (lastPathsBean != null) {
            lastPathsBean.mPaths.add(path);
            mLayerIndex = mPathsBeanList.size() - 1;
        }
    }

    /**
     * 后退操作时调用
     */
    public void pre() {
        PathsBean pathsBean = mPathsBeanList.get(mLayerIndex);
        if (pathsBean != null) {
            List<Path> paths = pathsBean.mPaths;
            if (paths.size() > 0) {
                Path path = paths.get(paths.size() - 1);
                pathsBean.mRecyclePaths.add(path);
                paths.remove(path);
            } else {
                if (mLayerIndex != 0) {
                    mLayerIndex--;
                    pre();
                }
            }
        }
    }

    /**
     * 前进时操作
     */
    public void next() {
        PathsBean pathsBean = mPathsBeanList.get(mLayerIndex);
        if (pathsBean != null) {
            List<Path> paths = pathsBean.mRecyclePaths;
            if (paths.size() > 0) {
                Path path = paths.get(paths.size() - 1);
                pathsBean.mPaths.add(path);
                paths.remove(path);
            } else {
                if (mLayerIndex != mPathsBeanList.size() - 1) {
                    mLayerIndex++;
                    next();
                }
            }
        }
    }

    /**
     * 移动画布时的操作
     *
     * @param offsetX x方向的偏移量
     * @param offsetY y方向的偏移量
     */
    public void translateCanvas(float offsetX, float offsetY) {
        PathsBean pathsBean = getLastPathsBean();
        if (pathsBean == null)
            return;
        if (pathsBean.mPaths.size() != 0 || pathsBean.mRecyclePaths.size() != 0) {
            PathsBean newPathsBean = new PathsBean(0, 0);
            mPathsBeanList.add(newPathsBean);
        }
        PathsBean prePathsBean = getPreLastPathsBean();
        if (prePathsBean == null)
            return;
        prePathsBean.mOffsetX += offsetX;
        prePathsBean.mOffsetY += offsetY;
    }

    private PathsBean getLastPathsBean() {
        if (mPathsBeanList != null && mPathsBeanList.size() > 0) {
            return mPathsBeanList.get(mPathsBeanList.size() - 1);
        }
        return null;
    }

    private PathsBean getPreLastPathsBean() {
        if (mPathsBeanList != null && mPathsBeanList.size() > 1) {
            return mPathsBeanList.get(mPathsBeanList.size() - 2);
        }
        return null;
    }

}
