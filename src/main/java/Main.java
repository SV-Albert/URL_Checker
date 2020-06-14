import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import java.io.IOException;


/**
 * Main class that starts the application and initiates the GUI Controller,
 * DataManager and ThreadMonitor objects
 *
 * @version 0.1
 * @author Albert Shakirzianov
 */
public class Main extends Application {

    private Stage stage;
    private GUIController controller;
    private DataManager dataManager;
    private String version = "URL Spy v0.1";


    public static void main(String[] args) {
        launch(args);
    }

    /**
     * The application start method
     *
     * @param stage
     */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        FXMLLoader controllerLoader = new FXMLLoader(getClass().getResource("GUI_Layout.fxml"));

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
        controller.getAboutMenuItem().setOnAction(e -> displayAboutWindow());
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

    /**
     * Create a success notification when a match was found
     *
     * @param url of the website were match occurred
     * @param keyword on which the match occurred
     */
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

    /**
     * Create an error notification
     *
     * @param message to display
     */
    public void failNotification(String message){
        Notifications.create()
                .darkStyle()
                .position(Pos.BOTTOM_RIGHT)
                .title("Something went wrong")
                .text(message)
                .hideAfter(Duration.seconds(10))
                .show();
    }

    /**
     * Create and display the About URL Spy window
     */
    private void displayAboutWindow(){
        VBox root = new VBox();
        Scene aboutPage = new Scene(root, 400, 180);
        aboutPage.getStylesheets().add("guiStyle.css");

        ImageView logo = new ImageView(new Image("URL_Spy_Logo.png"));
        logo.setFitHeight(150);
        logo.setFitWidth(150);
        ImageView github = new ImageView(new Image("GitHub-Mark-32px.png"));
        github.setOnMouseClicked(e -> dataManager.openInBrowser("https://github.com/SV-Albert/URL_Spy"));
        github.setOnMouseEntered(e -> aboutPage.setCursor(Cursor.HAND));
        github.setOnMouseExited(e -> aboutPage.setCursor(Cursor.DEFAULT));

        Text ver = new Text(version);
        Text copyright = new Text("\u00a9"+" 2020 Albert Shakirzianov");
        VBox text = new VBox();
        text.setAlignment(Pos.CENTER_LEFT);
        text.getChildren().addAll(ver, copyright);

        BorderPane info = new BorderPane();
        info.setLeft(text);
        info.setRight(github);

        root.getChildren().addAll(logo, info);
        root.getStyleClass().add("about-text");

        Stage aboutStage = new Stage();
        aboutStage.setScene(aboutPage);
        aboutStage.setResizable(false);
        aboutStage.setTitle("About URL Spy");
        aboutStage.getIcons().add(new Image(getClass().getResourceAsStream("URL_Spy_Logo.png")));
        aboutStage.show();
    }

}
