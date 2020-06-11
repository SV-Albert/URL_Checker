import javafx.application.Platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class DataManager {

    private HashMap<String, ArrayList<String>> urlKeyMap;
    private ArrayList<Integer> hashList;
    private ArrayList<String> logList;
    private final SaveManager saveManager;
    private Root root;

    public DataManager(Root root){
        this.root = root;
        saveManager = new SaveManager(this);
    }

    synchronized public void load(){
        try{
            urlKeyMap = saveManager.loadKeyMap();
            logList = saveManager.loadLogs();
        }
        catch (IOException e) {
            error("Unable to load a save file");
            urlKeyMap = new HashMap<>();
            logList = new ArrayList<>();
        }
        hashList = new ArrayList<>();
    }

    synchronized public void addUrl(String url){
        urlKeyMap.put(url, new ArrayList<>());
        save();
    }

    synchronized public void deleteUrl(String url) {
        urlKeyMap.get(url).clear();
        urlKeyMap.remove(url);
        save();
    }

    synchronized public void addKeyword(String url, String keyword){
        urlKeyMap.get(url).add(keyword);
        save();
    }

    synchronized public void deleteKeyword(String url, String keyword) {
        urlKeyMap.get(url).remove(keyword);
        save();
    }

    public ArrayList<String> getKeywords(String url){
        return urlKeyMap.get(url);
    }

    public HashMap<String, ArrayList<String>> getUrlKeyMap(){
        return urlKeyMap;
    }

    synchronized public boolean addHashIfNotExcluded(String str){
        int hash = str.hashCode();
        boolean isExcluded = hashList.contains(hash);
        if (!isExcluded) {
            hashList.add(hash);

            System.out.println(hash);
        }
        return isExcluded;
    }

    synchronized public void addLogEntry(String log){
        logList.add(log);
        save();
    }

    synchronized public void removeLogEntry(String log) {
        logList.remove(log);
        save();
    }

    synchronized public void clearLogs(){
        logList.clear();
        save();
    }

    public ArrayList<String> getLogs(){
        return logList;
    }

    synchronized public void save(){
        try {
            saveManager.save();
        } catch (IOException e) {
            error("An exception occurred while saving");
        }
    }

    synchronized public void matchFound(String url, String keyword){
        Platform.runLater(() -> root.successNotification(url, keyword));
        save();
    }

    synchronized public void error(String message){
        Platform.runLater(() -> root.failNotification(message));
    }

}
