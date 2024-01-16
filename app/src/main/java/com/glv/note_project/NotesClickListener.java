package com.glv.note_project;



import androidx.cardview.widget.CardView;

import com.glv.note_project.Model.Notes;

public interface NotesClickListener {
    void onClick(Notes notes);
    void onLongClick(Notes notes, CardView cardView);
}

