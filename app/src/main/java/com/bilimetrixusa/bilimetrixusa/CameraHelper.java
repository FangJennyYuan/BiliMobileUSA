package com.bilimetrixusa.bilimetrixusa;

import android.hardware.Camera;

public class CameraHelper {

    /**Purpose: Checks if a camera is available for use.
     * Precondition: Camera object must be created.
     * Postcondition: None.
     * @param camera
     * @return
     */
    public static boolean cameraAvailable(Camera camera) {
        return camera != null;
    }

    /**Purpose: Checks if camera opens.
     * Precondition: Camera object must be created.
     * Postcondition: None.
     * @return
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            //Camera is not available or doesn't exist
            //Log.d("getCamera failed", e);
        }
        return c;
    }

}
