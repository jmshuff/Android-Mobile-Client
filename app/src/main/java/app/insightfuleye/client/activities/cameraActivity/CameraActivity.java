package app.insightfuleye.client.activities.cameraActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.insightfuleye.client.R;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.app.IntelehealthApplication;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    public static final int TAKE_IMAGE = 205;
    public static final int TAKE_IMAGE_RIGHT = 207;
    public static final int TAKE_IMAGE_LEFT = 208;
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
    /**
     * Bundle key used for the {@link String} showing custom dialog
     * message before starting the camera.
     */
    public static final String SHOW_DIALOG_MESSAGE = "DEFAULT_DLG";

    private final String TAG = CameraActivity.class.getSimpleName();
    private String cameraId;
    private AutoFitTextureView textureView;
    private FloatingActionButton takePictureBtn;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    Context context = CameraActivity.this;

    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder previewBuilder;
    private Size previewSize;
    public CameraCharacteristics characteristics;
    public CameraManager manager;
    public CameraFeatures camera_features= new CameraFeatures();

    private ImageReader imageReader;
    //Pass Custom File Name Using intent.putExtra(CameraActivity.SET_IMAGE_NAME, "Image Name");
    private String mImageName = null;
    //Pass Dialog Message Using intent.putExtra(CameraActivity.SET_IMAGE_NAME, "Dialog Message");
    private String mDialogMessage = null;
    //Pass Custom File Path Using intent.putExtra(CameraActivitgy.SET_IMAGE_PATH, "Image Path");
    private String mFilePath = null;
    private boolean fabClickFlag = true;
    protected CameraDevice cameraDevice;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private static final int STATE_WAITING_LOCK = 1;

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * Camera state: Picture was taken.
     */
    private static final int STATE_PICTURE_TAKEN = 4;
    private int mState = STATE_PREVIEW;

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(SET_IMAGE_NAME))
                mImageName = extras.getString(SET_IMAGE_NAME);
            if (extras.containsKey(SHOW_DIALOG_MESSAGE))
                mDialogMessage = extras.getString(SHOW_DIALOG_MESSAGE);
            if (extras.containsKey(SET_IMAGE_PATH))
                mFilePath = extras.getString(SET_IMAGE_PATH);
        }

        setContentView(R.layout.activity_camera);
        textureView = findViewById(R.id.view_finder);
        takePictureBtn = findViewById(R.id.take_picture);
        textureView.setSurfaceTextureListener(textureListener);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);

        }
        //if (mCameraView != null) mCameraView.addCallback(stateCallback);
        if (takePictureBtn != null) {
            takePictureBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int[] capabilities = characteristics
                            .get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
                    boolean isManualFocusSupported = false;
                    for (int cap : capabilities) {
                        if (cap == CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR) {
                            isManualFocusSupported = true;
                            break;
                        }
                    }
                    if (isManualFocusSupported) {
                        Log.d("manualFocus", "true");
                        //runPrecaptureSequence();
                        takePictureBurst();
                        //takePicture();
                    } else {
                        Log.d("manualFocus", "false");
                        //takePicture();
                        //runPrecaptureSequence();
                        takePictureBurst();
                    }
                }
            });
        }
    }

    public CameraFeatures getCameraFeatures(){
        Float minimum_focus_distance=characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);//maybenullonsomedevices
        if(minimum_focus_distance!=null){
            camera_features.minimum_focus_distance=minimum_focus_distance;
        }
        else{
            camera_features.minimum_focus_distance=0.0f;
        }


        int [] supported_focus_modes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES); // Android format
        camera_features.supported_focus_values = convertFocusModesToValues(supported_focus_modes, camera_features.minimum_focus_distance); // convert to our format (also resorts)
        if( camera_features.supported_focus_values != null && camera_features.supported_focus_values.contains("focus_mode_manual2") ) {
            camera_features.supports_focus_bracketing = true;
            Log.d("supportsFocusBracketing", "true");
        }
        /*if( camera_features.supported_focus_values != null ) {
            // prefer continuous focus mode
            if( camera_features.supported_focus_values.contains("focus_mode_continuous_picture") ) {
                //initial focus is used to set the focus setting used in preview mode
                initial_focus_mode = "focus_mode_continuous_picture";
            }
            else {
                // just go with the first one
                initial_focus_mode = camera_features.supported_focus_values.get(0);
            }
        }
        else {
            initial_focus_mode = null;
        }*/
        return camera_features;
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        takePictureBurst();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            takePicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                        mState = STATE_PICTURE_TAKEN;
                        takePictureBurst();
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        takePictureBurst();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            getCameraFeatures();
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            //Toast.makeText(Camera2Activity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreview() {
        try {
            Log.d("createCameraPreview", "enter");
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(previewSize.getHeight(), previewSize.getWidth());
            Log.d("FinalPreviewSize", previewSize.getWidth() + " " + previewSize.getHeight());
            Surface surface = new Surface(texture);
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    //updatePreview();
                    cameraCaptureSessions = cameraCaptureSession;
                    try {
                        cameraCaptureSessions.setRepeatingRequest(previewBuilder.build(), mCaptureCallback, mBackgroundHandler); //capture callback was null, JS changed
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CameraActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size getOptimalPreviewSize(Size[] sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            Log.d("previewSizes", size.getWidth() + " " + size.getHeight());
            double ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private Size getOptimalCaptureSize(Size[] sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        int maxSize = 0;

        // Try to find an size match aspect ratio and the max size
        for (Size size : sizes) {
            double ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (size.getHeight() > maxSize && size.getHeight()<2000) {
                optimalSize = size;
                maxSize = size.getHeight();
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement and just get the biggest size
        if (optimalSize == null) {
            for (Size size : sizes) {
                if (size.getHeight() > maxSize) {
                    optimalSize = size;
                    maxSize = size.getHeight();
                }
            }
        }
        return optimalSize;
    }

    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            previewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            cameraCaptureSessions.capture(previewBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void takePicture() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        try {
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            Size captureSize;
            if (isDimensionSwapped(characteristics)) {
                captureSize = getOptimalCaptureSize(jpegSizes, 4, 3);
            } else {
                captureSize = getOptimalCaptureSize(jpegSizes, 3, 4);
            }

            ImageReader reader = ImageReader.newInstance(captureSize.getWidth(), captureSize.getHeight(), ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            Log.d("imageSurface", "imageReader surface: " + reader.getSurface().toString());
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            //Test

            final File file = new File(AppConstants.IMAGE_PATH + "/pic.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.d("readerListener", "enter");
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    String DIR_NAME = "AROMA Photos3";
                    File direct =
                            new File(Environment
                                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                    .getAbsolutePath() + "/" + DIR_NAME + "/");


                    //Bitmap bitmap = BitmapFactory.decodeFile(String.valueOf(file));

                    FileOutputStream outputStream = null;

                    direct.mkdirs();
                    File file = Environment.getExternalStorageDirectory();
                    String filename = String.format("%d.png", System.currentTimeMillis());
                    File outFile = new File(direct, filename);

                    try {
                        outputStream = new FileOutputStream(outFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    outputStream.write(bytes);
                    //bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                    try {
                        outputStream.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(outFile);
                    mediaScanIntent.setData(contentUri);
                    context.sendBroadcast(mediaScanIntent);
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(CameraActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    protected void takePictureBurst() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        try {
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            Size captureSize;
            if (isDimensionSwapped(characteristics)) {
                captureSize = getOptimalCaptureSize(jpegSizes, 4, 3);
            } else {
                captureSize = getOptimalCaptureSize(jpegSizes, 3, 4);
            }

            ImageReader reader = ImageReader.newInstance(captureSize.getWidth(), captureSize.getHeight(), ImageFormat.JPEG, 4);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            Log.d("imageSurface", "imageReader surface: " + reader.getSurface().toString());
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            //captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
            //captureBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_AUTO);
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);

            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            List<CaptureRequest> captureRequestList = new ArrayList<>();

            int numOfImages = 3;
            float minimumLens=characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
            float maxLens = characteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE);
            Log.d("minLens", String.valueOf(minimumLens));
            Log.d("maxLens", String.valueOf(maxLens));

            Range<Long> exposureRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
            Long minExposure=exposureRange.getLower();
            Long maxExposure=exposureRange.getUpper();
            Log.d("minExposure", String.valueOf(minExposure));
            Log.d("maxExposure", String.valueOf(maxExposure));
            Long exposureInc= (maxExposure-minExposure)/numOfImages-1;

/*            for (int i = 0; i < numOfImages; i++) {
                //captureBuilder.addTarget(reader.getSurface());
                float increment= (minimumLens-maxLens*2)/(numOfImages-1);
                Log.d("lens distance", String.valueOf(minimumLens-i*increment));
                captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, minimumLens-i*increment);
                captureRequestList.add(captureBuilder.build());
            }*/


            for (int i = 0; i<numOfImages; i++){
                captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, minExposure+exposureInc*i);
                captureRequestList.add(captureBuilder.build());
            }

            CameraCaptureSession.CaptureCallback burstCallback= new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    Log.d("captureComplete", "true");
                    super.onCaptureCompleted(session, request, result);
//                    if (!OpenCVLoader.initDebug())
//                        Log.e("OpenCV", "Unable to load OpenCV!");
//                    else
//                        Log.d("OpenCV", "OpenCV loaded Successfully!");
//
//                    String DIR_NAME = "AROMA Photos3";
//                    FocusStacking stack = new FocusStacking(Environment
//                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//                            .getAbsolutePath() + "/" + DIR_NAME + "/", String.valueOf(System.currentTimeMillis()));
//                    stack.fill();
//                    stack.focus_stack();
                }

                @Override
                public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
                    super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
                    Log.d("captureSequenceComplete", "true");
                    if (!OpenCVLoader.initDebug())
                        Log.e("OpenCV", "Unable to load OpenCV!");
                    else
                        Log.d("OpenCV", "OpenCV loaded Successfully!");

                    String DIR_NAME = "AROMA Photos3";
                    FocusStacking stack = new FocusStacking(Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                            .getAbsolutePath() + "/" + DIR_NAME + "/", String.valueOf(System.currentTimeMillis()));
                    stack.fill();
                    stack.focus_stack();
                }

                @Override
                public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                    super.onCaptureFailed(session, request, failure);
                }

            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.stopRepeating();
                        session.captureBurst(captureRequestList, burstCallback, mBackgroundHandler);
                        createCameraPreview();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);



            //final File file = new File(AppConstants.IMAGE_PATH + "/pic.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.d("readerListener", "enter");
                    Image image = null;
                    try {
                        image = reader.acquireNextImage();
//                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                        byte[] bytes = new byte[buffer.capacity()];
//                        buffer.get(bytes);
                        List<byte []> single_burst_complete_images = null;
                        boolean call_takePhotoPartial = false;
                        boolean call_takePhotoCompleted = false;

                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte [] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    String DIR_NAME = "AROMA Photos3";
                    File direct =
                            new File(Environment
                                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                    .getAbsolutePath() + "/" + DIR_NAME + "/");

                    FileOutputStream outputStream = null;

                    direct.mkdirs();
                    String filename = String.format("%d.png", System.currentTimeMillis());
                    File outFile = new File(direct, filename);

                    try {
                        outputStream = new FileOutputStream(outFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    outputStream.write(bytes);
                    //bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                    try {
                        outputStream.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(outFile);
                    mediaScanIntent.setData(contentUri);
                    context.sendBroadcast(mediaScanIntent);
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(CameraActivity.this, "Saved:", Toast.LENGTH_SHORT).show();
                    //createCameraPreview();
                }
            };

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    @NeedsPermission(Manifest.permission.CAMERA)
    void openCamera() {
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {

            cameraId = manager.getCameraIdList()[0]; //TO DO- add get rear facing camera
            characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;

            boolean swappedDimensions = isDimensionSwapped(characteristics);

//            Point displaySize = new Point();
//            this.getWindowManager().getDefaultDisplay().getSize(displaySize);
//            int rotatedPreviewWidth = textureView.getWidth();
//            int rotatedPreviewHeight = textureView.getHeight();
//            int maxPreviewWidth = displaySize.x;
//            int maxPreviewHeight = displaySize.y;
//
//            if (swappedDimensions) {
//                rotatedPreviewWidth = textureView.getWidth();
//                rotatedPreviewHeight = textureView.getHeight();
//                maxPreviewWidth = displaySize.y;
//                maxPreviewHeight = displaySize.x;
//            }
//
//            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
//                maxPreviewWidth = MAX_PREVIEW_WIDTH;
//            }
//
//            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
//                maxPreviewHeight = MAX_PREVIEW_HEIGHT;
//            }

            Size[] previewSizes = map.getOutputSizes(SurfaceTexture.class);
            if (swappedDimensions) {
                previewSize = getOptimalPreviewSize(previewSizes, 640, 480);
            } else {
                previewSize = getOptimalPreviewSize(previewSizes, 480, 640);
            }
            Log.d("optimalPreview", previewSize.getWidth() + " " + previewSize.getHeight());

            // We fit the aspect ratio of TextureView to the size of preview we picked.
            //int orientation = getResources().getConfiguration().orientation;
            //if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (swappedDimensions) {
                Log.d("orientation", "landscape");
                textureView.setAspectRatio(
                        previewSize.getHeight(), previewSize.getWidth());
            } else {
                Log.d("orientation", "portrait");
                textureView.setAspectRatio(
                        previewSize.getWidth(), previewSize.getHeight());
            }

            //configureTransform(previewSize.getWidth(), previewSize.getHeight());

            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    private boolean isDimensionSwapped(CameraCharacteristics characteristics) {
        int displayRotation = this.getWindowManager().getDefaultDisplay().getRotation();
        //noinspection ConstantConditions
        int mSensorOrientation;
        mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        boolean swappedDimensions = false;
        switch (displayRotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                    swappedDimensions = true;
                }
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                    swappedDimensions = true;
                }
                break;
            default:
                Log.e(TAG, "Display rotation is invalid: " + displayRotation);
        }
        return swappedDimensions;
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }

        previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        previewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);
        float minimumLens = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        previewBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, minimumLens);
        Log.d("isNullPreview", String.valueOf(previewBuilder.build() == null));
        Log.d("isNullHandler", String.valueOf(mBackgroundHandler == null));

        try {
            cameraCaptureSessions.setRepeatingRequest(previewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    void compressImageAndSave(Bitmap bitmap) {
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
                    //Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
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

                    ExifInterface exif;
                    try {
                        exif = new ExifInterface(filePath);

                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                        Log.e("EXIF", "Exif: " + orientation);
                        Matrix matrix = new Matrix();
                        if (orientation == 6) {
                            matrix.postRotate(90);
                            Log.e("EXIF", "Exif: " + orientation);
                        } else if (orientation == 3) {
                            matrix.postRotate(180);
                            Log.e("EXIF", "Exif: " + orientation);
                        } else if (orientation == 8) {
                            matrix.postRotate(270);
                            Log.e("EXIF", "Exif: " + orientation);
                        }
                        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(),
                                matrix, true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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


    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable()) {
            CameraActivityPermissionsDispatcher.openCameraWithCheck(this);
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
        closeCamera(); //maybe comment out
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.camera, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CameraActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

/*
    @NeedsPermission(Manifest.permission.CAMERA)
    void startCamera() {
        if (mDialogMessage != null) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                    .setMessage(mDialogMessage)
                    .setNeutralButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.show();
            IntelehealthApplication.setAlertDialogCustomTheme(this, dialog);
        }
        if (mCameraView != null)
            mCameraView.start();
    }
*/

    @OnShowRationale(Manifest.permission.CAMERA)
    void showRationaleForCamera(final PermissionRequest request) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setMessage(getString(R.string.permission_camera_rationale))
                .setPositiveButton(getString(R.string.button_allow), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(getString(R.string.button_deny), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                });
        AlertDialog dialog = builder.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, dialog);
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    void showDeniedForCamera() {
        Toast.makeText(this, getString(R.string.permission_camera_denied), Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    void showNeverAskForCamera() {
        Toast.makeText(this, getString(R.string.permission_camera_never_askagain), Toast.LENGTH_SHORT).show();
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    @Override
    public void onBackPressed() {
        //do nothing
        finish();

    }

    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = this;
        if (null == textureView || null == previewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / previewSize.getHeight(),
                    (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    private List<String> convertFocusModesToValues(int [] supported_focus_modes_arr, float minimum_focus_distance) {
        if( supported_focus_modes_arr.length == 0 ) {
            return null;
        }
        List<Integer> supported_focus_modes = new ArrayList<>();
        for(Integer supported_focus_mode : supported_focus_modes_arr)
            supported_focus_modes.add(supported_focus_mode);
        List<String> output_modes = new ArrayList<>();
        // also resort as well as converting
        if( supported_focus_modes.contains(CaptureRequest.CONTROL_AF_MODE_AUTO) ) {
            output_modes.add("focus_mode_auto");
            Log.d(TAG, " supports focus_mode_auto");
        }
        if( supported_focus_modes.contains(CaptureRequest.CONTROL_AF_MODE_MACRO) ) {
            output_modes.add("focus_mode_macro");
            Log.d(TAG, " supports focus_mode_macro");
        }
        if( supported_focus_modes.contains(CaptureRequest.CONTROL_AF_MODE_AUTO) ) {
            output_modes.add("focus_mode_locked");
            Log.d(TAG, " supports focus_mode_locked");
        }
        if( supported_focus_modes.contains(CaptureRequest.CONTROL_AF_MODE_OFF) ) {
            output_modes.add("focus_mode_infinity");
            Log.d(TAG, " supports focus_mode_infinity");

            if( minimum_focus_distance > 0.0f ) {
                output_modes.add("focus_mode_manual2");
                Log.d(TAG, " supports focus_mode_manual2");
            }
        }
        if( supported_focus_modes.contains(CaptureRequest.CONTROL_AF_MODE_EDOF) ) {
            output_modes.add("focus_mode_edof");
            Log.d(TAG, " supports focus_mode_edof");
        }
        if( supported_focus_modes.contains(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE) ) {
            output_modes.add("focus_mode_continuous_picture");
            Log.d(TAG, " supports focus_mode_continuous_picture");
        }
        if( supported_focus_modes.contains(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO) ) {
            output_modes.add("focus_mode_continuous_video");
            Log.d(TAG, " supports focus_mode_continuous_video");
        }
        return output_modes;
    }

    public static class CameraFeatures {
        public boolean is_zoom_supported;
        public int max_zoom;
        public List<Integer> zoom_ratios;
        public boolean supports_face_detection;
        public List<Size> picture_sizes;
        public List<Size> video_sizes;
        public List<Size> video_sizes_high_speed; // may be null if high speed not supported
        public List<Size> preview_sizes;
        public List<Integer> supported_extensions; // if non-null, list of supported camera vendor extensions, see https://developer.android.com/reference/android/hardware/camera2/CameraExtensionCharacteristics
        public List<String> supported_flash_values;
        public List<String> supported_focus_values;
        public float [] apertures; // may be null if not supported, else will have at least 2 values
        public int max_num_focus_areas;
        public float minimum_focus_distance;
        public boolean is_exposure_lock_supported;
        public boolean is_white_balance_lock_supported;
        public boolean is_optical_stabilization_supported;
        public boolean is_video_stabilization_supported;
        public boolean is_photo_video_recording_supported;
        public boolean supports_white_balance_temperature;
        public int min_temperature;
        public int max_temperature;
        public boolean supports_iso_range;
        public int min_iso;
        public int max_iso;
        public boolean supports_exposure_time;
        public long min_exposure_time;
        public long max_exposure_time;
        public int min_exposure;
        public int max_exposure;
        public float exposure_step;
        public boolean can_disable_shutter_sound;
        public int tonemap_max_curve_points;
        public boolean supports_tonemap_curve;
        public boolean supports_expo_bracketing; // whether setBurstTye(BURSTTYPE_EXPO) can be used
        public int max_expo_bracketing_n_images;
        public boolean supports_focus_bracketing; // whether setBurstTye(BURSTTYPE_FOCUS) can be used
        public boolean supports_burst; // whether setBurstTye(BURSTTYPE_NORMAL) can be used
        public boolean supports_raw;
        public float view_angle_x; // horizontal angle of view in degrees (when unzoomed)
        public float view_angle_y; // vertical angle of view in degrees (when unzoomed)
    }

}