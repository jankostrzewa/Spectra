package jankos.spectra;

public final class Config {
    /*Configuration file,
    * for storing and retrieving
    * program variables and parameters.
    * Implemented in singleton pattern
    * to ensure consistency across classes
    * of program */
    private static Config instance = null;

    private Config(){
    }

    public static Config GetInstance(){
        if(instance==null)
            instance = new Config();
        return instance;
    }

    boolean ImageLoaded = false;
    boolean HasCamera = false;
    boolean IsCalibrated = false;
    int SCREENWIDTH;
    int SCREENHEIGHT;
    obsoleteDatapoint minimalWavelength;
    obsoleteDatapoint maximumWavelength;
    boolean CALIBRATING;
    String filePath = "";





}
