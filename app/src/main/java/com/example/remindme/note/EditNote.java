package com.example.remindme.note;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.remindme.MainActivity;
import com.example.remindme.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class EditNote extends AppCompatActivity {
    Intent data;
    EditText editNoteTitle, editNoteContent;
    FirebaseFirestore fStore;   //for saving the changes in note in firebase database
    ProgressBar spinner;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        Toolbar toolbar=findViewById(R.id.toolbar);
       setSupportActionBar(toolbar);
       spinner=findViewById(R.id.progressBar2);

       user=FirebaseAuth.getInstance().getCurrentUser();

       fStore =fStore.getInstance();

        //getting information from NoteDetails
        data = getIntent();

        editNoteContent=findViewById(R.id.editNoteContent);
        editNoteTitle=findViewById(R.id.editNoteTitle);

        //saving data on clicking floating button
        FloatingActionButton fab = findViewById(R.id.saveEditedNote);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nTitle = editNoteTitle.getText().toString();
                String nContent = editNoteContent.getText().toString();

                if (nTitle.isEmpty() || nContent.isEmpty()) {
                    Toast.makeText(EditNote.this, "Cannot save note  with empty field", Toast.LENGTH_SHORT).show();
                    return;
                }
                spinner.setVisibility(View.VISIBLE);

                //save note (updated one)
                DocumentReference docref = fStore.collection("notes").document(user.getUid()).collection("myNotes").document(data.getStringExtra("noteId"));   //noteId is used for updating the current note not adding a new note to database

                Map<String, Object> note = new HashMap<>();
                note.put("title", nTitle);
                note.put("content", nContent);

                //use "update" instead of set.
                docref.update(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditNote.this, "Note Updated", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditNote.this, "Error, Try again", Toast.LENGTH_SHORT).show();
                        spinner.setVisibility(View.VISIBLE);
                    }
                });


            }
        });

        String noteTitle=data.getStringExtra("title");
        String noteContent=data.getStringExtra("content");

        //set noteTitle and noteContent to editNoteTitle and EditNoteContent resp
        editNoteTitle.setText(noteTitle);
        editNoteContent.setText(noteContent);



    }
}