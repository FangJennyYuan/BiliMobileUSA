package com.bilimetrixusa.bilimetrixusa;

import static android.content.ContentValues.TAG;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static com.bilimetrixusa.bilimetrixusa.CameraHelper.cameraAvailable;
import static com.bilimetrixusa.bilimetrixusa.CameraHelper.getCameraInstance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

public class CameraActivity extends Activity implements PictureCallback {
    protected static final String EXTRA_IMAGE_PATH = "com.bilimetrixusa.bilimetrixusa.CameraActivity.EXTRA_IMAGE_PATH";

    private Camera camera;
    private CameraPreview cameraPreview;
    private String filePath;

    /**Purpose: Overrides onCreate() to create an instance of a camera and also initializes
     * camera dimensions for cropping later.
     * Precondition: None
     * Postcondition: Camera object is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        setResult(RESULT_CANCELED);

        calcCropDimensions(1, 1);

        // Camera may be in use by another activity or the system or not available at all
        camera = getCameraInstance();
        if (cameraAvailable(camera)) {
            initCameraPreview();
        } else {
            finish();
        }
    }

    /**Purpose: Show the camera view on the activity.
     * Precondition: None.
     * Postcondition: Camera preview is initialized with camera.
     */
    private void initCameraPreview() {
        cameraPreview = findViewById(R.id.camera_preview);
        cameraPreview.init(camera);
    }

    /**Purpose: Take a picture.
     * Precondition: A view object is required for taking a picture using a button.
     * Postcondition: A picture is taken and an image is created.
     * @param button
     */
    @FromXML
    public void onCaptureClick(View button) {
        // Take a picture with a callback when the photo has been created
        // Here you can add callbacks if you want to give feedback when the picture is being taken
        camera.takePicture(null, null, this);

    }

    /**Purpose: Overrides onPictureTaken() to create a file path for the image taken so that the
     * image can be accessed for later such as getting the image for the analyzation process.
     * Activity started so image can be accessed in another class.
     * Precondition: A camera object must be initialized.
     * Postcondition: filePath is sent to ResultActivity class and the activity is also started
     * for ResultActivity.
     * @param data
     * @param camera
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions");
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("ImageFilePath", filePath);
        startActivity(intent);
        finish();
    }

    /**Purpose: Create a File for saving an image or video.
     * Precondition: filepath must be declared.
     * Postcondition: filepath created for file.
     * @param type
     * @return
     */
    private File getOutputMediaFile(int type) {
        // Check that the SDCard is mounted
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Log.d("BilimetrixUSA", "external storage is not available and not writable");
            return null;
        }

        //Create file
        File mediaStorageDir = new File(String.valueOf(getExternalFilesDir(Environment.DIRECTORY_PICTURES)));

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("BilimetrixUSA", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            filePath = mediaStorageDir.getPath() + File.separator +  "IMG_" + timeStamp + ".jpg";
            mediaFile = new File(filePath);
        } else {
            return null;
        }

        return mediaFile;
    }

    //Need this?
    private int calcCropDimensions(int w, int h) {
        float logicalDensity = getApplicationContext().getResources().getDisplayMetrics().density;
        int wpx = (int) Math.ceil(w * logicalDensity);
        int hpx = (int) Math.ceil(h * logicalDensity);
        Log.d("dimensions", "WIDTHY: " + w);
        Log.d("dimensions", "HEIGHTY: " + h);
        Log.d("dimensions", "DENSITYY: " + logicalDensity);
        Log.d("dimensions", "BUTT " + wpx);
        Log.d("dimensions", "FART " + hpx);

        return 0;
    }

    /**Purpose: Get path for the file on external storage. If external storage is not currently
     * mounted this will fail.
     * Precondition: File declared
     * Postcondition: File is deleted
     * @param file
     */
    void deleteExternalStoragePrivateFile(File file) {
        if (file != null) {
            file.delete();
        }
    }

    /**Purpose: Overrides onPause() to release camera when application is paused.
     * Precondition: Application and camera are running.
     * Postcondition: Camera is released.
     */
    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    /**Purpose: Releases camera.
     * Precondition: None.
     * Postcondition: Camera is released.
     */
    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    /**Purpose: Overrides onDestroy() to stop the preview and set the preview callback to null.
     * Precondition: Camera is not null.
     * Pocondition: Camera preview is stopped and camera is released.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);

            releaseCamera();
        }
    }


}
