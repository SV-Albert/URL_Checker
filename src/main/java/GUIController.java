import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * FXML controller class for the application's GUI
 *
 * @version 0.2
 * @author Albert Shakirzianov
 */

public class GUIController {

    @FXML private TextField urlField;
    @FXML private TextField keyField;
    @FXML private ListView<String> urlView;
    @FXML private ListView<String> keyView;
    @FXML private ListView<String> logView;
    @FXML private MenuItem quitMenu;
    @FXML private MenuItem refreshMenu;
    @FXML private MenuItem aboutMenu;
    @FXML private MenuItem openMenu;
    @FXML private MenuItem saveMenu;
    @FXML private MenuItem saveAsMenu;
    @FXML private Label versionLabel;
    private ThreadMonitor threadMonitor;
    private DataManager dataManager;
    private URLFormatter formatter;

    /**
     * Initialization method for the controller
     */
    @FXML
    public void initialize(){
        addChangeListener();
        setUrlOpenEvent();
        formatter = new URLFormatter();
    }

    /**
     * Set the ThreadMonitor object field
     *
     * @param threadMonitor
     */
    public void setThreadMonitor(ThreadMonitor threadMonitor){
        this.threadMonitor = threadMonitor;
    }

    /**
     * Set the DataManager object field
     *
     * @param dataManager
     */
    public void setDataManager(DataManager dataManager){
        this.dataManager = dataManager;
    }

    /**
     * Add a change listener to the urlView in order to update the keyView
     * when a new URL is selected
     */
    private void addChangeListener(){
        urlView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                updateKeyView();
            }
        });
    }

    /**
     * Add a double click event to the logView and urlView that opens a corresponding
     * URl in a default browser
     */
    private void setUrlOpenEvent(){
        logView.setOnMouseClicked(event -> {
            if(event.getClickCount() >= 2){
                String selectedEntry = logView.getSelectionModel().getSelectedItem();
                int endIndex = selectedEntry.indexOf(" on");
                String url = selectedEntry.substring(0, endIndex);
                dataManager.openInBrowser(url);
            }
        });
        urlView.setOnMouseClicked(event -> {
            if(event.getClickCount() >= 2){
                String selectedUrl = urlView.getSelectionModel().getSelectedItem();
                dataManager.openInBrowser(selectedUrl);
            }
        });
    }

    /**
     * Repopulate the views with data from the DataManager
     */
    public void repopulate(){
        urlView.getItems().setAll(dataManager.getUrlKeyMap().keySet());
        logView.getItems().setAll(dataManager.getLogs());
        updateKeyView();
    }

    /**
     * Set text for the versionLabel
     *
     * @param version text to set
     */
    public void setVersion(String version){
        versionLabel.setText(version);
    }

    /**
     * Method invoked by the URL Submit button that checks the validity of the input
     * and adds the formatted URl to the urlView
     */
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

    /**
     * Update the data displayed in the keyView
     */
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

    /**
     * Method invoked my the keyword Submit button
     */
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
                    threadMonitor.requestHashing(selectedUrl);
                    updateKeyView();
                    keyField.clear();
                }
            }
        }
    }

    /**
     * @return saveMenu MenuItem
     */
    public MenuItem getSaveMenuItem(){ return saveMenu; }

    /**
     * @return saveAsMenu MenuItem
     */
    public MenuItem getSaveAsMenuItem(){ return saveAsMenu; }

    /**
     * @return openMenu MenuItem
     */
    public MenuItem getOpenMenuItem(){ return openMenu; }

    /**
     * @return quitMenu MenuItem
     */
    public MenuItem getQuitMenuItem(){ return quitMenu; }

    /**
     * @return refreshMenu MenuItem
     */
    @FXML
    public MenuItem getRefreshMenuItem(){ return refreshMenu; }

    /**
     * @return aboutMenu MenuItem
     */
    @FXML
    public MenuItem getAboutMenuItem(){ return aboutMenu; }

    /**
     * Create a String log entry when a new match was found
     * and add it to the logView
     *
     * @param url where the match was found
     * @param keyword on which the match was found
     */
    public void addLogEntry(String url, String keyword){
        SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss/dd.MM");
        Date now = new Date();
        String logEntry = url + " on \"" + keyword + "\" at " + sdt.format(now);
        logView.getItems().add(logEntry);
        dataManager.addLogEntry(logEntry);
    }

    /**
     * Method invoked by the Delete Item contextual menu that removes a selected
     * keyword
     */
    @FXML
    private void deleteKeyword(){
        String selectedUrl = urlView.getSelectionModel().getSelectedItem();
        String selectedKey = keyView.getSelectionModel().getSelectedItem();
        dataManager.deleteKeyword(selectedUrl, selectedKey);
        threadMonitor.requestHashing(selectedUrl);
        updateKeyView();
    }

    /**
     * Method invoked by the Delete Item contextual menu that removes a selected
     * URL
     */
    @FXML
    private void deleteUrl(){
        String selectedUrl = urlView.getSelectionModel().getSelectedItem();
        dataManager.deleteUrl(selectedUrl);
        urlView.getItems().setAll(dataManager.getUrlKeyMap().keySet());
        threadMonitor.stopThread(selectedUrl);
    }

    /**
     * Method invoked by the Delete Item contextual menu that removes a selected
     * log entry
     */
    @FXML
    private void deleteLogEntry(){
        String selectedEntry = logView.getSelectionModel().getSelectedItem();
        dataManager.removeLogEntry(selectedEntry);
        logView.getItems().setAll(dataManager.getLogs());
    }

    /**
     * Method invoked by the Clear All contextual menu that removes all log
     * entries
     */
    @FXML
    private void clearLog(){
        dataManager.clearLogs();
        logView.getItems().clear();
    }

}
