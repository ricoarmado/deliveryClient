package Controllers;

/**
 * Created by Stas on 28.09.2016.
 */


import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.io.IOException;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import oilDelivery.MongoDB;
import oilDelivery.dbMode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import oilDelivery.App;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
public class BaseViewController implements Initializable{
    private Stage stage;
    private static BaseViewController instance;
    private BaseViewController(){}
    public static BaseViewController getInstance(){
        if(instance == null)
            instance = new BaseViewController();
        return instance;
    }

    public void AddToLog(String message){
        Log.appendText(message);
    }
    public void setWarningMessage(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Система");
        alert.setHeaderText("В результате работы возникла ошибка");
        alert.setContentText("Текст ошибки: " + message);
        alert.showAndWait();

    }
    public void setExceptionMessage(Exception ex){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception Dialog");
        alert.setHeaderText("В результате работы возникло исключение");
        alert.setContentText("Текст исключения:");

// Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

// Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }
    public void printErrorMessage(String text){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Система");
        alert.setHeaderText("В результате работы системы возникла ошибка.");
        alert.setContentText("Текст ошибки: " + text);
        alert.showAndWait();
    }
    public boolean isDirectory(String db){
        return db == "Виды топлива" || db == "Страны-поставщики нефти" || db == "Типы собственности";
    }
    public List<String>dbCursorToListString(DBCursor dbCursor){
        List<String> words = new ArrayList();
        for(DBObject dbObject: dbCursor.toArray()) {
            Set<String> colls = dbObject.keySet();
            String coll = (String) colls.toArray()[1];
            Object obj = dbObject.get(coll);
            words.add(obj.toString());
        }
        return words;
    }
    @FXML
    private Menu editmenu;
    
    @FXML
    private Menu reports;

    @FXML
    private TextArea Log;

    @FXML
    private ListView<?> tablesListView;

    @FXML
    private MenuItem refreshTableMenuItem;

    @FXML
    private MenuItem aboutMenuItem;

    @FXML
    private MenuItem closeMenuItem;

    @FXML
    private MenuItem refreshListMenuItem;

    @FXML
    private TableView<ObservableList<String>> mainTableView;

    @FXML
    void closeMenuItemClick(ActionEvent event) {
        this.stage.hide();
    }

    @FXML
    void aboutMenuItemClick(ActionEvent event) {
        this.setWarningMessage("Пока что здесь совсем пусто, но быть может, когда-нибудь что-то появится :)");
    }



    @FXML
    private void launchServerButtonClick(ActionEvent event) {
        try {
            Log.clear();
            MongoDB.getInstance().connect();
            editmenu.disableProperty().setValue(false);
            reports.disableProperty().setValue(false);
            tablesListView.disableProperty().setValue(false);
        } catch (Exception e) {
            this.printErrorMessage(e.getLocalizedMessage());
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        ObservableList names = FXCollections.observableArrayList(
                "Заводы", "Виды выпускаемого топлива","Сведения о заказах","Виды топлива",
                "Страны-поставщики нефти","Типы собственности");
        tablesListView.setItems(names);
        this.mainTableView.setOnMousePressed((event)->{
            if(event.isPrimaryButtonDown() && event.getClickCount() == 2)
                this.editFieldClick(new ActionEvent());
        });
        tablesListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                tablesListView.setDisable(true);
                Iterator<DBObject> objects = MongoDB.getInstance().getFullCollectionByName((String) tablesListView.getSelectionModel().getSelectedItem());
                if(objects.hasNext() == false)
                    printErrorMessage("Эта таблица пуста");
                else {
                    JSONObject header = null;
                    try {
                        header = App.getHeader(App.switchName((String) tablesListView.getSelectionModel().getSelectedItem()));
                    } catch (Exception ex) {
                        Logger.getLogger(BaseViewController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Set<String> colls = objects.next().keySet();
                    mainTableView.getColumns().clear();
                    List<String> columnNames = new ArrayList<String>();
                    
                    for(String coll : colls){
                        columnNames.add(header.get(coll).toString());
                    }
                    for (int i = 0; i < columnNames.size(); i++){
                        final int finalIdx = i;
                        TableColumn<ObservableList<String>, String>column = new TableColumn<ObservableList<String>, String>(columnNames.get(i));
                        column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(finalIdx)));
                        mainTableView.getColumns().add(column);
                    }
                    
                }
                ObservableList<List<String>> data = FXCollections.observableArrayList();
                mainTableView.getItems().clear();
                while(objects.hasNext()){
                    DBObject dbObject = objects.next();
                    Set<String>colls = dbObject.keySet();
                    List<String> words = new ArrayList();
                    for(String coll : colls) {
                        Object obj = dbObject.get(coll);
                        words.add(obj.toString());
                    }
                    mainTableView.getItems().add(FXCollections.observableArrayList(words));
                }
                tablesListView.setDisable(false);
                mainTableView.getColumns().get(0).setVisible(false);
            }
        });
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    public void editOrderField(dbMode mode, ObservableList row){
        try{
            Stage st = new Stage();
            st.setTitle("Учет нефтеперерабатывающих заводов");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/OrderFXML.fxml"));
            if(mode == dbMode.Edit){
                ObservableList ol = (ObservableList)row.get(0);
                loader.setController(new OrderController(mode,ol.get(0).toString(),ol.get(1).toString(),
                ol.get(2).toString(),ol.get(3).toString(), 
                        (ArrayList<String>) this.dbCursorToListString(MongoDB.getInstance().getAllCollectionByName("Виды топлива")),
                        ol.get(4).toString(),ol.get(5).toString(),(ArrayList<String>) this.dbCursorToListString(MongoDB.getInstance().getAllCollectionByName("Заводы"))
                        ,st));
            }
            else{
                loader.setController(new OrderController(dbMode.Create,null,null,null,null, 
                        (ArrayList<String>) this.dbCursorToListString(MongoDB.getInstance().getAllCollectionByName("Виды топлива")),
                        "Выберите топлоиво",null, (ArrayList<String>) this.dbCursorToListString(MongoDB.getInstance().getAllCollectionByName("Заводы"))
                        ,st));
            }
            Parent root = (Parent) loader.load();
            st.setResizable(false);
            st.setScene(new Scene(root));
            st.show();
        }catch(Exception e){
            this.setExceptionMessage(e);
        }
    }
    public void editFactoryOrderClick(dbMode dbMode, ObservableList row) {
        try{
            Stage st = new Stage();
            st.setTitle("Учет нефтеперерабатывающих заводов");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/FactoryOilFXML.fxml"));
            if(dbMode == dbMode.Edit){
               ObservableList ol = (ObservableList)row.get(0);
               loader.setController(new FactoryOilController(ol.get(0).toString(),ol.get(2).toString(),ol.get(1).toString(),
               ol.get(4).toString(),(ArrayList<String>) this.dbCursorToListString(MongoDB.getInstance().getAllCollectionByName("Виды топлива"))
               ,ol.get(3).toString(),dbMode,(ArrayList<String>) this.dbCursorToListString(MongoDB.getInstance().getAllCollectionByName("Заводы"))
               ,st));
            }
            else
                loader.setController(new FactoryOilController(null,null,null,null, 
                        (ArrayList<String>) this.dbCursorToListString(MongoDB.getInstance().getAllCollectionByName("Виды топлива"))
                        ,null,dbMode, (ArrayList<String>) this.dbCursorToListString(MongoDB.getInstance().getAllCollectionByName("Заводы"))
                        ,st));
            Parent root = (Parent) loader.load();
            st.setResizable(false);
            st.setScene(new Scene(root));
            st.show();
        }catch(Exception e){
            App.out("Ошибка при попытке загрузить данные о заказах");
        }
    }
    private void editFactoryField(dbMode mode, ObservableList row){
        try {
            Stage st = new Stage();
            st.setTitle("Учет нефтеперерабатывающих заводов");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/FactoryFXML.fxml"));
            if(mode == dbMode.Edit) {               
                ObservableList ol = (ObservableList)row.get(0);
                String order;
                try{
                    order = ol.get(5).toString();
                }catch(Exception e){
                    order = null;
                }
                
                loader.setController(new FactoryController(ol.get(1).toString(), ol.get(2).toString(), ol.get(0).toString(),
                        this.dbCursorToListString(MongoDB.getInstance().getAllCollectionByName("Типы собственности")),
                        ol.get(3).toString(),dbMode.Edit,st, order, ol.get(4).toString()));
            }
            else
                loader.setController(new FactoryController(null,null,null,
                        this.dbCursorToListString(MongoDB.getInstance().getAllCollectionByName("Типы собственности")),
                        null, dbMode.Create,st,null, "-"));
            Parent root = (Parent) loader.load();
            st.setResizable(false);
            st.setScene(new Scene(root));
            st.show();
        }catch (Exception e) {
            this.setExceptionMessage(e);
            App.out("Ошибка при попытке загрузить данные о заводах");
        }
    }

