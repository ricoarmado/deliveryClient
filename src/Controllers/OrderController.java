package Controllers;

/**
 * Created by Stas on 28.09.2016.
 */
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import oilDelivery.MongoDB;
import oilDelivery.dbMode;

public class OrderController implements Initializable{
    private String objectID;
    private String ordererName;
    private String volume;
    private String date;
    private ArrayList<String>oils;
    private String activeOil;
    private Stage st;
    private dbMode mode;
    private String factoryObjectId;
    private ArrayList<String>factories;
    
    public OrderController(dbMode mode,String objectID, String ordererName, String volume, String date, 
            ArrayList<String> oils, String activeOil, String factoryObjectId, ArrayList<String>factories, Stage stage) {
        this.objectID = objectID;
        this.ordererName = ordererName;
        this.volume = volume;
        this.date = date;
        this.oils = oils;
        this.activeOil = activeOil;
        this.st = stage;
        this.mode = mode;
        this.factoryObjectId = factoryObjectId;
        this.factories = factories;
    }
    

    @FXML
    private TextField volumeField;

    @FXML
    private ComboBox<String> factoryComboBox;
       
    @FXML
    private TextField objectIDField;

    @FXML
    private TextField customerField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ChoiceBox<String> choiseBox;

    public LocalDate LOCAL_DATE (String dateString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(dateString, formatter);
        return localDate;
    }
    
    @FXML
    void nextButtonClick(ActionEvent event) {
        MongoDB.getInstance().Order(mode, this.objectIDField.getText(), this.customerField.getText(),
                this.volumeField.getText(),datePicker.getValue().toString(),choiseBox.getValue(), this.factoryComboBox.getValue());
    }

    @FXML
    void cancelButtonClick(ActionEvent event) {
        this.st.hide();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.choiseBox.getItems().addAll(this.oils);
        this.factoryComboBox.getItems().addAll(this.factories);
        if(mode == dbMode.Edit){
            this.objectIDField.setText(this.objectID);
            this.customerField.setText(this.ordererName);
            this.volumeField.setText(this.volume);
            this.choiseBox.setValue(this.activeOil);
            this.datePicker.setValue(LOCAL_DATE(date));
            this.factoryComboBox.setValue(this.factoryObjectId);
        }   
    }
}

