import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class Monitor implements Runnable{

    private HashMap<String, ArrayList<String>> urlMap;
    private ArrayList<Integer> hashes;
    private final HashMap<URLChecker, Thread> threadMap;
    private final HashMap<String, URLChecker> checkerMap;
    private final Root root;
    private final FileManager fileManager;
    private ArrayList<String> matchLogs;

    public Monitor(Root root){
        this.root = root;
        urlMap = new HashMap<>();
        threadMap = new HashMap<>();
        checkerMap = new HashMap<>();
        hashes = new ArrayList<>();
        Path path = Paths.get("src/main/resources/url_checker_data.txt"); //temporary
        fileManager = new FileManager(path, this);
        load();
        root.repopulateViews(urlMap, matchLogs);
    }

    @Override
    public void run() {
        startThreads();
    }

    public void startThreads(){
        if (!urlMap.keySet().isEmpty()){
            for (String url: urlMap.keySet()) {
                createThread(url);
            }
        }
    }

    synchronized private void createThread(String url){
        ArrayList<String> keywords = urlMap.get(url);
        URLChecker checker = new URLChecker(this, url, keywords);
        Thread thread = new Thread(checker);
        threadMap.put(checker, thread);
        checkerMap.put(url, checker);
        System.out.println("Thread " + thread.toString() + " is created");
        thread.start();
    }

    synchronized public int exclude(String str) {
        int hash = str.hashCode();
        hashes.add(hash);
        System.out.println(hash);
        return hash;
    }

    public boolean isExcluded(String str){
        return hashes.contains(str.hashCode());
    }


    synchronized public void matchFound(String url, String keyword){
        Platform.runLater(() -> root.successNotification(url, keyword));
        save();
    }

    synchronized public void error(String message){
        Platform.runLater(() -> root.failNotification(message));
    }

    public void refresh(){
        for (Thread thread: threadMap.values()) {
            thread.interrupt();
        }
    }

    public ArrayList<Integer> getHashes() {
        return hashes;
    }

    public HashMap<String, ArrayList<String>> getUrlMap() {
        return urlMap;
    }

    synchronized public void save(){
        try {
            fileManager.save();
        }
        catch (IOException e){
            error("Failed to save the current state");
        }
    }

    synchronized public void load(){
        try{
            hashes = fileManager.loadHashes();
            urlMap = fileManager.loadKeyMap();
            matchLogs = fileManager.loadLogs();
        }
        catch (IOException e){
            error("Could not load the save data");
        }

    }

    public void addKeyword(String url){
        URLChecker checker = checkerMap.get(url);
        checker.requestHashing();
        threadMap.get(checker).interrupt();
    }

    public void addUrl(String url){
        urlMap.put(url, new ArrayList<String>());
        createThread(url);
    }

    public void deleteKeyword(String url){
        URLChecker checker = checkerMap.get(url);
        hashes.removeAll(checker.getHashes());
//        checker.deleteKeyword(keyword);
        checker.clearHashes();
        checker.requestHashing();
        threadMap.get(checker).interrupt();
    }

    public void deleteUrl(String url){
        URLChecker checker = checkerMap.get(url);
        hashes.removeAll(checker.getHashes());
        checker.stopRunning();
    }

    public void setLogs(ArrayList<String> logs){
        matchLogs = logs;
    }

    public ArrayList<String> getLogs(){ return matchLogs; }

}
