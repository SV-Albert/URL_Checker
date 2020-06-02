import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.ArrayList;

public class URLChecker implements Runnable {

    private final String url;
    private final ArrayList<String> keywords;
    private ArrayList<Integer> hashes;
    private final Monitor monitor;
    private boolean hashingRequired;
    private String lastMatch;
    private boolean running;

    public URLChecker(Monitor monitor, String url, ArrayList<String> keywords){
        this.monitor = monitor;
        this.url = url;
        this.keywords = keywords;
        hashingRequired = true;
        running = true;
        hashes = new ArrayList<Integer>();
    }

    @Override
    public void run() {
        while(running){
            try{
                if (hashingRequired){
                    runHashing();
                }
                boolean found = search();
                if(found){monitor.matchFound(url, lastMatch);}
            }
            catch (IOException e){
                monitor.error("Could not access " + url);
            }

            try{
                System.out.println("Thread " + Thread.currentThread().toString() +  " is sleeping...");
                Thread.sleep(10000);
            }
            catch (InterruptedException e){
                System.out.println(Thread.currentThread().toString() + " is awoken");
            }
        }
    }

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

                        if (!monitor.isExcluded(hashStr)) {
                            System.out.println(url + " " + word + " " + matchInd);
                            int hashedValue = monitor.exclude(hashStr);
                            hashes.add(hashedValue);
                            lastMatch = word;
                            found = true;
                        }
                    }
                }
            }
        }
        return found;
    }


    private void runHashing() throws IOException {
        search();
        System.out.println("Hashing complete");
        hashingRequired = false;
        monitor.save();
    }

    public ArrayList<Integer> getHashes(){ return hashes; }

    public void clearHashes(){
        hashes.clear();
    }

    public void requestHashing(){
        hashingRequired = true;
    }

    public void stopRunning(){ running = false; }

}
