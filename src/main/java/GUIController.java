import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class GUIController {

    @FXML private TextField urlField;
    @FXML private TextField keyField;
    @FXML private ListView<String> urlView;
    @FXML private ListView<String> keyView;
    @FXML private ListView<String> matchLogView;
    @FXML private MenuItem refreshMenu;
    private ThreadMonitor monitor;
    private final URLFormatter formatter = new URLFormatter();
    private HashMap<String, ArrayList<String>> urlMap;
    private ArrayList<String> matchLog;


    @FXML
    public void initialize(){
        addChangeListener();
    }

    @FXML
    private void urlSubmit(){
        String url = formatter.formatURL(urlField.getText());
        if(formatter.validateURL(url)){
            boolean estConnection = false;
            try{
                estConnection = formatter.pingURL(url);
            }
            catch (IOException e){
                monitor.error("Unable to connect to the URL");
            }
            if(estConnection){
                urlView.getItems().add(url);
                monitor.addUrl(url);
                urlMap.put(url, new ArrayList<>());
                urlField.clear();
            }
            else{
                monitor.error("Unable to connect to the URL");
            }
        }
        else {
            monitor.error("Invalid URL");
        }
    }

    @FXML
    private void updateKeyView(){
        if (urlView.getSelectionModel().isEmpty()){
            keyView.getItems().setAll(new ArrayList<>());
        }
        else{
            String selectedUrl = urlView.getSelectionModel().getSelectedItem();
            keyView.getItems().setAll(urlMap.get(selectedUrl));
        }
    }

    @FXML
    private void keySubmit() {
        if(urlView.getItems().isEmpty()){
            monitor.error("Please add a URL first");
        }
        else if(urlView.getSelectionModel().getSelectedItems().isEmpty()){
            monitor.error("Please select a URL");
        }
        else{
            String key = keyField.getText();
            if (key != null) {
                String selectedUrl = urlView.getSelectionModel().getSelectedItem();
                if(!urlMap.get(selectedUrl).contains(key.toLowerCase())){
                    urlMap.get(selectedUrl).add(key.toLowerCase());
                    monitor.addKeyword(selectedUrl);
                    updateKeyView();
                    keyField.clear();
                }
            }
        }
    }

    @FXML
    private void quit(){System.exit(0);}

    @FXML
    public MenuItem getRefreshMenuItem(){ return refreshMenu; }

    public void addMatch(String url, String keyword){
        SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss");
        Date now = new Date();
        String logEntry = url + ": " + keyword + " at " + sdt.format(now);
        matchLogView.getItems().add(logEntry);
        matchLog.add(logEntry);
    }

    public void setMonitor(ThreadMonitor monitor){
        this.monitor = monitor;
    }

    private void addChangeListener(){
        urlView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                updateKeyView();
            }
        });
    }

    public void loadUrlMap(HashMap<String, ArrayList<String>> urlMap){
        this.urlMap = urlMap;
        urlView.getItems().setAll(urlMap.keySet());
        updateKeyView();
    }

    @FXML
    private void deleteKeyword(){
        String selectedUrl = urlView.getSelectionModel().getSelectedItem();
        String selectedKey = keyView.getSelectionModel().getSelectedItem();
        urlMap.get(selectedUrl).remove(selectedKey);
        monitor.deleteKeyword(selectedUrl);
        updateKeyView();
    }

    @FXML
    private void deleteUrl(){
        String selectedUrl = urlView.getSelectionModel().getSelectedItem();
        urlMap.remove(selectedUrl);
        urlView.getItems().setAll(urlMap.keySet());
        monitor.deleteUrl(selectedUrl);
    }

    @FXML
    private void deleteLogEntry(){
        String selectedEntry = matchLogView.getSelectionModel().getSelectedItem();
        matchLog.remove(selectedEntry);
        matchLogView.getItems().setAll(matchLog);
    }

    @FXML
    private void clearLog(){
        matchLog.clear();
        matchLogView.getItems().clear();
    }
}
