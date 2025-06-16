package com.example.bamboospecies;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_IMPORT = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;

    private Bitmap selectedImageBitmap;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ Initialize Python in background to avoid blocking UI
        executorService.execute(() -> {
            if (!Python.isStarted()) {
                Python.start(new AndroidPlatform(getApplicationContext()));
            }
        });

        // ✅ Button bindings
        Button importImageButton = findViewById(R.id.importImageButton);
        Button takePhotoButton = findViewById(R.id.takePhotoButton);
        Button generalUseButton = findViewById(R.id.generalUse);

        importImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_IMPORT);
        });

        takePhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            }
        });

        generalUseButton.setOnClickListener(v ->
                Toast.makeText(this, "General Use Clicked", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_IMPORT) {
                Uri imageUri = data.getData();
                try {
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    selectedImageBitmap = BitmapFactory.decodeStream(imageStream);
                    processImageInBackground(resizeBitmap(selectedImageBitmap, 250, 250));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Image not found.", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_TAKE_PHOTO && data.getExtras() != null) {
                selectedImageBitmap = (Bitmap) data.getExtras().get("data");
                processImageInBackground(resizeBitmap(selectedImageBitmap, 250, 250));
            }
        }
    }

    private Bitmap resizeBitmap(Bitmap original, int width, int height) {
        return Bitmap.createScaledBitmap(original, width, height, true);
    }

    private void processImageInBackground(Bitmap bitmap) {
        executorService.execute(() -> {
            try {
                classifyImage(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Classification error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void classifyImage(Bitmap bitmap) {
        try {
            Python py = Python.getInstance();
            PyObject classifier = py.getModule("classify");  // ✅ classify.py must exist in src/main/python

            // ✅ Convert image to byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            // ✅ Ensure model.tflite is inside assets/
            PyObject result = classifier.callAttr("classify_image", byteArray, "model.tflite");

            if (result == null || getSafeString(result, "label", null) == null) {
                Log.e("CLASSIFY", "Python result was null or malformed.");
                runOnUiThread(() ->
                        Toast.makeText(this, "Python returned no result.", Toast.LENGTH_LONG).show());
                return;
            }

            String label = getSafeString(result, "label", "Unknown");
            String confidence = getSafeString(result, "confidence", "N/A");
            String scientific = getSafeString(result, "scientific_name", "Unknown");
            String family = getSafeString(result, "family_name", "Unknown");
            String description = getSafeString(result, "short_description", "No description available.");

            Log.d("PythonOutput", "Label: " + label + ", Confidence: " + confidence);

            runOnUiThread(() -> {
                if (!label.equalsIgnoreCase("Unknown") && !label.equalsIgnoreCase("Error")) {
                    Intent intent = new Intent(this, IdentificationResultActivity.class);
                    intent.putExtra("label", label);
                    intent.putExtra("confidence", confidence);
                    intent.putExtra("scientific_name", scientific);
                    intent.putExtra("family_name", family);
                    intent.putExtra("short_description", description);

                    ClassificationUtils.bitmapHolder = bitmap;
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Unrecognized bamboo species.", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(this, "Unexpected result format from Python.", Toast.LENGTH_SHORT).show());
        }
    }

    private String getSafeString(PyObject obj, String key, String defaultValue) {
        try {
            PyObject value = obj.get(key);
            return value != null ? value.toString() : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
