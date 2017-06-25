package jankos.spectra;

import android.graphics.Bitmap;

public interface AnalyzeCallback{

    public void onAnalyzeSuccess(double[] values, String result);

    public void onAnalyzeFailed();
}