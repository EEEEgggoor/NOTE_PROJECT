package com.glv.note_project;



public class Notes_FB {
    public String title, notes, data;
    public boolean pinned;
    int ID;
    String Unique_id;

    public Notes_FB() {
    }

    public Notes_FB(String title, String notes, String data, boolean pinned, int ID, String Unique_id) {
        this.title = title;
        this.notes = notes;
        this.data = data;
        this.pinned = pinned;
        this.ID = ID;
        this.Unique_id = Unique_id;

    }
}
