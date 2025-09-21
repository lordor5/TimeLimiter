package org.jeffrey.timeLimiter.tracker;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jeffrey.timeLimiter.playerEvents;


public class TimeTracker {

    private static final String DATA_FILE = "timer_data.json";
    private static final String DATA_FILE_GLOBAL = "timer_data_global.json";
    private Map<String, PlayerTimeTracker> userTimeMap;
    public final playerEvents pEvents;
    private ScheduledExecutorService scheduler;

    public TimeTracker(playerEvents pEvents) {
        this.pEvents = pEvents;
        this.userTimeMap = new HashMap<>(); // Start with empty map
    }

    public void saveDataToJson() {
        pEvents.plugin.getLogger().info("Saved data!");
        Map<String, Integer> oldData = loadRawDataFromJson(DATA_FILE);
        for(Map.Entry<String, PlayerTimeTracker> entry : userTimeMap.entrySet()){
            oldData.put(entry.getKey(), entry.getValue().getTimeSpent());
        }
        writeRawDataToJson(DATA_FILE,oldData);
    }
    
    public void saveAll(){
        for(Map.Entry<String, PlayerTimeTracker> entry : userTimeMap.entrySet()){
            entry.getValue().playerStopTimer(); // Stop the timer first
        }
        saveDataToJson(); // Then save the data
        userTimeMap.clear(); // Clear the map
    }

