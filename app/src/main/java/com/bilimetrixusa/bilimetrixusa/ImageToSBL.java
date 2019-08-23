//AUTHORS: Alex Runciman, Anna Gassen
//FILENAME: ImageToSBL.java
//REVISION HISTORY: none
//REFERENCES:

package com.bilimetrixusa.bilimetrixusa;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;

import android.content.Context;
import android.graphics.Bitmap;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;


public class ImageToSBL {

    static {
        OpenCVLoader.initDebug();
    }

    static SatScanner satScanner = null;
    static SBLCalculator calc = null;
    private static ArrayList<Double> standardSBL;

    private static double result;
    private static Bitmap imgResult;
    private static Bitmap blackWhiteImgResult;
    private static RGBColor testStripRBG;
    private static ArrayList<float[]> hsvList = new ArrayList<>();

    public static Mat img;
    public static Bitmap displayBmp;

    ImageToSBL() throws Exception {
        throw new Exception("Image required");
    }

    /**Purpose: To construct a the items needed for the SatScanner
     * class. These data values are read from a configuration file containing
     * each squares SBLat t value along with the width and height of the square.
     * It also contains the width and height of the test strip.
     * Precondition: An image has been passed in to the constructor as a bitmap
     * Postcondition: A SatScanner object has been appropriately initiated.
     * @param bmp the image taken by the user in bitmap format
     * @param context
     */
    ImageToSBL(Bitmap bmp, Context context) throws Exception {
        imgResult = bmp;
        int widthImg = imgResult.getWidth();
        int heightImg = imgResult.getHeight();
        boolean hasAlpha = imgResult.hasAlpha();
        cordinates[] blockLocations = new cordinates[7];
        //should be white

        //Find the average of each block using the average pixels.
        //Then write a method that find the test strip middle that started changing the color.
        //int color;
        //int redBucket;
        //int greenBucket;
        //int blueBucket;
        int alpha = 0;
        int realColor;
        RGBColor[] colorBlocks = new RGBColor[7];
        double[] diffBlocks = new double[7];
        RGBColor RGBcolorBlock;

        Mat mat = new Mat();
        Bitmap bmp32 = imgResult.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);

        blackWhiteImgResult = createContrast(imgResult, 50);


        //mat = satScanner.cvtTestCardOutlineToBlack(mat);

        testStripRBG = calcTestStripRBG(widthImg, heightImg);
        /*
        float[] hsv = new float[3];
        //hsv = [0.0, 0.0, 0.0];

        Color.colorToHSV(testStripRBG.getColorResult(), hsv);

        System.out.println(hsv);
        */

        System.out.println(hsvList);

        //testStripRBG = calcAverageRBG(widthImg/2, (heightImg/4)/2+(2*(heightImg/4)));

        //loop through expected area pixels, and find the pixels when color started changing.

        blockLocations = pixelLocation(widthImg, heightImg);


        for (int i=0; i<blockLocations.length; i++){
            RGBcolorBlock = calcAverageRGB(blockLocations[i].getX(), blockLocations[i].getY());
            //if (hasAlpha) alpha = (RGBcolorBlock.getColorResult() >>> 24);
            //realColor = Color.argb((hasAlpha) ? alpha : 255,
            //        RGBcolorBlock.getRed(), RGBcolorBlock.getGreen(), RGBcolorBlock.getBlue());
            diffBlocks[i] = calcDistance(
                    RGBcolorBlock.getRed(), RGBcolorBlock.getGreen(), RGBcolorBlock.getBlue(),
                    testStripRBG.getRed(), testStripRBG.getGreen(), testStripRBG.getBlue());
            colorBlocks[i] = RGBcolorBlock;
        }

        for (int i=0; i<7; i++){
            System.out.print(colorBlocks[i] + ", ");
            System.out.println(diffBlocks[i]);
        }

        //double min = diffBlocks[0];
        int minIndex = 0;
        for (int i=1; i<7; i++){
            if (diffBlocks[minIndex] > diffBlocks[i]){
                minIndex = i;
            }
        }

        findBlockSBL(minIndex);

