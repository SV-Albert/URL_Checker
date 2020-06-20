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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;


/**
 * Main class that starts the application and initiates the GUI Controller,
 * DataManager and ThreadMonitor objects
 *
 * @version 0.3
 * @author Albert Shakirzianov
 */
public class Main extends Application {

    private Stage stage;
    private GUIController controller;
    private DataManager dataManager;
    private ThreadMonitor threadMonitor;
    private final String version = "URL Spy v0.3";
    private TrayIcon trayIcon;
    private FileChooser fileChooser;
    private Preferences preferences;

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
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"));
        preferences = Preferences.userNodeForPackage(Main.class);

        setupController();

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

    /**
     * Show the JavaFX stage
     */
    private void showStage(){
        if(stage != null){
            stage.show();
            stage.toFront();
        }
    }

    /**
     * Give the controller referenced to ThreadMonitor and DataManager
     * objects, as well as set action handlers for the menu items
     */
    private void setupController(){
        controller.setThreadMonitor(threadMonitor);
        controller.setDataManager(dataManager);
        controller.setVersion(version);
        controller.getQuitMenuItem().setOnAction(e -> quit());
        controller.getRefreshMenuItem().setOnAction(e -> threadMonitor.refresh());
        controller.getAboutMenuItem().setOnAction(e -> displayAboutWindow());
        controller.getSaveMenuItem().setOnAction(e -> handleSave());
        controller.getSaveAsMenuItem().setOnAction(e -> handleSaveAs());
        controller.getOpenMenuItem().setOnAction(e -> handleOpen());
        controller.getBugMenuItem().setOnAction(e -> dataManager.openInBrowser("https://github.com/SV-Albert/URL_Spy/issues/new"));
    }

    /**
     * Create a success notification when a match was found
     *
     * @param url of the website were match occurred
     * @param keyword on which the match occurred
     */
    public void matchNotification(String url, String keyword){
        controller.addLogEntry(url, keyword);
        if(SystemTray.isSupported()) {
            SwingUtilities.invokeLater(() ->
                    trayIcon.displayMessage(
                            "Match found",
                            url + ": " + keyword,
                            TrayIcon.MessageType.INFO
                    )
            );
        }
        else{
            Notifications.create()
                .onAction(e -> dataManager.openInBrowser(url))
                .position(Pos.BOTTOM_RIGHT)
                .title("Match found")
                .text(url + ": " + keyword)
                .hideAfter(Duration.seconds(10))
                .showInformation();
        }
    }

    /**
     * Create an error notification
     *
     * @param message to display
     */
    public void errorNotification(String message){
        if(SystemTray.isSupported()){
            SwingUtilities.invokeLater(() ->
                    trayIcon.displayMessage(
                            "Something went wrong",
                            message,
                            TrayIcon.MessageType.ERROR
                    )
            );
        }
        else{
            Notifications.create()
                    .position(Pos.BOTTOM_RIGHT)
                    .title("Something went wrong")
                    .text(message)
                    .hideAfter(Duration.seconds(10))
                    .showWarning();
        }
    }

    /**
     * Stop the application
     */
    private void quit(){
        dataManager.save();
        SystemTray.getSystemTray().remove(trayIcon);
        Platform.exit();
        System.exit(0);
    }

    /**
     * Create and display the About URL Spy window
     */
    private void displayAboutWindow(){
        VBox root = new VBox();
        root.setPrefWidth(400);
        root.setPrefHeight(Region.USE_COMPUTED_SIZE);
        Scene aboutPage = new Scene(root);
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
        ver.getStyleClass().add("about-text");
        copyright.getStyleClass().add("about-text");
        VBox text = new VBox();
        text.setAlignment(Pos.CENTER_LEFT);
        text.getChildren().addAll(ver, copyright);

        BorderPane info = new BorderPane();
        info.setLeft(text);
        info.setRight(github);

        root.getChildren().addAll(logo, info);
        root.getStyleClass().add("about-window");

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

    /**
     * Check if the path to the last save file is set in preferences,
     * save to the file if yes, prompt the user to choose a path to a new
     * save if no
     */
    private void handleSave(){
        if(preferences.get("pathToLastSave", null) == null){
            handleSaveAs();
        }
        else{
            dataManager.save();
        }
    }

    /**
     * Prompt the user to choose a path to a new save file, update the
     * preferences with the new location and save the data to it
     */
    private void handleSaveAs(){
        File saveFile = fileChooser.showSaveDialog(stage);
        if(saveFile != null){
            dataManager.getSaveManager().setPathToSave(saveFile.toPath());
            dataManager.save();
            preferences.put("pathToLastSave", saveFile.getPath());
        }
    }

    /**
     * Prompt the user to choose a path to an existing save file and
     * load the data from it
     */
    private void handleOpen(){
        File saveFile = fileChooser.showOpenDialog(stage);
        if(saveFile != null && saveFile.getPath().endsWith(".txt")){
            dataManager.getSaveManager().setPathToSave(saveFile.toPath());
            dataManager.load();
            preferences.put("pathToLastSave", saveFile.getPath());
            controller.repopulate();
        }
    }

}
