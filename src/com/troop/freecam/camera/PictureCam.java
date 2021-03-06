package com.troop.freecam.camera;

import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import com.troop.freecam.interfaces.IShutterSpeedCallback;
import com.troop.freecam.interfaces.SavePictureCallback;
import com.troop.freecam.manager.ExifManager;
import com.troop.freecam.manager.MediaScannerManager;
import com.troop.freecam.manager.SettingsManager;
import com.troop.freecam.manager.SoundPlayer;
import com.troop.freecam.manager.parameters.ParametersManager;
import com.troop.freecam.surfaces.CamPreview;
import com.troop.freecam.utils.DeviceUtils;
import com.troop.freecam.utils.SavePicture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Handler;

/**
 * Created by troop on 18.10.13.
 */
public class PictureCam extends BaseCamera implements Camera.ShutterCallback, Camera.PictureCallback, SavePictureCallback
{


    //protected MediaScannerManager scanManager;
    public SoundPlayer soundPlayer;
    protected CamPreview context;
    protected SavePicture savePicture;
    public boolean crop = false;
    public ParametersManager parametersManager;
    public SavePictureCallback onsavePicture;
    public boolean IsWorking = false;
    protected IShutterSpeedCallback shutterSpeedCallback;
    public void setOnShutterSpeed(IShutterSpeedCallback shutterSpeedCallback){this.shutterSpeedCallback = shutterSpeedCallback; }

    final String TAG = "freecam.PictureCam";
    private void writeDebug(String s)
    {
        Log.d(TAG, s);
    }

    byte[] rawbuffer;



    public PictureCam(CamPreview context,SettingsManager preferences)
    {
        super(preferences);
        this.context = context;
        //this.scanManager = new MediaScannerManager(context.getContext());
        soundPlayer = new SoundPlayer(context.getContext());
        savePicture = new SavePicture(context.getContext(), preferences);
        savePicture.onSavePicture = this;
    }

    //private static final int CAMERA_MSG_RAW_IMAGE = 0x080;
    //private native final void _addCallbackBuffer(
            //byte[] callbackBuffer, int msgType);

    public void TakePicture(boolean crop)
    {
        IsWorking = true;
        this.crop = crop;
        //Camera.Size size = mCamera.getParameters().getPictureSize();
        //rawbuffer = new byte[size.width * size.height * 8];
        Log.d(TAG, "Start Taking Picture");
        try
        {
            soundPlayer.PlayShutter();
            mCamera.takePicture(null,null,this);
            Log.d(TAG, "Picture Taking is Started");
        }
        catch (Exception ex)
        {
            writeDebug("Take Picture Failed");
            ex.printStackTrace();
        }
    }

    /** Handles data for raw picture */
    public Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            writeDebug("onPictureTaken - raw");
            //if (data != null)
                //saveRawData(data);
        }
    };


    @Override
    public void onPictureTaken(byte[] data, Camera camera)
    {

        writeDebug("OnPictureTaken callback recieved");
        boolean is3d = false;
        if (Settings.Cameras.GetCamera().equals(SettingsManager.Preferences.MODE_3D))
        {
            is3d = true;
        }
        writeDebug("start saving to sd");
        try {
            savePicture.SaveToSD(data, crop, mCamera.getParameters().getPictureSize(), is3d);
            writeDebug("save successed");
        }
        catch (Exception ex)
        {
            Log.e(TAG, "saving to sd failed");
            ex.printStackTrace();
        }

        try {
            writeDebug("try to start preview");

            mCamera.startPreview();
            if (DeviceUtils.isEvo3d())
                parametersManager.LensShade.set(Settings.LensShade.get());
        }
        catch (Exception ex)
        {
            Log.e(TAG, "preview start failed");
            ex.printStackTrace();
        }

        IsWorking = false;
        data = null;
        //takePicture = false;
    }

    @Override
    public void onShutter()
    {
        soundPlayer.PlayShutter();
    }

    @Override
    public void onPictureSaved(File file)
    {
       /* ExifManager m = new ExifManager();
        try {
            m.LoadExifFrom(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        onShutterSpeed(m.getExposureTime());*/
    }

    private void saveRawData(byte[] data)
    {
        File file = SavePicture.getFilePath("raw", Environment.getExternalStorageDirectory());
        FileOutputStream outStream = null;
        try {
        outStream = new FileOutputStream(file);

        outStream.write(data);
        outStream.flush();
        outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onShutterSpeed(String speed)
    {
        if (shutterSpeedCallback != null)
            shutterSpeedCallback.ShutterSpeedRecieved(speed);
    }
}