        /*

        //bit map converted to mat
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
        */
    }


    private static Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static Bitmap createContrast(Bitmap src, double value) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int)(((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(R < 0) { R = 0; }
                else if(R > 255) { R = 255; }

                G = Color.red(pixel);
                G = (int)(((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(G < 0) { G = 0; }
                else if(G > 255) { G = 255; }

                B = Color.red(pixel);
                B = (int)(((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(B < 0) { B = 0; }
                else if(B > 255) { B = 255; }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        return bmOut;
    }


    private static float[] calcTestStripHSV(int widthImg, int heightImg){
        float[] result = null;



        return result;
    }

    private static RGBColor calcTestStripRBG(int widthImg, int heightImg){
        //loop through expected area pixels, and find the pixels when color started changing.
        cordinates possibleTestStripStart = new cordinates(widthImg/2, heightImg/2);
        cordinates possibleTestStripEnd = new cordinates(widthImg/2, heightImg/4*3);
        //int possibleStartHeight = heightImg/2;
        //int possibleEndHeight = heightImg/2 + heightImg/4;
        ArrayList<RGBColor> testStrip = new ArrayList<RGBColor>();

        int left = 0;
        int right = 0;
        int color;


        //Top average pixels
        for(int i=0; i<heightImg/4; i++){
            //testStrip.add(calcAverageRBG(widthImg/2, possibleStartHeight+i));
            testStrip.add(calcAverageRGB(widthImg/2, possibleTestStripStart.getY()+i));
        }

        //Test strip middle section from the top
        ArrayList<Double> colorDiff = new ArrayList<Double>();
        //Store color difference on the middle section and check for 10 pixels where color is not different.
        int validPixels = 10;
        //int validStart = possibleStartHeight;
        int validStart = possibleTestStripStart.getY();
        for(int i=1; i<testStrip.size() && validPixels!=0; i++){
            colorDiff.add(calcDistance(testStrip.get(i-1).getRed(),testStrip.get(i-1).getGreen(),testStrip.get(i-1).getBlue(),
                    testStrip.get(i).getRed(), testStrip.get(i).getGreen(), testStrip.get(i).getBlue()));
            if (colorDiff.get(i-1) < 5.0){
                validPixels--;
            }else{
                validPixels = 10;
            }

            if (validPixels == 0){
                validStart = validStart + i - 10;
            }
        }


        //RGB to HSV
        //ArrayList<float[]> hsvList = new ArrayList<>();
        float[] hsv = new float[3];
        //Color.colorToHSV(testStripRBG.getColorResult(), hsv);
        //System.out.println(hsv);

        //Get the average test strip 10x4 pixels
        color = 0;
        RGBColor result = null;
        int red = 0;
        int green = 0;
        int blue = 0;

        for (int i=validStart ; i<validStart+10; i++){
            color += imgResult.getPixel(widthImg/2, i);
            result = new RGBColor(imgResult.getPixel(widthImg/2, i));
            Color.colorToHSV(result.getColorResult(), hsv);
            hsvList.add(hsv);
            red += result.getRed();
            green += result.getGreen();
            blue += result.getBlue();
        }

        RGBColor top = new RGBColor(color/10, red/10, green/10, blue/10);



        //start from the middle, work to the left and find where color turned black,
        //then work down half and find the 5 pixels to the right.
        //Do the same for the right side.
        cordinates possibleLeftSide = null;
        cordinates possibleRightSide = null;

        do{
            color = blackWhiteImgResult.getPixel(possibleTestStripStart.getX()-left, possibleTestStripStart.getY());
            left++;
        }while (color != Color.BLACK && possibleTestStripStart.getX()-left > 0);

        System.out.print(left);

        possibleLeftSide = new cordinates(possibleTestStripStart.getX()-left+10, possibleTestStripStart.getY());


        do{
            color = blackWhiteImgResult.getPixel(possibleTestStripStart.getX()+right, possibleTestStripStart.getY());
            right++;
        }while (color != Color.BLACK && possibleTestStripStart.getX()+right < widthImg);

        System.out.print(right);

        possibleRightSide = new cordinates(possibleTestStripStart.getX()+right-10, possibleTestStripStart.getY());


        //Left side of the test strip.
        //color = 0;
        //red = 0;
        //green = 0;
        //blue =0;
        for (int i=0; i<5; i++){
            color += imgResult.getPixel(possibleLeftSide.getX()+i, possibleLeftSide.getY());
            result = new RGBColor(imgResult.getPixel(possibleLeftSide.getX()+i, possibleLeftSide.getY()));
            red += result.getRed();
            green += result.getGreen();
            blue += result.getBlue();
        }

        //RGBColor leftColor = new RGBColor(color/5, red/5, green/5, blue/5);

        //color = 0;
        //red = 0;
        //green = 0;
        //blue =0;
        for (int i=0; i<5; i++){
            color += imgResult.getPixel(possibleRightSide.getX()-i, possibleRightSide.getY());
            result = new RGBColor(imgResult.getPixel(possibleRightSide.getX()-i, possibleRightSide.getY()));
            red += result.getRed();
            green += result.getGreen();
            blue += result.getBlue();
        }

        //RGBColor rightColor = new RGBColor(color/5, red/5, green/5, blue/5);


        //result = new RGBColor((top.getColorResult()+leftColor.getColorResult()+rightColor.getColorResult())/3,
        //        (top.getRed() + leftColor.getRed() + rightColor.getRed())/3,
        //        (top.getGreen() + leftColor.getGreen() + rightColor.getGreen())/3,
        //        (top.getBlue() + leftColor.getBlue() + rightColor.getBlue())/3);

        result = new RGBColor(color/20, red/20, green/20, blue/20);

        //return top;
        return result;
    }

    private static cordinates[] pixelLocation(int width, int height){
        cordinates[] locations = new cordinates[7];

        //white
        locations[0] = new cordinates(width/2, (height/4)/2);

        //6 color blocks
        int index1 = 4; //right 3
        int index2 = 3; //left 3
        for (int i=0; i<3; i++) {
            locations[index1++] = new cordinates((width / 3) / 2, (height/4)/2 + (i*(height/4)));
            locations[index2--] = new cordinates(width - (width/3/2), (height/4)/2+(i*(height/4)));
        }

        return locations;
    }

    private static void findBlockSBL(int index){
        switch(index) {
            case 0:
                result = -0.2;
                break;
            case 1:
                result = 5.4;
                break;
            case 2:
                result = 10;
                break;
            case 3:
                result = 15.4;
                break;
            case 4:
                result = 21.5;
                break;
            case 5:
                result = 26;
                break;
            case 6:
                result = 29.8;
                break;
        }
    }

    private static RGBColor calcAverageRGB(int x, int y){
        int color = 0;
        RGBColor result = null;
        int red = 0;
        int green = 0;
        int blue = 0;
        for (int i=0; i<4; i++){
            color += imgResult.getPixel(x+i, y);
            result = new RGBColor(imgResult.getPixel(x+i, y));
            red += result.getRed();
            green += result.getGreen();
            blue += result.getBlue();
        }
        return new RGBColor(color/4, red/4, green/4, blue/4);
    }

    private static double calcDistance(int red, int green, int blue, int red2, int green2,int blue2){
        double result = Math.pow(red-red2, 2) + Math.pow(green-green2, 2) + Math.pow(blue-blue2, 2);
        return Math.sqrt(result);
    }

    public double getResult(){
        return result;
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
