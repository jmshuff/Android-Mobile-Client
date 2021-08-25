package app.insightfuleye.client.activities.cameraActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import app.insightfuleye.client.R;
import app.insightfuleye.client.app.AppConstants;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static android.widget.Toast.LENGTH_SHORT;

@RuntimePermissions
public class CameraActivity extends AppCompatActivity {

    public static final int TAKE_IMAGE = 205;

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
    private String mImageName = null;
    private String mFilePath = null;
    private final String TAG = CameraActivity.class.getSimpleName();
    PreviewView mPreviewView;
    ImageView captureImage;
    private Handler mBackgroundHandler;


    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            "android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(SET_IMAGE_NAME))
                mImageName = extras.getString(SET_IMAGE_NAME);
            if (extras.containsKey(SET_IMAGE_PATH))
                mFilePath = extras.getString(SET_IMAGE_PATH);
        }

        mPreviewView = findViewById(R.id.camera);
        captureImage = findViewById(R.id.take_picture);

        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
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

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

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
    }

    private byte[] toBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy planeProxy = image.getPlanes()[0];
        ByteBuffer buffer = planeProxy.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
        //return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    void compressImageAndSave(final byte[] data) {
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
}