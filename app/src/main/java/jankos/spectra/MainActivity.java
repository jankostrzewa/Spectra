package jankos.spectra;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private Config config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        config = Config.GetInstance();
        config();//Adjust global configuration class
        //findViewById(R.id.btnTakePhoto).setEnabled(config.HasCamera);//Enable or disable taking standalone photos if a system camera is detected
    }

    private void config() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        config.SCREENWIDTH = metrics.widthPixels;
        config.SCREENHEIGHT = metrics.heightPixels;
        config.HasCamera = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
        getApplicationContext();
    }

    public void captureUsbActivity(View view) {
        Intent intent = new Intent(this, CaptureUsbActivity.class);
        startActivity(intent);
    }

    public void previousResultsActivity(View view) {
        Intent intent = new Intent(this, PreviousResultsActivity.class);
        startActivity(intent);
    }

    public void calibrationActivity(View view) {
        Intent intent = new Intent(this, CalibrationActivity.class);
        startActivity(intent);
    }

    public void appConfigActivity(View view) {
        Intent intent = new Intent(this, AppConfigActivity.class);
        startActivity(intent);
    }



}
