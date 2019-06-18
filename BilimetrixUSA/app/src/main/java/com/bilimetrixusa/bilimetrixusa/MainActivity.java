package com.bilimetrixusa.bilimetrixusa;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MainActivity extends Activity {
    private static final int TAKE_PICTURE = 1;
    View background;

    /**Purpose: Overrides onCreate() to set a background for the application homepage and
     * declare an ImageButton object that can be clicked to start the next activity.
     * Precondition: None.
     * Postcondition: Home page created and camera activity started when button is clicked.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        background = this.getWindow().getDecorView();
        background.setBackgroundResource(R.color.white);

        ImageButton btn = findViewById(R.id.btnCamera);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(v.getContext(), CameraActivity.class);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cameraIntent, TAKE_PICTURE);
                }
            }
        });

    }

    /**Purpose: Overrides onActivityResult() to declare and intialize a bitmap after picture is
     * taken.
     * Precondition: Picture must be taken.
     * Postcondition: bitmap for image initialized.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
        }
    }

}



