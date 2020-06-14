import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;


/**
 * Main class that starts the application and initiates the GUI Controller,
 * DataManager and ThreadMonitor objects
 *
 * @version 0.2
 * @author Albert Shakirzianov
 */
public class Main extends Application {

    private Stage stage;
    private GUIController controller;
    private DataManager dataManager;
    private ThreadMonitor threadMonitor;
    private String version = "URL Spy v0.2";
    private TrayIcon trayIcon;


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
            errorNotification("Application start failed");
        }

        dataManager = new DataManager(this);
        dataManager.load();
        controller = controllerLoader.getController();
        threadMonitor = new ThreadMonitor(dataManager);
        controller.setThreadMonitor(threadMonitor);
        controller.setDataManager(dataManager);
        controller.getRefreshMenuItem().setOnAction(e -> threadMonitor.refresh());
        controller.getAboutMenuItem().setOnAction(e -> displayAboutWindow());
        controller.setVersion(version);


        Scene scene = new Scene(root, 900, 450);
        stage.setTitle(version);
        stage.setScene(scene);
        showStage();
        javax.swing.SwingUtilities.invokeLater(this::createTrayIcon);

        stage.getIcons().add(new Image(getClass().getResourceAsStream("URL_Spy_Logo.png")));
        Thread monitor = new Thread(threadMonitor);
        monitor.start();
        controller.repopulate();

        Platform.setImplicitExit(false);
    }

    private void showStage(){
        if(stage != null){
            stage.show();
            stage.toFront();
        }
    }

    /**
     * Create a success notification when a match was found
     *
     * @param url of the website were match occurred
     * @param keyword on which the match occurred
     */
    public void successNotification(String url, String keyword){
        controller.addLogEntry(url, keyword);
        javax.swing.SwingUtilities.invokeLater(() ->
                trayIcon.displayMessage(
                        "Match found",
                        url + ": " + keyword,
                        TrayIcon.MessageType.INFO
                )
        );
    }

    /**
     * Create an error notification
     *
     * @param message to display
     */
    public void errorNotification(String message){
        javax.swing.SwingUtilities.invokeLater(() ->
                trayIcon.displayMessage(
                        "Something went wrong",
                        message,
                        TrayIcon.MessageType.ERROR
                )
        );
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

    /**
     * Create the System tray icon
     */
    private void createTrayIcon(){
        Toolkit.getDefaultToolkit();
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                java.awt.Image iconImage = ImageIO.read(getClass().getResourceAsStream("URL_Spy_Logo.png"));
                trayIcon = new TrayIcon(iconImage);
                trayIcon.setImageAutoSize(true);
                trayIcon.addActionListener(e -> Platform.runLater(this::showStage));
                MenuItem refreshMenu = new MenuItem("Refresh");
                refreshMenu.addActionListener(e -> threadMonitor.refresh());
                MenuItem quitMenu = new MenuItem("Quit");
                quitMenu.addActionListener(e -> {
                    tray.remove(trayIcon);
                    Platform.exit();
                    System.exit(0);
                });
                PopupMenu popupMenu = new PopupMenu();
                popupMenu.add(refreshMenu);
                popupMenu.add(quitMenu);
                trayIcon.setPopupMenu(popupMenu);
                trayIcon.setToolTip(version);
                tray.add(trayIcon);
            } catch (AWTException | IOException e) {
                errorNotification("Failed to initialize the system tray");
            }
        }
    }


}
