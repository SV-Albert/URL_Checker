import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * ThreadMonitor class is responsible for creating and updating the
 * CheckerThread objects
 *
 * @version 0.4
 * @author Albert Shakirzianov
 */
public class ThreadMonitor implements Runnable{

    private final HashMap<CheckerThread, Thread> threadMap;
    private final HashMap<String, CheckerThread> checkerMap;
    private final DataManager dataManager;

    /**
     * Constructor for the ThreadMonitor class
     *
     * @param dataManager
     */
    public ThreadMonitor(DataManager dataManager){
        threadMap = new HashMap<>();
        checkerMap = new HashMap<>();
        this.dataManager = dataManager;
    }

    /**
     * Start method for the thread object
     */
    @Override
    public void run() {
        startThreads();
    }

    /**
     * Acquire a set of all saved URLs and call the createThread method
     * to create a CheckerThread object for each of the URLs
     */
    public void startThreads(){
        Set<String> urlList = dataManager.getUrlKeyMap().keySet();
        if (!urlList.isEmpty()){
            for (String url: urlList) {
                createThread(url);
            }
        }
    }

    /**
     * Create a CheckerThread object
     *
     * @param url for the CheckerThread to monitor
     */
    synchronized public void createThread(String url){
        ArrayList<String> keywords = dataManager.getKeywords(url);
        CheckerThread checker = new CheckerThread(this, url, keywords);
        Thread thread = new Thread(checker);
        threadMap.put(checker, thread);
        checkerMap.put(url, checker);
        System.out.println(thread.toString() + " is created");
        thread.start();
    }

    /**
     * Request a CheckerThread to run hashing when the list of
     * keywords it has access to has been modified
     *
     * @param url
     */
    public void requestHashing(String url){
        CheckerThread checker = checkerMap.get(url);
        checker.requestHashing();
        threadMap.get(checker).interrupt();
    }

    /**
     * Stop a CheckerThread thread
     *
     * @param url
     */
    public void stopThread(String url){
        CheckerThread checker = checkerMap.get(url);
        checker.stopRunning();
    }

    /**
     * Check if the input String has been hashed by the DataManager object
     *
     * @param str input String
     * @return true if the String was already hashed
     */
    public boolean addHashIfNotExcluded(String str) {
        return dataManager.addHashIfNotExcluded(str);
    }

    /**
     * Force search on all of the CheckerThread
     */
    public void refresh(){
        for (Thread thread: threadMap.values()) {
            thread.interrupt();
        }
    }

    /**
     * Notify the DataManager that a match has been found
     *
     * @param url that match has been found on
     * @param keyword that match has been found on
     */
    public void matchFound(String url, String keyword){
        dataManager.matchFound(url, keyword);
    }

    /**
     * Notify the DataManager that an error has occured
     *
     * @param errorMessage
     */
    public void error(String errorMessage){
        dataManager.error(errorMessage);
    }
}
