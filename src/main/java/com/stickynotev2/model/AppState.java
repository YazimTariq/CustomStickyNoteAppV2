package com.stickynotev2.model;

import java.util.ArrayList;
import java.util.List;

public class AppState {
    private int version = 1;
    private List<Note> notes = new ArrayList<>();

    public int getVersion() {
        return version;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }
}
