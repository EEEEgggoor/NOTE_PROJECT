package com.glv.note_project;

import com.glv.note_project.Model.Notes;


public class Notes_FB {
    public String title, notes, data;
    public boolean pinned;
    int ID;

    public Notes_FB() {
    }

    public Notes_FB(String title, String notes, String data, boolean pinned, int ID) {
        this.title = title;
        this.notes = notes;
        this.data = data;
        this.pinned = pinned;
        this.ID = ID;
    }
}
