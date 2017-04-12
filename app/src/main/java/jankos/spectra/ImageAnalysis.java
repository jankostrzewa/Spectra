package jankos.spectra;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;


public class ImageAnalysis extends AppCompatActivity {

    private double[] columnValues;
    private Config config = Config.GetInstance();
    String mImageFileLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze_image);
        try {
            config();
        }
        catch (Exception ex){//If config not loaded, restart the image loading activities
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show();
        }
        if(config.IsCalibrated)
            analyzeImage(mImageFileLocation);
    }

    private void analyzeImage(String filePath){
        if(filePath != null){

            Bitmap capturedPhotoBitmap = BitmapFactory.decodeFile(filePath);
            double[] values = sumImageToColumns(capturedPhotoBitmap);

        }
        else{

        }
    }

    private void config() throws Exception{
        if(config.ImageLoaded)
            mImageFileLocation = config.filePath;
        else {
            throw new Exception("Config not loaded!");
        }
        if(!config.IsCalibrated)
            throw new Exception("Camera not calibrated!");


    }

    private double[] sumImageToColumns(Bitmap bitmap){
        double[] summedColumns = null;
        double spectrumWidth = config.maximumWavelength.wavelength - config.minimalWavelength.wavelength;
        double resolution = spectrumWidth; //
        int x=bitmap.getWidth();
        int y=bitmap.getHeight();
        int ySumMin=y/2-50;//Minimum y in range
        for(int i=0;i<x;i++){//Iterate through each column
            double pixelValueColumn = 0;//Initialize sum of each column
            for(int j=0;j<100;j++){//Iterate through each pixel in column
                int pixel = bitmap.getPixel(i,ySumMin+j);//Get current pixel
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                float[]hsv=new float[3];
                Color.RGBToHSV(r,g,b,hsv);//Assign h,s,v from r,g,b to hsv[]
                double valuePx=hsv[2];//RAISE FLAG IF value = 255, because sensor dynamic might has been surpassed and results will
                pixelValueColumn+=valuePx;//Add value of current pixel to column sum
            }
            summedColumns[i]=pixelValueColumn;//Assign column sum to array position
        }
        return summedColumns;
    }


    //Class used to analyze images
    //knowing angles of diffraction
    //and other constants to calculate
    //wavelength of incandescing light

}
