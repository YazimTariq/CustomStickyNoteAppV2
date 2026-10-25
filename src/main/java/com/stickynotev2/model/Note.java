package com.stickynotev2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Note {
    private String id;
    private List<TextRun> content;
    private List<InkStroke> strokes;
    private NoteColour colour;
    private int x,y,width,height;
    private boolean open,pinned,deleted;
    private long createdAt,updatedAt,deletedAt;

    public Note() {
        id = UUID.randomUUID().toString();
        content = new ArrayList<>();
        strokes = new ArrayList<>();
        colour = NoteColour.YELLOW;
        x = 200;
        y = 150;
        width = 300;
        height = 320;
        open = true;
        pinned = false;
        deleted = false;
        createdAt = updatedAt = System.currentTimeMillis();
        deletedAt = 0;
    }

    public static Note createNew(int x, int y){
        Note newNote = new Note();
        newNote.setX(x);
        newNote.setY(y);
        return newNote;
    }

    public String getPlainText(){
        StringBuilder builder = new StringBuilder();
        for(TextRun runText: content){
            builder.append(runText.getText());
            builder.append("\n");
        }
        return builder.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<TextRun> getContent() {
        return content;
    }

    public void setContent(List<TextRun> content) {
        this.content = content;
    }

    public NoteColour getColour() {
        return colour;
    }

    public List<InkStroke> getStrokes() {
        return strokes;
    }

    public void setStrokes(List<InkStroke> strokes) {
        this.strokes = strokes;
    }

    public void setColour(NoteColour colour) {
        this.colour = colour;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(long deletedAt) {
        this.deletedAt = deletedAt;
    }
}
