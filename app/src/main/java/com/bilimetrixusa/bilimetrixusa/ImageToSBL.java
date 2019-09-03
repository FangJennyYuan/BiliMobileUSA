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
    private double[] diffBlocks = new double[7];
    private cordinates testStripTop;
    private cordinates testStripBottom;
    private cordinates testStripLeft;
    private cordinates testStripRight;
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
        RGBColor RGBcolorBlock;


        /* TO-DO: do we use this? */
        /*
        Mat mat = new Mat();
        Bitmap bmp32 = imgResult.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        */




        //mat = satScanner.cvtTestCardOutlineToBlack(mat);

        testStripRBG = calcTestStripRBG(widthImg, heightImg);
        /*
        float[] hsv = new float[3];
        //hsv = [0.0, 0.0, 0.0];

        Color.colorToHSV(testStripRBG.getColorResult(), hsv);

        System.out.println(hsv);
        */

        //System.out.println(hsvList);

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


        //double min = diffBlocks[0];

        int minIndex = 0;
        int secondIndex = 0;
        for (int i=1; i<7; i++){
            if (diffBlocks[minIndex] > diffBlocks[i]){
                minIndex = i;
            }
        }

        if (minIndex == 0){
            secondIndex = 1;
        }else if (minIndex == 6){
            secondIndex = 5;
        }else{
            if(diffBlocks[minIndex-1] > diffBlocks[minIndex+1]){
                secondIndex = minIndex+1;
            }else{
                secondIndex = minIndex-1;
            }
        }

        /*
        int minIndex = 0;
        int secondIndex = 0;
        for (int i=1; i<7; i++){
            if (diffBlocks[secondIndex] > diffBlocks[i]){
                if (diffBlocks[secondIndex] < diffBlocks[minIndex]){
                    secondIndex = i;
                }else{
                    minIndex = i;
                }
            }
        }
        */

        findBlockSBL(minIndex, secondIndex);


    }

    private RGBColor calcTestStripRBG(int widthImg, int heightImg){

        RGBColor top = calcTestStripRBGTop(widthImg, heightImg);

        RGBColor topLeftRight = calcTestStripRBGTopLeftRight(widthImg, heightImg);

        RGBColor squire = calcTestStripRBGSquire(widthImg, heightImg);

        Log.d("calc", "top: (" + top.getRed() + ", " + top.getGreen() + ", " + top.getBlue() + ")");
        Log.d("calc", "topLeftRight: (" + topLeftRight.getRed() + ", " + topLeftRight.getGreen() + ", " + topLeftRight.getBlue() + ")");
        Log.d("calc", "squire: (" + squire.getRed() + ", " + squire.getGreen() + ", " + squire.getBlue() + ")");
        //return top;
        //return topLeftRight;
        return squire;
    }


    private RGBColor calcTestStripRBGTop(int widthImg, int heightImg){
        //loop through expected area pixels, and find the pixels when color stop changing.
        cordinates possibleTestStripStart = new cordinates(widthImg/2, heightImg/2);

        ArrayList<RGBColor> testStrip = new ArrayList<RGBColor>();

        int color;
        int red = 0;
        int green = 0;
        int blue = 0;
        RGBColor temp;
        //Top average pixels
        for(int i=0; i<heightImg/4; i++){
            temp = new RGBColor(imgResult.getPixel(widthImg/2, possibleTestStripStart.getY()+i));
            testStrip.add(temp);
            //testStrip.add(calcAverageRGB(widthImg/2, possibleTestStripStart.getY()+i));
        }

        //Test strip middle section from the top
        ArrayList<Double> colorDiff = new ArrayList<Double>();
        //Store color difference on the middle section and check for 10 pixels where color is not different.
        int validPixels = 10;
        //int validStart = possibleStartHeight;
        testStripTop = new cordinates( widthImg/2, possibleTestStripStart.getY());

        for(int i=1; i<testStrip.size() && validPixels!=0; i++){
            colorDiff.add(calcDistance(testStrip.get(i-1).getRed(),testStrip.get(i-1).getGreen(),testStrip.get(i-1).getBlue(),
                    testStrip.get(i).getRed(), testStrip.get(i).getGreen(), testStrip.get(i).getBlue()));
            if (colorDiff.get(i-1) < 5.0){
                validPixels--;
            }else{
                validPixels = 10;
            }

            if (validPixels == 0){
                testStripTop.setY(testStripTop.getY() + i - 10);
            }
        }

        //RGB to HSV
        float[] hsv = new float[3];
        //Color.colorToHSV(testStripRBG.getColorResult(), hsv);
        //System.out.println(hsv);

        //Get the average test strip 10x1 pixels
        color = 0;
        RGBColor result = null;


        for (int i=testStripTop.getY() ; i<testStripTop.getY()+10; i++){
            color += imgResult.getPixel(widthImg/2, i);
            result = new RGBColor(imgResult.getPixel(widthImg/2, i));
            Color.colorToHSV(result.getColorResult(), hsv);
            hsvList.add(hsv);
            red += result.getRed();
            green += result.getGreen();
            blue += result.getBlue();
        }

        RGBColor top = new RGBColor(color/10, red/10, green/10, blue/10);

        return top;
    }

    private RGBColor calcTestStripRBGTopLeftRight(int widthImg, int heightImg){
        cordinates possibleTestStripStart = new cordinates(widthImg/2, heightImg/2+heightImg/6);
        int left = 0;
        int right = 0;
        int color;
        int red = 0;
        int green = 0;
        int blue = 0;
        RGBColor result = null;

        /*start from the middle, work to the left and find where color turned black,
        then work down half and find the 5 pixels to the right.
        Do the same for the right side. */
        testStripLeft = null;
        testStripRight = null;

        blackWhiteImgResult = createContrast(imgResult, 50);

        do{
            color = blackWhiteImgResult.getPixel(possibleTestStripStart.getX()-left, possibleTestStripStart.getY());
            left++;
        }while (color != Color.BLACK && possibleTestStripStart.getX()-left > 0);
        testStripLeft = new cordinates(possibleTestStripStart.getX()-left, possibleTestStripStart.getY());


        do{
            color = blackWhiteImgResult.getPixel(possibleTestStripStart.getX()+right, possibleTestStripStart.getY());
            right++;
        }while (color != Color.BLACK && possibleTestStripStart.getX()+right < widthImg);
        testStripRight = new cordinates(possibleTestStripStart.getX()+right, possibleTestStripStart.getY());



        /*start from the left, and stop at right.
        Then find 5 pixels where the colors stop changing from the left and right*/
        RGBColor temp;
        ArrayList<RGBColor> testStrip = new ArrayList<RGBColor>();
        int getX = 0;
        while( getX < (testStripRight.getX()-testStripLeft.getX())){
            temp = new RGBColor(imgResult.getPixel(testStripLeft.getX()+getX, testStripLeft.getY()));
            getX++;
            testStrip.add(temp);
        }


        //Test strip left side
        ArrayList<Double> colorDiff = new ArrayList<Double>();
        //Store color difference on the left section and check for 5 pixels where color is not different.
        int validPixels = 5;

        for(int i=1; i<testStrip.size() && validPixels!=0; i++){
            colorDiff.add(calcDistance(testStrip.get(i-1).getRed(),testStrip.get(i-1).getGreen(),testStrip.get(i-1).getBlue(),
                    testStrip.get(i).getRed(), testStrip.get(i).getGreen(), testStrip.get(i).getBlue()));
            if (colorDiff.get(i-1) < 5.0){
                validPixels--;
            }else{
                validPixels = 5;
            }

            if (validPixels == 0){
                testStripLeft.setX(testStripLeft.getX() + i - 5);
            }
        }

        colorDiff = new ArrayList<Double>();
        validPixels = 5;
        int counter = 0;
        for(int i=testStrip.size()-1; i>0 && validPixels!=0; i--){
            colorDiff.add(calcDistance(testStrip.get(i-1).getRed(),testStrip.get(i-1).getGreen(),testStrip.get(i-1).getBlue(),
                    testStrip.get(i).getRed(), testStrip.get(i).getGreen(), testStrip.get(i).getBlue()));
            if (colorDiff.get(counter) < 5.0){
                validPixels--;
            }else{
                validPixels = 5;
            }

            if (validPixels == 0){
                testStripRight.setX(testStripRight.getX() - counter + 5);
            }
            counter++;
        }

        //check where you start and end for the left->right
        //System.out.println(testStripLeft.getX());
        //System.out.println(testStripRight.getX());

        color = 0;
        //Left side of the test strip.
        for (int i=0; i<5; i++){
            color += imgResult.getPixel(testStripLeft.getX()+i, testStripLeft.getY());
            result = new RGBColor(imgResult.getPixel(testStripLeft.getX()+i, testStripLeft.getY()));
            red += result.getRed();
            green += result.getGreen();
            blue += result.getBlue();
        }

        for (int i=0; i<5; i++){
            color += imgResult.getPixel(testStripRight.getX()-i, testStripRight.getY());
            result = new RGBColor(imgResult.getPixel(testStripRight.getX()-i, testStripRight.getY()));
            red += result.getRed();
            green += result.getGreen();
            blue += result.getBlue();
        }

        RGBColor top = calcTestStripRBGTop(widthImg, heightImg);

        color += top.getColorResult()*10;
        red += top.getRed()*10;
        green += top.getGreen()*10;
        blue += top.getBlue()*10;

        result = new RGBColor(color/20, red/20, green/20, blue/20);

        return result;
    }

    private RGBColor calcTestStripRBGSquire(int widthImg, int heightImg){
        /*find where they start changing (already found the top, left and right)
        need to look at the bottom as well once we get the blood to know where to stop for red.
        Need to start from the top, then look at left/right stop at left.getY()(height/2 + height/4)
        */

        cordinates topLeft;
        cordinates topRight;
        cordinates bottomLeft;
        cordinates bottomRight;
        int color=0;
        int red = 0;
        int green = 0;
        int blue = 0;
        RGBColor result = null;

        topLeft = new cordinates(testStripLeft.getX(), testStripTop.getY());
        topRight = new cordinates(testStripRight.getX(), testStripTop.getY());
        bottomLeft = new cordinates(testStripLeft.getX(), testStripLeft.getY());
        bottomRight = new cordinates(testStripRight.getX(), testStripRight.getY());



        //Find the colors squire
        int squireWidth = topRight.getX() - topLeft.getX();
        int squireHeight = bottomLeft.getY() - topLeft.getY();
        for(int i=0; i<squireHeight;i++){
            for(int j=0; j<squireWidth; j++){
                color += imgResult.getPixel(topLeft.getX()+j, topLeft.getY()+i);
                result = new RGBColor(imgResult.getPixel(topLeft.getX()+j, topLeft.getY()+i));
                red += result.getRed();
                green += result.getGreen();
                blue += result.getBlue();
            }
        }


        //find the average of the squire
        result = new RGBColor(color/(squireHeight*squireWidth),
                red/(squireHeight*squireWidth),
                green/(squireHeight*squireWidth),
                blue/(squireHeight*squireWidth));

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

    private void findBlockSBL(int index, int secondIndex){
        if (Math.abs(secondIndex-index) > 1){
            //error
        }

        double minResult = findBlockSBL(index);
        double secondResult = findBlockSBL(secondIndex);
        double diffBili = Math.abs(minResult-secondResult)/2;
        double weightedDiff = Math.abs(diffBlocks[index]-diffBlocks[secondIndex]);
        Log.d("calc", "min index: " + index + ", minResult: " + minResult);
        Log.d("calc", "2nd min index: " + secondIndex + ", secondResult: " + secondResult);

        if (index < secondIndex){
            //add to index
            result = minResult + (1-(weightedDiff/diffBlocks[index]))*diffBili;
        }else{
            //minus from secondIndex
            result = Math.abs(secondResult - (1-(weightedDiff/diffBlocks[secondIndex]))*diffBili);
        }

        //System.out.println(result);


    }


    private double findBlockSBL(int val){
        double SBL = Double.MIN_VALUE;
        switch(val) {
            case 0:
                SBL = -0.2;
                break;
            case 1:
                SBL = 5.4;
                break;
            case 2:
                SBL = 10;
                break;
            case 3:
                SBL = 15.4;
                break;
            case 4:
                SBL = 21.5;
                break;
            case 5:
                SBL = 26;
                break;
            case 6:
                SBL = 29.8;
                break;
        }
        return SBL;
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

    /*
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
    */

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
