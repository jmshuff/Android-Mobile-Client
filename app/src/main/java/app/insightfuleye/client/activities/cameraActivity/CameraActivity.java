package app.insightfuleye.client.activities.cameraActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.RggbChannelVector;
import android.hardware.camera2.params.TonemapCurve;
import android.media.ExifInterface;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.camera.view.TextureViewMeteringPointFactory;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import app.insightfuleye.client.R;
import app.insightfuleye.client.app.AppConstants;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static android.widget.Toast.LENGTH_SHORT;

@RuntimePermissions
public class CameraActivity extends AppCompatActivity {

    public static final int TAKE_IMAGE = 205;
    public static final int TAKE_IMAGE_RIGHT=207;
    public static final int TAKE_IMAGE_LEFT=208;
    private final Object background_camera_lock = new Object();
    private final CameraSettings camera_settings = new CameraSettings();
    public static final long EXPOSURE_TIME_DEFAULT = 1000000000L/30; // note, responsibility of callers to check that this is within the valid min/max range
    public enum Facing {
        FACING_BACK,
        FACING_FRONT,
        FACING_EXTERNAL,
        FACING_UNKNOWN // returned if the Camera API returned an error or an unknown type
    }

    /**
     * Returns whether the camera is front, back or external.
     */
    int cameraId = mPreview.getCameraId();
    public Facing getFacing() {
        CameraManager manager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraIdS = manager.getCameraIdList()[cameraId];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraIdS);
            switch (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                case CameraMetadata.LENS_FACING_FRONT:
                    return Facing.FACING_FRONT;
                case CameraMetadata.LENS_FACING_BACK:
                    return Facing.FACING_BACK;
                case CameraMetadata.LENS_FACING_EXTERNAL:
                    return Facing.FACING_EXTERNAL;
            }
        } catch (Throwable e) {
            // in theory we should only get CameraAccessException, but Google Play shows we can get a variety of exceptions
            // from some devices, e.g., AssertionError, IllegalArgumentException, RuntimeException, so just catch everything!
            // We don't want users to experience a crash just because of buggy camera2 drivers - instead the user can switch
            // back to old camera API.
            e.printStackTrace();
        }
        return Facing.FACING_UNKNOWN;
    }
    /**
     * Bundle key used for the {@link String} setting custom Image Name
     * for the file generated
     */
    public static final String SET_IMAGE_NAME = "IMG_NAME";
    /**
     * Bundle key used for the {@link String} setting custom FilePath for
     * storing the file generated
     */
    public static final String SET_IMAGE_PATH = "IMG_PATH";

    public static final String SET_EYE_TYPE= "EYE_TYPE";
    /**
     * Bundle key used for the {@link String} showing custom dialog
     * message before starting the camera.
     */
    private String mImageName = null;
    private String mFilePath = null;
    private String mType= null;
    private final String TAG = CameraActivity.class.getSimpleName();
    PreviewView mPreviewView;
    ImageView captureImage;
    private Handler mBackgroundHandler;
    public CameraInfo cInfo;
    public CameraControl cControl;
    private SeekBar zoomBar;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
    private Handler handler = new Handler();
    private View focusView;
    private TextureView txView;
    private TextView textType;
    //private ImageCapture imageCapture = null;
    private Runnable focusingTOInvisible = new Runnable() {
        @Override
        public void run() {
            focusView.setVisibility(View.INVISIBLE);
        }
    };





    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            "android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mPreviewView = findViewById(R.id.camera);
        captureImage = findViewById(R.id.take_picture);
        focusView = findViewById(R.id.focus);
        textType=findViewById(R.id.camera_tv_type);
        txView=findViewById(R.id.view_finder);
        zoomBar = findViewById(R.id.zoomBar);
        zoomBar.setMax(100);
        zoomBar.setProgress(0);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(SET_IMAGE_NAME))
                mImageName = extras.getString(SET_IMAGE_NAME);
            if (extras.containsKey(SET_IMAGE_PATH))
                mFilePath = extras.getString(SET_IMAGE_PATH);
            if(extras.containsKey(SET_EYE_TYPE))
                mType=extras.getString(SET_EYE_TYPE);
            if (extras.containsKey("requestCode")){
                if (extras.getInt("requestCode")==CameraActivity.TAKE_IMAGE_RIGHT)
                    textType.setText("Right Eye");
                if (extras.getInt("requestCode")==CameraActivity.TAKE_IMAGE_LEFT)
                    textType.setText("Left Eye");
            }

        }


        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }


    }


    @NeedsPermission(Manifest.permission.CAMERA)

    /*void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        try {
            orientationEventListener.enable();
        }catch (Exception e){
        }
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }
*/
    void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(()->{
            try{
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                //bind Camera Preview to Surface provider ie:viewFinder in my case
                Preview preview = new Preview.Builder().build();

                ImageCapture imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).setTargetAspectRatio(AspectRatio.RATIO_4_3)
                                //.setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                                .build();

                Preview.SurfaceProvider surfaceProvider = mPreviewView.createSurfaceProvider();
                preview.setSurfaceProvider(surfaceProvider);

                try {
                    cameraProvider.unbindAll();
                    Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                    cControl = camera.getCameraControl();
                    cInfo = camera.getCameraInfo();

                    //AutoFocus Every X Seconds
                    MeteringPointFactory AFfactory = new SurfaceOrientedMeteringPointFactory((float)mPreviewView.getWidth(),(float)mPreviewView.getHeight());
                    float centerWidth = (float)mPreviewView.getWidth()/2;
                    float centerHeight = (float)mPreviewView.getHeight()/2;
                    MeteringPoint AFautoFocusPoint = AFfactory.createPoint(centerWidth, centerHeight);
                    try {
                        FocusMeteringAction action = new FocusMeteringAction.Builder(AFautoFocusPoint,FocusMeteringAction.FLAG_AF).setAutoCancelDuration(1, TimeUnit.SECONDS).build();
                        cControl.startFocusAndMetering(action);
                    }catch (Exception e){

                    }

                    OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
                        @Override
                        public void onOrientationChanged(int orientation) {
                                int rotation;
                                // Monitors orientation values to determine the target rotation value
                                if (orientation >= 45 && orientation < 135) {
                                    rotation = Surface.ROTATION_270;
                                } else if (orientation >= 135 && orientation < 225) {
                                    rotation = Surface.ROTATION_180;
                                } else if (orientation >= 225 && orientation < 315) {
                                    rotation = Surface.ROTATION_90;
                                } else {
                                    rotation = Surface.ROTATION_0;
                                }
                                imageCapture.setTargetRotation(rotation);
                            }

                        };
                    orientationEventListener.enable();
                    captureImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imageCapture.takePicture(executor, new ImageCapture.OnImageCapturedCallback() {
                                @Override
                                public void onCaptureSuccess(ImageProxy image) {
                                    final byte[] data;
                                    data = toBitmap(image);
                                    compressImageAndSave(data, imageCapture);
                                    orientationEventListener.disable();
                                }

                                @Override
                                public void onError(@NonNull ImageCaptureException error) {
                                    error.printStackTrace();
                                    orientationEventListener.disable();
                                }

                            });

                        }

                    });


                }catch (Exception e){
                    Toast.makeText(this,"Failed", LENGTH_SHORT).show();
                }
                pinchToZoom();
                setUpZoomSlider();
                //autofocusOnStart();
                setUpTapToFocus();
            }catch (ExecutionException | InterruptedException e){

            }
        },ContextCompat.getMainExecutor(this));



    }

    /*void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)

        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }
        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);



        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageCapture.takePicture(executor, new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(ImageProxy image) {
                        final byte[] data;
                        data = toBitmap(image);
                        compressImageAndSave(data);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException error) {
                        error.printStackTrace();
                    }

                });

            }

        });

    }*/

    private void pinchToZoom() {
        //Pinch Zoom Camera
        ScaleGestureDetector.SimpleOnScaleGestureListener listener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                LiveData<ZoomState> ZoomRatio = cInfo.getZoomState();
                float currentZoomRatio = 0;
                try {
                    currentZoomRatio = ZoomRatio.getValue().getZoomRatio();
                } catch (NullPointerException e) {

                }
                float linearValue = ZoomRatio.getValue().getLinearZoom();
                float delta = detector.getScaleFactor();
                cControl.setZoomRatio(currentZoomRatio * delta);
                float mat = (linearValue) * (100);
                zoomBar.setProgress((int) mat);
                return true;
            }
        };

        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getBaseContext(), listener);
    }
    private void setUpZoomSlider(){
        zoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float mat = (float) (progress) / (100);
                cControl.setLinearZoom(mat);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setUpTapToFocus() {
        txView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_UP) {
                /* Original post returns false here, but in my experience this makes
                onTouch not being triggered for ACTION_UP event */
                    return true;
                }
                TextureViewMeteringPointFactory factory = new TextureViewMeteringPointFactory(txView);
                MeteringPoint point = factory.createPoint(event.getX(), event.getY());
                FocusMeteringAction action = new FocusMeteringAction.Builder(point).build();
                cControl.startFocusAndMetering(action);
                handler.removeCallbacks(focusingTOInvisible);
                focusView.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_focus));
                focusView.setX(event.getX());
                focusView.setY(event.getY());
                focusView.setVisibility(View.VISIBLE);
                handler.postDelayed(focusingTOInvisible,1000);
                return true;
            }
        });
    }

    private void autofocusOnStart(){
        TextureViewMeteringPointFactory factory = new TextureViewMeteringPointFactory(txView);
        float centerWidth=(float)mPreviewView.getWidth()/2;
        float centerHeight= (float)mPreviewView.getHeight()/2;
        MeteringPoint point = factory.createPoint(centerWidth,centerHeight);
        FocusMeteringAction action = new FocusMeteringAction.Builder(point).build();
        cControl.startFocusAndMetering(action);
    }


    private byte[] toBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy planeProxy = image.getPlanes()[0];
        ByteBuffer buffer = planeProxy.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
        //return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    void compressImageAndSave(final byte[] data, ImageCapture imageCapture) {
        getBackgroundHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mImageName == null) {
                    mImageName = "IMG";
                }
                String filePath = AppConstants.IMAGE_PATH + mImageName + ".jpg";

                File file;
                if (mFilePath == null) {
                    file = new File(AppConstants.IMAGE_PATH + mImageName + ".jpg");
                } else {
                    file = new File(AppConstants.IMAGE_PATH + mImageName + ".jpg");
                }
                OutputStream os = null;
                try {
                    os = new FileOutputStream(file);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    //  Bitmap bitmap = Bitmap.createScaledBitmap(bmp, 600, 800, false);
                    //  bitmap.recycle();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.flush();
                    os.close();
                    bitmap.recycle();


                    Bitmap scaledBitmap = null;

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

                    int actualHeight = options.outHeight;
                    int actualWidth = options.outWidth;
                    float maxHeight = 816.0f;
                    float maxWidth = 612.0f;
                    float imgRatio = actualWidth / actualHeight;
                    float maxRatio = maxWidth / maxHeight;

                    if (actualHeight > maxHeight || actualWidth > maxWidth) {
                        if (imgRatio < maxRatio) {
                            imgRatio = maxHeight / actualHeight;
                            actualWidth = (int) (imgRatio * actualWidth);
                            actualHeight = (int) maxHeight;
                        } else if (imgRatio > maxRatio) {
                            imgRatio = maxWidth / actualWidth;
                            actualHeight = (int) (imgRatio * actualHeight);
                            actualWidth = (int) maxWidth;
                        } else {
                            actualHeight = (int) maxHeight;
                            actualWidth = (int) maxWidth;
                        }
                    }

                    options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
                    options.inJustDecodeBounds = false;
                    options.inDither = false;
                    options.inPurgeable = true;
                    options.inInputShareable = true;
                    options.inTempStorage = new byte[16 * 1024];

                    try {
                        bmp = BitmapFactory.decodeFile(filePath, options);
                    } catch (OutOfMemoryError exception) {
                        exception.printStackTrace();

                    }
                    try {
                        scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
                    } catch (OutOfMemoryError exception) {
                        exception.printStackTrace();
                    }

                    float ratioX = actualWidth / (float) options.outWidth;
                    float ratioY = actualHeight / (float) options.outHeight;
                    float middleX = actualWidth / 2.0f;
                    float middleY = actualHeight / 2.0f;

                    Matrix scaleMatrix = new Matrix();
                    scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

                    Canvas canvas = new Canvas(scaledBitmap);
                    canvas.setMatrix(scaleMatrix);
                    canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(
                            Paint.FILTER_BITMAP_FLAG));

                    int orientation=imageCapture.getTargetRotation();

                    Log.e("EXIF", "Exif: " + orientation);
                    Matrix matrix = new Matrix();
                    if (orientation == 0) {
                        matrix.postRotate(90);
                        Log.e("EXIF", "Exif: " + orientation);
                    } else if (orientation == 2) {
                        matrix.postRotate(270);
                        Log.e("EXIF", "Exif: " + orientation);
                    } else if (orientation == 3) {
                        matrix.postRotate(180);
                        Log.e("EXIF", "Exif: " + orientation);
                    }
                    scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(),
                            matrix, true);


                    FileOutputStream out = null;
                    String filename = filePath;
                    try {
                        out = new FileOutputStream(file);
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        if (bmp != null) {
                            bmp.recycle();
                            bmp = null;
                        }
                        if (scaledBitmap != null) {
                            scaledBitmap.recycle();
                        }
                    }
                    Intent intent = new Intent();
                    intent.putExtra("RESULT", file.getAbsolutePath());
                    intent.putExtra("Type", mType);
                    setResult(RESULT_OK, intent);
                    Log.i(TAG, file.getAbsolutePath());
                    finish();
                } catch (IOException e) {
                    Log.w(TAG, "Cannot write to " + file, e);
                    setResult(RESULT_CANCELED, new Intent());
                    finish();
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                }

            }
        });
    }


    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }


    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }



    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    private void takePictureBurstBracketing() {
        List<CaptureRequest> requests = new ArrayList<>();
        double focus_bracketing_source_distance=0.1;
        double focus_bracketing_target_distance=0.5;
        int focus_bracketing_n_images=3;

        synchronized( background_camera_lock ) {

            try {

                int n_dummy_requests = 0;

                CaptureRequest.Builder stillBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL);
                // Needs to be TEMPLATE_MANUAL! Otherwise first image in burst may come out incorrectly (on Pixel 6 Pro,
                // the first image incorrectly had HDR+ applied, which we don't want here).
                // n.b., don't set RequestTagType.CAPTURE here - we only do it for the last of the burst captures (see below)
                camera_settings.setupBuilder(stillBuilder, true);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O  ) {
                    Boolean zsl = stillBuilder.get(CaptureRequest.CONTROL_ENABLE_ZSL);
                    Log.d(TAG, "CONTROL_ENABLE_ZSL: " + (zsl==null ? "null" : zsl));
                }

                clearPending();
                // shouldn't add preview surface as a target - see note in takePictureAfterPrecapture()
                // but also, adding the preview surface causes the dark/light exposures to be visible, which we don't want
                stillBuilder.addTarget(imageReader.getSurface());
                if( raw_todo )
                    stillBuilder.addTarget(imageReaderRaw.getSurface());

                // BURSTTYPE_FOCUS

                if( use_fake_precapture_mode && fake_precapture_torch_performed ) {
                    if( !camera_settings.has_iso )
                        stillBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                    stillBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                    test_fake_flash_photo++;
                }

                stillBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF); // just in case

                if( Math.abs(camera_settings.focus_distance - focus_bracketing_source_distance) < 1.0e-5 ) {

                }
                else if( Math.abs(camera_settings.focus_distance - focus_bracketing_target_distance) < 1.0e-5 ) {

                }
                else {
                    Log.d(TAG, "current focus matches neither source nor target");
                }

                List<Float> focus_distances = setupFocusBracketingDistances(focus_bracketing_source_distance, focus_bracketing_target_distance, focus_bracketing_n_images);
