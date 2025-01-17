package com.baidu.paddle.fastdeploy.vision.segmentation;

import android.graphics.Bitmap;

import com.baidu.paddle.fastdeploy.FastDeployInitializer;
import com.baidu.paddle.fastdeploy.RuntimeOption;
import com.baidu.paddle.fastdeploy.vision.SegmentationResult;

public class PaddleSegModel {
    protected long mCxxContext = 0; // Context from native.
    protected boolean mInitialized = false;

    public PaddleSegModel() {
        mInitialized = false;
    }

    // Constructor with default runtime option
    public PaddleSegModel(String modelFile,
                          String paramsFile,
                          String configFile) {
        init_(modelFile, paramsFile, configFile, new RuntimeOption());
    }

    // Constructor with custom runtime option
    public PaddleSegModel(String modelFile,
                          String paramsFile,
                          String configFile,
                          RuntimeOption runtimeOption) {
        init_(modelFile, paramsFile, configFile, runtimeOption);
    }

    public boolean init(String modelFile,
                        String paramsFile,
                        String configFile,
                        RuntimeOption runtimeOption) {
        return init_(modelFile, paramsFile, configFile, runtimeOption);
    }

    public boolean release() {
        mInitialized = false;
        if (mCxxContext == 0) {
            return false;
        }
        return releaseNative(mCxxContext);
    }

    public boolean initialized() {
        return mInitialized;
    }

    // Predict without image saving and bitmap rendering.
    public SegmentationResult predict(Bitmap ARGB8888Bitmap) {
        if (mCxxContext == 0) {
            return new SegmentationResult();
        }
        // Only support ARGB8888 bitmap in native now.
        SegmentationResult result = predictNative(mCxxContext, ARGB8888Bitmap,
                false, "", false, 0.5f);
        if (result == null) {
            return new SegmentationResult();
        }
        return result;
    }

    public SegmentationResult predict(Bitmap ARGB8888Bitmap,
                                      boolean rendering,
                                      float weight) {
        if (mCxxContext == 0) {
            return new SegmentationResult();
        }
        // Only support ARGB8888 bitmap in native now.
        SegmentationResult result = predictNative(mCxxContext, ARGB8888Bitmap,
                false, "", rendering, weight);
        if (result == null) {
            return new SegmentationResult();
        }
        return result;
    }

    // Predict with image saving and bitmap rendering (will cost more times)
    public SegmentationResult predict(Bitmap ARGB8888Bitmap,
                                      String savedImagePath,
                                      float weight) {
        if (mCxxContext == 0) {
            return new SegmentationResult();
        }
        // Only support ARGB8888 bitmap in native now.
        SegmentationResult result = predictNative(
                mCxxContext, ARGB8888Bitmap, true,
                savedImagePath,true, weight);
        if (result == null) {
            return new SegmentationResult();
        }
        return result;
    }

    private boolean init_(String modelFile,
                          String paramsFile,
                          String configFile,
                          RuntimeOption runtimeOption) {
        if (!mInitialized) {
            mCxxContext = bindNative(
                    modelFile,
                    paramsFile,
                    configFile,
                    runtimeOption);
            if (mCxxContext != 0) {
                mInitialized = true;
            }
            return mInitialized;
        } else {
            // release current native context and bind a new one.
            if (release()) {
                mCxxContext = bindNative(
                        modelFile,
                        paramsFile,
                        configFile,
                        runtimeOption);
                if (mCxxContext != 0) {
                    mInitialized = true;
                }
                return mInitialized;
            }
            return false;
        }
    }

    // Bind predictor from native context.
    private native long bindNative(String modelFile,
                                   String paramsFile,
                                   String configFile,
                                   RuntimeOption runtimeOption);

    // Call prediction from native context with rendering.
    private native SegmentationResult predictNative(long CxxContext,
                                                    Bitmap ARGB8888Bitmap,
                                                    boolean saveImage,
                                                    String savePath,
                                                    boolean rendering,
                                                    float weight);

    // Release buffers allocated in native context.
    private native boolean releaseNative(long CxxContext);

    // Initializes at the beginning.
    static {
        FastDeployInitializer.init();
    }
}
