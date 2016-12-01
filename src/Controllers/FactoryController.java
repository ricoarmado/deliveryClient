package Controllers;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import oilDelivery.MongoDB;
import oilDelivery.dbMode;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import oilDelivery.App;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class FactoryController implements Initializable{
    private final Stage stage;
    private String name;
    private String city;
    private String objectID;
    private List<String>properties;
    private String Year;
    private dbMode dbmode;
    private String orderObjID;
    private String activeProp;
    
    public FactoryController(String name, String city, String objectID,
                             List<String> properties,String year, dbMode dbmode, Stage stage, String orderObjID, String activeProp) {
        this.name = name;
        this.city = city;
        this.objectID = objectID;
        this.properties = properties;
        this.Year = year;
        this.dbmode = dbmode;
        this.stage = stage;
        this.activeProp = activeProp;
    }

    @FXML
    private ChoiceBox<String> propertyType;
    
    @FXML
    private TableView<ObservableList<String>> orderTableView;

    @FXML
    private TableView<ObservableList<String>> oilTableView;
    
    @FXML
    private TextField year;

    @FXML
    private TextField objectIDField;


    @FXML
    private TextField City;

    @FXML
    private TextField Name;

    @FXML
    void nextButtonClick(ActionEvent event) {
        MongoDB.getInstance().Factory(dbmode,objectIDField.getText(),Name.getText(),City.getText(),
                propertyType.getValue(),
                year.getText());
        stage.hide();
    }

    @FXML
    void cancelButtonClick(ActionEvent event) {
        stage.hide();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        propertyType.getItems().addAll(properties);
        if(dbmode == dbMode.Edit){
            try {
                Name.setText(this.name);
                objectIDField.setText(this.objectID);
                City.setText(this.city);
                year.setText(Year);
                propertyType.setValue(activeProp);
                fillTable(this.oilTableView,MongoDB.getInstance().getOilByFactoryId(this.objectID), "Виды выпускаемого топлива");
                fillTable(this.orderTableView,MongoDB.getInstance().getOrdersByFactoryId(this.objectID),"Сведения о заказах");
                
                this.orderTableView.setOnMousePressed((event)->{
                    if(event.isPrimaryButtonDown() && event.getClickCount() == 2){
                        ObservableList tmp = orderTableView.getSelectionModel().getSelectedItems();
                        BaseViewController.getInstance().editOrderField(dbMode.Edit, tmp);
                    }});
                this.oilTableView.setOnMousePressed((event)->{
                    if(event.isPrimaryButtonDown() && event.getClickCount() == 2){
                        ObservableList tmp = oilTableView.getSelectionModel().getSelectedItems();
                        BaseViewController.getInstance().editFactoryOrderClick(dbMode.Edit, tmp);
                    }});
            } catch (ParseException ex) {
                Logger.getLogger(FactoryController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(FactoryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    public void setWarningMessage(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Система");
        alert.setHeaderText("В результате работы возникла ошибка");
        alert.setContentText("Текст ошибки: " + message);
        alert.showAndWait();

    }


    public void showCountriesClick(ActionEvent actionEvent) throws IOException {
        if(objectIDField.getText().isEmpty())
            setWarningMessage("Для начала создайте завод");
        else{
                Stage st = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/cartel.fxml"));
                loader.setController(new CartelController(objectIDField.getText()));
                Parent root = (Parent)loader.load();
                st.setScene(new Scene(root));
                st.setResizable(false);
                st.show();

        }
    }
    private void fillTable(TableView mainTableView, Iterator<DBObject> objects, String name) throws ParseException, IOException{
        JSONObject header = App.getHeader(App.switchName(name));
        mainTableView.getColumns().clear();
        if(objects.hasNext()){
            Set<String>_colls = objects.next().keySet();
            List<String> columnNames = new ArrayList<String>();
            for(String coll : _colls)
                columnNames.add(header.get(coll).toString());
            for (int i = 0; i < columnNames.size(); i++) {
                final int finalIdx = i;
                TableColumn<ObservableList<String>, String> column = new TableColumn<ObservableList<String>, String>(columnNames.get(i));
                column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(finalIdx)));
                mainTableView.getColumns().add(column);
            }
        }
        ObservableList<List<String>> data = FXCollections.observableArrayList();
        mainTableView.getItems().clear();
        while(objects.hasNext()){
            DBObject dbObject = objects.next();
            BasicDBObject bdobj = (BasicDBObject) dbObject;
                Set<String>_colls = dbObject.keySet();
                List<String> words = new ArrayList();
                 for(String coll : _colls) {
                    Object obj = dbObject.get(coll);
                    words.add(obj.toString());
                }
            mainTableView.getItems().add(FXCollections.observableArrayList(words));
            }
            ((TableColumn<ObservableList<String>, ?>)mainTableView.getColumns().get(0)).setVisible(false);
    }
}
