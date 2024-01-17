package com.glv.note_project;



import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.glv.note_project.Model.Notes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NotesTakerActivity extends AppCompatActivity {
    EditText editText_note, editText_title;
    ImageView imageView_save;
    Notes notes;
    String User_Note_key = "User_Key";
    DatabaseReference mDataBase;
    boolean isOldNote = false;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_taker);

        imageView_save = findViewById(R.id.imageView_save);
        editText_note = findViewById(R.id.editText_note);
        editText_title = findViewById(R.id.editText_title);
        User_Note_key = getIntent().getStringExtra("EmailName").toString();
        for (int i; i<User_Note_key)











        notes = new Notes();
        try {
            notes = (Notes) getIntent().getSerializableExtra("old_notes");


            editText_title.setText(notes.getTitle());
            editText_note.setText(notes.getNotes());
            isOldNote = true;

        }catch (Exception e){


        }


        //создание заметки
        imageView_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String title = editText_title.getText().toString();
                String note_title = editText_note.getText().toString();

                if (note_title.isEmpty()){
                    Toast.makeText(NotesTakerActivity.this, "Enter note", Toast.LENGTH_SHORT).show();
                    return;
                }
                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy");
                Date date = new Date();
                if (!isOldNote){
                    notes = new Notes();

                }
//                mDataBase.child(postID).child("postComment/default").addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        tvWoowCount.setText(""+dataSnapshot.getChildrenCount());
//                    }
//                });
                notes.setTitle(title);
                notes.setNotes(note_title);
                notes.setData(format.format(date));
                Intent intent = new Intent();
                intent.putExtra("notes", notes);
                setResult(Activity.RESULT_OK, intent);
                //        добавление в конструктор Notes_FB листвьюва notes
                Notes_FB new_notes_fb = new Notes_FB(notes.getTitle(), notes.getNotes(), notes.getData(), notes.isPinned(), notes.getID());

                mDataBase = FirebaseDatabase.getInstance().getReference(User_Note_key);
                mDataBase.push().setValue(new_notes_fb);

                Toast.makeText(NotesTakerActivity.this, "Enter note", Toast.LENGTH_SHORT).show();


                finish();


            }
        });
    }
}