import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ThreadMonitor implements Runnable{

    private final HashMap<CheckerThread, Thread> threadMap;
    private final HashMap<String, CheckerThread> checkerMap;
    private final DataManager dataManager;

    public ThreadMonitor(DataManager dataManager){
        threadMap = new HashMap<>();
        checkerMap = new HashMap<>();
        this.dataManager = dataManager;
    }

    @Override
    public void run() {
        startThreads();
    }

    public void startThreads(){
        Set<String> urlList = dataManager.getUrlKeyMap().keySet();
        if (!urlList.isEmpty()){
            for (String url: urlList) {
                createThread(url);
            }
        }
    }

    synchronized public void createThread(String url){
        ArrayList<String> keywords = dataManager.getKeywords(url);
        CheckerThread checker = new CheckerThread(this, url, keywords);
        Thread thread = new Thread(checker);
        threadMap.put(checker, thread);
        checkerMap.put(url, checker);
        System.out.println("Thread " + thread.toString() + " is created");
        thread.start();
    }

    public void addKeyword(String url){
        CheckerThread checker = checkerMap.get(url);
        checker.requestHashing();
        threadMap.get(checker).interrupt();
    }

    public void deleteKeyword(String url){
        CheckerThread checker = checkerMap.get(url);
        checker.requestHashing();
        threadMap.get(checker).interrupt();
    }

    public void deleteThread(String url){
        CheckerThread checker = checkerMap.get(url);
        checker.stopRunning();
    }

    public boolean addHashIfNotExcluded(String str) {
        return dataManager.addHashIfNotExcluded(str);
    }

    public void refresh(){
        for (Thread thread: threadMap.values()) {
            thread.interrupt();
        }
    }

    public void matchFound(String url, String keyword){
        dataManager.matchFound(url, keyword);
    }

    public void error(String errorMessage){
        dataManager.error(errorMessage);
    }
}
