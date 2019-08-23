//AUTHORS: Alex Runciman, Anna Gassen
//FILENAME: StandardColorPicker.java
//REVISION HISTORY: none
//REFERENCES:

/**
 * DESCRIPTION:
 * This class is used to create a StandardColorPickerObject. If the object is appropriately
 * initialized with a image of type Mat the image is able to be processed. If no image is
 * provided an error is thrown to the IU. The class is able to process an image by
 * converting the black pixels of an image to true black for OpenCV, determine all the areas
 * of interest within the image, determine the mean color for each area of interest found,
 * and to perform k-means cluster on the image.
 */

package com.bilimetrixusa.bilimetrixusa;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

import android.graphics.Bitmap;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

public class SatScanner {

    public static Mat img;

    RotatedRect testStripROI;
    ArrayList<RotatedRect> cardStandardROIs;
    ArrayList<RotatedRect> drawRectangles = new ArrayList<RotatedRect>();

    int numRegions;

    double testStripSat;
    ArrayList<Double> cardStandardSats;

    ArrayList<Double> cardStandardWidths;
    ArrayList<Double> cardStandardHeights;

    double testStripWidth;
    double testStripHeight;

    /**
     * Purpose: Constructor with no parameters, always throws Exception
     * because the StandardColorPicker class requires an image to perform
     * its' functionality.
     * Precondition: None.
     * Postcondition: Throws Exception.
     */
    public SatScanner() throws Exception {
        throw new Exception("Image required");
    }

    /**Purpose: Constructor that takes in an image as a parameter and necessary
     * test card and strip heights and widths.
     * Precondition: None.
     * Postcondition: The image will be temporarily stored as a global variable
     * so that it can be processed.
     * @param inFile The image taken by the user
     * @param inNumRegions The desired number of regions in the image taken by the user
     * @param inCardStandardHeights The heights of each square/rectangle on the test card
     * @param inCardStandardWidths The widths of each square/rectangle on the test card
     * @param inTestStripHeight The height of each square/rectangle on the test strip
     * @param inTestStripWidth The width of each square/rectangle on the test strip
     */
    public SatScanner(Mat inFile, int inNumRegions, double inTestStripWidth,
                      double inTestStripHeight, ArrayList<Double> inCardStandardWidths,
                      ArrayList<Double> inCardStandardHeights) {
        img = inFile.clone();

        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGRA2BGR);

        numRegions = inNumRegions;

        testStripROI = null;

        cardStandardROIs = new ArrayList<RotatedRect>();
        cardStandardSats = new ArrayList<Double>();

        cardStandardWidths = new ArrayList<Double>(inCardStandardWidths);
        cardStandardHeights = new ArrayList<Double>(inCardStandardHeights);

