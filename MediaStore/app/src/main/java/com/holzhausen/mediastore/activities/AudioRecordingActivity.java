package com.holzhausen.mediastore.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holzhausen.mediastore.R;
import com.holzhausen.mediastore.util.ImageHelper;

import java.io.File;
import java.io.IOException;

public class AudioRecordingActivity extends AppCompatActivity {

    private static final int RECORD_VOICE_PERMISSION = 6;

    private boolean isRecording = false;

    private boolean permissionToRecordAccepted = false;

    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    private MediaRecorder mediaRecorder;

    private File outputFile;

    private FloatingActionButton recordButton;

    private ImageView stopRecodingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recording);

        ActivityCompat.requestPermissions(this, permissions, RECORD_VOICE_PERMISSION);


        recordButton  = findViewById(R.id.recordActionButton);
        recordButton.setOnClickListener(this::startRecording);
        stopRecodingView = findViewById(R.id.stopRecordingButton);
        stopRecodingView.setOnClickListener(this::stopRecording);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case RECORD_VOICE_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) {
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaRecorder.release();
    }

    private void startRecording(View view){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        try {
            outputFile = ImageHelper.createImageFile(this);
        } catch (IOException e){
            e.printStackTrace();
            return;
        }
        mediaRecorder.setOutputFile(outputFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        mediaRecorder.start();
        recordButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_pause_64));
        recordButton.setOnClickListener(this::pauseRecording);
        stopRecodingView.setVisibility(View.VISIBLE);
    }

    private void pauseRecording(View view){
        mediaRecorder.pause();
        recordButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_play_arrow_64));
        recordButton.setOnClickListener(this::resumeRecording);
    }

    private void resumeRecording(View view) {
        mediaRecorder.resume();
        recordButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_pause_64));
        recordButton.setOnClickListener(this::pauseRecording);
    }

    private void stopRecording(View view) {
        if(outputFile != null) {
            mediaRecorder.stop();
            Intent intent = new Intent();
            intent.setData(FileProvider.getUriForFile(this, ImageHelper.FILE_PROVIDER_ACCESS, outputFile));
            intent.putExtra("fileName", outputFile.getName());
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}