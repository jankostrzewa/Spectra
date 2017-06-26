package jankos.spectra;

import android.graphics.Bitmap;

public interface AnalyzeCallback{

    public void onAnalyzeSuccess(Number[] values, String result);

    public void onAnalyzeFailed();
}