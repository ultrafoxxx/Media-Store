package com.holzhausen.mediastore.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.holzhausen.mediastore.R;

public class NameNewFileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_new_file);

        final Bitmap filePreview = (Bitmap) getIntent().getExtras().get("filePreview");
        final ImageView imageView = findViewById(R.id.file_preview);
        imageView.setImageBitmap(filePreview);

        final EditText titleInput = findViewById(R.id.title_text_input);
        final Button submitButton = findViewById(R.id.set_title_button);

        submitButton.setOnClickListener(view -> {
            String fileTitle = titleInput.getText().toString();
            Intent result = new Intent();
            result.putExtra("filePreview", filePreview);
            result.putExtra("fileTitle", fileTitle);
            setResult(RESULT_OK, result);
            finish();
        });

    }
}