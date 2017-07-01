package jankos.spectra;

interface AnalyzeCallback{

    void onAnalyzeSuccess(Number[] values, String result);

    void onAnalyzeFailed();
}