    @FXML
    public void topClick(ActionEvent actionEvent){
        Stage st = new Stage();
        st.setScene(new Scene(getParent(),500,500));
        st.setTitle("Топ 10 заводов по выпуску топлива");
        st.show();
    }
//Обработчики добавления/редактирования/удаления
    @FXML
    public void editFieldClick(ActionEvent actionEvent) {
        try {
            String db = (String) tablesListView.getSelectionModel().getSelectedItem();
            if(db == "Картель")
                throw new Exception("Для данной таблицы не предусмотрена возможность редактирования");
            ObservableList tmp = mainTableView.getSelectionModel().getSelectedItems();
            if(isDirectory(db))
                editDirField(db,tmp, dbMode.Edit);
            switch (db){
                case "Заводы":
                    editFactoryField(dbMode.Edit,tmp);
                    break;
                case "Сведения о заказах":
                    this.editOrderField(dbMode.Edit, tmp);
                    break;
                case "Виды выпускаемого топлива":
                    this.editFactoryOrderClick(dbMode.Edit,tmp);
                    break;
            }
        }catch (Exception e){
            this.setWarningMessage("Выберите строку");
        }
    }
    @FXML 
    public void yearVolumeByCountry(ActionEvent actionEvent){
        DBCursor acbn = MongoDB.getInstance().getAllCollectionByName("Страны-поставщики нефти");
        List<String> choices = new ArrayList<>();
        acbn.forEach(action -> choices.add(action.get("value").toString()));
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Годовой объем топлива в стране");
        dialog.setHeaderText("Для продолжения необходимо выбрать страну");
        dialog.setContentText("Ваш выбор:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            Stage st = new Stage();
            st.setTitle("Учет нефтеперерабатывающих заводов");
            Group root = new Group();
            Iterator<DBObject> top = MongoDB.getInstance().yearVolumeInCountry(result.get());
             ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        while(top.hasNext()){
            BasicDBObject next = (BasicDBObject) top.next();
            pieChartData.add(new Data(next.getString("_id"),next.getInt("volume")));
        }
        final PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Объем топлива по заводам страны");
        chart.setLabelLineLength(10);
        chart.setLegendSide(Side.LEFT);
        chart.setLegendVisible(false);
        final Label caption = new Label("");
        caption.setTextFill(Color.RED);
        caption.setStyle("-fx-font: 24 arial;");
        for (final PieChart.Data data : chart.getData()) {
            data.getNode().addEventHandler(MouseEvent.MOUSE_MOVED,e ->{
                caption.setTranslateX(e.getSceneX());
                caption.setTranslateY(e.getSceneY());
                caption.setText(String.valueOf(data.getPieValue()));
            });
        }
        chart.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> caption.setText(""));
        root.getChildren().addAll(chart, caption);
        st.setScene(new Scene(root));
        st.show();
        }
        
    }
    @FXML
    public void createFieldClick(ActionEvent actionEvent) {
        try {
            String db = (String) tablesListView.getSelectionModel().getSelectedItem();
            if(db == "Картель")
                throw new Exception("Для данной таблицы не предусмотрена возможность создания");
            if(isDirectory(db))
                editDirField(db,null, dbMode.Create);
            switch (db){
                case "Заводы":
                    editFactoryField(dbMode.Create,null);
                    break;
                case "Сведения о заказах":
                    this.editOrderField(dbMode.Create, null);
                    break;
                case "Виды выпускаемого топлива":
                    this.editFactoryOrderClick(dbMode.Create,null);
                    break;
            }
        }catch (Exception e){
            this.setWarningMessage(e.getMessage());
        }
    }
    @FXML
    public void deleteFieldClick(ActionEvent actionEvent) {
        try {
            String db = (String) tablesListView.getSelectionModel().getSelectedItem();
            if(db == "Картель")
                throw new Exception("Для данной таблицы не предусмотрена возможность удаления");
            ObservableList tmp = mainTableView.getSelectionModel().getSelectedItems();
            if(isDirectory(db))
                deleteDirField(db,tmp);
        }catch (Exception e){
            setExceptionMessage(e);
        }
    }
    @FXML
    public void adminViewClick(ActionEvent actionEvent) throws IOException{
        Stage st = new Stage();
        Parent root =  FXMLLoader.load(getClass().getResource("../FXML/adminView.fxml"));
        st.setScene(new Scene(root));
        st.setMaximized(true);
        st.setTitle("Учет нефтеперерабатывающих заводов");
        st.show();
    }
    @FXML
    public void bigOrdersClick(ActionEvent actionEvent){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Учет нефтеперерабывающих предприятий");
        dialog.setHeaderText("Самые большие заказы в году");
        dialog.setContentText("Укажите год:");
        Optional<String>result = dialog.showAndWait();
        result.ifPresent(consumer->{
            Iterator<DBObject> top = MongoDB.getInstance().biggestOrderInYear(Integer.decode(consumer));
             Group root = new Group();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        while(top.hasNext()){
            BasicDBObject next = (BasicDBObject) top.next();
            pieChartData.add(new Data(next.getString("factoryName"),next.getInt("volume")));
        }
        final PieChart chart = new PieChart(pieChartData);
        chart.setTitle("5 Заводов с наибольшим количеством заказов в году");
        chart.setLabelLineLength(10);
        chart.setLegendSide(Side.LEFT);
        chart.setLegendVisible(true);
        final Label caption = new Label("");
        caption.setTextFill(Color.RED);
        caption.setStyle("-fx-font: 24 arial;");
        for (final PieChart.Data data : chart.getData()) {
            data.getNode().addEventHandler(MouseEvent.MOUSE_MOVED,e ->{
                caption.setTranslateX(e.getSceneX());
                caption.setTranslateY(e.getSceneY());
                caption.setText(String.valueOf(data.getPieValue()));
            });
        }
        chart.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> caption.setText(""));
        chart.setLabelLineLength(10);
        chart.setLegendSide(Side.BOTTOM);
        root.getChildren().addAll(chart, caption);
        Stage st = new Stage();
        st.setScene(new Scene(root,500,500));
        st.setTitle("Учет нефтеперерабатывающих заводов");
        st.show();
        });
    }
    @FXML
    public void financialReportClick(ActionEvent actionEvent) throws IOException{
        DBCursor curs = MongoDB.getInstance().getAllCollectionByName("Заводы");
        List<String>choices = new ArrayList();
        curs.forEach(action->choices.add(action.get("name").toString()));
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Статистика дохода завода по годам");
        dialog.setHeaderText("Для продолжения необходимо выбрать завод");
        dialog.setContentText("Ваш выбор:");
        Optional<String> result = dialog.showAndWait();
        if(result.isPresent()){
            Iterator<DBObject> top = MongoDB.getInstance().financialStat(result.get());
            Stage st = new Stage();
            st.setTitle("Учет нефтеперерабатывающих заводов");
            Group root = new Group();
            final CategoryAxis xAxis = new CategoryAxis();
            final NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Прибыль ($)");
            xAxis.setLabel("Год");
            XYChart.Series series = new XYChart.Series<>();
            series.setName(result.get());
            Map<String,Integer> map = new TreeMap();
            while(top.hasNext()){
                BasicDBObject next = (BasicDBObject) top.next();
                String x = next.getString("_id");
                int y = Integer.decode(next.getString("sum"));
                map.put(x, y);
            }
            map.forEach((x,y)->series.getData().add(new XYChart.Data(x,y)));
            final LineChart<String,Number> lineChart = new LineChart(xAxis,yAxis);
            lineChart.setTitle("Статистика заработанных денег по годам");
            lineChart.getData().add(series);
            
            root.getChildren().add(lineChart);
            st.setScene(new Scene(root));
            st.show();
        }
    }
