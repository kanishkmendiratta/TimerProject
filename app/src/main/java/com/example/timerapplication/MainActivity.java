package com.example.timerapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.timerapplication.databinding.ActivityMainBinding;

import java.util.Timer;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding viewBinding;

    BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getExtras() != null){
                String progress = intent.getExtras().getString("progress");
                if(viewBinding != null){
                    if(viewBinding.timerTv != null){
                        viewBinding.timerTv.setText(progress);
                    }
                    viewBinding.timerET.setEnabled(false);
                    viewBinding.startBtn.setEnabled(false);
                }
            }
        }
    };
    BroadcastReceiver timerEndedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(viewBinding != null){
                viewBinding.timerTv.setText("");
                viewBinding.timerET.setEnabled(true);
                viewBinding.startBtn.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View root = viewBinding.root;
        setContentView(root);

        setupBroadCastRecievers();
        setupListeners();
    }

    private void setupBroadCastRecievers() {
        IntentFilter progressIntentFilter = new IntentFilter();
        progressIntentFilter.addAction("timerProgress");
        MainActivity.this.registerReceiver(timerReceiver,progressIntentFilter);

        IntentFilter progressEndedFilter = new IntentFilter();
        progressEndedFilter.addAction("timerProgressEnded");
        MainActivity.this.registerReceiver(timerEndedReceiver,progressEndedFilter);
    }

    private void setupListeners() {
        viewBinding.startBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try{
                    int minutes = Integer.parseInt(viewBinding.timerET.getText().toString());
                    setupTimer(minutes);
                }
                catch(NumberFormatException e){
                    Toast.makeText(MainActivity.this,"Please enter a valid number",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(timerReceiver);
        this.unregisterReceiver(timerEndedReceiver);
        super.onDestroy();
    }

    private void setupTimer(int minutes) {
        int milliseconds = minutes*60*1000;
        Intent timerIntent = new Intent(MainActivity.this, TimerService.class);
        timerIntent.putExtra("milliseconds",milliseconds);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(timerIntent);
        }
    }
}