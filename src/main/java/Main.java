import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.nio.file.Paths;

public class Main extends Application {

    private Stage stage;
    private GUIController controller;
    private DataManager dataManager;
    private String version = "URL Spy v0.1";


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        FXMLLoader controllerLoader =  new FXMLLoader(getClass().getResource("GUI_Layout.fxml"));

        Parent root = new VBox();
        try {
            root = controllerLoader.load();
        } catch (IOException e) {
            failNotification("Application start failed");
        }

        dataManager = new DataManager(this);
        dataManager.load();
        controller = controllerLoader.getController();
        ThreadMonitor threadMonitor = new ThreadMonitor(dataManager);
        controller.setThreadMonitor(threadMonitor);
        controller.setDataManager(dataManager);
        controller.getRefreshMenuItem().setOnAction(e -> threadMonitor.refresh());
        controller.setVersion(version);


        Scene scene = new Scene(root, 900, 450);
        stage.setTitle(version);
        stage.setScene(scene);
        stage.show();

        stage.getIcons().add(new Image(getClass().getResourceAsStream("URL_Spy_Logo.png")));
        Thread monitor = new Thread(threadMonitor);
        monitor.start();
        controller.repopulate();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public void successNotification(String url, String keyword){
        controller.addLogEntry(url, keyword);
        Notifications.create()
                .darkStyle()
                .position(Pos.BOTTOM_RIGHT)
                .title("Match found!")
                .text("Match found at " + url + " on the \"" + keyword + "\" keyword ")
                .onAction(e -> dataManager.openInBrowser(url))
                .hideAfter(Duration.seconds(15))
                .show();
    }

    public void failNotification(String message){
        Notifications.create()
                .darkStyle()
                .position(Pos.BOTTOM_RIGHT)
                .title("Something went wrong")
                .text(message)
                .hideAfter(Duration.seconds(10))
                .show();
    }

}
