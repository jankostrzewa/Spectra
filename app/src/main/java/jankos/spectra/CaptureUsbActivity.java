package jankos.spectra;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.androidplot.xy.*;
import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.widget.SimpleUVCCameraTextureView;



public final class CaptureUsbActivity extends BaseActivity implements CameraDialog.CameraDialogParent  {
    private static final boolean DEBUG = true;	// set false when releasing
    private static final String TAG = "CaptureUsbActivity";
    private boolean ANIMATEPLOT = true;

    private final Object mSync = new Object();
    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    private SimpleUVCCameraTextureView mUVCCameraView;
    // for open&start / stop&close camera preview
    private ToggleButton mCameraButton;
    private Surface mPreviewSurface;

    private XYPlot plot;
    private Config config;

    private String mSupportedSizesJSON;

    private Bitmap tempBitmap;
    private LineAndPointFormatter series1Format;
    private XYSeries series1;
    private ToggleButton btnAnimatePlot;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_capture);
        config = Config.GetInstance();

        /* Setting up USB capture */

        mUVCCameraView = (SimpleUVCCameraTextureView) findViewById(R.id.UVCCameraTextureView1);
        mUVCCameraView.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        mUVCCameraView.setSurfaceTextureListener(mSurfaceTextureListener);
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mCameraButton = (ToggleButton) findViewById(R.id.camera_button);
        mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);

        /* End USB Capture */

        /* Setting up XYPlot */

        plot = (XYPlot) findViewById(R.id.plot);
        plot.setRangeUpperBoundary(100,BoundaryMode.AUTO);
        plot.setRangeLowerBoundary(0,BoundaryMode.FIXED);
        plot.setRangeStep(StepMode.INCREMENT_BY_VAL,1);
