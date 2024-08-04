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
    TextView createnote_or_update;
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
        createnote_or_update = findViewById(R.id.createnote_or_update);

        notes = new ArrayList<>();
        User_Note_key = "User_Note";
        mDataBase = FirebaseDatabase.getInstance().getReference(User_Note_key);
        UserEmailName = getIntent().getStringExtra("EmailDB");
        UserEmailName = "" + UserEmailName.split("@")[0];
        max_last_number = -1000;
        max_not_size=0;

        updateRecycle(notes);
        Text_update(notes.size());


        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setupDrawer(UserEmailName);


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
        syncFromDB(UserEmailName);


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


    private void Text_update(int notes_size){
        mDataBase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> rootData = (Map<String, Object>) snapshot.child(UserEmailName).getValue();
                if (rootData == null && notes_size==0){
                    createnote_or_update.setText("Создайте заметку!");
                    createnote_or_update.setVisibility(View.VISIBLE);
                }
                else if (rootData != null){
                    createnote_or_update.setVisibility(View.GONE);

                }
                else if (rootData != null && notes_size==0){
                    createnote_or_update.setText("Обновление...");
                    createnote_or_update.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

        View nav_header = navigationView.getHeaderView(0);

        TextView textViewUsername = nav_header.findViewById(R.id.nav_header_title);
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

    private void syncFromDB(String UserEmailName) {

        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                notes.clear();
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
                    notret.setTAG_note(return_note_FB.TAG);
                    notes.add(notret);
                    getData();
                }
                notesListAdapter.notifyDataSetChanged();
                Text_update(notes.size());
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
                notes.clear();
                syncFromDB(UserEmailName);
                Text_update(notes.size());
                notesListAdapter.notifyDataSetChanged();
                del_dublicate(notes.size(), notes);

            }
        }

        if (requestCode == 102) {
            if (resultCode == Activity.RESULT_OK) {
                Notes new_notes = (Notes) data.getSerializableExtra("notes");
                getData();
                notes.clear();
                syncFromDB(UserEmailName);
                Text_update(notes.size());
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
            intent.putExtra("TAG_Note", notes.getTAG_note());
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
        if (id == R.id.del_check) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Удалить выделенные заметки?")
                    .setPositiveButton("OK", (dialog, id1) -> {
                        if (notes.size()==1 & notes.get(0).isCheck_state()==true){
                            mDataBase.child(UserEmailName).child(notes.get(0).getUnique_id()).removeValue();
                            notes.clear();
                            Text_update(notes.size());
                            notesListAdapter.notifyDataSetChanged();

                        }
                        else {
                            for (int i = 0; i < (notes.size()); i++) {
                                if (notes.get(i).isCheck_state()) {
                                    mDataBase.child(UserEmailName).child(notes.get(i).getUnique_id()).removeValue();

                                    notesListAdapter.notifyDataSetChanged();
                                    syncFromDB(UserEmailName);


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
            notes.clear();
            Text_update(notes.size());
            notesListAdapter.notifyDataSetChanged();
            max_not_size = (sizeN + Integer.parseInt(max_uniquenote_last_number));
            syncFromDB(UserEmailName);

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
                notes.remove(selectednote);
                notesListAdapter.notifyDataSetChanged();
                mDataBase.child(UserEmailName).child(delete_Unique_id).removeValue();
                Text_update(notes.size());
            } else {
                Toast.makeText(MainActivity.this, "Для удаления нужно открепить заметку", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        if (item.getItemId() == R.id.pin) {
            String pin_Unique_id = selectednote.getUnique_id();
            if (selectednote.isPinned()) {
                notesListAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Заметка откреплена", Toast.LENGTH_SHORT).show();
                mDataBase.child(UserEmailName).child(pin_Unique_id).child("pinned").setValue(false);
            } else {
                notesListAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Заметка закреплена", Toast.LENGTH_SHORT).show();
                mDataBase.child(UserEmailName).child(pin_Unique_id).child("pinned").setValue(true);
            }

        }
        notes.clear();
        return false;
    }


}