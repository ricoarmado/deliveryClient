package oilDelivery;

import Controllers.BaseViewController;
import java.io.FileReader;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by Stas on 27.09.2016.
 */
public class App {
    public static void out(String toWrite){ BaseViewController.getInstance().AddToLog(toWrite + "\n"); }
    public static JSONObject getHeader(String collectionName) throws ParseException, IOException{
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(new FileReader("collectionInfo.json"));
        JSONObject get = (JSONObject)obj.get(collectionName);
        return get;
    }
    public static String switchName(String name){
        switch (name){
            case "Заводы": return "Factory";
            case "Виды выпускаемого топлива": return "oilFactory";
            case "Сведения о заказах": return "orders";
            case "Виды топлива": return "oilTypes";
            case "Страны-поставщики нефти": return "countries";
            case "Картель": return "Cartel";
            case "Типы собственности": return "propertyTypes";
            default: return null;
        }
    }
}
