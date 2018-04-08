package com.cse.videoframegrabber;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private File videoFile;
    private Uri videoFileUri;
    private MediaMetadataRetriever retriever;
    private MediaPlayer mediaPlayer;
    private Bitmap bitmap;
    private Thread extractor;
    private ImageView imageView;
    static private Activity activity;
    final private int FPS = 10;
    final private int MICROSECOND = 1000000;
    final private int RESULT_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
    }

    private void init(){
        String videoPath = "/path/to/your/video/file/with/extension";
        try {
            videoFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + videoPath);
            videoFileUri = Uri.parse(videoFile.toString());
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(videoFile.toString());
            mediaPlayer = MediaPlayer.create(getBaseContext(), videoFileUri);
        }catch(IllegalArgumentException e) {
            e.printStackTrace();
            Log.d("DEBUG", "Can't load the video file");
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
        imageView = (ImageView)findViewById(R.id.image_view);
        final long totalMilliseconds = mediaPlayer.getDuration();
        extractor = new Thread(new Runnable() {
            @Override
            public void run() {
                for(long microseconds = MICROSECOND*10; microseconds < totalMilliseconds*1000; microseconds += MICROSECOND/FPS){
                    bitmap=retriever.getFrameAtTime(microseconds, MediaMetadataRetriever.OPTION_CLOSEST);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                }
                retriever.release();
            }
        });
        extractor.start();
    }

    private boolean requestPermissions() {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RESULT_PERMISSIONS);
            }else{
                showToast("Succeeded to get Read External Storage permission");
                init();
            }
        }else{
            return true;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (RESULT_PERMISSIONS == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Succeeded to get Read External Storage permission");
                init();
            } else {
                showToast("Failed to get Read External Storage permission");
            }
            return;
        }
    }

    public void showToast(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