    public void saveGlobalDataToJson(String fileName, Map<String, Map<String, PlayerTimeTracker>> userMap) {
        Gson gson = new Gson();
        Map<String, Map<String, Integer>> mainTimeMap = new HashMap<>();
        for(Map.Entry<String, Map<String, PlayerTimeTracker>> entry : userMap.entrySet()){
            Map<String, Integer> tempTimeMap = new HashMap<>();
            for(Map.Entry<String, PlayerTimeTracker> entry2 : entry.getValue().entrySet()){
                tempTimeMap.put(entry2.getKey(), entry2.getValue().getTimeSpent());
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            mainTimeMap.put(timeStamp,tempTimeMap);
        }
        String json = gson.toJson(mainTimeMap);
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(json);
            pEvents.plugin.getLogger().info("Global time spent data saved to JSON file.");
        } catch (IOException e) {
            pEvents.plugin.getLogger().severe("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    void writeRawDataToJson(String fileName,Map<String, Integer> map){
        Gson gson = new Gson();
        String json = gson.toJson(map);
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(json);
            pEvents.plugin.getLogger().info("Time spent data saved to JSON file.");
        } catch (IOException e) {
            pEvents.plugin.getLogger().severe("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    public void saveDataToJson(String fileName,Map<String, PlayerTimeTracker> userMap) {
        Gson gson = new Gson();
        Map<String, Integer> timeMap = new HashMap<>();
        for(Map.Entry<String, PlayerTimeTracker> entry : userMap.entrySet()){
            timeMap.put(entry.getKey(), entry.getValue().getTimeSpent());
        }
        String json = gson.toJson(timeMap);
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(json);
            pEvents.plugin.getLogger().info("Time spent data saved to JSON file.");
        } catch (IOException e) {
            pEvents.plugin.getLogger().severe("An error occurred while writing to the file: " + e.getMessage());
        }
    }
    
    public void kickPlayer(Player player){
        pEvents.plugin.getLogger().info("Kicking player " + player.getDisplayName());
        if(player != null){
            if(!player.isOp()){
                pEvents.kickPlayer(player);
            }else{
                player.sendMessage("You were going to be kicked, but you are an op!");
            }
        }
    }

    public void startTimer(Player player){
        String playerId = player.getUniqueId().toString();
        
        // Stop any existing timer for this player first
        if(userTimeMap.containsKey(playerId)){
            userTimeMap.get(playerId).playerStopTimer();
            userTimeMap.remove(playerId);
        }
        
        // Load existing time from file
        int existingTime = loadTimeSpentFromJson(playerId);
        
        // Check if player should be kicked before starting timer
        if(existingTime > this.pEvents.plugin.getConfig().getInt("timelimit")){
            pEvents.kickPlayer(player);
            return;
        }
        
        // Create new tracker and start timer
        PlayerTimeTracker tracker = new PlayerTimeTracker(player, existingTime, this);
        userTimeMap.put(playerId, tracker);
        tracker.playerStartTimer();
        
        pEvents.plugin.getLogger().info("Timer started for player: " + player.getDisplayName() + " with existing time: " + existingTime);
    }

    public void stopTimer(Player player){
        String playerId = player.getUniqueId().toString();
        pEvents.plugin.getLogger().info("Trying to stop timer for: " + player.getDisplayName());
        
        if(userTimeMap.containsKey(playerId)){
            userTimeMap.get(playerId).playerStopTimer();
            saveDataToJson();
            userTimeMap.remove(playerId);
            pEvents.plugin.getLogger().info("Timer stopped for: " + player.getDisplayName());
        } else {
            pEvents.plugin.getLogger().warning("No active timer found for: " + player.getDisplayName());
        }
    }

    public int getPlayerTime(Player player){
        String playerId = player.getUniqueId().toString();
        if(userTimeMap.containsKey(playerId)){
            return userTimeMap.get(playerId).getTimeSpent();
        }else{
            return loadTimeSpentFromJson(playerId);
        }
    }

    public boolean setPlayerTime(Player player, int time){
        String playerId = player.getUniqueId().toString();
        if(userTimeMap.containsKey(playerId)){
            // Player is online - update the active tracker
            userTimeMap.get(playerId).setPlayerTime(time);
            return true;
        }else{
            // Player is not online - update the saved data directly
            Map<String, Integer> data = loadRawDataFromJson(DATA_FILE);
            data.put(playerId, time);
            writeRawDataToJson(DATA_FILE, data);
            return true;
        }
    }

    // New method to set time by UUID string (for offline players)
    public boolean setPlayerTimeByUUID(String playerUUID, int time){
        if(userTimeMap.containsKey(playerUUID)){
            // Player is online - update the active tracker
            userTimeMap.get(playerUUID).setPlayerTime(time);
            return true;
        }else{
            // Player is not online - update the saved data directly
            Map<String, Integer> data = loadRawDataFromJson(DATA_FILE);
            data.put(playerUUID, time);
            writeRawDataToJson(DATA_FILE, data);
            pEvents.plugin.getLogger().info("Set time for offline player (UUID: " + playerUUID + ") to " + time + " minutes");
            return true;
        }
    }

    // New method to get player time by UUID string (for offline players)
    public int getPlayerTimeByUUID(String playerUUID){
        if(userTimeMap.containsKey(playerUUID)){
            return userTimeMap.get(playerUUID).getTimeSpent();
        }else{
            return loadTimeSpentFromJson(playerUUID);
        }
    }

    public Map<String, PlayerTimeTracker> loadDataFromJson(){
        return loadDataFromJson(DATA_FILE);
    }
    
    public Map<String, PlayerTimeTracker> loadDataFromJson(String fileName) {
        // This method should only load data, not start timers
        // Timers should only be started when players actually join
        return new HashMap<>();
    }

    public Map<String, Integer> loadRawDataFromJson(String fileName) {
        if (Files.exists(Paths.get(fileName))) {
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, Integer>>() {}.getType();
            try (FileReader reader = new FileReader(fileName)) {
                Map<String, Integer> data = gson.fromJson(reader, mapType);
                pEvents.plugin.getLogger().info("Loaded time spent data from JSON.");
                return data != null ? data : new HashMap<>();
            } catch (IOException | JsonParseException e) {
                pEvents.plugin.getLogger().severe("An error occurred while reading from the file: " + e.getMessage());
            }
        } else {
            pEvents.plugin.getLogger().info("No existing timer data found. Starting fresh.");
        }

        return new HashMap<>();
    }

    public int loadTimeSpentFromJson(String playerId) {
        Map<String, Integer> data = loadRawDataFromJson(DATA_FILE);
        return data.getOrDefault(playerId, 0);
    }

    public int loadTimeSpentFromJson(Player player) {
        return loadTimeSpentFromJson(player.getUniqueId().toString());
    }
}