package com.example.bamboospecies;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_IMPORT = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;
    private Bitmap selectedImageBitmap;
    private Interpreter tflite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button importButton = findViewById(R.id.importImageButton);
        Button cameraButton = findViewById(R.id.takePhotoButton);

        importButton.setOnClickListener(v -> importImage());
        cameraButton.setOnClickListener(v -> takePhoto());

        try {
            tflite = new Interpreter(loadModelFile());  // ✅ Loading updated 27-class quantized model
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Model loading failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void importImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_IMPORT);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            try {
                if (requestCode == REQUEST_IMAGE_IMPORT) {
                    Uri imageUri = data.getData();
                    if (imageUri != null) {
                        InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        selectedImageBitmap = BitmapFactory.decodeStream(imageStream);
                    }
                } else if (requestCode == REQUEST_TAKE_PHOTO) {
                    selectedImageBitmap = (Bitmap) data.getExtras().get("data");
                }

                if (selectedImageBitmap != null) {
                    ClassificationUtils.bitmapHolder = selectedImageBitmap;
                    Bitmap resized = resizeBitmap(selectedImageBitmap, 250, 250); // ✅ Match input shape
                    processImageInBackground(resized);
                } else {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processImageInBackground(Bitmap bitmap) {
        new Thread(() -> {
            int imageSize = 250;
            byte[][][][] input = new byte[1][imageSize][imageSize][3];
            int[] intValues = new int[imageSize * imageSize];
            bitmap.getPixels(intValues, 0, imageSize, 0, 0, imageSize, imageSize);

            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int pixel = intValues[i * imageSize + j];
                    input[0][i][j][0] = (byte) ((pixel >> 16) & 0xFF); // R
                    input[0][i][j][1] = (byte) ((pixel >> 8) & 0xFF);  // G
                    input[0][i][j][2] = (byte) (pixel & 0xFF);         // B
                }
            }

            // ✅ Match model's output size: 27 classes
            byte[][] output = new byte[1][27];
            tflite.run(input, output);

            // ✅ Quantized model: dequantize outputs using Tensor params
            Tensor outputTensor = tflite.getOutputTensor(0);
            float scale = outputTensor.quantizationParams().getScale();
            int zeroPoint = outputTensor.quantizationParams().getZeroPoint();

            float[] confidences = new float[27];
            for (int i = 0; i < 27; i++) {
                confidences[i] = (output[0][i] & 0xFF) * scale + zeroPoint;
            }

            int maxIndex = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxIndex = i;
                }
            }

            String label;
            String confidenceStr;
            String scientificName;
            String familyName;
            String description;

            if (maxIndex == 26) {
                // ✅ Index 26 reserved for "not bamboo"
                label = "This is not bamboo";
                confidenceStr = "N/A";
                scientificName = "N/A";
                familyName = "N/A";
                description = "N/A";
            } else {
                label = getLabel(maxIndex);
                confidenceStr = String.format("%.2f%%", maxConfidence * 100);
                scientificName = ClassificationUtils.getScientificName(label);
                familyName = ClassificationUtils.getFamilyName(label);
                description = ClassificationUtils.getDescription(label);
            }

            Intent resultIntent = new Intent(MainActivity.this, IdentificationResultActivity.class);
            resultIntent.putExtra("label", label);
            resultIntent.putExtra("confidence", confidenceStr);
            resultIntent.putExtra("scientific_name", scientificName);
            resultIntent.putExtra("family_name", familyName);
            resultIntent.putExtra("description", description);

            startActivity(resultIntent);
        }).start();
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private String getLabel(int index) {
        String[] labels = {
                "Beema", "Bical Babi", "Black Bamboo", "Boos Bamboo", "Buddha Belly",
                "Buho", "Giant Bamboo", "Giant Bolo", "Hedge Bamboo", "Iron Bamboo",
                "Japanese Bamboo", "Kawayan Kiling", "Kawayang Bayog", "Kawayang Tinik", "Kayali",
                "Long Bamboo", "Malayan Bamboo", "Malaysian Bamboo", "Old ham Bamboo", "Pole Vault Bamboo",
                "Running Bamboo", "Solid Calcutta", "Taiwan Bamboo", "Wamin", "Yello Bamboo", "Yellow Buho",
                "This is not bamboo"  // ✅ index 26
        };

        return (index >= 0 && index < labels.length) ? labels[index] : "This is not bamboo";
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        // ✅ Ensure filename matches your uploaded new model
        AssetFileDescriptor fileDescriptor = getAssets().openFd("model_int8.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}