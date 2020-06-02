import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Root extends Application {

    private Stage stage;
    private GUIController controller;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        FXMLLoader controllerLoader =  new FXMLLoader(getClass().getResource("GUI_Layout.fxml"));

        Parent root = new BorderPane();
        try {
            root = controllerLoader.load();
        } catch (IOException e) {
            failNotification("Application start failed");
        }

        controller = controllerLoader.getController();
        Monitor monitorObject = new Monitor(this);
        controller.setMonitor(monitorObject);
        controller.getRefreshMenuItem().setOnAction(e -> monitorObject.refresh());


        Scene scene = new Scene(root, 900, 450);
        stage.setTitle("URL Checker");
        stage.setScene(scene);
        stage.show();

        Thread monitor = new Thread(monitorObject);
        monitor.start();
    }

    public void successNotification(String url, String keyword){
        controller.addLogEntry(url, keyword);
        Notifications.create()
                .darkStyle()
                .position(Pos.BOTTOM_RIGHT)
                .title("Match found!")
                .text("Match found at " + url + " on word " + keyword)
                .onAction(e -> getHostServices().showDocument(url))
                .hideAfter(Duration.seconds(5))
                .show();
    }

    public void failNotification(String message){
        Notifications.create()
                .darkStyle()
                .position(Pos.BOTTOM_RIGHT)
                .title("Something went wrong")
                .text(message)
                .hideAfter(Duration.INDEFINITE)
                .show();
    }

    public void repopulateViews(HashMap<String, ArrayList<String>> urlMap, ArrayList<String> logs){
        controller.repopulate(urlMap, logs);
    }

}
