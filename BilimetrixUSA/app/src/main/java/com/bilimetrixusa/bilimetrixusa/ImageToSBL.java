//AUTHORS: Alex Runciman, Anna Gassen
//FILENAME: ImageToSBL.java
//REVISION HISTORY: none
//REFERENCES:

package com.bilimetrixusa.bilimetrixusa;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.Arrays;
import java.util.Properties;
import android.content.res.AssetManager;
import android.util.Log;


public class ImageToSBL {

    static {
        OpenCVLoader.initDebug();
    }

    static SatScanner satScanner = null;
    static SBLCalculator calc = null;
    private static ArrayList<Double> standardSBL;

    public static Bitmap img;
    public static Bitmap displayBmp;

    ImageToSBL() throws Exception {
        throw new Exception("Image required");
    }

    /**Purpose: To construct a the items needed for the SatScanner
     * class. These data values are read from a configuration file containing
     * each squares SBL value along with the width and height of the square.
     * It also contains the width and height of the test strip.
     * Precondition: An image has been passed in to the constructor as a bitmap
     * Postcondition: A SatScanner object has been appropriately initiated.
     * @param bmp the image taken by the user in bitmap format
     * @param context
     */
    ImageToSBL(Bitmap bmp, Context context) throws Exception { //bit map converted to mat
        Mat image = new Mat();
        Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, image);

        standardSBL = new ArrayList<Double>();
        Properties prop = new Properties();
        InputStream input = null;

        int squares = 0;
        ArrayList<Double> cardHeights = new ArrayList<Double>();
        ArrayList<Double> cardWidths = new ArrayList<Double>();
        Double stripWidth = null;
        Double stripHeight = null;
        Double blackCardWidth = null;
        Double blackCardHeight = null;

        try {
            // load properties file
            AssetManager assetManager = context.getAssets();
            input = assetManager.open("config.properties");
            prop.load(input);

            // get number of regions
            squares = Integer.parseInt(prop.getProperty("squares"));

            // get black-region of card values
            blackCardWidth = Double.parseDouble(prop.getProperty("blackCardWidth"));
            blackCardHeight = Double.parseDouble(prop.getProperty("blackCardHeight"));

            // get test strip values
            stripWidth = Double.parseDouble(prop.getProperty("TestStripWidth")) / blackCardWidth;
            stripHeight = Double.parseDouble(prop.getProperty("TestStripHeight")) / blackCardHeight;

            // get test card square values
            for(int i = 1; i < squares+1; i++) {
                standardSBL.add(Double.parseDouble(prop.getProperty("SBL"+i)));
                Double h = Double.parseDouble(prop.getProperty("Height"+i));
                Double w = Double.parseDouble(prop.getProperty("Width"+i));

                // validate width and height values before adding
                if( h < 0 || w < 0){
                    throw new Exception("Height or width value negative");
                }

            }

            cardHeights.add(1.5/8.2);
            cardHeights.add(1.5/8.2);
            cardWidths.add(1.3/4.9);
            cardWidths.add(0.8/4.9);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        satScanner = new SatScanner(image, squares, stripWidth, stripHeight, cardWidths, cardHeights);
    }

    /**Purpose: To calculate the serum bilirubin percentage by using
     *  the standardColorPickerClass to get the necessary information from the
     *  image taken by the user. The SBLCalculator class is then called to determine
     *  the SBL from the test strip in the given image.
     * Preconditions: A class object has been appropriately initialized with an image.
     * The properties file was able to be read.
     * Postcondition: The test strips SBL value is returned to the user.
     */
    public static double calcSBLFromImage() throws Exception {
        satScanner.calcTestCardandStripSats();

        Mat rectImg = satScanner.getImg();
        displayBmp = Bitmap.createBitmap(rectImg.cols(), rectImg.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rectImg, displayBmp);

        Double testStripSat = satScanner.getTestStripSat();
        ArrayList<Double> testCardSat = satScanner.getCardStandardSats();
        calc = new SBLCalculator(testStripSat, testCardSat, standardSBL);
        double sbl = calc.calcTestStripSBL();
        Log.d("calc", "OTHER PREDICTION: " + sbl);
        return sbl;
    }

    private static int indexInArray(ArrayList<Double> a, double element) {
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i) == element) return i;
        }
        return -1;
    }
}
