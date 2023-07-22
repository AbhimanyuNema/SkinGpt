package com.example.mydashboard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mydashboard.ml.Skindiseasedetection2;
import com.example.mydashboard.ml.Skindiseasedetection2;
import com.example.mydashboard.ml.Skindiseasedetection2;
import com.example.mydashboard.ml.Skindiseasedetection2;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class home extends AppCompatActivity {

    private TextView result, demoTxt, classfield, clickhere;
    private ImageView imageView, arrowimg;
    private Button picture;

    private int imageSize = 224; // default image size

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        result = findViewById(R.id.result);
        imageView = findViewById(R.id.gifview);
        picture = findViewById(R.id.cam);

        demoTxt = findViewById(R.id.demotext);
        clickhere = findViewById(R.id.clickhere);
        arrowimg = findViewById(R.id.demoimg);
        classfield = findViewById(R.id.classified);

        demoTxt.setVisibility(View.VISIBLE);
        clickhere.setVisibility(View.GONE);
        arrowimg.setVisibility(View.VISIBLE);
        classfield.setVisibility(View.GONE);
        result.setVisibility(View.GONE);

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch camera if we have permission
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 1);
                } else {
                    // Request camera permission
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            int dimension = Math.min(image.getWidth(), image.getHeight());
            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);

            imageView.setImageBitmap(image);
            demoTxt.setVisibility(View.GONE);
            clickhere.setVisibility(View.VISIBLE);
            arrowimg.setVisibility(View.GONE);
            classfield.setVisibility(View.VISIBLE);
            result.setVisibility(View.VISIBLE);
            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
            classifyImage(image);
        }
    }

    private void classifyImage(Bitmap image) {
        try {
            Skindiseasedetection2 model = Skindiseasedetection2.newInstance(getApplicationContext());
            TensorBuffer inputFeatureO = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValue = new int[imageSize * imageSize];
            image.getPixels(intValue, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            // Iterate over pixels and extract R, G, B values and add to byte buffer
            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValue[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeatureO.loadBuffer(byteBuffer);

            // Run the model and get the result
            Skindiseasedetection2.Outputs outputs = model.process(inputFeatureO);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidence = outputFeature0.getFloatArray();

            // Find the index of the class with the highest confidence
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidence.length; i++) {
                if (confidence[i] > maxConfidence) {
                    maxConfidence = confidence[i];
                    maxPos = i;
                }
            }

            String[] classes = { "Basal cell carcinoma", "Benign Keratosis Lesion", "Dermatofibroma", "Melanocytic nevi", "Melanoma","Normal Skin","dumb try it on skin"};
            result.setText(classes[maxPos]);
            result.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Search the disease on the internet
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + result.getText())));
                }
            });

            model.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}