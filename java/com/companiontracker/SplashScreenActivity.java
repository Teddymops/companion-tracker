package com.companiontracker;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.companiontracker.utility.Preferences;


public class SplashScreenActivity extends AppCompatActivity {

    private Handler handler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splashscreen);

    }

    private void openActivity() {
        if (new Preferences().getUserKey(SplashScreenActivity.this).equals("")) {
            startActivity(new Intent(SplashScreenActivity.this, SignInActivity.class));


        } else {
            startActivity(new Intent(SplashScreenActivity.this, DashboardActivity.class));
        }
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        handler = new Handler();
        handler.postDelayed(splashTimer, 2000);   //2seconds

    }

    private Runnable splashTimer = new Runnable() {
        @Override
        public void run() {
            openActivity();

            finish();
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
