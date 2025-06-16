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

        // ✅ Link XML IDs
        resultImageView = findViewById(R.id.bambooImageView);
        resultLabel = findViewById(R.id.commonNameTextView);
        resultConfidence = findViewById(R.id.accuracyTextView);
        resultScientificName = findViewById(R.id.scientificNameTextView);
        resultFamilyName = findViewById(R.id.familyNameTextView);
        resultDescription = findViewById(R.id.shortDescriptionTextView);

        // ✅ Get extras from intent
        String label = getIntent().getStringExtra("label");
        String confidence = getIntent().getStringExtra("confidence");
        String scientificName = getIntent().getStringExtra("scientific_name");
        String familyName = getIntent().getStringExtra("family_name");
        String shortDescription = getIntent().getStringExtra("short_description");

        // ✅ Get bitmap from static holder class
        Bitmap imageBitmap = ClassificationUtils.bitmapHolder;
        if (imageBitmap != null) {
            resultImageView.setImageBitmap(imageBitmap);
        }

        // ✅ Set result details to views
        resultLabel.setText("Common Name: " + label);
        resultConfidence.setText("Confidence: " + confidence);
        resultScientificName.setText("Scientific Name: " + scientificName);
        resultFamilyName.setText("Family Name: " + familyName);
        resultDescription.setText("Description: " + shortDescription);
    }
}