/*                if( focus_bracketing_add_infinity ) {
                    focus_distances.add(0.0f);
                }*/
                for(int i=0;i<focus_distances.size();i++) {
                    stillBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focus_distances.get(i));
                    //stillBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focus_distances.get(focus_distances.size()-1));
                    if( i == focus_distances.size()-1 ) {
                        stillBuilder.setTag(new RequestTagObject(RequestTagType.CAPTURE)); // set capture tag for last only
                    }
                    else {
                        // note, even if we didn't need to set CAPTURE_BURST_IN_PROGRESS, we'd still want
                        // to set a RequestTagObject (e.g., type NONE) so that it can be changed later,
                        // so that cancelling focus bracketing works
                        //stillBuilder.setTag(new RequestTagObject(RequestTagType.NONE));
                        stillBuilder.setTag(new RequestTagObject(RequestTagType.CAPTURE_BURST_IN_PROGRESS));
                    }
                    requests.add( stillBuilder.build() );

                    focus_bracketing_in_progress = true;
                }

                burst_single_request = false; // we set to false for focus bracketing, as we support bracketing with large numbers of images in this mode
                //burst_single_request = true; // test


                int n_burst = requests.size() - n_dummy_requests;
                int n_burst_total = n_burst;
                int n_burst_taken = 0;
                int n_burst_raw = raw_todo ? n_burst : 0;


                if( !previewIsVideoMode ) {
                    captureSession.stopRepeating(); // see note under takePictureAfterPrecapture()
                }
            }
            catch(CameraAccessException e) {
                    Log.e(TAG, "failed to take picture expo burst");
                    Log.e(TAG, "reason: " + e.getReason());
                    Log.e(TAG, "message: " + e.getMessage());

                e.printStackTrace();
                ok = false;
                jpeg_todo = false;
                raw_todo = false;
                picture_cb = null;
                push_take_picture_error_cb = take_picture_error_cb;
            }
            catch(IllegalStateException e) {

                e.printStackTrace();
                ok = false;
                jpeg_todo = false;
                raw_todo = false;
                picture_cb = null;
                // don't report error, as camera is closed or closing
            }
        }

        // need to call callbacks without a lock
        if( ok && picture_cb != null ) {
            if( MyDebug.LOG )
                Log.d(TAG, "call onStarted() in callback");
            picture_cb.onStarted();
        }

        if( ok ) {
            synchronized( background_camera_lock ) {
                if( camera == null || !hasCaptureSession() ) {
                    if( MyDebug.LOG )
                        Log.d(TAG, "no camera or capture session");
                    return;
                }
                try {
                    modified_from_camera_settings = true;
                    //setRepeatingRequest(requests.get(0));
                    if( use_expo_fast_burst && burst_type == BurstType.BURSTTYPE_EXPO ) { // alway use slow burst for focus bracketing
                        if( MyDebug.LOG )
                            Log.d(TAG, "using fast burst");
                        int sequenceId = captureSession.captureBurst(requests, previewCaptureCallback, handler);
                        if( MyDebug.LOG )
                            Log.d(TAG, "sequenceId: " + sequenceId);
                    }
                    else {
                        if( MyDebug.LOG )
                            Log.d(TAG, "using slow burst");
                        slow_burst_capture_requests = requests;
                        slow_burst_start_ms = System.currentTimeMillis();
                        captureSession.capture(requests.get(0), previewCaptureCallback, handler);
                    }

                    playSound(MediaActionSound.SHUTTER_CLICK); // play shutter sound asap, otherwise user has the illusion of being slow to take photos
                }
                catch(CameraAccessException e) {
                    if( MyDebug.LOG ) {
                        Log.e(TAG, "failed to take picture expo burst");
                        Log.e(TAG, "reason: " + e.getReason());
                        Log.e(TAG, "message: " + e.getMessage());
                    }
                    e.printStackTrace();
                    //noinspection UnusedAssignment
                    ok = false;
                    jpeg_todo = false;
                    raw_todo = false;
                    picture_cb = null;
                    push_take_picture_error_cb = take_picture_error_cb;
                }
                catch(IllegalStateException e) {
                    if( MyDebug.LOG )
                        Log.d(TAG, "captureSession already closed!");
                    e.printStackTrace();
                    //noinspection UnusedAssignment
                    ok = false;
                    jpeg_todo = false;
                    raw_todo = false;
                    picture_cb = null;
                    // don't report error, as camera is closed or closing
                }
            }
        }

        // need to call callbacks without a lock
        if( push_take_picture_error_cb != null ) {
            push_take_picture_error_cb.onError();
        }
    }


    private class CameraSettings {
        // keys that we need to store, to pass to the stillBuilder, but doesn't need to be passed to previewBuilder (should set sensible defaults)
        private int rotation;
        private byte jpeg_quality = 90;

        // keys that we have passed to the previewBuilder, that we need to store to also pass to the stillBuilder (should set sensible defaults, or use a has_ boolean if we don't want to set a default)
        private int scene_mode = CameraMetadata.CONTROL_SCENE_MODE_DISABLED;
        private int color_effect = CameraMetadata.CONTROL_EFFECT_MODE_OFF;
        private int white_balance = CameraMetadata.CONTROL_AWB_MODE_AUTO;
        private boolean has_antibanding;
        private int antibanding = CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_AUTO;
        private boolean has_edge_mode;
        private int edge_mode = CameraMetadata.EDGE_MODE_FAST;
        private boolean has_default_edge_mode;
        private Integer default_edge_mode;
        private boolean has_noise_reduction_mode;
        private int noise_reduction_mode = CameraMetadata.NOISE_REDUCTION_MODE_FAST;
        private boolean has_default_noise_reduction_mode;
        private Integer default_noise_reduction_mode;
        private int white_balance_temperature = 5000; // used for white_balance == CONTROL_AWB_MODE_OFF
        private String flash_value = "flash_off";
        private boolean has_iso;
        //private int ae_mode = CameraMetadata.CONTROL_AE_MODE_ON;
        //private int flash_mode = CameraMetadata.FLASH_MODE_OFF;
        private int iso;
        private long exposure_time = EXPOSURE_TIME_DEFAULT;
        private boolean has_aperture;
        private float aperture;
        private boolean has_control_zoom_ratio; // zoom for Android 11+
        private float control_zoom_ratio; // zoom for Android 11+
        private Rect scalar_crop_region; // zoom for older Android versions; no need for has_scalar_crop_region, as we can set to null instead
        private boolean has_ae_exposure_compensation;
        private int ae_exposure_compensation;
        private boolean has_af_mode;
        private int af_mode = CaptureRequest.CONTROL_AF_MODE_AUTO;
        private float focus_distance; // actual value passed to camera device (set to 0.0 if in infinity mode)
        private float focus_distance_manual; // saved setting when in manual mode (so if user switches to infinity mode and back, we'll still remember the manual focus distance)
        private boolean ae_lock;
        private boolean wb_lock;
        private MeteringRectangle[] af_regions; // no need for has_af_regions, as we can set to null instead
        private MeteringRectangle [] ae_regions; // no need for has_ae_regions, as we can set to null instead
        private boolean has_face_detect_mode;
        private int face_detect_mode = CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF;
        private Integer default_optical_stabilization;
        private boolean video_stabilization;
        //private TonemapProfile tonemap_profile = TonemapProfile.TONEMAPPROFILE_OFF;
        private float log_profile_strength; // for TONEMAPPROFILE_LOG
        private float gamma_profile; // for TONEMAPPROFILE_GAMMA
        private Integer default_tonemap_mode; // since we don't know what a device's tonemap mode is, we save it so we can switch back to it
        private Range<Integer> ae_target_fps_range;
        private long sensor_frame_duration;

        private int getExifOrientation() {
            int exif_orientation = ExifInterface.ORIENTATION_NORMAL;
            switch( (rotation + 360) % 360 ) {
                case 0:
                    exif_orientation = ExifInterface.ORIENTATION_NORMAL;
                    break;
                case 90:
                    exif_orientation = (getFacing() == Facing.FACING_FRONT) ?
                            ExifInterface.ORIENTATION_ROTATE_270 :
                            ExifInterface.ORIENTATION_ROTATE_90;
                    break;
                case 180:
                    exif_orientation = ExifInterface.ORIENTATION_ROTATE_180;
                    break;
                case 270:
                    exif_orientation = (getFacing() == Facing.FACING_FRONT) ?
                            ExifInterface.ORIENTATION_ROTATE_90 :
                            ExifInterface.ORIENTATION_ROTATE_270;
                    break;
                default:
                    // leave exif_orientation unchanged
                    if( MyDebug.LOG )
                        Log.e(TAG, "unexpected rotation: " + rotation);
                    break;
            }
            if( MyDebug.LOG ) {
                Log.d(TAG, "rotation: " + rotation);
                Log.d(TAG, "exif_orientation: " + exif_orientation);
            }
            return exif_orientation;
        }

        private void setupBuilder(CaptureRequest.Builder builder, boolean is_still) {
            //builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            //builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            //builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
            //builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            //builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);

            if( sessionType != SessionType.SESSIONTYPE_EXTENSION ) {
                builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
            }

            setSceneMode(builder);
            setColorEffect(builder);
            setWhiteBalance(builder);
            setAntiBanding(builder);
            setAEMode(builder, is_still);
            setControlZoomRatio(builder);
            setCropRegion(builder);
            setExposureCompensation(builder);
            setFocusMode(builder);
            setFocusDistance(builder);
            setAutoExposureLock(builder);
            setAutoWhiteBalanceLock(builder);
            setAFRegions(builder);
            setAERegions(builder);
            setFaceDetectMode(builder);
            setRawMode(builder);
            setStabilization(builder);
            setTonemapProfile(builder);

            if( is_still ) {
                if( location != null && sessionType != SessionType.SESSIONTYPE_EXTENSION ) {
                    // JPEG_GPS_LOCATION not supported for camera extensions, so instead this must
                    // be set by the caller when receiving the image data (see ImageSaver.modifyExif(),
                    // where we do this using ExifInterface.setGpsInfo()).
                    builder.set(CaptureRequest.JPEG_GPS_LOCATION, location);
                }
                builder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
                builder.set(CaptureRequest.JPEG_QUALITY, jpeg_quality);
            }

            setEdgeMode(builder);
            setNoiseReductionMode(builder);

            /*builder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_OFF);
            builder.set(CaptureRequest.SHADING_MODE, CaptureRequest.SHADING_MODE_OFF);
            builder.set(CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE, CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE_OFF);
            builder.set(CaptureRequest.HOT_PIXEL_MODE, CaptureRequest.HOT_PIXEL_MODE_OFF);*/

            /*builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_OFF);
            builder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_OFF);
            builder.set(CaptureRequest.EDGE_MODE, CaptureRequest.EDGE_MODE_OFF);
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
                builder.set(CaptureRequest.TONEMAP_MODE, CaptureRequest.TONEMAP_MODE_GAMMA_VALUE);
                builder.set(CaptureRequest.TONEMAP_GAMMA, 5.0f);
            }*/
            /*if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
                builder.set(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST, 0);
            }*/
            /*builder.set(CaptureRequest.CONTROL_EFFECT_MODE, CaptureRequest.CONTROL_EFFECT_MODE_OFF);
            builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_OFF);
            builder.set(CaptureRequest.HOT_PIXEL_MODE, CaptureRequest.HOT_PIXEL_MODE_OFF);
            builder.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_DISABLED);
            builder.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_HIGH_QUALITY);
            builder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY);
            builder.set(CaptureRequest.EDGE_MODE, CaptureRequest.EDGE_MODE_OFF);
            builder.set(CaptureRequest.SHADING_MODE, CaptureRequest.SHADING_MODE_OFF);
            builder.set(CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE, CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE_OFF);*/
            /*if( MyDebug.LOG ) {
                builder.set(CaptureRequest.TONEMAP_MODE, CaptureRequest.TONEMAP_MODE_HIGH_QUALITY);
                TonemapCurve original_curve = builder.get(CaptureRequest.TONEMAP_CURVE);
                for(int c=0;c<3;c++) {
                    Log.d(TAG, "color c = " + c);
                    for(int i=0;i<original_curve.getPointCount(c);i++) {
                        PointF point = original_curve.getPoint(c, i);
                        Log.d(TAG, "    i = " + i);
                        Log.d(TAG, "        in: " + point.x);
                        Log.d(TAG, "        out: " + point.y);
                    }
                }
            }*/
            /*if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
                builder.set(CaptureRequest.TONEMAP_MODE, CaptureRequest.TONEMAP_MODE_PRESET_CURVE);
                builder.set(CaptureRequest.TONEMAP_PRESET_CURVE, CaptureRequest.TONEMAP_PRESET_CURVE_SRGB);
            }*/

            if( MyDebug.LOG ) {
                if( is_still ) {
                    Integer nr_mode = builder.get(CaptureRequest.NOISE_REDUCTION_MODE);
                    Log.d(TAG, "nr_mode: " + (nr_mode==null ? "null" : nr_mode));
                    Integer edge_mode = builder.get(CaptureRequest.EDGE_MODE);
                    Log.d(TAG, "edge_mode: " + (edge_mode==null ? "null" : edge_mode));
                    Integer cc_mode = builder.get(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE);
                    Log.d(TAG, "cc_mode: " + (cc_mode==null ? "null" : cc_mode));
                    /*if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
                        Integer raw_sensitivity_boost = builder.get(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST);
                        Log.d(TAG, "raw_sensitivity_boost: " + (raw_sensitivity_boost==null ? "null" : raw_sensitivity_boost));
                    }*/
                }
                //Integer ois_mode = builder.get(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE);
                //Log.d(TAG, "ois_mode: " + (ois_mode==null ? "null" : ois_mode));
            }
        }

        private boolean setSceneMode(CaptureRequest.Builder builder) {
            if( MyDebug.LOG ) {
                Log.d(TAG, "setSceneMode");
                Log.d(TAG, "builder: " + builder);
            }

            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
                return false;
            }

            Integer current_scene_mode = builder.get(CaptureRequest.CONTROL_SCENE_MODE);
            if( has_face_detect_mode ) {
                // face detection mode overrides scene mode
                if( current_scene_mode == null || current_scene_mode != CameraMetadata.CONTROL_SCENE_MODE_FACE_PRIORITY ) {
                    if (MyDebug.LOG)
                        Log.d(TAG, "setting scene mode for face detection");
                    builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_USE_SCENE_MODE);
                    builder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_FACE_PRIORITY);
                    return true;
                }
            }
            else if( current_scene_mode == null || current_scene_mode != scene_mode ) {
                if( MyDebug.LOG )
                    Log.d(TAG, "setting scene mode: " + scene_mode);
                if( scene_mode == CameraMetadata.CONTROL_SCENE_MODE_DISABLED ) {
                    builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                }
                else {
                    builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_USE_SCENE_MODE);
                }
                builder.set(CaptureRequest.CONTROL_SCENE_MODE, scene_mode);
                return true;
            }
            return false;
        }

        private boolean setColorEffect(CaptureRequest.Builder builder) {
            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
            }
            /*else if( builder.get(CaptureRequest.CONTROL_EFFECT_MODE) == null && color_effect == CameraMetadata.CONTROL_EFFECT_MODE_OFF ) {
                // can leave off
            }*/
            else if( builder.get(CaptureRequest.CONTROL_EFFECT_MODE) == null || builder.get(CaptureRequest.CONTROL_EFFECT_MODE) != color_effect ) {
                if( MyDebug.LOG )
                    Log.d(TAG, "setting color effect: " + color_effect);
                builder.set(CaptureRequest.CONTROL_EFFECT_MODE, color_effect);
                return true;
            }
            return false;
        }

        private boolean setWhiteBalance(CaptureRequest.Builder builder) {
            boolean changed = false;
            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
            }
            /*else if( builder.get(CaptureRequest.CONTROL_AWB_MODE) == null && white_balance == CameraMetadata.CONTROL_AWB_MODE_AUTO ) {
                // can leave off
            }*/
            else if( builder.get(CaptureRequest.CONTROL_AWB_MODE) == null || builder.get(CaptureRequest.CONTROL_AWB_MODE) != white_balance ) {
                if( MyDebug.LOG )
                    Log.d(TAG, "setting white balance: " + white_balance);
                builder.set(CaptureRequest.CONTROL_AWB_MODE, white_balance);
                changed = true;
            }
            if( white_balance == CameraMetadata.CONTROL_AWB_MODE_OFF ) {
                if( MyDebug.LOG )
                    Log.d(TAG, "setting white balance temperature: " + white_balance_temperature);
                // manual white balance
                RggbChannelVector rggbChannelVector = convertTemperatureToRggbVector(white_balance_temperature);
                builder.set(CaptureRequest.COLOR_CORRECTION_MODE, CameraMetadata.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX);
                builder.set(CaptureRequest.COLOR_CORRECTION_GAINS, rggbChannelVector);
                changed = true;
            }
            return changed;
        }

        private boolean setAntiBanding(CaptureRequest.Builder builder) {
            boolean changed = false;
            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
            }
            else if( has_antibanding ) {
                if( builder.get(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE) == null || builder.get(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE) != antibanding ) {
                    if( MyDebug.LOG )
                        Log.d(TAG, "setting antibanding: " + antibanding);
                    builder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, antibanding);
                    changed = true;
                }
            }
            return changed;
        }

        private boolean setEdgeMode(CaptureRequest.Builder builder) {
            if( MyDebug.LOG ) {
                Log.d(TAG, "setEdgeMode");
                Log.d(TAG, "has_default_edge_mode: " + has_default_edge_mode);
                Log.d(TAG, "default_edge_mode: " + default_edge_mode);
            }
            boolean changed = false;
            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
            }
            else if( has_edge_mode ) {
                if( !has_default_edge_mode ) {
                    // save the default_edge_mode edge_mode
                    has_default_edge_mode = true;
                    default_edge_mode = builder.get(CaptureRequest.EDGE_MODE);
                    if( MyDebug.LOG )
                        Log.d(TAG, "default_edge_mode: " + default_edge_mode);
                }
                if( builder.get(CaptureRequest.EDGE_MODE) == null || builder.get(CaptureRequest.EDGE_MODE) != edge_mode ) {
                    if( MyDebug.LOG )
                        Log.d(TAG, "setting edge_mode: " + edge_mode);
                    builder.set(CaptureRequest.EDGE_MODE, edge_mode);
                    changed = true;
                }
                else {
                    if( MyDebug.LOG )
                        Log.d(TAG, "edge_mode was already set: " + edge_mode);
                }
            }
            else if( is_samsung_s7 ) {
                if( MyDebug.LOG )
                    Log.d(TAG, "set EDGE_MODE_OFF");
                // see https://sourceforge.net/p/opencamera/discussion/general/thread/48bd836b/ ,
                // https://stackoverflow.com/questions/36028273/android-camera-api-glossy-effect-on-galaxy-s7
                // need EDGE_MODE_OFF to avoid a "glow" effect
                builder.set(CaptureRequest.EDGE_MODE, CaptureRequest.EDGE_MODE_OFF);
            }
            else if( has_default_edge_mode ) {
                if( builder.get(CaptureRequest.EDGE_MODE) != null && !builder.get(CaptureRequest.EDGE_MODE).equals(default_edge_mode) ) {
                    builder.set(CaptureRequest.EDGE_MODE, default_edge_mode);
                    changed = true;
                }
            }
            return changed;
        }

        private boolean setNoiseReductionMode(CaptureRequest.Builder builder) {
            if( MyDebug.LOG ) {
                Log.d(TAG, "setNoiseReductionMode");
                Log.d(TAG, "has_default_noise_reduction_mode: " + has_default_noise_reduction_mode);
                Log.d(TAG, "default_noise_reduction_mode: " + default_noise_reduction_mode);
            }
            boolean changed = false;
            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
            }
            else if( has_noise_reduction_mode ) {
                if( !has_default_noise_reduction_mode ) {
                    // save the default_noise_reduction_mode noise_reduction_mode
                    has_default_noise_reduction_mode = true;
                    default_noise_reduction_mode = builder.get(CaptureRequest.NOISE_REDUCTION_MODE);
                    if( MyDebug.LOG )
                        Log.d(TAG, "default_noise_reduction_mode: " + default_noise_reduction_mode);
                }
                if( builder.get(CaptureRequest.NOISE_REDUCTION_MODE) == null || builder.get(CaptureRequest.NOISE_REDUCTION_MODE) != noise_reduction_mode ) {
                    if( MyDebug.LOG )
                        Log.d(TAG, "setting noise_reduction_mode: " + noise_reduction_mode);
                    builder.set(CaptureRequest.NOISE_REDUCTION_MODE, noise_reduction_mode);
                    changed = true;
                }
                else {
                    if( MyDebug.LOG )
                        Log.d(TAG, "noise_reduction_mode was already set: " + noise_reduction_mode);
                }
            }
            else if( is_samsung_s7 ) {
                if( MyDebug.LOG )
                    Log.d(TAG, "set NOISE_REDUCTION_MODE_OFF");
                // see https://sourceforge.net/p/opencamera/discussion/general/thread/48bd836b/ ,
                // https://stackoverflow.com/questions/36028273/android-camera-api-glossy-effect-on-galaxy-s7
                // need NOISE_REDUCTION_MODE_OFF to avoid excessive blurring
                builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_OFF);
            }
            else if( has_default_noise_reduction_mode ) {
                if( builder.get(CaptureRequest.NOISE_REDUCTION_MODE) != null && !builder.get(CaptureRequest.NOISE_REDUCTION_MODE).equals(default_noise_reduction_mode)) {
                    builder.set(CaptureRequest.NOISE_REDUCTION_MODE, default_noise_reduction_mode);
                    changed = true;
                }
            }
            return changed;
        }

        private boolean setAperture(CaptureRequest.Builder builder) {
            if( MyDebug.LOG )
                Log.d(TAG, "setAperture");
            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
            }
            else if( has_aperture ) {
                if( MyDebug.LOG )
                    Log.d(TAG, "    aperture: " + aperture);
                builder.set(CaptureRequest.LENS_APERTURE, aperture);
                return true;
            }
            // don't set at all if has_aperture==false
            return false;
        }

        @SuppressWarnings("SameReturnValue")
        private boolean setAEMode(CaptureRequest.Builder builder, boolean is_still) {
            if( MyDebug.LOG )
                Log.d(TAG, "setAEMode");

            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
                return false;
            }

            if( has_iso ) {
                if( MyDebug.LOG ) {
                    Log.d(TAG, "manual mode");
                    Log.d(TAG, "iso: " + iso);
                    Log.d(TAG, "exposure_time: " + exposure_time);
                }
                builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                builder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);
                long actual_exposure_time = exposure_time;
                if( !is_still ) {
                    // if this isn't for still capture, have a max exposure time of 1/12s
                    actual_exposure_time = Math.min(exposure_time, max_preview_exposure_time_c);
                    if( MyDebug.LOG )
                        Log.d(TAG, "actually using exposure_time of: " + actual_exposure_time);
                }
                builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, actual_exposure_time);
                if (sensor_frame_duration > 0) {
                    builder.set(CaptureRequest.SENSOR_FRAME_DURATION, sensor_frame_duration);
                }
                //builder.set(CaptureRequest.SENSOR_FRAME_DURATION, 1000000000L);
                //builder.set(CaptureRequest.SENSOR_FRAME_DURATION, 0L);
                // only need to account for FLASH_MODE_TORCH, otherwise we use fake flash mode for manual ISO
                if( flash_value.equals("flash_torch") ) {
                    builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                }
                else {
                    builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                }
            }
            else {
                if( MyDebug.LOG ) {
                    Log.d(TAG, "auto mode");
                    Log.d(TAG, "flash_value: " + flash_value);
                }
                if( ae_target_fps_range != null ) {
                    Log.d(TAG, "set ae_target_fps_range: " + ae_target_fps_range);
                    builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, ae_target_fps_range);
                }

                // prefer to set flash via the ae mode (otherwise get even worse results), except for torch which we can't
                //noinspection DuplicateBranchesInSwitch
                switch(flash_value) {
                    case "flash_off":
                        builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                        builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                        break;
                    case "flash_auto":
                        // note we set this even in fake flash mode (where we manually turn torch on and off to simulate flash) so we
                        // can read the FLASH_REQUIRED state to determine if flash is required
                    /*if( use_fake_precapture || CameraController2.this.want_expo_bracketing )
                        builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                    else*/
                        builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                        break;
                    case "flash_on":
                        // see note above for "flash_auto" for why we set this even fake flash mode - arguably we don't need to know
                        // about FLASH_REQUIRED in flash_on mode, but we set it for consistency...
                    /*if( use_fake_precapture || CameraController2.this.want_expo_bracketing )
                        builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                    else*/
                        builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                        builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                        break;
                    case "flash_torch":
                        builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                        builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                        break;
                    case "flash_red_eye":
                        // not supported for expo bracketing or burst
                        if( CameraController2.this.burst_type != BurstType.BURSTTYPE_NONE )
                            builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                        else
                            builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE);
                        builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                        break;
                    case "flash_frontscreen_auto":
                    case "flash_frontscreen_on":
                    case "flash_frontscreen_torch":
                        builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                        builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                        break;
                }
            }
            return true;
        }

        private void setControlZoomRatio(CaptureRequest.Builder builder) {
            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
            }
            else if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && has_control_zoom_ratio ) {
                builder.set(CaptureRequest.CONTROL_ZOOM_RATIO, control_zoom_ratio);
            }
        }

        private void setCropRegion(CaptureRequest.Builder builder) {
            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
            }
            else if( scalar_crop_region != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.R ) {
                builder.set(CaptureRequest.SCALER_CROP_REGION, scalar_crop_region);
            }
        }

        private boolean setExposureCompensation(CaptureRequest.Builder builder) {
            if( !has_ae_exposure_compensation )
                return false;
            if( has_iso ) {
                if( MyDebug.LOG )
                    Log.d(TAG, "don't set exposure compensation in manual iso mode");
                return false;
            }
            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
                return false;
            }
            if( builder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION) == null || ae_exposure_compensation != builder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION) ) {
                if( MyDebug.LOG )
                    Log.d(TAG, "change exposure to " + ae_exposure_compensation);
                builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ae_exposure_compensation);
                return true;
            }
            return false;
        }

        private void setFocusMode(CaptureRequest.Builder builder) {
            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
            }
            else if( has_af_mode ) {
                if( MyDebug.LOG )
                    Log.d(TAG, "change af mode to " + af_mode);
                builder.set(CaptureRequest.CONTROL_AF_MODE, af_mode);
            }
            else {
                if( MyDebug.LOG ) {
                    Log.d(TAG, "af mode left at " + builder.get(CaptureRequest.CONTROL_AF_MODE));
                }
            }
        }

        private void setFocusDistance(CaptureRequest.Builder builder) {
            if( MyDebug.LOG )
                Log.d(TAG, "change focus distance to " + focus_distance);
            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
            }
            else {
                builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focus_distance);
            }
        }

        private void setAutoExposureLock(CaptureRequest.Builder builder) {
            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
            }
            else {
                builder.set(CaptureRequest.CONTROL_AE_LOCK, ae_lock);
            }
        }

        private void setAutoWhiteBalanceLock(CaptureRequest.Builder builder) {
            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
            }
            else {
                builder.set(CaptureRequest.CONTROL_AWB_LOCK, wb_lock);
            }
        }

        private void setAFRegions(CaptureRequest.Builder builder) {
            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
            }
            else if( af_regions != null && characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) > 0 ) {
                builder.set(CaptureRequest.CONTROL_AF_REGIONS, af_regions);
            }
        }

        private void setAERegions(CaptureRequest.Builder builder) {
            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
            }
            else if( ae_regions != null && characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE) > 0 ) {
                builder.set(CaptureRequest.CONTROL_AE_REGIONS, ae_regions);
            }
        }

        private void setFaceDetectMode(CaptureRequest.Builder builder) {
            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
            }
            else if( has_face_detect_mode )
                builder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, face_detect_mode);
            else
                builder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF);
        }

        private void setRawMode(CaptureRequest.Builder builder) {
            // DngCreator says "For best quality DNG files, it is strongly recommended that lens shading map output is enabled if supported"
            // docs also say "ON is always supported on devices with the RAW capability", so we don't check for STATISTICS_LENS_SHADING_MAP_MODE_ON being available
            if( want_raw && !previewIsVideoMode ) {
                builder.set(CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE, CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE_ON);
            }
        }

        private void setStabilization(CaptureRequest.Builder builder) {
            if( MyDebug.LOG )
                Log.d(TAG, "setStabilization: " + video_stabilization);

            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
                return;
            }

            builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, video_stabilization ? CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON : CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF);
            if( supports_optical_stabilization ) {
                if( video_stabilization ) {
                    // should also disable OIS
                    if( default_optical_stabilization == null ) {
                        // save the default optical_stabilization
                        default_optical_stabilization = builder.get(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE);
                        if( MyDebug.LOG )
                            Log.d(TAG, "default_optical_stabilization: " + default_optical_stabilization);
                    }
                    builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF);
                }
                else if( default_optical_stabilization != null ) {
                    if( builder.get(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE) != null && !builder.get(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE).equals(default_optical_stabilization) ) {
                        if( MyDebug.LOG )
                            Log.d(TAG, "set optical stabilization back to: " + default_optical_stabilization);
                        builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, default_optical_stabilization);
                    }
                }
            }
        }

        private float getLogProfile(float in) {
            //final float black_level = 4.0f/255.0f;
            //final float power = 1.0f/2.2f;
            final float log_A = log_profile_strength;
            /*float out;
            if( in <= black_level ) {
                out = in;
            }
            else {
                float in_m = (in - black_level) / (1.0f - black_level);
                out = (float) (Math.log1p(log_A * in_m) / Math.log1p(log_A));
                out = black_level + (1.0f - black_level)*out;
            }*/
            float out = (float) (Math.log1p(log_A * in) / Math.log1p(log_A));

            // apply gamma
            // update: no longer need to do this with improvements made in 1.48 onwards
            //out = (float)Math.pow(out, power);
            //out = Math.max(out, 0.5f);

            return out;
        }

        private float getGammaProfile(float in) {
            return (float)Math.pow(in, 1.0f/gamma_profile);
        }

        private void setTonemapProfile(CaptureRequest.Builder builder) {
            if( MyDebug.LOG ) {
                Log.d(TAG, "setTonemapProfile");
                Log.d(TAG, "tonemap_profile: " + tonemap_profile);
                Log.d(TAG, "log_profile_strength: " + log_profile_strength);
                Log.d(TAG, "gamma_profile: " + gamma_profile);
                Log.d(TAG, "default_tonemap_mode: " + default_tonemap_mode);
            }
            boolean have_tonemap_profile = tonemap_profile != TonemapProfile.TONEMAPPROFILE_OFF;
            if( tonemap_profile == TonemapProfile.TONEMAPPROFILE_LOG && log_profile_strength == 0.0f )
                have_tonemap_profile = false;
            else if( tonemap_profile == TonemapProfile.TONEMAPPROFILE_GAMMA && gamma_profile == 0.0f )
                have_tonemap_profile = false;

            // to use test_new, also need to uncomment the test code in setFocusValue() to call setTonemapProfile()
            //boolean test_new = this.af_mode == CaptureRequest.CONTROL_AF_MODE_AUTO; // testing

            //if( test_new )
            //    have_tonemap_profile = false;

            if( sessionType == SessionType.SESSIONTYPE_EXTENSION ) {
                // don't set for extensions
            }
            else if( have_tonemap_profile ) {
                if( default_tonemap_mode == null ) {
                    // save the default tonemap_mode
                    default_tonemap_mode = builder.get(CaptureRequest.TONEMAP_MODE);
                    if( MyDebug.LOG )
                        Log.d(TAG, "default_tonemap_mode: " + default_tonemap_mode);
                }

                final boolean use_preset_curve = true;
                //final boolean use_preset_curve = false; // test
                //final boolean use_preset_curve = test_new; // test
                if( use_preset_curve && tonemap_profile == TonemapProfile.TONEMAPPROFILE_REC709 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
                    if( MyDebug.LOG )
                        Log.d(TAG, "set TONEMAP_PRESET_CURVE_REC709");
                    builder.set(CaptureRequest.TONEMAP_MODE, CaptureRequest.TONEMAP_MODE_PRESET_CURVE);
                    builder.set(CaptureRequest.TONEMAP_PRESET_CURVE, CaptureRequest.TONEMAP_PRESET_CURVE_REC709);
                }
                else if( use_preset_curve && tonemap_profile == TonemapProfile.TONEMAPPROFILE_SRGB && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
                    if( MyDebug.LOG )
                        Log.d(TAG, "set TONEMAP_PRESET_CURVE_SRGB");
                    builder.set(CaptureRequest.TONEMAP_MODE, CaptureRequest.TONEMAP_MODE_PRESET_CURVE);
                    builder.set(CaptureRequest.TONEMAP_PRESET_CURVE, CaptureRequest.TONEMAP_PRESET_CURVE_SRGB);
                }
                else {
                    if( MyDebug.LOG )
                        Log.d(TAG, "handle via TONEMAP_MODE_CONTRAST_CURVE / TONEMAP_CURVE");
                    float [] values = null;
                    switch( tonemap_profile ) {
                        case TONEMAPPROFILE_REC709:
                            // y = 4.5x if x < 0.018, else y = 1.099*x^0.45 - 0.099
                            float [] x_values = new float[] {
                                    0.0000f, 0.0667f, 0.1333f, 0.2000f,
                                    0.2667f, 0.3333f, 0.4000f, 0.4667f,
                                    0.5333f, 0.6000f, 0.6667f, 0.7333f,
                                    0.8000f, 0.8667f, 0.9333f, 1.0000f
                            };
                            values = new float[2*x_values.length];
                            int c = 0;
                            for(float x_value : x_values) {
                                float out;
                                if( x_value < 0.018f ) {
                                    out = 4.5f * x_value;
                                }
                                else {
                                    out = (float)(1.099*Math.pow(x_value, 0.45) - 0.099);
                                }
                                values[c++] = x_value;
                                values[c++] = out;
                            }
                            break;
                        case TONEMAPPROFILE_SRGB:
                            values = new float [] {
                                    0.0000f, 0.0000f, 0.0667f, 0.2864f, 0.1333f, 0.4007f, 0.2000f, 0.4845f,
                                    0.2667f, 0.5532f, 0.3333f, 0.6125f, 0.4000f, 0.6652f, 0.4667f, 0.7130f,
                                    0.5333f, 0.7569f, 0.6000f, 0.7977f, 0.6667f, 0.8360f, 0.7333f, 0.8721f,
                                    0.8000f, 0.9063f, 0.8667f, 0.9389f, 0.9333f, 0.9701f, 1.0000f, 1.0000f
                            };
                            break;
                        case TONEMAPPROFILE_LOG:
                        case TONEMAPPROFILE_GAMMA:
                        {
                            // better to use uniformly spaced values, otherwise we get a weird looking effect - this can be
                            // seen most prominently when using gamma 1.0f, which should look linear (and hence be independent
                            // of the x values we use)
                            // can be reproduced on at least OnePlus 3T and Galaxy S10e (although the exact behaviour of the
                            // poor results is different on those devices)
                            int n_values = tonemap_log_max_curve_points_c;
                            if( is_samsung ) {
                                // unfortunately odd bug on Samsung devices (at least S7 and S10e) where if more than 32 control points,
                                // the maximum brightness value is reduced (can best be seen with 64 points, and using gamma==1.0)
                                // note that Samsung devices also need at least 16 control points - or in some cases 32, see comments for
                                // enforceMinTonemapCurvePoints().
                                // 32 is better than 16 anyway, as better to have more points for finer curve where possible.
                                n_values = 32;
                            }
                            //int n_values = test_new ? 32 : 128;
                            //int n_values = 32;
                            if( MyDebug.LOG )
                                Log.d(TAG, "n_values: " + n_values);
                            values = new float [2*n_values];
                            for(int i=0;i<n_values;i++) {
                                float in = ((float)i) / (n_values-1.0f);
                                float out = (tonemap_profile==TonemapProfile.TONEMAPPROFILE_LOG) ? getLogProfile(in) : getGammaProfile(in);
                                values[2*i] = in;
                                values[2*i+1] = out;
                            }
                        }

                        /*if( test_new ) {
                            // if changing this, make sure we don't exceed tonemap_log_max_curve_points_c
                            // we want:
                            // 0-15: step 1 (16 values)
                            // 16-47: step 2 (16 values)
                            // 48-111: step 4 (16 values)
                            // 112-231 : step 8 (15 values)
                            // 232-255: step 24 (1 value)
                            int step = 1, c = 0;
                            //int step = 4, c = 0;
                            //int step = test_new ? 4 : 1, c = 0;
                            values = new float[2*tonemap_log_max_curve_points_c];
                            for(int i=0;i<232;i+=step) {
                                float in = ((float)i) / 255.0f;
                                float out = (tonemap_profile==TonemapProfile.TONEMAPPROFILE_LOG) ? getLogProfile(in) : getGammaProfile(in);
                                if( tonemap_profile==TonemapProfile.TONEMAPPROFILE_LOG )
                                    out = (float)Math.pow(out, 1.0f/2.2f);
                                values[c++] = in;
                                values[c++] = out;
                                if( (c/2) % 16 == 0 ) {
                                    step *= 2;
                                }
                            }
                            values[c++] = 1.0f;
                            float last_out = (tonemap_profile==TonemapProfile.TONEMAPPROFILE_LOG) ? getLogProfile(1.0f) : getGammaProfile(1.0f);
                            if( tonemap_profile==TonemapProfile.TONEMAPPROFILE_LOG )
                                last_out = (float)Math.pow(last_out, 1.0f/2.2f);
                            values[c++] = last_out;
                            values = Arrays.copyOfRange(values,0,c);
                        }*/
                        /*if( test_new )
                        {
                            // x values are ranged 0 to 255
                            float [] x_values = new float[] {
                                    0.0f, 4.0f, 8.0f, 12.0f, 16.0f, 20.0f, 24.0f, 28.0f,
                                    //0.0f, 8.0f, 16.0f, 24.0f,
                                    32.0f, 40.0f, 48.0f, 56.0f,
                                    64.0f, 72.0f, 80.0f, 88.0f,
                                    96.0f, 104.0f, 112.0f, 120.0f,
                                    128.0f, 136.0f, 144.0f, 152.0f,
                                    160.0f, 168.0f, 176.0f, 184.0f,
                                    192.0f, 200.0f, 208.0f, 216.0f,
                                    224.0f, 232.0f, 240.0f, 248.0f,
                                    255.0f
                            };
                            values = new float[2*x_values.length];
                            c = 0;
                            for(float x_value : x_values) {
                                float in = x_value / 255.0f;
                                float out = (tonemap_profile==TonemapProfile.TONEMAPPROFILE_LOG) ? getLogProfile(in) : getGammaProfile(in);
                                values[c++] = in;
                                values[c++] = out;
                            }
                        }*/
                        /*if( test_new )
                        {
                            values = new float [2*256];
                            step = 8;
                            c = 0;
                            for(int i=0;i<254;i+=step) {
                                float in = ((float)i) / 255.0f;
                                float out = (tonemap_profile==TonemapProfile.TONEMAPPROFILE_LOG) ? getLogProfile(in) : getGammaProfile(in);
                                values[c++] = in;
                                values[c++] = out;
                            }
                            values[c++] = 1.0f;
                            values[c++] = (tonemap_profile==TonemapProfile.TONEMAPPROFILE_LOG) ? getLogProfile(1.0f) : getGammaProfile(1.0f);
                            values = Arrays.copyOfRange(values,0,c);
                        }*/
                        if( MyDebug.LOG ) {
                            int n_values = values.length/2;
                            for(int i=0;i<n_values;i++) {
                                float in = values[2*i];
                                float out = values[2*i+1];
                                Log.d(TAG, "i = " + i);
                                //Log.d(TAG, "    in: " + (int)(in*255.0f+0.5f));
                                //Log.d(TAG, "    out: " + (int)(out*255.0f+0.5f));
                                Log.d(TAG, "    in: " + (in*255.0f));
                                Log.d(TAG, "    out: " + (out*255.0f));
                            }
                        }
                        break;
                        case TONEMAPPROFILE_JTVIDEO:
                            values = jtvideo_values;
                            if( MyDebug.LOG )
                                Log.d(TAG, "setting JTVideo profile");
                            break;
                        case TONEMAPPROFILE_JTLOG:
                            values = jtlog_values;
                            if( MyDebug.LOG )
                                Log.d(TAG, "setting JTLog profile");
                            break;
                        case TONEMAPPROFILE_JTLOG2:
                            values = jtlog2_values;
                            if( MyDebug.LOG )
                                Log.d(TAG, "setting JTLog2 profile");
                            break;
                    }

                    // sRGB:
                    /*values = new float []{0.0000f, 0.0000f, 0.0667f, 0.2864f, 0.1333f, 0.4007f, 0.2000f, 0.4845f,
                            0.2667f, 0.5532f, 0.3333f, 0.6125f, 0.4000f, 0.6652f, 0.4667f, 0.7130f,
                            0.5333f, 0.7569f, 0.6000f, 0.7977f, 0.6667f, 0.8360f, 0.7333f, 0.8721f,
                            0.8000f, 0.9063f, 0.8667f, 0.9389f, 0.9333f, 0.9701f, 1.0000f, 1.0000f};*/
                    /*values = new float []{0.0000f, 0.0000f, 0.05f, 0.3f, 0.1f, 0.4f, 0.2000f, 0.4845f,
                            0.2667f, 0.5532f, 0.3333f, 0.6125f, 0.4000f, 0.6652f,
                            0.5f, 0.78f, 1.0000f, 1.0000f};*/
                    /*values = new float []{0.0f, 0.0f, 0.05f, 0.4f, 0.1f, 0.54f, 0.2f, 0.6f, 0.3f, 0.65f, 0.4f, 0.7f,
                            0.5f, 0.78f, 1.0f, 1.0f};*/
                    /*values = new float[]{0.0f, 0.0f, 0.0667f, 0.2864f, 0.1333f, 0.4007f, 0.2000f, 0.4845f,
                            1.0f, 1.0f};*/
                    //values = new float []{0.0f, 0.5f, 0.05f, 0.6f, 0.1f, 0.7f, 0.2f, 0.8f, 0.5f, 0.9f, 1.0f, 1.0f};
                    /*values = new float []{0.0f, 0.0f,
                            0.05f, 0.05f,
                            0.1f, 0.1f,
                            0.15f, 0.15f,
                            0.2f, 0.2f,
                            0.25f, 0.25f,
                            0.3f, 0.3f,
                            0.35f, 0.35f,
                            0.4f, 0.4f,
                            0.5f, 0.5f,
                            0.6f, 0.6f,
                            0.7f, 0.7f,
                            0.8f, 0.8f,
                            0.9f, 0.9f,
                            0.95f, 0.95f,
                            1.0f, 1.0f};*/
                    //values = enforceMinTonemapCurvePoints(new float[]{0.0f, 0.0f, 1.0f, 1.0f});
                    //values = enforceMinTonemapCurvePoints(values);

                    if( MyDebug.LOG  )
                        Log.d(TAG, "values: " + Arrays.toString(values));
                    if( values != null ) {
                        builder.set(CaptureRequest.TONEMAP_MODE, CaptureRequest.TONEMAP_MODE_CONTRAST_CURVE);
                        TonemapCurve tonemap_curve = new TonemapCurve(values, values, values);
                        builder.set(CaptureRequest.TONEMAP_CURVE, tonemap_curve);
                        test_used_tonemap_curve = true;
                    }
                    else {
                        Log.e(TAG, "unknown log type: " + tonemap_profile);
                    }
                }
            }
            else if( default_tonemap_mode != null ) {
                builder.set(CaptureRequest.TONEMAP_MODE, default_tonemap_mode);
            }
        }

        // n.b., if we add more methods, remember to update setupBuilder() above!
    }
}