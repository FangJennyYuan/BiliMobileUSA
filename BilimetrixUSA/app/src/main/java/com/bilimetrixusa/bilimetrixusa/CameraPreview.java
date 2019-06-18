package com.bilimetrixusa.bilimetrixusa;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private Camera camera;
    private SurfaceHolder holder;
    private List<Camera.Size> mSupportedPreviewSizes;
    private static Camera.Size mPreviewSize;

    /**Purpose: Initializes camera preview with Context, AttributeSet, and int.
     * Precondition: Requires Context and AttributeSet objects.
     * Postcondition: CameraPreview is initialized.
     * @param context
     * @param attrs
     * @param defStyle
     */
    public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**Purpose: Initializes camera preview with Context and AttributeSet.
     * Precondition: Requires Context and AttributeSet objects.
     * Postcondition: CameraPreview is initialized.
     * @param context
     * @param attrs
     */
    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**Purpose: Initializes camera preview with Context.
     * Precondition: Requires Context object.
     * Postcondition: CameraPreview is initialized.
     * @param context
     */
    public CameraPreview(Context context) {
        super(context);
    }

    /**Purpose: Initializes Camera object.
     * Precondition: Camera object must be created.
     * Postcondition: Camera and preview sizes initialized.
     * @param camera
     */
    public void init(Camera camera) {
        this.camera = camera;
        mSupportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
        initSurfaceHolder();
    }

    /**Purpose: Initializes surface holder.
     * Precondition: SurfaceHolder must be created.
     * Postcondition: SurfaceHolder object initialized.
     */
    @SuppressWarnings("deprecation") // needed for < 3.0
    private void initSurfaceHolder() {
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**Purpose: Creates surface for camera.
     * Precondition: SurfaceHolder must be created.
     * Postcondition: Camera initialized with SurfaceHolder.
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera(holder);
    }

    /**Purpose: Initialize camera with SurfaceHolder.
     * Precondition: Camera and SurfaceHolder objects must be created.
     * Postcondition: Camera initialized, parameters set, preview started and displayed.
     * @param holder
     */
    private void initCamera(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            camera.setParameters(parameters);
            camera.startPreview();
        } catch (Exception e) {
            Log.d("CameraPreview","Error setting camera preview");
        }
    }

    /**Purpose: Get the optimal preview size so camera preview is not stretched when displaying
     * before a picture is taken.
     * Precondition: Camera object must be created and initialized.
     * Postcondition: Camera.Size is updated with optimal preview size.
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    /**Purpose: Overrides onMeasure to measure original preview size and gets optimal preview size
     * if there is no supported preview size.
     * Precondition: Requires getOptimalPreviewSize() and mSupportedPreviewSizes must be created.
     * Postcondition: mPreviewSize is updated if there is no mSupportedPreviewSizes.
      * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            Log.d("dimensions", "PREVIEWSIZE:" + mPreviewSize.width + " " + mPreviewSize.height);
        }
    }

    /**Purpose: Overrides surfaceChanged().
     * Precondition: None.
     * Postcondition: None.
     * @param holder
     * @param format
     * @param width
     * @param height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    /**Purpose: Overrides surfaceDestroyed().
     * Precondition: None.
     * Postcondition: None.
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}
}
