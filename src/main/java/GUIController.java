import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
    private Monitor monitor;
    private final URLFormatter formatter = new URLFormatter();
    private HashMap<String, ArrayList<String>> urlMap;
    private ArrayList<String> matchLogs = new ArrayList<>();


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
                monitor.save();
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
                    monitor.save();
                }
            }
        }
    }

    @FXML
    private void quit(){
        monitor.save();
        System.exit(0);
    }

    @FXML
    public MenuItem getRefreshMenuItem(){ return refreshMenu; }

    public void addLogEntry(String url, String keyword){
        SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss");
        Date now = new Date();
        String logEntry = url + ": " + keyword + " at " + sdt.format(now);
        matchLogView.getItems().add(logEntry);
        matchLogs.add(logEntry);
        monitor.save();
    }

    public void setMonitor(Monitor monitor){
        this.monitor = monitor;
        monitor.setLogs(matchLogs);
    }

    private void addChangeListener(){
        urlView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                updateKeyView();
            }
        });
    }

    public void repopulate(HashMap<String, ArrayList<String>> urlMap, ArrayList<String> matchLogs){
        this.urlMap = urlMap;
        urlView.getItems().setAll(urlMap.keySet());
        matchLogView.getItems().setAll(matchLogs);
        updateKeyView();
    }

    @FXML
    private void deleteKeyword(){
        String selectedUrl = urlView.getSelectionModel().getSelectedItem();
        String selectedKey = keyView.getSelectionModel().getSelectedItem();
        urlMap.get(selectedUrl).remove(selectedKey);
        monitor.deleteKeyword(selectedUrl);
        updateKeyView();
        monitor.save();
    }

    @FXML
    private void deleteUrl(){
        String selectedUrl = urlView.getSelectionModel().getSelectedItem();
        urlMap.remove(selectedUrl);
        urlView.getItems().setAll(urlMap.keySet());
        monitor.deleteUrl(selectedUrl);
        monitor.save();
    }

    @FXML
    private void deleteLogEntry(){
        String selectedEntry = matchLogView.getSelectionModel().getSelectedItem();
        matchLogs.remove(selectedEntry);
        matchLogView.getItems().setAll(matchLogs);
        monitor.save();
    }

    @FXML
    private void clearLog(){
        matchLogs.clear();
        matchLogView.getItems().clear();
        monitor.save();
    }
}
