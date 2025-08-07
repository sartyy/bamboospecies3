package com.example.bamboospecies;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class IdentificationResultActivity extends AppCompatActivity {

    private ImageView resultImageView;
    private TextView resultLabel;
    private TextView resultConfidence;
    private TextView resultScientificName;
    private TextView resultFamilyName;
    private TextView resultDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification_result);

        resultImageView = findViewById(R.id.bambooImageView);
        resultLabel = findViewById(R.id.commonNameTextView);
        resultConfidence = findViewById(R.id.accuracyTextView);
        resultScientificName = findViewById(R.id.scientificNameTextView);
        resultFamilyName = findViewById(R.id.familyNameTextView);
        resultDescription = findViewById(R.id.shortDescriptionTextView);

        String label = getIntent().getStringExtra("label");
        String confidence = getIntent().getStringExtra("confidence");
        String scientificName = getIntent().getStringExtra("scientific_name");
        String familyName = getIntent().getStringExtra("family_name");
        String shortDescription = getIntent().getStringExtra("description");

        Bitmap imageBitmap = ClassificationUtils.bitmapHolder;
        if (imageBitmap != null) {
            int maxDim = 1024;
            int width = imageBitmap.getWidth();
            int height = imageBitmap.getHeight();

            if (width > maxDim || height > maxDim) {
                float scale = Math.min((float) maxDim / width, (float) maxDim / height);
                int newWidth = Math.round(width * scale);
                int newHeight = Math.round(height * scale);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, newWidth, newHeight, true);
                resultImageView.setImageBitmap(resizedBitmap);
            } else {
                resultImageView.setImageBitmap(imageBitmap);
            }
        }

        // ✅ Display result safely (even for "This is not bamboo")
        resultLabel.setText(safeText(label));
        resultConfidence.setText(safeText(confidence));
        resultScientificName.setText(safeText(scientificName));
        resultFamilyName.setText(safeText(familyName));
        resultDescription.setText(safeText(shortDescription));
    }

    private String safeText(String input) {
        return (input == null || input.trim().isEmpty()) ? "N/A" : input;
    }
}
