import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import java.io.IOException;

public class GUIController {

    @FXML private TextField urlField;
    @FXML private TextField keyField;
    @FXML private ListView<String> urlView;
    @FXML private ListView<String> keyView;
    @FXML private MenuItem refreshMenu;
    private ThreadMonitor monitor;
    private final URLFormatter formatter = new URLFormatter();

    @FXML
    private void urlSubmit(){
        String url = formatter.formatURL(urlField.getText());
        if(formatter.validateURL(url)){
            boolean estConnection = false;
            try{
                estConnection = formatter.pingURL(url);
            }
            catch (IOException e){
                monitor.error("Unable to connect to the URL E");
            }
            if(estConnection){
                urlView.getItems().add(url);
                monitor.addUrl(url);
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
    private void keySubmit() {
        String key = keyField.getText();
        if (key != null) {
            keyView.getItems().add(key);
            monitor.addKeyword(key);
        }
    }

    @FXML
    private void quit(){System.exit(0);}

    @FXML
    public MenuItem getRefreshMenuItem(){ return refreshMenu; }

    public void setMonitor(ThreadMonitor monitor){
        this.monitor = monitor;
    }

}
