package jankos.spectra;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.Arrays;


public class ImageAnalysis{

    private static Config config;
    static{
        config = Config.GetInstance();
    }

    public static void analyzeImage(Bitmap bitmap, AnalyzeCallback analyzeCallback){
        if(bitmap != null){
            Number[] values = sumImageToColumns(bitmap);
            analyzeCallback.onAnalyzeSuccess(values, "Analysis completed");
        }
        else{
            analyzeCallback.onAnalyzeFailed();
        }
    }

    private static Number[] sumImageToColumns(Bitmap bitmap){
        int bitmapWidth = bitmap.getWidth()/4;
        int bitmapHeight = bitmap.getHeight()/24;
        Number[] summedColumns = new Number[bitmapWidth];
        for(int i=0;i<bitmapWidth;i++){
            int intensity = 0;
            for(int j=0;j<bitmapHeight;j++){
                int pixel = bitmap.getPixel(4*i,575+j);//Get current pixel
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                float[]hsv=new float[3];
                Color.RGBToHSV(r,g,b,hsv);//Assign h,s,v from r,g,b to hsv[]
                intensity += (int)(33*hsv[2]);

            }
            //Log.i("intensity:", String.valueOf(intensity));
            summedColumns[i] = intensity;
        }







//        int ySumMin=bitmapHeight/2-50;//Minimum bitmapHeight in range
//        for(int i=0;i<x;i++){//Iterate through each column
//            Number pixelValueColumn = 0;//Initialize sum of each column
//            for(int j=0;j<10;j++){//Iterate through each pixel in column
//                int pixel = bitmap.getPixel(x/2,bitmapHeight/2);//Get current pixel
//                int r = Color.red(pixel);
//                int g = Color.green(pixel);
//                int b = Color.blue(pixel);
//                float[]hsv=new float[3];
//                Color.RGBToHSV(r,g,b,hsv);//Assign h,s,v from r,g,b to hsv[]
//                int intensity = (int)(100*hsv[2]);//RAISE FLAG IF value = 255, because sensor dynamic might has been surpassed and results will
//               // pixelValueColumn+=valuePx;//Add value of current pixel to column sum
//            }
//            summedColumns[i]=pixelValueColumn;//Assign column sum to array position
//        }
        return summedColumns;
    }


    //Class used to analyze images
    //knowing angles of diffraction
    //and other constants to calculate
    //wavelength of incandescing light

}
