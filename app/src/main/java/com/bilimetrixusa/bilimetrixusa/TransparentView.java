package com.bilimetrixusa.bilimetrixusa;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class TransparentView extends View {

    private int width, height, outlineWidth, outlineHeight;

    /**Purpose: Initialized with parent constructor.
     * Precondition: Requires View class.
     * Postcondition: TransparentView() initialized.
     * @param context
     * @param attrs
     * @param defStyle
     */
    public TransparentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**Purpose: Initialized with parent constructor
     * Precondition: Requires View class.
     * Postcondition: TransparentView() initialized.
     * @param context
     * @param attrs
     */
    public TransparentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**Purpose: Initialized with parent constructor.
     * Precondition: Requires View class.
     * Postcondition: TransparentView() initialized.
     * @param context
     */
    public TransparentView(Context context) {
        super(context);
    }

    /**Purpose: Creates dark transparent background around frame.
     * Precondition: Requires camera preview to e initialized.
     * Postcondition: Transparent background drawn.
     * @param canvas
     */
    public void draw(Canvas canvas) {
        super.draw(canvas);

        width = this.getMeasuredWidth();
        height = this.getMeasuredHeight();

        Paint outerPaint = new Paint();
        int transparentBlack = Color.argb(127, 0, 0, 0);
        outerPaint.setColor(transparentBlack);
        outerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        outerPaint.setAntiAlias(true);

        Paint innerPaint = new Paint();
        innerPaint.setColor(Color.TRANSPARENT);
        innerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        innerPaint.setAntiAlias(true);

        canvas.drawRect(0, 0, width, height, outerPaint);
        canvas.drawRect(width/2 - 119, height/2 - 194, width/2 + 119, height/2 + 194, innerPaint);

    }

}
