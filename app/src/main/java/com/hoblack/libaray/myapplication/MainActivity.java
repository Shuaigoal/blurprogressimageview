package com.hoblack.libaray.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;

import com.hoblack.libaray.ui.BlurProgressImageView;

public class MainActivity extends AppCompatActivity {
    BlurProgressImageView blurProgressImageView;
    Switch switchView;
    Switch switchView_orientation;
    SeekBar seekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBar= findViewById(R.id.seekBar);
        blurProgressImageView = findViewById(R.id.blurProgressImageView);
        switchView = findViewById(R.id.switchView);
        switchView_orientation = findViewById(R.id.switchView_orientation);
        switchView.setChecked(true);
        switchView_orientation.setChecked(true);
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    blurProgressImageView.setShowImage(true);
                } else {
                    blurProgressImageView.setShowImage(false);
                }
            }
        });
        switchView_orientation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    blurProgressImageView.setOrientation(BlurProgressImageView.VERTICAL);
                } else {
                    blurProgressImageView.setOrientation(BlurProgressImageView.HORIZONTAL);
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                blurProgressImageView.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar.setProgress(80);
    }
}