//ДИРЕКТОРИИ
    private void deleteDirField(String db, ObservableList row) {
        ObservableList tmp = (ObservableList)row.get(0);
        String objectid = (String) tmp.get(0);
        MongoDB.getInstance().Directory(db,null,dbMode.Delete,objectid);
        App.out("Удалена запись справочной таблицы");
    }
    private void editDirField(String db, ObservableList tmp, dbMode mode) {
        try {
            Stage st = new Stage();
            st.setTitle("Учет нефтеперерабатывающих заводов");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/DirectoryFXML.fxml"));
            loader.setController(new DirectoryController(tmp, mode,db, st));
            Parent root = (Parent) loader.load();
            st.setResizable(false);
            st.setScene(new Scene(root));
            st.show();
        }catch (Exception e) {
            this.setExceptionMessage(e);
        }
    }

    private Parent getParent() {
        Iterator<DBObject> top = MongoDB.getInstance().getTop();
        Group root = new Group();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        while(top.hasNext()){
            BasicDBObject next = (BasicDBObject) top.next();
            pieChartData.add(new Data(next.getString("factory"),next.getInt("volume")));
        }
        final PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Топ 10 заводов по объему выпускаемого топлива(т.)");
        chart.setLabelLineLength(10);
        chart.setLegendSide(Side.LEFT);
        chart.setLegendVisible(true);
        final Label caption = new Label("");
        caption.setTextFill(Color.RED);
        caption.setStyle("-fx-font: 24 arial;");
        for (final PieChart.Data data : chart.getData()) {
            data.getNode().addEventHandler(MouseEvent.MOUSE_MOVED,e ->{
                caption.setTranslateX(e.getSceneX());
                caption.setTranslateY(e.getSceneY());
                caption.setText(String.valueOf(data.getPieValue()));
            });
        }
        chart.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> caption.setText(""));
        root.getChildren().addAll(chart, caption);
        return root;
    }

}
