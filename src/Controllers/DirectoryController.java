package Controllers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import oilDelivery.MongoDB;
import oilDelivery.dbMode;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Stas on 28.09.2016.
 */


public class DirectoryController implements Initializable{

    private Stage st;
    private ObservableList list;
    private dbMode mode;
    private String dbname;
    @FXML
    private TextField objectIDField;

    @FXML
    private TextField objectIDField1;

    public DirectoryController(ObservableList list, dbMode mode, String dbname, Stage st) {
        this.st = st;
        this.list = list;
        this.mode = mode;
        this.dbname = dbname;
    }

    @FXML
    void nextButtonClick(ActionEvent event) {
        MongoDB.getInstance().Directory(dbname,objectIDField1.getText(),mode,objectIDField.getText());
        st.hide();
    }

    @FXML
    void cancelButtonClick(ActionEvent event) {
        st.hide();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(mode == dbMode.Edit){
            ObservableList obj = (ObservableList) list.get(0);
            objectIDField.setText(obj.get(0).toString());
            objectIDField1.setText(obj.get(1).toString());
        }
    }
}
