package Controllers;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import oilDelivery.MongoDB;
import oilDelivery.dbMode;

/**
 * Created by Stas on 28.09.2016.
 */

public class FactoryOilController implements Initializable{
    private String objectId;
    private String volume;
    private String priceValue;
    private String factoryObjectId;
    private List<String>oilList;
    private String activeItem;
    private dbMode mode;
    private Stage st;
    private List<String>factoryList;
    public FactoryOilController(String objectId, String volume, String priceValue, String factoryObjectId,
            ArrayList<String> oilList, String activeItem, dbMode mode, ArrayList<String> factoryList, Stage st) {
        this.objectId = objectId;
        this.volume = volume;
        this.priceValue = priceValue;
        this.factoryObjectId = factoryObjectId;
        this.oilList = oilList;
        this.activeItem = activeItem;
        this.mode = mode;
        this.st = st;
        this.factoryList = factoryList;
    }

    @FXML
    private TextField objectIDField;

    @FXML
    private TextField Volume;

    @FXML
    private TextField price;

    @FXML
    private ChoiceBox<String> objectIdFactory;

    @FXML
    private ChoiceBox<String> choiseBox;

    @FXML
    void nextButtonClick(ActionEvent event) {
        MongoDB.getInstance().FactoryOrder(mode, objectId, choiseBox.getValue(), price.getText(), objectIdFactory.getValue(), Volume.getText());
        st.hide();
    }

    @FXML
    void cancelButtonClick(ActionEvent event) {
        st.hide();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        choiseBox.getItems().addAll(oilList);
        this.objectIdFactory.getItems().addAll(this.factoryList);
        if(mode == dbMode.Edit){
            choiseBox.setValue(this.activeItem);
            objectIdFactory.setValue(this.factoryObjectId);
            this.objectIDField.setText(this.objectId);
            this.Volume.setText(volume);
            this.price.setText(priceValue);
        }
    }

}
