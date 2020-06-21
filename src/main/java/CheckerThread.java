import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.ArrayList;


/**
 * CheckerThread is a runnable object that checks a particular URL
 * for updates that include specified keywords
 *
 * @version 0.4
 * @author Albert Shakirzianov
 */
public class CheckerThread implements Runnable {

    private final String url;
    private final ArrayList<String> keywords;
    private final ThreadMonitor threadMonitor;
    private boolean hashingRequired;
    private String lastMatch;
    private boolean running;

    /**
     * Constructor for the CheckerThread class
     *
     * @param threadMonitor ThreadMonitor that created the object
     * @param url of the website to monitor
     * @param keywords list of keywords to check the updates against
     */
    public CheckerThread(ThreadMonitor threadMonitor, String url, ArrayList<String> keywords){
        this.threadMonitor = threadMonitor;
        this.url = url;
        this.keywords = keywords;
        hashingRequired = true;
    }

    /**
     * Start method for the thread object
     */
    @Override
    public void run() {
        running = true;
        while(running){
            try{
                if (hashingRequired){ //run the search method and hash the matches without creating a success notification
                    search();
                    System.out.println("Hashing complete");
                    hashingRequired = false;
                }
                boolean found = search();
                if(found){
                    threadMonitor.matchFound(url, lastMatch);}
            }
            catch (IOException e){
                threadMonitor.error("Could not access " + url);
            }

            try{
                System.out.println(Thread.currentThread().toString() +  " is sleeping...");
                Thread.sleep(30000);
            }
            catch (InterruptedException e){
                System.out.println(Thread.currentThread().toString() + " is awoken");
            }
        }
    }

    /**
     * Search the url page for matches against the list of keywords
     *
     * @return true if a match was found
     * @throws IOException
     */
    synchronized private boolean search() throws IOException{
        boolean found = false;
        if(!keywords.isEmpty()){
            for (String word : keywords) { //search every listed website for every listed word
                Document doc = Jsoup.connect(url).get();
                String body = doc.body().text().toLowerCase();
                if (body.contains(word)) { //check if the document contains the currently checked keyword
                    int pageIndex = 0; //index counter
                    while (pageIndex < body.lastIndexOf(word)) { //check indexes from 0 to last character of the last found word occurrence
                        int matchInd = body.indexOf(word, pageIndex); //index of the first letter of the matched word
                        String hashStr;
                        if (matchInd + 100 < body.length()) {
                            hashStr = body.substring(matchInd, matchInd + 100); //make a substring of 100 characters starting with the matched word
                        } else {
                            hashStr = body.substring(matchInd, body.length() - 1);
                        }
                        pageIndex = matchInd + word.length(); //set the index to the last char of the matched word
                        boolean isExcluded = threadMonitor.addHashIfNotExcluded(hashStr);
                        if (!isExcluded) {
                            System.out.println(url + " " + word + " " + matchInd);
                            lastMatch = word;
                            found = true;
                        }
                    }
                }
            }
        }
        return found;
    }

    /**
     * Force the checker to run hashing again
     */
    public void requestHashing(){
        hashingRequired = true;
    }

    /**
     * Stop the thread
     */
    public void stopRunning(){ running = false; }

}
