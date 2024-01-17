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
import android.content.Intent;
import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;

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
    FloatingActionButton fab_add, fab_clear;
    NotesListAdapter notesListAdapter;
    RoomDB database;
    public Notes new_not;
    SearchView searchView_home;
    List<Notes> notes = new ArrayList<>();
    DatabaseReference mDataBase;
    String User_Note_key = "glugach3";




    int i;
    String EmailName;
    Notes selectednote;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        recyclerView = findViewById(R.id.recycler_home);
        fab_add = findViewById(R.id.fab_add);
        fab_clear = findViewById(R.id.fab_clear);
        mDataBase = FirebaseDatabase.getInstance().getReference(User_Note_key);
        searchView_home = findViewById(R.id.searchView_home);

//        добавление в конструктор Notes_FB листвьюва notes
//        Notes_FB new_notes_fb = new Notes_FB(notes);
//        mDataBase.push().setValue(new_notes_fb);

        database = RoomDB.getInstance(this);
        notes = database.mainDAO().getAll();










        updateRecycle(notes);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
                intent.putExtra("EmailName", getIntent().getStringExtra("EmailDB"));
                startActivityForResult(intent, 101);

            }
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


        fab_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                while (notes.size()!=0){
                    i=-1;
                    i++;
                    if (!notes.get(i).isPinned()) {
                        database.mainDAO().delete(notes.get(i));
                        notes.remove(notes.get(i));
                        notesListAdapter.notifyDataSetChanged();
                        mDataBase.removeValue();



                    }
                }

            }
        });



        Toast.makeText(MainActivity.this, getIntent().getStringExtra("EmailDB"), Toast.LENGTH_SHORT).show();







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



    private void onValueIsBD(){
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (notes.size()>0) notes.clear();

                for (DataSnapshot ds : snapshot.getChildren())
                {
                    Notes_FB note_return = ds.getValue(Notes_FB.class);
                    new_not = new Notes();
                    new_not.setTitle(note_return.title);
                    new_not.setNotes(note_return.notes);
                    new_not.setData(note_return.data);
                    new_not.setPinned(note_return.pinned);
                    new_not.setID(note_return.ID);


//                    database.mainDAO().insert(new_not);
//                    notes.clear();
//                    notes.addAll(database.mainDAO().getAll());
                    notes.add(new_not);
                    notesListAdapter.notifyDataSetChanged();
                }
            }




            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mDataBase.addValueEventListener(vListener);


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

            startActivityForResult(intent, 102);


        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {
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

        if (item.getItemId() == R.id.pin) {
            selectednote = new Notes();

            String title = selectednote.getTitle();
            String nots = selectednote.getNotes();
            String date = selectednote.getData();

            if (item.getItemId() == R.id.imageView_pin) {
                database.mainDAO().pin(selectednote.getID(), false);

            } else {
                database.mainDAO().pin(selectednote.getID(), true);

            }
            notes.clear();
            notes.addAll(database.mainDAO().getAll());
            notesListAdapter.notifyDataSetChanged();
            return true;
        }



        if (item.getItemId() == R.id.Delete) {
            database.mainDAO().delete(selectednote);
            notes.remove(selectednote);
            notesListAdapter.notifyDataSetChanged();
            Toast.makeText(MainActivity.this, selectednote.getTitle().toString(), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }




}
