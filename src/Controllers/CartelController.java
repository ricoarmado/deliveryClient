package Controllers;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import oilDelivery.MongoDB;
import oilDelivery.dbMode;

import java.net.URL;
import java.util.*;

public class CartelController  implements Initializable{
    private String objectid;

    @FXML
    private TableView<ObservableList<String>> cartelTableView;

    public CartelController(String objectid) {
        this.objectid = objectid;
    }

    @FXML
    void AddField(ActionEvent event) {
        List<String>choices = new ArrayList<String>();
        DBCursor cursor = MongoDB.getInstance().getAllCollectionByName("Страны-поставщики нефти");
        while (cursor.hasNext()){
            BasicDBObject obj = (BasicDBObject)cursor.next();
            String tmp = obj.get("value").toString();
            choices.add(tmp);
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Страны-поставщики");
        dialog.setHeaderText("Выберите страну-поставщика для данного завода");
        dialog.setContentText("Ваш выбор:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(country -> {
            MongoDB.getInstance().Cartel(dbMode.Create,objectid,country);
        });
    }

    @FXML
    void DeleteField(ActionEvent event) {
        ObservableList row = cartelTableView.getSelectionModel().getSelectedItems();
        ObservableList tmp = (ObservableList)row.get(0);
        String objectid = (String) tmp.get(1);
        MongoDB.getInstance().Cartel(dbMode.Delete,objectid,null);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Iterator<DBObject> objects = MongoDB.getInstance().getCartelByFactory(objectid);
        cartelTableView.getColumns().clear();
        List<String> columnNames = new ArrayList<String>();
        columnNames.add("Завод");
        columnNames.add("Страна");
        for (int i = 0; i < columnNames.size(); i++) {
            final int finalIdx = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<ObservableList<String>, String>(columnNames.get(i));
            column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(finalIdx)));
            cartelTableView.getColumns().add(column);
        }
        
        ObservableList<List<String>> data = FXCollections.observableArrayList();
        cartelTableView.getItems().clear();
        while(objects.hasNext()){
            DBObject dbObject = objects.next();
            BasicDBObject bdobj = (BasicDBObject) dbObject;
                Set<String>_colls = dbObject.keySet();
                List<String> words = new ArrayList();
                for(String coll : _colls) {
                    String obj = dbObject.get(coll).toString();
                    words.add(obj.split("\"")[3]);
                }
            cartelTableView.getItems().add(FXCollections.observableArrayList(words));
            }
    }
}
