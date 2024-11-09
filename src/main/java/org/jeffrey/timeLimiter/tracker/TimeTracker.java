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
        this.userTimeMap = loadDataFromJson(DATA_FILE);
        startGlobalTimer();
    }

    public void saveDataToJson() {
        System.out.println("Saved data!");
        Map<String, Integer> oldData = loadRawDataFromJson(DATA_FILE);
        for(Map.Entry<String, PlayerTimeTracker> entry : userTimeMap.entrySet()){
            oldData.put(entry.getKey(), entry.getValue().getTimeSpent());
        }
        writeRawDataToJson(DATA_FILE,oldData);
    }
    public void saveAll(){
        for(Map.Entry<String, PlayerTimeTracker> entry : userTimeMap.entrySet()){
            stopTimer(entry.getValue().getPlayer());
        }
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
            System.out.println("Global time spent data saved to JSON file.");
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    public void startGlobalTimer(){
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::cheekTime, 0, 1, TimeUnit.HOURS);

    }

    public void cheekTime() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int month = Calendar.getInstance().get(Calendar.MONTH);

        File cheek = new File("./old/"+day+"-"+month+DATA_FILE);
        if(!cheek.exists()){
            saveDataToJson("./old/"+day+"-"+month+DATA_FILE,userTimeMap);
            clearJson();
        }
    }

    void writeRawDataToJson(String fileName,Map<String, Integer> map){
        Gson gson = new Gson();
        String json = gson.toJson(map);
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(json);
            System.out.println("Time spent data saved to JSON file.");
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
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
            System.out.println("Time spent data saved to JSON file.");
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }
    public void kickPlayer(Player player){
        System.out.println("Kicking player "+player.getDisplayName());
        if(player != null){
            if(!player.isOp()){
                pEvents.kickPlayer(player);
            }else{
                player.sendMessage("You were going to be kicked, but you are an op!");
            }
        }
    }

    public void startTimer(Player player){
        Map<String, PlayerTimeTracker> tempTimeMap = loadDataFromJson(DATA_FILE);
        if(tempTimeMap.containsKey(player.getUniqueId().toString())){
            userTimeMap.put(
                    player.getUniqueId().toString(),
                    tempTimeMap.get(player.getUniqueId().toString()));
            if(userTimeMap.get(player.getUniqueId().toString()).getTimeSpent()>this.pEvents.plugin.getConfig().getInt("timelimit")){
                pEvents.kickPlayer(player);
            }
        }else{
            System.out.println("New player!");
            userTimeMap.put(player.getUniqueId().toString(), new PlayerTimeTracker(player, 0, this));
            userTimeMap.get(player.getUniqueId().toString()).playerStartTimer();
        }
    }
    private void clearJson(){

        for(Map.Entry<String, PlayerTimeTracker> entry : userTimeMap.entrySet()){
            entry.getValue().setPlayerTime(0);
        }
        saveDataToJson();
    }
    public void stopTimer(Player player){
        System.out.println("Trying to stop timer");
        userTimeMap.get(player.getUniqueId().toString()).playerStopTimer();
        saveDataToJson();
        userTimeMap.remove(player.getUniqueId().toString());
    }

    public int getPlayerTime(Player player){
        if(userTimeMap.containsKey(player.getUniqueId().toString())){
            return userTimeMap.get(player.getUniqueId().toString()).getTimeSpent();
        }else{
            return 0;
        }
    }

    public boolean setPlayerTime(Player player, int time){
        if(userTimeMap.containsKey(player.getUniqueId().toString())){
            userTimeMap.get(player.getUniqueId().toString()).setPlayerTime(time);
            return true;
        }else{
            return false;
        }
    }

    public void cheekDay(){

    }

//    private void saveTimeToGlobal(){
//        Map<Player, Integer> globalUserTimeMap = loadDataFromJson(DATA_FILE_GLOBAL);
//        for(Map.Entry<String, Integer> entry : userTimeMap.entrySet()){
//            if(globalUserTimeMap.containsKey(entry.getKey())){
//                globalUserTimeMap.put(entry.getKey(), globalUserTimeMap.get(entry.getKey()) + entry.getValue());
//            }else{
//                globalUserTimeMap.put(entry.getKey(), entry.getValue());
//            }
//        }
//        saveDataToJson(DATA_FILE_GLOBAL,globalUserTimeMap);
//    }

    public Map<String, PlayerTimeTracker> loadDataFromJson(){
            return loadDataFromJson(DATA_FILE);
    }
    public Map<String, PlayerTimeTracker> loadDataFromJson(String fileName) {
        if (Files.exists(Paths.get(fileName))) {
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, Integer>>() {}.getType();
            try (FileReader reader = new FileReader(fileName)) {
                Map<String, Integer> data = gson.fromJson(reader, mapType);
                Map<String, PlayerTimeTracker> playerTimeTrackerMap = new HashMap<>();
                System.out.println("Loaded time spent data from JSON.");
                for(Map.Entry<String, Integer> entry : data.entrySet()){
                    Player p = Bukkit.getPlayer(UUID.fromString(entry.getKey()));
                    if(p!=null){
                        PlayerTimeTracker ptt = new PlayerTimeTracker(p, entry.getValue(), this);
                        ptt.playerStartTimer();
                        playerTimeTrackerMap.put(entry.getKey(), ptt);
                    }
                }
                return playerTimeTrackerMap;
            } catch (IOException | JsonParseException e) {
                System.err.println("An error occurred while reading from the file: " + e.getMessage());
            }
        } else {
            System.out.println("No existing timer data found. Starting fresh.");
        }

        return new HashMap<>();
    }

    public Map<String, Integer> loadRawDataFromJson(String fileName) {
        if (Files.exists(Paths.get(fileName))) {
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, Integer>>() {}.getType();
            try (FileReader reader = new FileReader(fileName)) {
                return gson.fromJson(reader, mapType);
            } catch (IOException | JsonParseException e) {
                System.err.println("An error occurred while reading from the file: " + e.getMessage());
            }
        } else {
            System.out.println("No existing timer data found. Starting fresh.");
        }

        return new HashMap<>();
    }
}