        testStripWidth = inTestStripWidth;
        testStripHeight = inTestStripHeight;
    }

    /**Purpose: To set all pixels of an image that are black to the standard
     * black value used by the OpenCv libraries. Uses the K-means method to
     * determine the black pixel clusters. This method is called before any
     * image segmentation or analysis occurs as the image returned is what
     * should be used in all functions relating to image analysis.
     * Preconditions: The parameter img has black pixels which need to be
     * converted to true black.
     * Postconditions: The black pixels in img have been replaced with true
     * black pixels.
     * @param img The current cropped image being processed
     * @return The cropped image with all darker pixels converted to true black.
     */
    public Mat cvtTestCardOutlineToBlack(Mat img) {

        Mat blackImg = img.clone();

        Imgproc.medianBlur(blackImg, blackImg, 5);

        // Perform K-means clustering where k = 2
        int k = 2;
        ClusterReturn clusters = kMeansCluster(blackImg, k);
        Mat labels = clusters.getLabels();
        Mat centers = clusters.getCenters();

        // Determine which cluster holds the black pixels
        double blackCluster;
        if (centers.get(0,2)[0] < centers.get(1,2)[0]) {
            blackCluster = 0.0;
        }
        else {
            blackCluster = 1.0;
        }

        // Convert all pixels in black cluster to true black
        double[] trueBlack = new double [] {0.0, 0.0, 0.0};
        int rows = 0;
        for (int i = 0; i < blackImg.rows(); i++) {
            for (int j = 0; j < blackImg.cols(); j++) {
                double label = labels.get(rows, 0)[0];
                if (label == blackCluster) {
                    blackImg.put(i, j, trueBlack);
                }
                rows++;
            }
        }

        return blackImg;
    }


    /**
     * Purpose: Given an image of a bilirubin test strip attached to a test
     * card, find the test strip and standard color squares on the card, and
     * stores all appropriate regions of the image in drawRectangles. These
     * are our regions of interest.
     * Preconditions: The image is in BGR colorspace. The image is of a
     * Bilimetrix bilirubin test strip which has been properly attached to
     * a test card.
     * Postconditions: A list of the regions of interest will be stored. The
     * test strip ROI is always the first in the list.
     * @param img
     */
    private void findStandardAndTestStripROIs(Mat img) throws Exception {

        Mat copyImg = img.clone();
        double[] color = copyImg.get(0, 0);


        // list of ROIs to be returned
        ArrayList<RotatedRect> rois = new ArrayList<RotatedRect>();

        // Copy input image, convert it to grayscale
        Mat roiImg = img.clone();
        Imgproc.cvtColor(roiImg, roiImg, Imgproc.COLOR_BGR2GRAY);

        // Find contours in image
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(roiImg, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        // Buffer for comparing ROI widths and heights
        double buffer = .05;

        // Find the minimum sized rectangle that bounds each contour
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint2f contour2f = new MatOfPoint2f();
            contours.get(i).convertTo(contour2f, CvType.CV_32FC2);

            RotatedRect minRect = Imgproc.minAreaRect(contour2f);

            if (minRect.angle < -45.0) {
                minRect.angle += 90.0;
                double tmp = minRect.size.width;
                minRect.size.width = minRect.size.height;
                minRect.size.height = tmp;
            }

            double minRectWidth = minRect.size.width;
            double minRectHeight = minRect.size.height;
            double imgWidth = img.size().width;
            double imgHeight = img.size().height;
            Log.d("dimensions", "IMG DIMENSIONS: " + imgWidth + " " + imgHeight);
            double w = 0;
            double h = 0;

            boolean added = false;
            int j = 0;
            while (!added && j < cardStandardWidths.size()) {
                w = cardStandardWidths.get(j);
                h = cardStandardHeights.get(j);
                if ((minRectWidth > (w - buffer) * imgWidth) && (minRectHeight > (h - buffer) * imgHeight)
                        && (minRectWidth < (w + buffer) * imgWidth) && (minRectHeight < (h + buffer) * imgHeight)) {
                    minRect.size.width = minRect.size.width * .9;
                    minRect.size.height = minRect.size.height * .9;
                    cardStandardROIs.add(minRect);
                    added = true;
                }
                if (added) {
                    Log.d("dimensions", "RECT INFO (" + j + "): " + minRectWidth/imgWidth +
                            " " + minRectHeight/imgHeight + " " + w + " " + h + " XX");
                }
                else {
                    Log.d("dimensions", "RECT INFO (" + j + "): " + minRectWidth/imgWidth +
                            " " + minRectHeight/imgHeight + " " + w + " " + h);
                }
                j++;
            }

            if (!added && testStripROI == null) {
                w = testStripWidth;
                h = testStripHeight;
                double testStripBuffer = 2.5;
                if ((minRectWidth > (w - testStripBuffer) * imgWidth) && (minRectHeight > (h - testStripBuffer) * imgHeight)
                        && (minRectWidth < (w + testStripBuffer) * imgWidth) && (minRectHeight < (h + testStripBuffer) * imgHeight)) {
                    Log.d("dimensions", "STRIP ADDED");
                    minRect.size.width = minRect.size.width * .75;
                    minRect.size.height = minRect.size.height * .5;
                    testStripROI = minRect;
                }
            }
            Log.d("dimensions", "STRIP INFO: " + minRectWidth/imgWidth + " " + minRectHeight/imgHeight + " " + w + " " + h);

            drawRectangles.add(minRect);

        }

        if ((cardStandardROIs.size() != numRegions) || (testStripROI == null)) {
            throw new Exception("Incorrect image");
        }

    }

    /**
     * Purpose: Given an image and a list of RotatedRect objects (which
     * represent regions of interest in the image), this function calculates
     * the mean of the pixels in each region and returns them as a list.
     * Preconditions: The image is in BGR colorspace. The method
     * findStandardAndTestStripROIs() has been run on img (parameter for this
     * method). The list of ROIs are the produced from
     * findStandardAndTestStripROIs() and have ben properly stored int the global
     * variable cardStandardROIS.
     * Postconditions: A list of the mean color of each ROI is stored in a global
     * variable cardStandardSat. The serum bilirubin level can now be calculated.
     * @param img
     */
    private void rectangleMeanColors(Mat img) {
        // Convert image from BGR to HSV
        Mat hsv_img = new Mat();
        Imgproc.cvtColor(img, hsv_img, Imgproc.COLOR_BGR2HSV);

        for (int i = 0; i < cardStandardROIs.size() + 1; i++) {

            RotatedRect currentROI;

            if (i == cardStandardROIs.size()) {
                currentROI = testStripROI;
            }
            else {
                currentROI = cardStandardROIs.get(i);
            }

            Mat rotated = new Mat();
            Mat cropped = new Mat();

            // get angle and size of the ROI
            double angle = currentROI.angle;
            Size rect_size = currentROI.size;

            // thanks to http://felix.abecassis.me/2011/10/opencv-rotation-deskewing/
            if (currentROI.angle < -45.0) {
                angle += 90.0;
                double tmp = rect_size.width;
                rect_size.width = rect_size.height;
                rect_size.height = tmp;
            }

            // Perform an affine transformation to get a cropped version of the
            // ROI
            Mat M = Imgproc.getRotationMatrix2D(currentROI.center, angle, 1.0);
            Imgproc.warpAffine(hsv_img, rotated, M, hsv_img.size(), Imgproc.INTER_CUBIC);
            Imgproc.getRectSubPix(rotated, rect_size, currentROI.center, cropped);

            // Calculate mean color of the cropped image and add its
            // saturation (S) value to the return array
            double meanSat = (Core.mean(cropped)).val[1];

            if (i == cardStandardROIs.size()) {
                testStripSat = meanSat;
            }
            else {
                cardStandardSats.add(meanSat);
            }
        }
    }

    /**Purpose: performs k-means clustering on an image
     * Preconditions: The image is in BGR colorspace. The number of desired
     * clusters is known and is passed in.
     * Postconditions: Returns cluster information as a ClusterReturn object
     * (see ClusterReturn.java)
     * @param img Cropped image in BGR colorspace
     * @param k the desired number of clusters
     */
    private ClusterReturn kMeansCluster(Mat img, int k) {
        Mat samples = img.reshape(1, img.cols() * img.rows());
        Mat samples32f = new Mat();
        samples.convertTo(samples32f, CvType.CV_32F, 1.0 / 255.0);

        Mat labels = new Mat();
        TermCriteria criteria = new TermCriteria(TermCriteria.COUNT, 100, 1);
        Mat centers = new Mat();

        Core.kmeans(samples32f, k, labels, criteria, 1, Core.KMEANS_PP_CENTERS,
                centers);

        ClusterReturn cr = new ClusterReturn(labels, centers);
        return cr;
    }

    /**
     * Purpose: Calls helper functions to segment and manipulate an image
     * of a bilirubin test card to determine the average colors of the test
     * strip and card standard color squares.
     * Precondition: The constructor has been called.
     * Postcondition: A list of the average colors are stored in cardSandardSast. The first
     * element of the array represents the color of the test strip, the rest
     * are the card's standard color squares.
     *
     */
    public void calcTestCardandStripSats() throws Exception {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Draw black rectangle on bottom of image to aid which segmentation
        // of the test strip
        Imgproc.rectangle(img, new Point(0, img.rows()), new Point (img.cols(),
                img.rows() - img.rows()*0.03), new Scalar(0.0, 0.0, 0.0), -1);

        img = cvtTestCardOutlineToBlack(img);
        findStandardAndTestStripROIs(img);

        // draw rectangles returned from rois onto original image
        //// DELETE
        for (int i = 0; i < drawRectangles.size(); i++) {
            Point[] rectPoints = new Point[4];
            /*if (i == cardStandardROIs.size() && testStripROI != null) {
                testStripROI.points(rectPoints);
            }
            else {*/
                drawRectangles.get(i).points(rectPoints);
            //}
            for (int j = 0; j < 4; j++) {
                Imgproc.line(img, rectPoints[j], rectPoints[(j+1) % 4],
                        new Scalar(0, 255, 0), 4);
            }
        }

        rectangleMeanColors(img);
        Collections.sort(cardStandardSats, Collections.reverseOrder());
    }

    /**
     * Purpose: Returns the saturation value of the test strip
     * Precondition: getTestCardandStripColors() method has been run to
     * find the saturation values of the test strip and card squares.
     *  Postcondition: None.
     */
    public double getTestStripSat() {
        return testStripSat;
    }

    /**
     * Purpose: Returns the saturation values of the test card squares
     * Precondition: getTestCardandStripColors() method has been run to
     * find the saturation values of the test strip and card squares.
     * Postcondition: None.
     */
    public ArrayList<Double> getCardStandardSats() {
        return cardStandardSats;
    }

    public Mat getImg() {return img;};

}
