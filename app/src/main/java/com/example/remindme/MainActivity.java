package com.example.remindme;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.remindme.auth.Login;
import com.example.remindme.auth.Register;
import com.example.remindme.model.Adapter;
import com.example.remindme.model.Note;
import com.example.remindme.note.AddANote;
import com.example.remindme.note.EditNote;
import com.example.remindme.note.NoteDetails;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    Toolbar toolBar;
    RecyclerView noteLists;
    Adapter adapter;
    FirebaseFirestore fStore;
    FirestoreRecyclerAdapter<Note, NoteViewHolder> noteAdapter;
    FirebaseUser user;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();                          //current user information stored

        //for query implementation
        Query query = fStore.collection("notes").document(user.getUid()).collection("myNotes").orderBy("title", Query.Direction.DESCENDING);  //gets data from "notes" node  //query only those nodes created by a single user
// query notes  >  userId   >    myNotes


        // to execute the query
        FirestoreRecyclerOptions<Note> allNotes = new FirestoreRecyclerOptions.Builder<Note>()     //retrieving data and showing on the main activity of our application
                .setQuery(query, Note.class)                                   //getting information from Note.class
                .build();                                   //builds all the query

        noteAdapter = new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allNotes)    // (query) list of data retrieve from fireStore database
        {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, final int i, @NonNull final Note note) {
                // binding the data from note_view_layout.xml to noteViewHolder
                noteViewHolder.noteTitle.setText(note.getTitle());
                noteViewHolder.noteContent.setText(note.getContent());
                final int code = getRandomColor();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    noteViewHolder.mCardView.setCardBackgroundColor(noteViewHolder.view.getResources().getColor(code, null)); // background color in notes in main activity  (Random)(and getting the color details to pass on)

                }
                final String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();
                noteViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), NoteDetails.class);
                        i.putExtra("title", note.getTitle());      //passing of title data from main act to noteDetails act  starting from 0th position(in main act)
                        i.putExtra("content", note.getContent());   //passing of content data from main act to noteDetails act starting from 0th position(in main act)
                        i.putExtra("code", code);  //passing color details
                        i.putExtra("noteId", docId);
                        v.getContext().startActivity(i);

                    }
                });

                //pop-up menu option (3-dots)  Edit and Delete option
                ImageView menuIcon = noteViewHolder.view.findViewById(R.id.menuIcon);               //to get the  xml resources
                menuIcon.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(final View v) {
                        //for ID reference
                        final String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();

                        //implementation for pop-up menu and its gravity
                        PopupMenu menu = new PopupMenu(v.getContext(), v);
                        menu.setGravity(Gravity.END);

                        //implementation for pop-up menu for edit option
                        menu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                //for going to editNote from main activity from pop-up option
                                Intent i = new Intent(v.getContext(), EditNote.class);
                                i.putExtra("title", note.getTitle());
                                i.putExtra("content", note.getContent());
                                i.putExtra("noteId", docId);
                                startActivity(i);
                                return false;
                            }
                        });

                        //implementation for pop-up menu for delete option
                        menu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                DocumentReference docRef = fStore.collection("notes").document(user.getUid()).collection("myNotes").document(docId);
                                docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //note deleted


                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Failure" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return false;
                            }
                        });

                        //showing pop-up menu
                        menu.show();

                    }
                });

            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout, parent, false);
                return new NoteViewHolder(view);
            }
        };


        noteLists = findViewById(R.id.notelist);
        drawerLayout = findViewById(R.id.drawer);
        nav_view = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);
        setSupportActionBar(toolBar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolBar, R.string.open, R.string.close); //opening and closing of navigation drawer

        // listener for drawer layout (toggle)
        drawerLayout.addDrawerListener(toggle);

        //enable hamburger sign in toolbar
        toggle.setDrawerIndicatorEnabled(true);

        //syncState for navigationBar (is open or is close)
        toggle.syncState();


        noteLists.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));   //orientation of the notes in app
        noteLists.setAdapter(noteAdapter);


        // for display name and display email in  the navigation drawer
        View headerView = nav_view.getHeaderView(0);
        TextView username = headerView.findViewById(R.id.displayUserName);
        TextView userEmail = headerView.findViewById(R.id.displayUserEmail);

        if(user.isAnonymous()){
            userEmail.setVisibility(View.GONE);
            username.setText("Temporary User");
        }else{
            userEmail.setText(user.getEmail());
            username.setText(user.getDisplayName());
        }

        FloatingActionButton fab = findViewById(R.id.addNoteFloat);    //Floating button add note option
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), AddANote.class));
            }
        });


    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);                  //close the navigation drawer after any operation

        switch (item.getItemId()) {
            case R.id.notes:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.addNote:                                               //add a new note from NavigationDrawer
                startActivity(new Intent(this, AddANote.class));
                break;
            case R.id.sync:
                if (user.isAnonymous()) {
                    startActivity(new Intent(MainActivity.this, Login.class));
                } else {
                    Toast.makeText(this, "You Are Connected", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.logout:
                //after this check few things
                checkUser();
                break;
            case R.id.share:
                Intent intentInvite = new Intent(Intent.ACTION_SEND);
                intentInvite.setType("text/plain");
                String body = "Link to your app";
                String subject = "Your Subject";
                intentInvite.putExtra(Intent.EXTRA_SUBJECT, subject);
                intentInvite.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(Intent.createChooser(intentInvite, "Share using"));
                break;
            default:
                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    // check few things before user selects logout option
    private void checkUser() {

        // if the user is real or not
        if (user.isAnonymous()) {         //if user is anonymous then show  WARNING about data will be lost
            displayAlert();                 //display alert dialog to the user
        } else {
// ask registered user "LOGOUT ?"
            displayAlert2();
        }
    }

    private void displayAlert2() {
        AlertDialog.Builder warning2 = new AlertDialog.Builder(this)
                .setTitle("LOGOUT ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(MainActivity.this, SplashScreen.class));           //if user is authenticated then no need to ask ..take them  to splash Screen
                        finish();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        warning2.show();
    }

    // alert dialog for anonymous user before logging  out
    private void displayAlert() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("Are you sure ?")
                .setMessage("You are logged in with TEMPORARY ACCOUNT. Logging out will Delete all the notes.")
                .setPositiveButton("Sync Note", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(MainActivity.this, Register.class));
                        finish();
                    }
                }).setNegativeButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //ToDO: delete all the notes created by the Anonymous User

                        //ToDo: delete the Anonymous User
                        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                startActivity(new Intent(MainActivity.this, SplashScreen.class));
                                finish();
                            }
                        });

                    }
                });
        warning.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Toast.makeText(this, "Settings menu is clicked", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    //noteViewHolder class
    public class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle, noteContent;
        View view;
        CardView mCardView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.titles);
            noteContent = itemView.findViewById(R.id.content);
            view = itemView;
            mCardView = itemView.findViewById(R.id.noteCard);
        }
    }

    private int getRandomColor() {
        List<Integer> colorCode = new ArrayList<>();
        colorCode.add(R.color.blue);
        colorCode.add(R.color.yellow);
        colorCode.add(R.color.skyblue);
        colorCode.add(R.color.lightPurple);
        colorCode.add(R.color.lightGreen);
        colorCode.add(R.color.gray);
        colorCode.add(R.color.pink);
        colorCode.add(R.color.red);
        colorCode.add(R.color.greenlight);
        colorCode.add(R.color.notgreen);
        Random randomColor = new Random();
        int number = randomColor.nextInt(colorCode.size());
        return colorCode.get(number);
    }

    //listen to the data change in fireStore or in the notes collection
    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();  //whenever activity starts we listen to the noteAdapter (listening to any change post by the database)
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (noteAdapter != null) {
            noteAdapter.stopListening(); //once application is closed we stop listening here
        }
    }
}
