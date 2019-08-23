//AUTHORS: Alex Runciman, Anna Gassen
//FILENAME: ClusterReturn.java
//REVISION HISTORY: none
//REFERENCES:

/**
 * DESCRIPTION:
 * This class is a helper class which is used as a return type from
 * kMeanCluster() method in the StandardColorPicker class. It holds the labels
 * (assignment of pixels to cluster) and centers (cluster colors) of the
 * k-means clustering.
 */

package com.bilimetrixusa.bilimetrixusa;

import org.opencv.core.Mat;

public class ClusterReturn {
    private static Mat labels; // assignment of pixels to clusters
    private static Mat centers; // color of each cluster

    /**Purpose: Initialize labels and centers.
     * Precondition: A Mat is required from OpenCV.
     * Postcondition: labels and centers updated.
     * @param l
     * @param c
     */
    public ClusterReturn(Mat l, Mat c) {
        labels = l;
        centers = c;
    }

    /**Purpose: Get labels.
     * Precondition: A Mat is required from OpenCV.
     * Postcondition: None.
     * @return
     */
    public Mat getLabels() {
        return labels;
    }

    /**Purpose: Get centers.
     * Precondition: A Mat is required from OpenCV.
     * Postcondition: None.
     * @return
     */
    public Mat getCenters() {
        return centers;
    }
}
