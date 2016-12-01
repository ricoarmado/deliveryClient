/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import static javafx.application.ConditionalFeature.FXML;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import oilDelivery.MongoDB;

/**
 *
 * @author Stas
 */
public class AdminViewController implements Initializable {
    
    @FXML
    private TableView<ObservableList<String>> cartelTableView;
    
    @FXML
    private ListView<?> listView;
        
    public void printErrorMessage(String text){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Система");
        alert.setHeaderText("В результате работы системы возникла ошибка.");
        alert.setContentText("Текст ошибки: " + text);
        alert.showAndWait();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList names = FXCollections.observableArrayList(
                "Заводы", "Виды выпускаемого топлива","Сведения о заказах","Виды топлива",
                "Страны-поставщики нефти","Картель","Типы собственности");
        listView.setItems(names);

        listView.setOnMouseClicked((event)->{
            DBCursor objects = MongoDB.getInstance().getAllCollectionByName((String) listView.getSelectionModel().getSelectedItem());
            if(objects.count() == 0)
                printErrorMessage("Эта таблица пуста");
            else {
                Set<String> colls = objects.toArray().get(0).keySet();
                cartelTableView.getColumns().clear();
                List<String> columnNames = new ArrayList<String>();
                for(String coll : colls){
                    columnNames.add(coll);
                }
                for (int i = 0; i < columnNames.size(); i++){
                    final int finalIdx = i;
                    TableColumn<ObservableList<String>, String>column = new TableColumn<ObservableList<String>, String>(columnNames.get(i));
                    column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(finalIdx)));
                    cartelTableView.getColumns().add(column);
                }

            }
            ObservableList<List<String>> data = FXCollections.observableArrayList();
            cartelTableView.getItems().clear();

            for(DBObject dbObject : objects.toArray()) {
                Set<String>colls = dbObject.keySet();
                List<String> words = new ArrayList();
                for(String coll : colls) {
                    Object obj = dbObject.get(coll);
                    words.add(obj.toString());
                }
                cartelTableView.getItems().add(FXCollections.observableArrayList(words));
            }
        });

    }
    
}
