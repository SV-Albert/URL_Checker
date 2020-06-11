import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GUIController {

    @FXML private TextField urlField;
    @FXML private TextField keyField;
    @FXML private ListView<String> urlView;
    @FXML private ListView<String> keyView;
    @FXML private ListView<String> logView;
    @FXML private MenuItem refreshMenu;
    @FXML private Label versionLabel;
    private ThreadMonitor threadMonitor;
    private DataManager dataManager;
    private URLFormatter formatter;

    @FXML
    public void initialize(){
        addChangeListener();
        setUrlOpenEvent();
        formatter = new URLFormatter();
    }

    public void setThreadMonitor(ThreadMonitor threadMonitor){
        this.threadMonitor = threadMonitor;
    }

    public void setDataManager(DataManager dataManager){
        this.dataManager = dataManager;
    }

    private void addChangeListener(){
        urlView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                updateKeyView();
            }
        });
    }

    private void setUrlOpenEvent(){
        logView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount() >= 2){
                    String selectedEntry = logView.getSelectionModel().getSelectedItem();
                    int endIndex = selectedEntry.indexOf(" on");
                    String url = selectedEntry.substring(0, endIndex);
                    dataManager.openInBrowser(url);
                }
            }
        });
        urlView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount() >= 2){
                    String selectedUrl = urlView.getSelectionModel().getSelectedItem();
                    dataManager.openInBrowser(selectedUrl);
                }
            }
        });
    }

    public void repopulate(){
        urlView.getItems().setAll(dataManager.getUrlKeyMap().keySet());
        logView.getItems().setAll(dataManager.getLogs());
        updateKeyView();
    }

    public void setVersion(String version){
        versionLabel.setText(version);
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
                dataManager.error("Unable to connect to the URL");
            }
            if(estConnection){
                urlView.getItems().add(url);
                dataManager.addUrl(url);
                threadMonitor.createThread(url);
                urlField.clear();
            }
            else{
                dataManager.error("Unable to connect to the URL");
            }
        }
        else {
            dataManager.error("Invalid URL");
        }
    }

    @FXML
    private void updateKeyView(){
        if (urlView.getSelectionModel().isEmpty()){
            keyView.getItems().setAll(new ArrayList<>());
        }
        else{
            String selectedUrl = urlView.getSelectionModel().getSelectedItem();
            keyView.getItems().setAll(dataManager.getKeywords(selectedUrl));
        }
    }

    @FXML
    private void keySubmit() {
        if(urlView.getItems().isEmpty()){
            dataManager.error("Please add a URL first");
        }
        else if(urlView.getSelectionModel().getSelectedItems().isEmpty()){
            dataManager.error("Please select a URL");
        }
        else{
            String key = keyField.getText();
            if (key != null) {
                String selectedUrl = urlView.getSelectionModel().getSelectedItem();
                if(!dataManager.getKeywords(selectedUrl).contains(key.toLowerCase())){
                    dataManager.addKeyword(selectedUrl, key.toLowerCase());
                    threadMonitor.addKeyword(selectedUrl);
                    updateKeyView();
                    keyField.clear();
                }
            }
        }
    }

    @FXML
    private void quit(){
        System.exit(0);
    }

    @FXML
    public MenuItem getRefreshMenuItem(){ return refreshMenu; }

    public void addLogEntry(String url, String keyword){
        SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss/dd.MM");
        Date now = new Date();
        String logEntry = url + " on \"" + keyword + "\" at " + sdt.format(now);
        logView.getItems().add(logEntry);
        dataManager.addLogEntry(logEntry);
    }

    @FXML
    private void deleteKeyword(){
        String selectedUrl = urlView.getSelectionModel().getSelectedItem();
        String selectedKey = keyView.getSelectionModel().getSelectedItem();
        dataManager.deleteKeyword(selectedUrl, selectedKey);
        threadMonitor.deleteKeyword(selectedUrl);
        updateKeyView();
    }

    @FXML
    private void deleteUrl(){
        String selectedUrl = urlView.getSelectionModel().getSelectedItem();
        dataManager.deleteUrl(selectedUrl);
        urlView.getItems().setAll(dataManager.getUrlKeyMap().keySet());
        threadMonitor.deleteThread(selectedUrl);
    }

    @FXML
    private void deleteLogEntry(){
        String selectedEntry = logView.getSelectionModel().getSelectedItem();
        dataManager.removeLogEntry(selectedEntry);
        logView.getItems().setAll(dataManager.getLogs());
    }

    @FXML
    private void clearLog(){
        dataManager.clearLogs();
        logView.getItems().clear();
    }

}
