package com.glv.note_project;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.glv.note_project.Adapter.NotesListAdapter;
import com.glv.note_project.Model.Notes;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.text.SimpleDateFormat;
import java.util.Date;

public class NotesTakerActivity extends AppCompatActivity {
    EditText editText_note, editText_title;
    TextView textTegTOOLBAR;
    ImageView imageView_save;
    Notes notes;
    String User_Note_key1, Unique_name_notes, Unique_id, TAG_Note;
    DatabaseReference mDataBase;
    BottomNavigationView bottomNavigationView;
    boolean isOldNote = false;
    public String teg_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_taker);

        String TAG = "NotesTakerActivity";
        imageView_save = findViewById(R.id.imageView_save);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        editText_note = findViewById(R.id.editText_note);
        editText_title = findViewById(R.id.editText_title);
        textTegTOOLBAR = findViewById(R.id.textTegTOOLBAR);

        User_Note_key1 = getIntent().getStringExtra("EmailName").toString();
        String name1 = getIntent().getStringExtra("Unique_name_notes");
        Unique_id = getIntent().getStringExtra("size_notes");
        TAG_Note = getIntent().getStringExtra("TAG_Note");
        textTegTOOLBAR.setText(TAG_Note);

        String User_Note_key123 = User_Note_key1.split("@")[0];
        Unique_name_notes = User_Note_key123 + Unique_id;






        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemID = item.getItemId();

                if (itemID==R.id.buttonSetText) {
                    LayoutInflater inflater = LayoutInflater.from(NotesTakerActivity.this);
                    View dialogView = inflater.inflate(R.layout.dialog_edittext_layout, null);
                    final EditText edittext_teg = dialogView.findViewById(R.id.editTeg);

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(NotesTakerActivity.this);
                    dialogBuilder.setTitle("Введите тег")
                            .setView(dialogView)
                            .setPositiveButton("OK", (dialog, id1) -> {
                                teg_text = edittext_teg.getText().toString();
                                textTegTOOLBAR.setText(teg_text);

                            })
                            .setNegativeButton("Отмена", (dialog, id12) -> {
                                // Обработка отмены
                            });
                    dialogBuilder.create().show();
                }

                if (itemID==R.id.cursiv_tex_style){     }
                return false;
            }
        });


        notes = new Notes();
        try {
            notes = (Notes) getIntent().getSerializableExtra("old_notes");


            editText_title.setText(notes.getTitle());
            editText_note.setText(notes.getNotes());


            isOldNote = true;

        } catch (Exception e) {}


        //создание заметки
        imageView_save.setOnClickListener(v -> {


            String title = editText_title.getText().toString();
            String note_title = editText_note.getText().toString();

            SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy");
            Date date = new Date();
            if (!isOldNote) {
                notes = new Notes();
                notes.setTitle(title);
                notes.setNotes(note_title);
                notes.setData(format.format(date));
                notes.setUnique_id(Unique_name_notes);
                if (teg_text==null) {notes.setTAG_note(TAG_Note);}
                else{notes.setTAG_note(teg_text);}
                textTegTOOLBAR.setText(teg_text);
                Intent intent = new Intent();
                intent.putExtra("notes", notes);
                setResult(Activity.RESULT_OK, intent);




//              добавление в конструктор Notes_FB листвьюва notes
                Notes_FB new_notes_fb = new Notes_FB(notes.getTitle(), notes.getNotes(), notes.getData(), notes.isPinned(), notes.getID(), notes.getUnique_id(), notes.getTAG_note());
                mDataBase = FirebaseDatabase.getInstance().getReference("User_Note");
                mDataBase.child(User_Note_key123).child(Unique_name_notes).setValue(new_notes_fb);
                mDataBase.child(User_Note_key123).child(Unique_name_notes).child("Unique_id").setValue(Unique_name_notes);
            }

            if (isOldNote){
                notes.setTitle(title);
                notes.setNotes(note_title);
                notes.setData(format.format(date));
                notes.setUnique_id(Unique_name_notes);
                if (teg_text==null) {notes.setTAG_note(TAG_Note);}
                else{notes.setTAG_note(teg_text);}
                Intent intent = new Intent();
                intent.putExtra("notes", notes);
                setResult(Activity.RESULT_OK, intent);



//              добавление в конструктор Notes_FB листвьюва notes
                Notes_FB new_notes_fb = new Notes_FB(notes.getTitle(), notes.getNotes(), notes.getData(), notes.isPinned(), notes.getID(), notes.getUnique_id(), notes.getTAG_note());
                mDataBase = FirebaseDatabase.getInstance().getReference("User_Note");
                mDataBase.child(User_Note_key123).child(name1).setValue(new_notes_fb);
                mDataBase.child(User_Note_key123).child(name1).child("Unique_id").setValue(name1);
            }




            finish();


        });



    }
}