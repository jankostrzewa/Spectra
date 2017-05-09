package jankos.spectra;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Arrays;


public class ImageAnalysis{

    private double[] columnValues;
    private Config config;
    String mImageFileLocation;
    private static ImageAnalysis imageAnalysis;

    private ImageAnalysis() {
        config = Config.GetInstance();
    }

    public static ImageAnalysis ImageAnalysisFactory(){
        if(imageAnalysis == null)
            imageAnalysis = new ImageAnalysis();
        return imageAnalysis;
    }

    public double[] analyzeImage(Bitmap bitmap){
        if(bitmap != null){
            double[] values = sumImageToColumns(bitmap);
            return values;
        }
        else return null;
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

    public double[] sumImageToColumns(Bitmap bitmap){
    //    double spectrumWidth = config.maximumWavelength.wavelength - config.minimalWavelength.wavelength;
    //    double resolution = spectrumWidth; //
        int x=bitmap.getWidth();
        int y=bitmap.getHeight();
        double[] summedColumns = new double[x];
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
