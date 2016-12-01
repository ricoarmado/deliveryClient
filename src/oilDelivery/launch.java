package oilDelivery;/**
 * Created by stanislavtyrsa on 17.09.16.
 */

import Controllers.BaseViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class launch extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/mainframe.fxml"));
        loader.setController(BaseViewController.getInstance());
        Parent root = (Parent)loader.load();
        primaryStage.setScene(new Scene(root));
        BaseViewController.getInstance().setStage(primaryStage);
        primaryStage.setTitle("Учет нефтеперерабатывающих заводов");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

}
