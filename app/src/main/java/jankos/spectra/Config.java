package jankos.spectra;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    int SCREENWIDTH = 200;
    int SCREENHEIGHT = 320;
    int CAMERAWIDTH = 640;
    int CAMERAHEIGHT = 480;
    int LAMBDAMIN = 350;
    int LAMBDAMAX = 800;
    String filePath;
    private String[] sizes;

    private void setSizes(){
        String[] sizes = this.sizes;
        int maxWidth = 0;
        int maxHeight = 0;
        int maxArea = 0;
        for (String size : sizes) {
            String[] parts = size.split("x");
            int width = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);
            int surfaceArea = width * height;
            if (width > maxWidth && height > maxHeight && surfaceArea > maxArea) {
                maxWidth = width;
                maxHeight = height;
                maxArea = surfaceArea;
            }
        }
        this.CAMERAWIDTH = maxWidth;
        this.CAMERAHEIGHT = maxHeight;
    }


    public void SetCameraSizes(String mSupportedSizesJSON) {
        JSONObject jsonResult;
        JSONArray formats;
        JSONArray sizes;
        try{
            jsonResult = new JSONObject(mSupportedSizesJSON);
            formats = jsonResult.getJSONArray("formats");
            sizes = formats.getJSONObject(0).getJSONArray("size");
            if(sizes != null) {
                String[] size = new String[sizes.length()];
                for(int i = 0 ; i < size.length ; i++) {
                   size[i] = sizes.getString(i);
                }
                this.sizes = size;
                setSizes();
            }
        }
        catch(JSONException e){
            e.printStackTrace();
        }
    }
}