//        plot.setRa
        series1Format = new LineAndPointFormatter(Color.RED, Color.GREEN, null, null);
        //plot.setLegend(null);
        //plot.setDomainBoundaries(config.LAMBDAMIN,config.LAMBDAMAX,BoundaryMode.FIXED);

        /* Setting other elements */
        btnAnimatePlot = (ToggleButton) findViewById(R.id.btnAnimatePlot);
        btnAnimatePlot.setOnCheckedChangeListener(AnimatePlotListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        synchronized (mSync) {
            if (mUSBMonitor != null) {
                mUSBMonitor.register();
            }
            if (mUVCCamera != null)
                mUVCCamera.startPreview();
        }
        setCameraButton(false);
    }

    @Override
    protected void onStop() {
        synchronized (mSync) {
            if (mUVCCamera != null) {
                mUVCCamera.stopPreview();
            }
            mUSBMonitor.unregister();
        }
        setCameraButton(false);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        synchronized (mSync) {
            if (mUVCCamera != null) {
                mUVCCamera.destroy();
                mUVCCamera = null;
            }
            if (mUSBMonitor != null) {
                mUSBMonitor.destroy();
                mUSBMonitor = null;
            }
        }
        mCameraButton = null;
        mUVCCameraView = null;
        super.onDestroy();
    }

    private final OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            synchronized (mSync) {
                if (isChecked && mUVCCamera == null) {
                    CameraDialog.showDialog(CaptureUsbActivity.this);
                } else if (mUVCCamera != null) {
                    mUVCCamera.destroy();
                    mUVCCamera = null;
                }
            }
        }
    };

    private final OnCheckedChangeListener AnimatePlotListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            ANIMATEPLOT = isChecked;
            if(mUVCCamera == null)
                return;
            if(isChecked)
                mUVCCamera.setFrameCallback(mFrameCallback,UVCCamera.FRAME_FORMAT_YUYV);
            else
                mUVCCamera.setFrameCallback(null,UVCCamera.FRAME_FORMAT_YUYV);
        }
    };

    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            Toast.makeText(CaptureUsbActivity.this, "USB Camera connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.destroy();
                    mUVCCamera = null;
                }
            }
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    final UVCCamera camera = new UVCCamera();
                    camera.open(ctrlBlock);
                    mSupportedSizesJSON = camera.getSupportedSize();
                    if (DEBUG) Log.i(TAG, "supportedSize:" + mSupportedSizesJSON);
                    config.SetCameraSizes(mSupportedSizesJSON);
                    if (mPreviewSurface != null) {
                        mPreviewSurface.release();
                        mPreviewSurface = null;
                    }
                    try {
                        camera.setPreviewSize(config.CAMERAWIDTH, config.CAMERAHEIGHT,UVCCamera.FRAME_FORMAT_YUYV);
                    } catch (final IllegalArgumentException e) {
                        try {
                            e.printStackTrace();
                            // fallback to YUV mode
                            camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
                        } catch (final IllegalArgumentException e1) {
                            camera.destroy();
                            return;
                        }
                    }
                    Toast.makeText(CaptureUsbActivity.this,camera.getPreviewSize().width + " " + camera.getPreviewSize().height,Toast.LENGTH_LONG).show();
                    final SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
                    if (st != null) {
                        mPreviewSurface = new Surface(st);
                        camera.setPreviewDisplay(mPreviewSurface);
                        if(ANIMATEPLOT){
                            tempBitmap = Bitmap.createBitmap(config.CAMERAWIDTH,config.CAMERAHEIGHT,Bitmap.Config.RGB_565);
                            camera.setFrameCallback(mFrameCallback,UVCCamera.PIXEL_FORMAT_RGB565);
                        }
                        camera.startPreview();
                    }
                    synchronized (mSync) {
                        mUVCCamera = camera;
                    }
                }
            }, 0);
        }

        @Override
        public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
            // XXX you should check whether the comming device equal to camera device that currently using
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        if (mUVCCamera != null) {
                            mUVCCamera.setFrameCallback(null,UVCCamera.FRAME_FORMAT_YUYV);
                            mUVCCamera.close();
                        }
                    }
                    if (mPreviewSurface != null) {
                        mPreviewSurface.release();
                        mPreviewSurface = null;
                    }
                }
            }, 0);
            setCameraButton(false);
        }

        @Override
        public void onDettach(final UsbDevice device) {
            Toast.makeText(CaptureUsbActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
            setCameraButton(false);
        }
    };

    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            setCameraButton(false);
        }
    }

    private void setCameraButton(final boolean isOn) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCameraButton != null) {
                    try {
                        mCameraButton.setOnCheckedChangeListener(null);
                        mCameraButton.setChecked(isOn);
                    } finally {
                        mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
                    }
                }
            }
        }, 0);
    }
    //**********************************************************************
    private final SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
        }

        @Override
        public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
            if (mPreviewSurface != null) {
                mPreviewSurface.release();
                mPreviewSurface = null;
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(final SurfaceTexture surface) {

        }
    };


    private final IFrameCallback mFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(final ByteBuffer frame) {
//        YuvImage image = new
            synchronized(tempBitmap){
                tempBitmap.copyPixelsFromBuffer(frame);
            }
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    ImageAnalysis.analyzeImage(tempBitmap, new AnalyzeCallback() {
                        @Override
                        public void onAnalyzeSuccess(Number[] values, String result) {
                            plot.clear();
                            series1 = new SimpleXYSeries(Arrays.asList(values), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");
                            plot.addSeries(series1,series1Format);
                            plot.redraw();
                            Log.i(TAG,result);
                        }

                        @Override
                        public void onAnalyzeFailed() {
                            Log.i(TAG,"fail!");
                        }
                    });
                }
            }, 0);
        }
    };

    public void exportPlot(View view) {
        int width = plot.getWidth();
        int height = plot.getHeight();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(new Date());
        String imageFileName = "Spectrum_".concat(timeStamp); //name of taken photo to be saved
        File storageDirectory = Environment.getExternalStorageDirectory();//get location of storage
        File file = new File(storageDirectory,imageFileName + ".png");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            plot.setDrawingCacheEnabled(true);
            plot.measure(width, height);
            plot.setDrawingCacheEnabled(true);
            Bitmap bmp = Bitmap.createBitmap(plot.getDrawingCache());
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            plot.setDrawingCacheEnabled(false);
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}