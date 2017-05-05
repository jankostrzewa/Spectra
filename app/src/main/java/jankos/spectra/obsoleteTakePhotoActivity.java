package jankos.spectra;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class obsoleteTakePhotoActivity extends AppCompatActivity {

    private final static int ACTIVITY_START_CAMERA_APP = 0;//a unique code returned to intent in takePhoto to know who's answering
    private ImageView mPhotoCapturedImageView; //a reference to the ImageView element in layout to hold and view taken image
    private String mImageFileLocation = "";
    private boolean enableImageAnalysisFlag = false;
    private Config config;

    //added to API21 to get permissions
    private final static int REQUEST_EXTERNAL_STORAGE_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config = Config.GetInstance();
        setContentView(R.layout.activity_take_photo);

        mPhotoCapturedImageView = (ImageView) findViewById(R.id.capturedPhotoImageView);
    }

    public void takePhoto(View view){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            callCameraApp();
        }
        else{
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_EXTERNAL_STORAGE_RESULT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(requestCode == REQUEST_EXTERNAL_STORAGE_RESULT){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                callCameraApp();
            }
            Toast.makeText(this,"Need external storage",Toast.LENGTH_SHORT).show();
        }
        else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void callCameraApp(){
        Intent callCameraApplicationIntent = new Intent();
        callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);//pass a string with desired action - to take a photo
        File photoFile = null; //a temporary variable in function to assign captured image to
        try{
            //Bitmap.CompressFormat = Bitmap.CompressFormat.PNG;
            photoFile = createImageFile();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));//KRYTYCZNA LINIJKA - PRZEKAZ WSKAZNIK NA STWORZONY PLIK JAKO ADRES ZAPISU
        startActivityForResult(callCameraApplicationIntent, ACTIVITY_START_CAMERA_APP);//start the desired activity by intent, and expect described code
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){//when activity returns,
        if(requestCode == ACTIVITY_START_CAMERA_APP && resultCode == RESULT_OK) { //if the resulted activity gives the code of a camera intent AND result of intent was successful
            Bitmap capturedPhotoBitmap = BitmapFactory.decodeFile(mImageFileLocation);
           // mPhotoCapturedImageView.setImageBitmap(setReducedImageSize());

            findViewById(R.id.btnTakeAnalyzeImage).setEnabled(true);
            mPhotoCapturedImageView.setImageBitmap(capturedPhotoBitmap);
        }
        else{
            super.onActivityResult(requestCode,resultCode,data);
        }
    }

    private File createImageFile() throws IOException {//a function to save the resulted file to storage
        //created image must have a unique, non collision filename e.g. with a timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(new Date());
        String imageFileName = "IMG_".concat(timeStamp); //name of taken photo to be saved
        File storageDirectory = Environment.getExternalStorageDirectory();//get location of storage

        File takenImage = File.createTempFile(imageFileName,".jpg",storageDirectory);//describe the file in directory
        mImageFileLocation = takenImage.getAbsolutePath();//globally set taken image location
   //     enableImageAnalysisFlag = true;
        return takenImage;
    }

    private Bitmap setReducedImageSize(){
        int targetImageViewWidth = mPhotoCapturedImageView.getWidth();//assign w and h values to know the scale of target image
        int targetImageViewHeight = mPhotoCapturedImageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();//bitmap options - to keep its properties
        bmOptions.inJustDecodeBounds = true;//decode image size from Input file
        BitmapFactory.decodeFile(mImageFileLocation, bmOptions);//decode image to options
        int cameraImageWidth = bmOptions.outWidth;
        int cameraImageHeight = bmOptions.outWidth;

        int scaleFactor = Math.min(cameraImageWidth/targetImageViewWidth, cameraImageHeight/targetImageViewHeight);
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(mImageFileLocation, bmOptions);//resize the bitmap to view in ImageView
    }

    public void imageAnalysis(View view){
        config.filePath = mImageFileLocation;
        config.ImageLoaded = true;
        Intent intent = new Intent(this, ImageAnalysis.class);
        startActivity(intent);
    }
}
