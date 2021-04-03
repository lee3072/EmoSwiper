package com.example.emoswiper;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    //private VelocityTracker mVelocityTracker = null;
    private ArrayList<Trajectory> images;
    private MyTouchDetectorView swiperImageView;
    private ProgressBar timerProgressBar;
    private LinearLayout linearLayout;
    private Button collectDataButton;
    private Button predictButton;
    private int index;
    private CountDownTimer timer;

    private static final int MAX_TRAIN_SIZE = 10;
    private static final int MAX_TEST_SIZE = 10;

    private ArrayList<String> predictions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        ActivityCompat.requestPermissions(FullscreenActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                10);

        Field[] drawables = com.example.emoswiper.R.drawable.class.getFields();
        Log.d("Home", String.valueOf(drawables.length));

        swiperImageView = findViewById(R.id.myTouchDetector);
        timerProgressBar = findViewById(R.id.progressBar);
        collectDataButton = findViewById(R.id.collectDataButton);
        predictButton = findViewById(R.id.predictButton);
        linearLayout = findViewById(R.id.linearLayout);

        collectDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout.setVisibility(View.INVISIBLE);
                startCollectingData();
            }
        });

        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout.setVisibility(View.INVISIBLE);
                startPredicting();
            }
        });

        images = new ArrayList<>();
        for (Field s : drawables) {
            Log.d("Home", s.getName());
            String name = s.getName();
            if (name.startsWith("amusement")) {
                images.add(new Trajectory("P", name));
            }
            if (name.startsWith("awe")) {
                images.add(new Trajectory("P", name));
            }
            if (name.startsWith("excitement")) {
                images.add(new Trajectory("P", name));
            }
            if (name.startsWith("disgust")) {
                images.add(new Trajectory("N", name));
            }
            if (name.startsWith("anger")) {
                images.add(new Trajectory("N", name));
            }
            if (name.startsWith("fear")) {
                images.add(new Trajectory("N", name));
            }
        }
        Collections.shuffle(images);
    }

    private void startCollectingData() {
        index = 0;
        swiperImageView.startShowingImage(images.get(index));
        timer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long l) {
                timerProgressBar.setProgress((int) l / 50);
            }

            @Override
            public void onFinish() {
                timerProgressBar.setProgress(0);
                Toast.makeText(FullscreenActivity.this, "Please Swipe Up!", Toast.LENGTH_SHORT).show();
                swiperImageView.setDetectingSwipe(true, new SwipeRefreshListener(){
                    @Override
                    public void onSwipeRefresh() {
                        index = index + 1;
                        if (index > MAX_TRAIN_SIZE) {
                            try {
                                saveData();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            swiperImageView.startShowingImage(images.get(index));
                            timer.start();
                        }
                    }
                });
            }
        };
        timer.start();
    }

    private void startPredicting() {
        index = 0;
        swiperImageView.startShowingImage(images.get(index));
        predictions = new ArrayList<>();
        timer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long l) {
                timerProgressBar.setProgress((int) l / 50);
            }

            @Override
            public void onFinish() {
                timerProgressBar.setProgress(0);
                Toast.makeText(FullscreenActivity.this, "Please Swipe Up!", Toast.LENGTH_SHORT).show();
                swiperImageView.setDetectingSwipe(true, new SwipeRefreshListener(){
                    @Override
                    public void onSwipeRefresh() {
                        String prediction = predict(images.get(index));
                        predictions.add(prediction);
                        index = index + 1;
                        if (index > MAX_TEST_SIZE) {
                            try {
                                computeAccuracy();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            swiperImageView.startShowingImage(images.get(index));
                            timer.start();
                        }
                    }
                });
            }
        };
        timer.start();
    }

    private String predict(Trajectory j) {
        // TODO: fill in the pytorch predictive model

        return "P";
    }

    private void computeAccuracy() throws IOException {
        float tp = 0.0f;
        float fp = 0.0f;
        float tn = 0.0f;
        float fn = 0.0f;
        for (int i = 0; i < index; i++) {
            Trajectory traj = images.get(i);
            String y_true = traj.emotion;
            String y_pred = predictions.get(i);
            if (y_true.equals("P")) {
                if (y_pred.equals("P")) {
                    tp = tp + 1;
                } else {
                    fn = fn + 1;
                }
            } else {
                if (y_pred.equals("P")) {
                    fp = fp + 1;
                } else {
                    tn = tn + 1;
                }
            }
        }

        float accuracy = (tp + tn) / (tp + tn + fp + fn);
        float precision = tp / (tp + fp + 0.001f);
        float recall = tp / (tp + fn + 0.001f);
        float f1 = 2 * precision * recall / (precision + recall + 0.001f);

        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = "EmoSwipeTest.txt";
        String filePath = baseDir + File.separator + fileName;
        File f = new File(filePath);
        FileWriter s = new FileWriter(f);
        s.write("Accuracy: " + accuracy + "\n");
        s.write("TP: " + tp + "\n");
        s.write("FP: " + fp + "\n");
        s.write("TN: " + tn + "\n");
        s.write("fn: " + fn + "\n");
        s.write("Precision: " + precision + "\n");
        s.write("Recall: " + recall + "\n");
        s.write("F-Score: " + f1 + "\n");
        Toast.makeText(FullscreenActivity.this, String.format("Acc %.2f, F-Score %.2f", accuracy, f1), Toast.LENGTH_SHORT).show();
    }

    private void saveData() throws IOException {
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        Toast.makeText(FullscreenActivity.this, "The app is saving the file, please do not close the app!", Toast.LENGTH_SHORT).show();
        String fileName = "EmoSwipeData.csv";
        String filePath = baseDir + File.separator + fileName;
        File f = new File(filePath);
        FileWriter s = new FileWriter(f);
        s.write("Image;Emotion;Trajectory\n");
        for (int i = 0; i < index; i++) {
            Trajectory traj = images.get(i);
            String line = traj.image + ";" + traj.emotion + ";";
            for (Trajectory.Coordinate k: traj.trajectory) {
                line = line + "<"+k.x + "," + k.y + "," + k.t + ">,";
            }
            line = line + "\n";
            s.write(line);
        }
        Toast.makeText(FullscreenActivity.this, "The saving is complete. You may close the app. ", Toast.LENGTH_SHORT).show();
    }
}
