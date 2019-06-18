//AUTHORS: Alex Runciman, Anna Gassen
//FILENAME: StandardColorPicker.java
//REVISION HISTORY: none
//REFERENCES: none

/**
 * DESCRIPTION:
 * This class calculates a serum bilirubin level (SBL) from saturation color
 * standards which are associated with SBL values. Saturation is the S value of
 * the HSV colorspace. These values are retrieved from an image containing
 * Bilimetrix test card and test strip. Using a Simple Regression, the SBL of
 * the test strip is predicted and returned.
 *
 * The saturation values are found using the StandardColorPicker class, and
 * the SBL values are found using the configuration file.
 */

package com.bilimetrixusa.bilimetrixusa;

import android.util.Log;

import java.util.ArrayList;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import static android.content.ContentValues.TAG;

public class SBLCalculator {

    private static double testStripSaturation;
    private static ArrayList<Double> testCardSaturations;
    private static ArrayList<Double> testCardSBLs;
    private static SimpleRegression sr;

    /** Purpose: Constructor. Takes in S value of test strip, S values of
     * test card ROIs, and the SBL values of the ROIs on the test card.
     * Preconditions: inTestStripSaturation is greater than or equal to zero.
     * inTestCardSaturations and intestCardSBLs have the same number of entries,
     * which correspond to each other: for the same index, the saturation of
     * the test card ROI represents the SBL at the same index.
     * Postconditions: Exceptions will be thrown if input parameters are
     * invalid. The input parameters will be stored for use in calculation.
     */
    public SBLCalculator(double inTestStripSaturation, ArrayList<Double> inTestCardSaturations, ArrayList<Double> inTestCardSBLs)
            throws Exception {

        if (inTestStripSaturation < 0) {
            throw new Exception("Invalid test strip saturation");
        }
        if (inTestCardSaturations.size() != inTestCardSBLs.size()) {
            throw new Exception("Data sets not same length");
        }

        testStripSaturation = inTestStripSaturation;
        testCardSaturations = new ArrayList<Double>(inTestCardSaturations);
        testCardSBLs = new ArrayList<Double>(inTestCardSBLs);

        Log.d("calc", "Test Strip SAT: " + testStripSaturation);
        for (int i = 0; i < testCardSaturations.size(); i++) {
            Log.d("calc", "SAT: " + testCardSaturations.get(i) + ", SBL: " + testCardSBLs.get(i));
        }
    }

    /** Purpose: Builds simple regression from card saturation and SBL values.
     * Preconditions: None
     * Postconditions: A simple regression is created so that the SBL of the
     * test strip can be calculated using calcTestStripSBL()
    */
    private static void createColorBilirubinSR() {
        sr = new SimpleRegression();
        for (int i = 0; i < testCardSaturations.size(); i++) {
            sr.addData(testCardSaturations.get(i), testCardSBLs.get(i));
        }
    }

    /** Purpose: Calculate the SBL from the test strip saturation.
     * Preconditions: A simple regression has been created using
     * createColorBilirubinSR()
     * Postconditions: The SBL of the test strip is calculated and returned.
     */
    public static double calcTestStripSBL() {
        createColorBilirubinSR();
        double prediction = sr.predict(testStripSaturation);
        Log.d("calc", "PREDICTION: " + prediction);
        return prediction;
    }
}
