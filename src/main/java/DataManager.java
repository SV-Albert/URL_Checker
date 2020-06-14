import javafx.application.Platform;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * DataManager class is responsible for modifying the data used
 * by other objects
 *
 * @version 0.2
 * @author Albert Shakirzianov
 */
public class DataManager {

    private HashMap<String, ArrayList<String>> urlKeyMap;
    private ArrayList<Integer> hashList;
    private ArrayList<String> logList;
    private SaveManager saveManager;
    private Main main;

    /**
     * Constructor for the DataManager class
     *
     * @param main Main object
     */
    public DataManager(Main main){
        this.main = main;
        try {
            saveManager = new SaveManager(this);
        } catch (IOException e) {
            error("Could not create a save file");
        }
    }

    /**
     * Load the data from the save manager
     */
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

    /**
     * Add a new URL to the urlKeyMap
     *
     * @param url to add
     */
    synchronized public void addUrl(String url){
        urlKeyMap.put(url, new ArrayList<>());
        save();
    }

    /**
     * Delete a URL from the urlKeyMap
     *
     * @param url
     */
    synchronized public void deleteUrl(String url) {
        urlKeyMap.get(url).clear();
        urlKeyMap.remove(url);
        save();
    }

    /**
     * Add a new keyword to the list mapped to an existing URL
     *
     * @param url Hashmap key
     * @param keyword to add
     */
    synchronized public void addKeyword(String url, String keyword){
        urlKeyMap.get(url).add(keyword);
        save();
    }

    /**
     * Remove a keyword from the list mapped to an existing URL
     *
     * @param url Hashmap key
     * @param keyword to add
     */
    synchronized public void deleteKeyword(String url, String keyword) {
        urlKeyMap.get(url).remove(keyword);
        save();
    }

    /**
     * Get the list of keywords mapped to a URL
     *
     * @param url key map
     * @return keyword list associated to the URL
     */
    public ArrayList<String> getKeywords(String url){
        return urlKeyMap.get(url);
    }

    /**
     * Get the urlKeyMap object
     *
     * @return urlKeyMap
     */
    public HashMap<String, ArrayList<String>> getUrlKeyMap(){
        return urlKeyMap;
    }

    /**
     * Check if the input String has a corresponding hash value stored
     * in the hashList, add it if not
     *
     * @param str input String
     * @return true if the String was already hashed
     */
    synchronized public boolean addHashIfNotExcluded(String str){
        int hash = str.hashCode();
        boolean isExcluded = hashList.contains(hash);
        if (!isExcluded) {
            hashList.add(hash);

            System.out.println(hash);
        }
        return isExcluded;
    }

    /**
     * Add a new log entry to the logList
     *
     * @param log entry to add
     */
    synchronized public void addLogEntry(String log){
        logList.add(log);
        save();
    }

    /**
     * Remove a log entry from the logList
     *
     * @param log entry to remove
     */
    synchronized public void removeLogEntry(String log) {
        logList.remove(log);
        save();
    }

    /**
     * Remove all log entries from the logList
     */
    synchronized public void clearLogs(){
        logList.clear();
        save();
    }

    /**
     * Get the logList
     *
     * @return logList
     */
    public ArrayList<String> getLogs(){
        return logList;
    }

    /**
     * Invoke the save method on the SaveManager object
     */
    synchronized public void save(){
        try {
            saveManager.save();
        } catch (IOException e) {
            error("An exception occurred while saving");
        }
    }

    /**
     * Create a success notification in the Main object
     *
     * @param url of the website were match occurred
     * @param keyword on which the match occurred
     */
    synchronized public void matchFound(String url, String keyword){
        Platform.runLater(() -> main.successNotification(url, keyword));
        save();
    }

    /**
     * Create an error notification in the Main object
     *
     * @param message to display
     */
    synchronized public void error(String message){
        Platform.runLater(() -> main.errorNotification(message));
    }

    /**
     * Open the URl in the default browser
     *
     * @param url to open
     */
    public void openInBrowser(String url) {
        try{
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(new URI(url));
            }
            else {
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("xdg-open " + url);
            }
        }
        catch (Exception e) {
            error("Failed to open the link");
        }
    }
}
