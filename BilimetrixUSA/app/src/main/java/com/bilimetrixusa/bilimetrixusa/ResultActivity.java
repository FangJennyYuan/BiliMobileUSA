package com.bilimetrixusa.bilimetrixusa;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ResultActivity extends Activity {
    private ImageToSBL imageProcessor;
    private double us_sbl, eu_sbl;
    private Bitmap convertedFile;
    TextView mgdl_num, mgdl_units, micromol_num, micromol_units;

    /**Purpose: Overrides onCreate() to display a progress bar while the analyzation takes place.
     * Gets converted file after the file has been analyzed. Also displays results
     * once file is successfully anaylzed.
     * Precondition: Photo file must undergo analyzation
     * Postcondition: Visibility of results is updated to display plasma bilirubin level
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setVisibility(ProgressBar.VISIBLE);

        mgdl_num = (TextView) findViewById(R.id.sbl_mgdl_num);
        mgdl_units = (TextView) findViewById(R.id.sbl_mgdl_units);
        micromol_num = (TextView) findViewById(R.id.sbl_micromol_num);
        micromol_units = (TextView) findViewById(R.id.sbl_micromol_units);

        mgdl_num.setVisibility(TextView.INVISIBLE);
        mgdl_units.setVisibility(TextView.INVISIBLE);
        micromol_num.setVisibility(TextView.INVISIBLE);
        micromol_units.setVisibility(TextView.INVISIBLE);

        // Retrieve pictureFile
        String filePath = getIntent().getStringExtra("ImageFilePath");
        convertedFile = convertFileToBitmap(filePath);


        try {

            imageProcessor = new ImageToSBL(convertedFile, getApplicationContext());
            us_sbl = imageProcessor.calcSBLFromImage();
            Log.d("calc", "OTHER OTHER PREDICTION: " + us_sbl);

            //// TODO: DELETE
            ImageView displayImg = findViewById(R.id.displayImg);
            displayImg.setVisibility(ImageView.INVISIBLE);
            //displayImg.setImageBitmap(imageProcessor.displayBmp);

            eu_sbl = us_sbl * 17.1; // conversion to micro-mols

            mgdl_num.setText(String.format("%.2f", us_sbl));
            micromol_num.setText(String.format("%.2f", eu_sbl));

            pb.setVisibility(ProgressBar.INVISIBLE);

            mgdl_num.setVisibility(TextView.VISIBLE);
            mgdl_units.setVisibility(TextView.VISIBLE);
            micromol_num.setVisibility(TextView.VISIBLE);
            micromol_units.setVisibility(TextView.VISIBLE);
        }
        catch (Exception e) {
            e.printStackTrace();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error")
                    .setMessage(e.getMessage() + ",\n" + "Please retake image.");
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    returnToMain();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    /**Purpose: Converts a file to a bitmap.
     * Preconditions: Requires a file that is not empty
     * Postconditions: bitmap is created from file
     * @param fp
     * @return
     */
    private Bitmap convertFileToBitmap(String fp) {
        Bitmap unrotatedBitmap = BitmapFactory.decodeFile(fp);

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap bitmap = Bitmap.createBitmap(unrotatedBitmap, 0, 0, unrotatedBitmap.getWidth(), unrotatedBitmap.getHeight(), matrix, true);
        unrotatedBitmap.recycle();

        float w_scale_factor, h_scale_factor;
        w_scale_factor = (float) (2*400) / bitmap.getWidth();
        h_scale_factor = (float) (2*670) / bitmap.getHeight();
        Matrix m = new Matrix();
        m.postScale(w_scale_factor, h_scale_factor);

        float centerX = bitmap.getWidth() / 2;
        float centerY = bitmap.getHeight() / 2;

        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, (int) (centerX - 400), (int) (centerY - 670), (int) (2*400), (int) (2*670), m, true);
        return croppedBitmap;
    }

    /**Purpose: Returns to home page when button is clicked.
     * Precondition: Requires a home page to return to
     * Postcondition: Returns to homepage
     * @param button
     */
    @FromXML
    public void onCloseClick(View button) {
        returnToMain();
    }

    /**Purpose: Returns to main activity
     * Precondition: Requires MainActivity class
     * Postcondition: Returns to MainActivity
     */
    private void returnToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
