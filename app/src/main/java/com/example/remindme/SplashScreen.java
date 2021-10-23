package com.example.remindme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {
    ImageView splashImage;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        splashImage = findViewById(R.id.imageView);
        getSupportActionBar().hide();

        fAuth = FirebaseAuth.getInstance();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                // check if user is logged in
                if (fAuth.getCurrentUser() != null) {           //if user is already logged in then go to MainActivity page
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                    finish();
                } else {
                    //create new anonymous account
                    fAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText(SplashScreen.this, "Logged in with Temporary account", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SplashScreen.this, MainActivity.class));   //with temporary account send user to MainActivity
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SplashScreen.this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });


                }

            }
        }, 2000);

    }
}