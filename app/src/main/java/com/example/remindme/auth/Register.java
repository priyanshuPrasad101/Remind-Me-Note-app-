package com.example.remindme.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remindme.MainActivity;
import com.example.remindme.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Register extends AppCompatActivity {
    EditText rUserName,rUserEmail,rUserPass,rUserConfPass;
    Button  syncAccount;
    TextView loginAct;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    FirebaseUser usr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setTitle("Create New Remind Me Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);        //adding back button to  the toolBar

rUserName= findViewById(R.id.userName);
rUserEmail= findViewById(R.id.userEmail);
rUserPass= findViewById(R.id.password);
rUserConfPass= findViewById(R.id.passwordConfirm);
syncAccount=findViewById(R.id.createAccount);
loginAct=findViewById(R.id.login);
progressBar=findViewById(R.id.progressBar4);

fAuth=FirebaseAuth.getInstance();

//Login button
        loginAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this,Login.class));
                finish();
            }
        });


//button clicked
syncAccount.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        //extract data
        final String uUserName=rUserName.getText().toString();
        String uUserEmail=rUserEmail.getText().toString();
        String uUserPass=rUserPass.getText().toString();
        String uConfPass=rUserConfPass.getText().toString();

        if(uUserEmail.isEmpty() || uUserPass.isEmpty() || uConfPass.isEmpty() || uUserName.isEmpty() ){
            Toast.makeText(Register.this, "All Fields are required", Toast.LENGTH_SHORT).show();
            return;
        }
           if(!uUserPass.equals(uConfPass)){
               //set error to rUserPass in Register activity stating pass and confPass doesnt match
               rUserPass.setError("Password do not match.");
           }

           progressBar.setVisibility(View.VISIBLE);


           //setting Authentication (new account  to FireBase)
        AuthCredential credential = EmailAuthProvider.getCredential(uUserEmail,uUserPass);
           fAuth.getCurrentUser().linkWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
               @Override
               public void onSuccess(AuthResult authResult) {
                   Toast.makeText(Register.this, "Notes are Synced", Toast.LENGTH_SHORT).show();
                   startActivity(new Intent(Register.this,MainActivity.class));

                   FirebaseUser usr= fAuth.getCurrentUser();
                   UserProfileChangeRequest request= new UserProfileChangeRequest.Builder()
                           .setDisplayName(uUserName)
                           .build();
                   usr.updateProfile(request);
                   startActivity(new Intent(Register.this,MainActivity.class));
               }




           }).addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull Exception e) {
                   Toast.makeText(Register.this, "Try Again" + e.getMessage(), Toast.LENGTH_SHORT).show();
                   progressBar.setVisibility(View.INVISIBLE);
               }
           });


    }
});




    }
//enable click on back button on toolBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(Register.this, MainActivity.class));
        finish();
        return super.onOptionsItemSelected(item);
    }
}