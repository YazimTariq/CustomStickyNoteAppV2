package com.stickynotev2.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.stickynotev2.model.AppState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NoteRepository {

    private final Path file;
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public NoteRepository(Path file){
        this.file = file;
    }

    public static Path defaultPath(){
        String appData = System.getenv("APPDATA");
        if(appData == null){
            appData = System.getProperty("user.home");
        }

        Path path = Path.of(appData);
        path = path.resolve("StickyLite");
        path = path.resolve("notes.json");
        return path;
    }

    public AppState load(){
        if(!Files.exists(file)){
            return new AppState();
        }
        try {
            String jsonText = Files.readString(file, StandardCharsets.UTF_8);
            AppState state = gson.fromJson(jsonText, AppState.class);
            if(state == null){
                return new AppState();
            }
            else{
                return state;
            }
        }
        catch (IOException | JsonSyntaxException e){
            String filePath = file.getFileName().toString();
            filePath += ".bak";
            Path backup = file.resolveSibling(filePath);
            try {
                Files.copy(file, backup);
            } catch (IOException backupError) {

            }
        }
        return new AppState();
    }

    public void save(AppState appState){
        try {
            Path parentPath = file.getParent();
            Path temp = file.resolveSibling("notes.json.tmp");
            String jsonText = gson.toJson(appState);

            Files.createDirectories(file.getParent());
            Files.writeString(temp, jsonText, StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e){
            System.err.println("Failed to save notes: " + e.getMessage());
        }
    }


}
