package com.jat;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * JavaFX App
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainscene.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        // Add the CSS file to the scene
        scene.getStylesheets().add(getClass().getResource("/main.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}