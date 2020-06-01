import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class ThreadMonitor implements Runnable{

    private HashMap<String, ArrayList<String>> urlMap;
    private final ArrayList<Thread> checkerThreads;
    private ArrayList<Integer> hashes;
    private final HashMap<URLChecker, Thread> threadMap;
    private final GUI gui;
    private final FileManager fileManager;

    public ThreadMonitor(GUI gui){
        this.gui = gui;
        urlMap = new HashMap<>();
        checkerThreads = new ArrayList<>();
        threadMap = new HashMap<>();
        hashes = new ArrayList<>();
        Path path = Paths.get("src/main/resources/url_checker_data.txt"); //temporary
        fileManager = new FileManager(path, this);
        load();
        gui.repopulateViews(urlMap);
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
        checkerThreads.add(thread);
        threadMap.put(checker, thread);
        System.out.println("Thread " + thread.toString() + " is created");
        thread.start();
    }

    synchronized public void exclude(String str) {
        int hash = str.hashCode();
        hashes.add(hash);

        System.out.println(hash);
    }

    public boolean isExcluded(String str){
        return hashes.contains(str.hashCode());
    }

    public void addKeyword(String url, String keyword){
        for (URLChecker checker: threadMap.keySet()) {
            if (checker.getURL().equals(url)){
                checker.addKeyword(keyword);
                threadMap.get(checker).interrupt();
                break;
            }
        }
        save();
    }

    public void addUrl(String url){
        urlMap.put(url, new ArrayList<String>());
        createThread(url);
        save();
    }

    synchronized public void matchFound(String url, String keyword){
        Platform.runLater(() -> gui.successNotification(url, keyword));
    }

    synchronized public void error(String message){
        Platform.runLater(() -> gui.failNotification(message));
    }

    public void refresh(){
        for (Thread thread: checkerThreads) {
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
        }
        catch (IOException e){
            error("Could not load the save data");
        }

    }
}
