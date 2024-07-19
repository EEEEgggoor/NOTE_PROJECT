package com.glv.note_project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import android.widget.TextView;
import android.widget.Toast;

import com.glv.note_project.Adapter.NotesListAdapter;
import com.glv.note_project.DataBase.RoomDB;
import com.glv.note_project.Model.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    RecyclerView recyclerView;
    FloatingActionButton fab_add;
    NotesListAdapter notesListAdapter;
    RoomDB database;
    Notes selectednote;
    SearchView searchView_home;
    List<Notes> notes;
    DatabaseReference mDataBase;
    String User_Note_key, UserEmailName;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    CheckBox chbox;
    RecyclerView recycler_home;
    public static String max_uniquenote_last_number;
    int max_last_number, max_not_size;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        LayoutInflater mak_card_view = LayoutInflater.from(this);
        View card_note = mak_card_view.inflate(R.layout.notes_list, recycler_home, false);

        chbox = card_note.findViewById(R.id.check_Box);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        recyclerView = findViewById(R.id.recycler_home);
        fab_add = findViewById(R.id.fab_add);
        searchView_home = findViewById(R.id.searchView_home);
        notes = new ArrayList<>();
        User_Note_key = "User_Note";
        mDataBase = FirebaseDatabase.getInstance().getReference(User_Note_key);
        UserEmailName = getIntent().getStringExtra("EmailDB");
        UserEmailName = "" + UserEmailName.split("@")[0];
        database = RoomDB.getInstance(this);
        notes = database.mainDAO().getAll();
        max_last_number = -1000;
        max_not_size=0;

        updateRecycle(notes);


        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setupDrawer(UserEmailName);
        add_Note_from_BD(UserEmailName);

        fab_add.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
            intent.putExtra("EmailName", getIntent().getStringExtra("EmailDB"));
            intent.putExtra("size_notes", (max_uniquenote_last_number));


            startActivityForResult(intent, 101);
        });


        CompletableFuture<String> future = getData();
        future.thenAccept(key -> {
            max_uniquenote_last_number = key;


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

    }


    public CompletableFuture<String> getData() {
        CompletableFuture<String> future = new CompletableFuture<>();
        mDataBase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> rootData = (Map<String, Object>) dataSnapshot.child(UserEmailName).getValue();

                if (rootData != null) {

                    HashMap<String, Object> rootDataMap = new HashMap<>(rootData);

                    for (String key : rootDataMap.keySet()) {

                        Pattern del_email = Pattern.compile(UserEmailName);
                        Matcher matcher = del_email.matcher(key);

                        int m = Integer.parseInt("" + matcher.replaceAll(""));
                        max_last_number = Math.max(max_last_number, m);

                    }
                    max_uniquenote_last_number = "" + (max_last_number+1);
                    future.complete(max_uniquenote_last_number);
                    del_dublicate(notes.size(), notes);
                } else {
                    future.complete("-0");
                }


            }

            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибок
                System.err.println("Error: " + databaseError.getMessage());
            }
        });
        return future;

    }


    private void setupDrawer(String userEmailName) {
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View headerView = navigationView.getHeaderView(0);

        TextView textViewUsername = headerView.findViewById(R.id.nav_header_title);
        textViewUsername.setText(userEmailName);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        toggle.setToolbarNavigationClickListener(v -> {
            if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home_n) {
                Toast.makeText(this, "Home selected", Toast.LENGTH_SHORT).show();
            }
            if (item.getItemId() == R.id.nav_settings) {
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show();
            }
            if (item.getItemId() == R.id.nav_about) {
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
                FirebaseAuth.getInstance().signOut();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (onTopItemSelected(item, notes.size())) {
            return true;
        }
        if (toggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void add_Note_from_BD(String UserEmailName) {

        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notes.clear();
                database.clearAllTables();
                notesListAdapter.notifyDataSetChanged();

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
                    getData();
                }
                database.mainDAO().inserAll(notes);
                notesListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        mDataBase.addValueEventListener(vListener);
    }

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

    protected void del_dublicate(int notsize, List<Notes> notes){
        int nt = (notsize);
        for (int i = 0; i < nt-1; i++) {
            Notes k = notes.get(i);
            Notes j = notes.get(i+1);
            String k_uq = k.getUnique_id();
            String j_uq = j.getUnique_id();

            if (Objects.equals(k_uq, j_uq.intern())){
                notes.remove(j);
                database.mainDAO().delete(j);
                notesListAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                Notes new_notes = (Notes) data.getSerializableExtra("notes");
                getData();
                database.mainDAO().insert(new_notes);
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();
                del_dublicate(notes.size(), notes);

            }
        }

        if (requestCode == 102) {
            if (resultCode == Activity.RESULT_OK) {
                Notes new_notes = (Notes) data.getSerializableExtra("notes");
                getData();
                database.mainDAO().update(new_notes.getID(), new_notes.getTitle(), new_notes.getNotes(), new_notes.getData());
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

            Log.d(TAG, notes.getUnique_id());

            Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
            intent.putExtra("old_notes", notes);
            intent.putExtra("Unique_name_notes", String.valueOf(notes.getUnique_id()));
            intent.putExtra("EmailName", getIntent().getStringExtra("EmailDB"));
            startActivityForResult(intent, 102);
        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_in_toolbar, menu);
        return true;
    }


    private boolean onTopItemSelected(MenuItem item, int sizeN) {
        int id = item.getItemId();

        mDataBase = FirebaseDatabase.getInstance().getReference("User_Note");
        if (id == R.id.all_del) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Удалить выделенные заметки?")
                    .setPositiveButton("OK", (dialog, id1) -> {
                        if (notes.size()==1 & notes.get(0).isCheck_state()==true){
                            mDataBase.child(UserEmailName).child(notes.get(0).getUnique_id()).removeValue();
                            database.mainDAO().delete_all(notes);
                            notes.clear();

                            notesListAdapter.notifyDataSetChanged();

                        }
                        else {
                            for (int i = 0; i < (notes.size()); i++) {
                                if (notes.get(i).isCheck_state()) {
                                    mDataBase.child(UserEmailName).child(notes.get(i).getUnique_id()).removeValue();
                                    database.mainDAO().delete_all(notes);
                                    notesListAdapter.notifyDataSetChanged();
                                    add_Note_from_BD(UserEmailName);


                                    Toast.makeText(MainActivity.this, (notes.size()) + "_" + i + "_" + notes.get(i).getTitle(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        }

                    })
                    .setNegativeButton("Отмена", (dialog, id12) -> {

                    });
            builder.create().show();
            return true;
        }

        if (id == R.id.rel_for_db) {
            database.mainDAO().delete_all(notes);
            notes.clear();
            notesListAdapter.notifyDataSetChanged();
            max_not_size = (sizeN + Integer.parseInt(max_uniquenote_last_number));
            Log.d("MainActivity","Global Key_for_update---------> " + max_not_size);
            add_Note_from_BD(UserEmailName);

            return true;
        }

        if (id == R.id.select_all) {
            if (notes.get(0).isChek()==false){ for (int i = 0; i < notes.size(); i++){ notes.get(i).setChek(true); }
                notesListAdapter.notifyDataSetChanged();}


            else { for (int i = 0; i < notes.size(); i++){ notes.get(i).setChek(false); }
                notesListAdapter.notifyDataSetChanged();}
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.Delete) {
            if (!selectednote.isPinned()) {
                String delete_Unique_id = selectednote.getUnique_id();
                database.mainDAO().delete(selectednote);
                notes.remove(selectednote);
                notesListAdapter.notifyDataSetChanged();
                mDataBase.child(UserEmailName).child(delete_Unique_id).removeValue();
            } else {
                Toast.makeText(MainActivity.this, "Для удаления нужно открепить заметку", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        if (item.getItemId() == R.id.pin) {
            String pin_Unique_id = selectednote.getUnique_id();
            if (selectednote.isPinned()) {
                database.mainDAO().pin(selectednote.getID(), false);
                notesListAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Заметка откреплена", Toast.LENGTH_SHORT).show();
                mDataBase.child(UserEmailName).child(pin_Unique_id).child("pinned").setValue(false);
            } else {
                database.mainDAO().pin(selectednote.getID(), true);
                notesListAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Заметка закреплена", Toast.LENGTH_SHORT).show();
                mDataBase.child(UserEmailName).child(pin_Unique_id).child("pinned").setValue(true);
            }

        }
        notes.clear();
        notes.addAll(database.mainDAO().getAll());
        return false;
    }


}