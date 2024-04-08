package com.glv.note_project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.view.MenuItem;


import android.widget.Toast;

import com.glv.note_project.Adapter.NotesListAdapter;
import com.glv.note_project.DataBase.RoomDB;
import com.glv.note_project.Model.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    RecyclerView recyclerView;


    FloatingActionButton fab_add, fab_clear, reload_btn;
    NotesListAdapter notesListAdapter;
    RoomDB database;
    Notes selectednote;
    SearchView searchView_home;
    List<Notes> notes = new ArrayList<>();
    DatabaseReference mDataBase;
    String User_Note_key = "User_Note", UserEmailName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        recyclerView = findViewById(R.id.recycler_home);
        fab_add = findViewById(R.id.fab_add);
        fab_clear = findViewById(R.id.fab_clear);
        reload_btn = findViewById(R.id.reload_button);
        mDataBase = FirebaseDatabase.getInstance().getReference(User_Note_key);
        searchView_home = findViewById(R.id.searchView_home);

        UserEmailName = getIntent().getStringExtra("EmailDB");


        UserEmailName = "" + UserEmailName.split("@")[0];


        database = RoomDB.getInstance(this);
        notes = database.mainDAO().getAll();


        updateRecycle(notes);


        fab_add.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
            intent.putExtra("EmailName", getIntent().getStringExtra("EmailDB"));
            intent.putExtra("size_notes", ("" + notes.size()));
            startActivityForResult(intent, 101);

        });

        reload_btn.setOnClickListener(v -> {

            database.mainDAO().delete_all(notes);
            notes.removeAll(notes);
            notesListAdapter.notifyDataSetChanged();

            add_Note_from_BD(UserEmailName);
        });

        searchView_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });


        fab_clear.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Удалить все заметки?")
                    .setMessage("Вы уверены?")
                    .setPositiveButton("OK", (dialog, id) -> {
                        database.mainDAO().delete_all(notes);
                        notes.removeAll(notes);
                        mDataBase.child(UserEmailName).removeValue();

                        notesListAdapter.notifyDataSetChanged();

                    })
                    .setNegativeButton("Отмена", (dialog, id) -> {

                    });
            builder.create().show();
        });


    }

    private void add_Note_from_BD(String UserEmailName) {
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (notes.size() > 0) {
                    notes.clear();
                    database.mainDAO().delete_all(notes);
                    notesListAdapter.notifyDataSetChanged();
                }

                for (DataSnapshot DS : snapshot.child(UserEmailName).getChildren()) {
                    Notes_FB return_note_FB = DS.getValue(Notes_FB.class);
                    Notes notret = new Notes();
                    notret.setID(return_note_FB.ID);
                    notret.setTitle(return_note_FB.title);
                    notret.setData(return_note_FB.data);
                    notret.setNotes(return_note_FB.notes);
                    notret.setUnique_id(return_note_FB.Unique_id);
                    notret.setPinned(return_note_FB.pinned);
                    notes.add(notret);
                }

                notesListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        mDataBase.addValueEventListener(vListener);
    }


    //    Фильрт
    private void filter(String newText) {
        List<Notes> filteredList = new ArrayList<>();
        boolean isFilterApplied = !newText.isEmpty();

        for (Notes singleNote : notes) {
            if (isFilterApplied && (singleNote.getTitle().toLowerCase().contains(newText.toLowerCase())
                    || singleNote.getNotes().toLowerCase().contains(newText.toLowerCase()))) {
                filteredList.add(singleNote);
            }
        }

        if (isFilterApplied) {
            notesListAdapter.filterlist(filteredList);
        } else {
            notesListAdapter.filterlist(notes);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                Notes new_notes = (Notes) data.getSerializableExtra("notes");
                database.mainDAO().insert(new_notes);
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();




            }
        }
        if (requestCode == 102) {
            if (resultCode == Activity.RESULT_OK) {
                Notes new_notes = (Notes) data.getSerializableExtra("notes");
                database.mainDAO().update(new_notes.getID(), new_notes.getTitle(), new_notes.getNotes());
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();


            }
        }

    }

    private void updateRecycle(List<Notes> notes) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        notesListAdapter = new NotesListAdapter(MainActivity.this, notes, notesClickListener);
        recyclerView.setAdapter(notesListAdapter);


    }

    private final NotesClickListener notesClickListener = new NotesClickListener() {
        @Override
        public void onClick(Notes notes) {
            Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
            intent.putExtra("old_notes", notes);
            Toast.makeText(MainActivity.this, notes.getUnique_id(), Toast.LENGTH_SHORT).show();
            intent.putExtra("Unique_name_notes", notes.getUnique_id());
            intent.putExtra("EmailName", getIntent().getStringExtra("EmailDB"));
            startActivityForResult(intent, 102);


        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            selectednote = new Notes();
            selectednote = notes;
            showPopUp(cardView);
        }
    };

    private void showPopUp(CardView cardView) {
        PopupMenu popupMenu = new PopupMenu(this, cardView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {


        if (item.getItemId() == R.id.Delete) {
            if (selectednote.isPinned()==false) {
                String delete_Unique_id = selectednote.getUnique_id();
                database.mainDAO().delete(selectednote);
                notes.remove(selectednote);
                notesListAdapter.notifyDataSetChanged();
                mDataBase.child(UserEmailName).child(delete_Unique_id).removeValue();

            }
            else{
                Toast.makeText(MainActivity.this, "Для удаления нужно открепить заметку", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        if (item.getItemId() == R.id.pin) {
            String pin_Unique_id = selectednote.getUnique_id();
            if (selectednote.isPinned()){
                database.mainDAO().pin(selectednote.getID(), false);
                Toast.makeText(MainActivity.this, "Заметка окреплена", Toast.LENGTH_SHORT).show();
                mDataBase.child(UserEmailName).child(pin_Unique_id).child("pinned").setValue(false);
            }
            else{
                database.mainDAO().pin(selectednote.getID(), true);
                Toast.makeText(MainActivity.this, "Заметка закреплена", Toast.LENGTH_SHORT).show();
                mDataBase.child(UserEmailName).child(pin_Unique_id).child("pinned").setValue(true);
            }
        }
        notes.clear();
        notes.addAll(database.mainDAO().getAll());
        return false;
    }


}
