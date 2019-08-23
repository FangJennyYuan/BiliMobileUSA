package com.bilimetrixusa.bilimetrixusa;

import android.graphics.ColorSpace;

public class RGBColor {
    private int red;
    private int green;
    private int blue;
    private int colorResult;

    public RGBColor(int color){
        red = (color >> 16) & 0xFF;
        green = (color >> 8) & 0xFF;
        blue = (color & 0xFF);
        colorResult = color;
    }

    public RGBColor(int color, int red, int green, int blue){
        this(color);
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public int getRed(){
        return red;
    }

    public int getGreen(){
        return green;
    }

    public int getBlue(){
        return blue;
    }

    public int getColorResult(){
        return colorResult;
    }

}
