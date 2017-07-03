package jankos.spectra;

import android.graphics.Bitmap;
import android.graphics.Color;


class ImageAnalysis{

    private static final Config config;
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
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight()/50;
        int yMin = (bitmap.getHeight() - bitmapHeight)/2;
        int yMax = yMin + bitmapHeight;
        Number[] summedColumns = new Number[bitmapWidth];
        for(int i=0;i<bitmapWidth;i+=4){
            float intensity = 0;
            for(int j=yMin;j<yMax;j++){
                int pixel = bitmap.getPixel(i,j);//Get current pixel
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                float[]hsv=new float[3];
                Color.RGBToHSV(r,g,b,hsv);//Assign h,s,v from r,g,b to hsv[]
                intensity += hsv[2];
            }
            summedColumns[i] = intensity;
        }
        return summedColumns;
    }
}
