package com.glv.note_project;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.app.Activity;

import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;


import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.glv.note_project.Model.Notes;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


public class NotesTakerActivity extends AppCompatActivity {
    EditText editText_note, editText_title;
    TextView textTegTOOLBAR;
    ImageView imageView_save;
    Notes notes;
    String User_Note_key1, Unique_name_notes, Unique_id, TAG_Note, User_Note_key123;
    DatabaseReference mDataBase;
    BottomNavigationView bottomNavigationView;
    String TAG = "NotesTakerActivity";
    List<String> TAGList = new ArrayList<>();
    boolean isOldNote = false;
    public String teg_text;
    private int originalHeight;
    private final int ANIMATION_DURATION = 1000; // Duration of animation in milliseconds
    private boolean isListViewExpanded = false;


    private void expandListView(final ListView listView) {
        final int targetHeight = originalHeight;

        ValueAnimator animator = ValueAnimator.ofInt(listView.getLayoutParams().height, targetHeight);
        animator.setDuration(ANIMATION_DURATION);
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
            layoutParams.height = (int) animation.getAnimatedValue();
            listView.setLayoutParams(layoutParams);
        });
        animator.start();
    }

    private void collapseListView(final ListView listView) {
        final int initialHeight = listView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.setDuration(ANIMATION_DURATION);
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
            layoutParams.height = (int) animation.getAnimatedValue();
            listView.setLayoutParams(layoutParams);
        });
        animator.start();
    }

    public interface FirebaseCallback {
        void onCallback(List<String> list);
    }

    private void listTAGUpdate(List<String> TAGList, String User_Note_key123, FirebaseCallback callback){
        TAGList.clear();
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.child(User_Note_key123).getChildren()){
                    Notes_FB notesFb = ds.getValue(Notes_FB.class);
                    TAGList.add(notesFb.TAG);
                }
                List<String> TAGListUpdate = TAGList.stream().distinct()
                        .collect(Collectors.toList());

                callback.onCallback(TAGListUpdate);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mDataBase.addValueEventListener(vListener);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_taker);


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
        mDataBase = FirebaseDatabase.getInstance().getReference("User_Note");
        User_Note_key123 = User_Note_key1.split("@")[0];
        Unique_name_notes = User_Note_key123 + Unique_id;
        LayoutInflater inflater = LayoutInflater.from(NotesTakerActivity.this);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemID = item.getItemId();

            if (itemID == R.id.buttonSetText) {
                // Убедитесь, что dialogView не имеет родителя
                View dialogView = inflater.inflate(R.layout.dialog_edittext_layout, null);
                final ListView TAGListView = dialogView.findViewById(R.id.listForTag);
                final EditText edittext_teg = dialogView.findViewById(R.id.editTeg);
                final Button buttonTAG = dialogView.findViewById(R.id.buttonTAG);

                TAGListView.post(() -> {
                    originalHeight = TAGListView.getMeasuredHeight();
                    TAGListView.getLayoutParams().height = 0;
                    TAGListView.requestLayout();
                });

                buttonTAG.setOnClickListener(v -> {
                    if (isListViewExpanded) {
                        collapseListView(TAGListView);
                        buttonTAG.setText("Показать доступные теги");
                    } else {
                        expandListView(TAGListView);
                        buttonTAG.setText("Скрыть доступные теги");
                    }
                    isListViewExpanded = !isListViewExpanded;
                });


                listTAGUpdate(TAGList, User_Note_key123, updatedTAGList -> {
                    TAGListView.setAdapter(new ArrayAdapter<>(NotesTakerActivity.this, android.R.layout.simple_list_item_1, updatedTAGList));


                    TAGListView.post(() -> {
                        originalHeight = TAGListView.getMeasuredHeight();
                        TAGListView.getLayoutParams().height = 0;
                        TAGListView.requestLayout();
                    });

                    TAGListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //Нажатый элемент
                            String selectedTag = TAGList.get(position);
                            edittext_teg.setText(selectedTag);
                        }
                    });

                    // Перед добавлением dialogView убедитесь, что у него нет родителя
                    if (dialogView.getParent() != null) {
                        ((ViewGroup) dialogView.getParent()).removeView(dialogView);
                    }

                    // Проверка, что активность все еще активна перед показом диалога
                    if (!isFinishing() && !isDestroyed()) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(NotesTakerActivity.this);
                        dialogBuilder.setTitle("Введите тег")
                                .setView(dialogView)
                                .setPositiveButton("OK", (dialog, id1) -> {
                                    teg_text = edittext_teg.getText().toString();
                                    if (teg_text==""){teg_text=null;}
                                    textTegTOOLBAR.setText(teg_text);
                                })
                                .setNegativeButton("Отмена", (dialog, id12) -> {
                                    // Обработка отмены
                                });
                        dialogBuilder.create().show();
                    }
                });
            }

            if (itemID == R.id.cursiv_tex_style) {
                // Ваша логика для кнопки cursiv_tex_style
            }
            return false;
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
