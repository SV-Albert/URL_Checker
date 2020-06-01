import javafx.application.Platform;
import java.util.ArrayList;
import java.util.HashMap;

public class ThreadMonitor implements Runnable{

    private final ArrayList<String> urls;
    private final ArrayList<String> keywords;
    private final ArrayList<Thread> checkerThreads;
    private final ArrayList<Integer> hashes;
    private final HashMap<URLChecker, Thread> threadMap;
    private final GUI gui;

    public ThreadMonitor(GUI gui){
        this.gui = gui;
        urls = new ArrayList<>();
        urls.add("http://scpfoundation.ru/");
        urls.add( "https://xakep.ru");
        keywords = new ArrayList<>();
        keywords.add("devops");
        keywords.add("scp");
        checkerThreads = new ArrayList<>();
        threadMap = new HashMap<>();
        hashes = new ArrayList<>();
//        repopulate();
    }

    @Override
    public void run() {
        startThreads();
    }

    public void startThreads(){
        if (!urls.isEmpty()){
            for (String url: urls) {
                createThread(url);
            }
        }
    }

    synchronized private void createThread(String url){
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

    public void addKeyword(String keyword){
        keywords.add(keyword.toLowerCase());
        for (URLChecker checker: threadMap.keySet()) {
            checker.updateKeywords(keywords);
        }
        for (Thread thread: checkerThreads) {
            thread.interrupt();
        }
    }

    public void addUrl(String url){
        urls.add(url);
        createThread(url);
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

}
