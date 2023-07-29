package com.example.password_manager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonSave;
    private Button buttonLoad;
    private TextView textViewResult;
    private SharedPreferences sharedPreferences;
    private ListView listViewCredentials;
    private List<String> credentialsList;
    private ArrayAdapter<String> credentialsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSave = findViewById(R.id.buttonSave);
        buttonLoad = findViewById(R.id.buttonLoad);
        listViewCredentials = findViewById(R.id.listViewCredentials);
        credentialsList = new ArrayList<>();
        credentialsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, credentialsList);
        listViewCredentials.setAdapter(credentialsAdapter);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                saveCredentials(username, password);
            }
        });

        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCredentialsFromFirebase();
            }
        });
    }

    private void saveCredentials(String username, String password) {
        // Save the data to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();

        // Get a reference to the Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference credentialsRef = database.getReference("credentials");

        // Create a new entry in the "credentials" node with a unique key (push() generates a unique key)
        DatabaseReference newCredentialRef = credentialsRef.push();

        // Set the username and password as key-value pairs in the new entry
        newCredentialRef.child("username").setValue(username);
        newCredentialRef.child("password").setValue(password);
        Toast.makeText(this, "Password Successfully Saved", Toast.LENGTH_SHORT).show();
    }


    private void loadCredentialsFromFirebase() {
        DatabaseReference credentialsRef = FirebaseDatabase.getInstance().getReference("credentials");
        credentialsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                // This will be called for each child (username/password) under "credentials"
                String username = snapshot.child("username").getValue(String.class);
                String password = snapshot.child("password").getValue(String.class);

                String credentials = "Username: " + username + "\nPassword: " + password;
                credentialsList.add(credentials);
                credentialsAdapter.notifyDataSetChanged();
            }

            // Other overridden methods: onChildChanged, onChildRemoved, onChildMoved, onCancelled
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
