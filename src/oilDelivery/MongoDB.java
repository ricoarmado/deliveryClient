    package oilDelivery;

import com.ee.dynamicmongoquery.MongoQuery;
import com.ee.dynamicmongoquery.MongoQueryParser;
import com.mongodb.*;
import com.mongodb.util.JSON;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jongo.Aggregate;


/**
 * Created by stanislavtyrsa on 30.09.16.
 */
public class MongoDB {
    private MongoClient mongoClient;
    private DB db;
    private static volatile MongoDB instance;
    private DBCollection coll;
    private MongoDB(){}
    public static MongoDB getInstance(){
        if(instance == null)
            instance = new MongoDB();
        return instance;
    }
    public void connect(){
        try {
            mongoClient = new MongoClient();
            db = mongoClient.getDB("oil");
            App.out("Подключение к  MongoDB localhost:27017 [Успешно]");
        }catch (Exception e){
            App.out("Подключение к  MongoDB localhost:27017 [Ошибка]");
        }
    }
    public void disconnect(){
        mongoClient.close();
        App.out("Отключение от MongoDB");
    }
    
    
    public void Cartel(dbMode mode, String factory, String country){
        DBCollection tmp = getCollectionByName("Картель");
        switch (mode){
            case Create:
                DBCollection coll = getCollectionByName("Страны-поставщики нефти");
                BasicDBObject object = (BasicDBObject)coll.findOne(new BasicDBObject("value",country));
                ObjectId countryObjectId = object.getObjectId("_id");
                tmp.insert(new BasicDBObject("factoryID", new ObjectId(factory))
                .append("countryID", countryObjectId));
                break;
            case Delete:
                tmp.remove(new BasicDBObject("factoryID", new ObjectId(factory)));
                break;
        }
    }
    public void Order(dbMode mode, String objectID, String name, String volume, String date, String oil, String factoryObjectId){
        DBCollection tmp = getCollectionByName("Сведения о заказах");
        String oilObjectID = this.valueToObjectID("Виды топлива", oil);
        String _factoryObjectId = this.valueToObjectID("Заводы", factoryObjectId);
        switch(mode){
            case Create:
                tmp.insert(new BasicDBObject("name",name)
                .append("volume", Integer.parseInt(volume))
                .append("date", date)
                .append("oil",new ObjectId(oilObjectID))
                .append("factory", new ObjectId(_factoryObjectId)));
                break;
            case Edit:
                tmp.update(new BasicDBObject("_id", new ObjectId(objectID)), 
                        new BasicDBObject("$set",new BasicDBObject("name",name)
                            .append("volume", Integer.parseInt(volume))
                            .append("date", date)
                            .append("oil",new ObjectId(oilObjectID))
                            .append("factory", new ObjectId(_factoryObjectId))));
                break;
            case Delete:
                tmp.remove(new BasicDBObject("_id", new ObjectId(objectID)));
                break;
        }
    }
    public void FactoryOrder(dbMode mode, String objectID, String oil,String price, String factoryObjectId, String volume){
        DBCollection tmp = getCollectionByName("Виды выпускаемого топлива");
        String _oil = this.valueToObjectID("Виды топлива", oil);
        String _factoryObjectId = this.valueToObjectID("Заводы", factoryObjectId);
        switch(mode){
            case Create:
                tmp.insert(new BasicDBObject("oil",_oil)
                .append("price", Integer.parseInt(price))
                .append("oil", new ObjectId(_oil))
                .append("factory", new ObjectId(_factoryObjectId))
                .append("volume", Integer.parseInt(volume)));
                break;
            case Edit:
                
                break;
                
        }
    }
    public void Factory(dbMode mode, String objectID, String name, String city, String property, String year){
        DBCollection tmp = getCollectionByName("Заводы");
        String propField = valueToObjectID("Типы собственности",property);
        switch (mode){
            case Create:
                tmp.insert(new BasicDBObject("name",name).append("city",city)
                .append("propertyType",new ObjectId(propField))
                .append("year", year));
                break;
            case Edit:
                tmp.update(new BasicDBObject("_id",new ObjectId(objectID)),
                        new BasicDBObject("$set",new BasicDBObject("name",name)
                        .append("city", city)
                        .append("propertyType", new ObjectId(propField))
                        .append("year", Integer.parseInt(year))));
                break;
        }
    }
    public void Directory(String dbname, String value, dbMode mode, String objectID){
        DBCollection tmp = getCollectionByName(dbname);
        Object[] arr = tmp.findOne().keySet().toArray();
        String _field = (String)arr[1];
        switch (mode){
            case Create:
                tmp.insert(new BasicDBObject(_field,value));
                App.out("Создана запись в таблице " + dbname);
                break;
            case Edit:
                tmp.update(new BasicDBObject("_id", new ObjectId(objectID)),
                        new BasicDBObject("$set",new BasicDBObject(_field,value)));
                break;
            case Delete:
                tmp.remove(new BasicDBObject("_id",new ObjectId(objectID)));
                App.out("Удалена запись в таблице " + dbname);
                break;
        }
    }
    public String objectIDToValue(String dbname,String objectId){
        DBObject obj =  getCollectionByName(dbname).findOne(new BasicDBObject("_id",new ObjectId(objectId)));
        String coll = (String) obj.keySet().toArray()[1];
        return obj.get(coll).toString();
    }
    public String valueToObjectID(String dbname,String value){
        DBCollection tmp = getCollectionByName(dbname);
        Object[] arr = tmp.findOne().keySet().toArray();
        String _field = (String) arr[1];
        BasicDBObject query = new BasicDBObject(_field, value);
        DBCursor cursor = this.getCollectionByName(dbname).find(query);
        BasicDBObject object = null;
        if (cursor.hasNext()) {
            object = (BasicDBObject) cursor.next();
        }
        return object.getObjectId("_id").toString();
    }
    private DBCollection getCollectionByName(String name){
        switch (name){
            case "Заводы":
                return db.getCollection("Factory");
            case "Виды выпускаемого топлива":
                return db.getCollection("oilFactory");
            case "Сведения о заказах":
                return db.getCollection("orders");
            case "Виды топлива":
                return db.getCollection("oilTypes");
            case "Страны-поставщики нефти":
                return db.getCollection("countries");
            case "Картель":
                return db.getCollection("Cartel");
            case "Типы собственности":
                return db.getCollection("propertyTypes");
            default:
                return null;
        }
    }
    public Iterator<DBObject> getTop(){
        List<DBObject> pipeline = new ArrayList();
        DBObject fLookUp = new BasicDBObject("$lookup",
                new BasicDBObject("from","Factory")              
                    .append("localField", "factory")
                    .append("foreignField", "_id")
                    .append("as", "factory"));
        DBObject oLookUp = new BasicDBObject("$lookup",
                new BasicDBObject("from","oilTypes")              
                    .append("localField", "oil")
                    .append("foreignField", "_id")
                    .append("as", "oil"));
        DBObject unwind = new BasicDBObject("$unwind","$oil");
        DBObject unwind2 = new BasicDBObject("$unwind","$factory");
        DBObject proj = new BasicDBObject("$project",
        new BasicDBObject("Oil","$oil.id")
        .append("factory", "$factory.name")
        .append("volume", 1)
        .append("_id", 0));
        DBObject sort = new BasicDBObject("$sort",new BasicDBObject("volume",-1));
        DBObject limit = new BasicDBObject("$limit",10);
        return db.getCollection("oilFactory").aggregate(fLookUp, oLookUp,unwind,unwind2,proj,sort,limit).results().iterator();
    }
    public Iterator<DBObject> yearVolumeInCountry(String country){
    DBObject fLookUp = new BasicDBObject("$lookup",
                new BasicDBObject("from","Factory")              
                    .append("localField", "factoryID")
                    .append("foreignField", "_id")
                    .append("as", "Factory")); 
    DBObject cLookUp = new BasicDBObject("$lookup",
                new BasicDBObject("from","countries")              
                    .append("localField", "countryID")
                    .append("foreignField", "_id")
                    .append("as", "Country"));
    DBObject unwind = new BasicDBObject("$unwind","$Factory");
    DBObject unwind2 = new BasicDBObject("$unwind","$Country");
    DBObject proj = new BasicDBObject("$project",
        new BasicDBObject("factory","$Factory.name")
        .append("country", "$Country.value")
        .append("factoryID", 1)
        .append("_id", 0));
    DBObject match = new BasicDBObject("$match",new BasicDBObject("country",country)); 
    DBObject oLookUp = new BasicDBObject("$lookup",
                new BasicDBObject("from","oilFactory")              
                    .append("localField", "factoryID")
                    .append("foreignField", "factory")
                    .append("as", "oil"));
    DBObject unwind3 = new BasicDBObject("$unwind","$oil");
    DBObject proj2 = new BasicDBObject("$project",
        new BasicDBObject("volume","$oil.volume")
        .append("country", 1)
        .append("factory", 1));
    DBObject group = new BasicDBObject("$group",
    new BasicDBObject("_id","$factory")
    .append("volume", new BasicDBObject("$sum","$volume")));
    DBObject lim = new BasicDBObject("$limit",10);
        return db.getCollection("Cartel").aggregate(fLookUp,cLookUp,unwind,unwind2,proj,match, oLookUp,
                unwind3,proj2,group,lim).results().iterator();
    }
    public DBCursor getAllCollectionByName(String selectedItem) {
        return getCollectionByName(selectedItem).find();
    }
    public Iterator<DBObject> getFullCollectionByName(String selectedItem) {
        AggregationOutput out;
        List<DBObject> pipeline = new ArrayList();
        switch (selectedItem){
            case "Заводы":
                DBObject fLookUp = new BasicDBObject("$lookup",
                new BasicDBObject("from","propertyTypes")              
                    .append("localField", "propertyType")
                    .append("foreignField", "_id")
                    .append("as", "Property"));
                DBObject unwind = new BasicDBObject("$unwind","$Property");
                DBObject proj = new BasicDBObject("$project", 
                new BasicDBObject("name",1)
                .append("city", 1)
                .append("year", 1)
                .append("property", "$Property.type")
                .append("_id", 1));
                pipeline.add(fLookUp);
                pipeline.add(unwind);
                pipeline.add(proj);
                break;
            case "Виды выпускаемого топлива":
                DBObject oLookUp = new BasicDBObject("$lookup",
                new BasicDBObject("from","oilTypes")
                .append("localField", "oil")
                .append("foreignField", "_id")
                .append("as", "Oil"));
                DBObject ffLookUp = new BasicDBObject("$lookup",
                    new BasicDBObject("from","Factory")
                .append("localField", "factory")
                .append("foreignField", "_id")
                .append("as", "Factory"));
                DBObject unwindFactory = new BasicDBObject("$unwind","$Factory");
                DBObject unwindOil = new BasicDBObject("$unwind","$Oil");
                DBObject projOil = new BasicDBObject("$project",
                new BasicDBObject("Factory","$Factory.name")
                .append("Oil", "$Oil.value")
                .append("_id", 1)
                .append("price", 1)
                .append("volume", 1));
                pipeline.add(oLookUp);
                pipeline.add(ffLookUp);
                pipeline.add(unwindFactory);
                pipeline.add(unwindOil);
                pipeline.add(projOil);
                break;
            case "Сведения о заказах":
               DBObject fffLookUp = new BasicDBObject("$lookup",
                new BasicDBObject("from","Factory")
        .append("localField", "factory")
        .append("foreignField", "_id")
        .append("as", "Factory"));
        DBObject ooLookUp = new BasicDBObject("$lookup",
                new BasicDBObject("from","oilTypes")
        .append("localField", "oil")
        .append("foreignField", "_id")
        .append("as", "Oil"));
        DBObject unwindFactory2 = new BasicDBObject("$unwind","$Factory");
        DBObject unwindOil2 = new BasicDBObject("$unwind","$Oil");
        DBObject proj2 = new BasicDBObject("$project",
        new BasicDBObject("Factory","$Factory.name")
        .append("Oil", "$Oil.value")
        .append("_id", 1)
        .append("date", 1)
        .append("volume", 1)
        .append("name", 1)); 
                pipeline.add(ooLookUp);
                pipeline.add(fffLookUp);
                pipeline.add(unwindFactory2);
                pipeline.add(unwindOil2);
                pipeline.add(proj2);
               break;
            case "Виды топлива":
                return this.getAllCollectionByName(selectedItem).iterator();
            case "Страны-поставщики нефти":
                return this.getAllCollectionByName(selectedItem).iterator();
            case "Картель":
                DBObject ffffLookUp = new BasicDBObject("$lookup",
                new BasicDBObject("from","Factory")
        .append("localField", "factoryID")
        .append("foreignField", "_id")
        .append("as", "Factory"));
        DBObject cLookUp = new BasicDBObject("$lookup",
                new BasicDBObject("from","countries")
        .append("localField", "countryID")
        .append("foreignField", "_id")
        .append("as", "Country"));
        DBObject unwindFactory3 = new BasicDBObject("$unwind","$Factory");
        DBObject unwindOil3 = new BasicDBObject("$unwind","$Country");
        DBObject proj3 = new BasicDBObject("$project",
                new BasicDBObject("Factory","$Factory.name")
        .append("Country", "$Country.value")
        .append("_id", 1));
                pipeline.add(cLookUp);
                pipeline.add(ffffLookUp);
                pipeline.add(unwindFactory3);
                pipeline.add(unwindOil3);
                pipeline.add(proj3);
                break;
            case "Типы собственности":
                return this.getAllCollectionByName(selectedItem).iterator();
            default:
                return null;
        }
        return this.getCollectionByName(selectedItem).aggregate(pipeline).results().iterator();
    }
    
    public Iterator<DBObject> getCartelByFactory(String factoryID){
        DBObject match = new BasicDBObject("$match",new BasicDBObject("factoryID", new ObjectId(factoryID)));
        DBObject fLookUp = new BasicDBObject("$lookup",
                new BasicDBObject("from","Factory")
        .append("localField", "factoryID")
        .append("foreignField", "_id")
        .append("as", "Factory"));
        DBObject cLookUp = new BasicDBObject("$lookup",
                new BasicDBObject("from","countries")
        .append("localField", "countryID")
        .append("foreignField", "_id")
        .append("as", "Country"));
        DBObject proj = new BasicDBObject("$project",
                new BasicDBObject("Factory.name",1)
        .append("Country.value", 1)
        .append("_id", 0));
        AggregationOutput out = this.getCollectionByName("Картель").aggregate(match,fLookUp,cLookUp,proj);
        return out.results().iterator();
    }
    public Iterator<DBObject> getOrdersByFactoryId(String factoryId){
        DBObject match = new BasicDBObject("$match", new BasicDBObject("factory", new ObjectId(factoryId)));
        DBObject fLookUp = new BasicDBObject("$lookup",
                new BasicDBObject("from","Factory")
        .append("localField", "factory")
        .append("foreignField", "_id")
        .append("as", "Factory"));
        DBObject oLookUp = new BasicDBObject("$lookup",
                new BasicDBObject("from","oilTypes")
        .append("localField", "oil")
        .append("foreignField", "_id")
        .append("as", "Oil"));
        DBObject unwindFactory = new BasicDBObject("$unwind","$Factory");
        DBObject unwindOil = new BasicDBObject("$unwind","$Oil");
        DBObject proj = new BasicDBObject("$project",
        new BasicDBObject("Factory","$Factory.name")
        .append("Oil", "$Oil.value")
        .append("_id", 1)
        .append("date", 1)
        .append("volume", 1)
        .append("name", 1)); 
        AggregationOutput out = this.getCollectionByName("Сведения о заказах").aggregate(match,oLookUp,fLookUp,unwindFactory,unwindOil,proj);
        return out.results().iterator();
    }
    public Iterator<DBObject> getOilByFactoryId(String factoryId){
        DBObject match = new BasicDBObject("$match", new BasicDBObject("factory", new ObjectId(factoryId)));
        DBObject oLookUp = new BasicDBObject("$lookup",
                new BasicDBObject("from","oilTypes")
                .append("localField", "oil")
                .append("foreignField", "_id")
                .append("as", "Oil"));
                DBObject fLookUp = new BasicDBObject("$lookup",
                    new BasicDBObject("from","Factory")
                .append("localField", "factory")
                .append("foreignField", "_id")
                .append("as", "Factory"));
                DBObject unwindFactory = new BasicDBObject("$unwind","$Factory");
                DBObject unwindOil = new BasicDBObject("$unwind","$Oil");
                DBObject proj = new BasicDBObject("$project",
                new BasicDBObject("Factory","$Factory.name")
                .append("Oil", "$Oil.value")
                .append("_id", 1)
                .append("price", 1)
                .append("volume", 1));
        AggregationOutput out = this.getCollectionByName("Виды выпускаемого топлива").aggregate(match,oLookUp,fLookUp,unwindFactory,unwindOil,proj);
        return out.results().iterator();
    }
    public Iterator<DBObject>biggestOrderInYear(int Year){
        List<DBObject> pipeline = new ArrayList();
        String query;
        query = "{$lookup:{from:'Factory', localField:'factory', foreignField:'_id', as:'Factory'}}";
        pipeline.add((BasicDBObject)JSON.parse(query));
        query = "{$unwind: '$Factory'}";
        pipeline.add((BasicDBObject)JSON.parse(query));
        query = "{$project:{'factoryName':'$Factory.name','Year':{ $substr:[ '$date', 0, 4] }, '_id':0, 'volume':1}}";
        pipeline.add((BasicDBObject)JSON.parse(query));
        query = "{$match:{'Year': '"+Year+"'}}";
        pipeline.add((BasicDBObject)JSON.parse(query));
        query =  "{$sort:{'volume': -1}}";
        pipeline.add((BasicDBObject)JSON.parse(query));
        query = "{$limit:5}";
        pipeline.add((BasicDBObject)JSON.parse(query));
        return this.getCollectionByName("Сведения о заказах").aggregate(pipeline).results().iterator();
    }
    public Iterator<DBObject> financialStat(String factoryName){
        List<DBObject> pipeline = new ArrayList();
        String query;
        query = "{$lookup:{from:'orders', localField:'_id', foreignField:'factory', as:'orders'}}";
        pipeline.add((DBObject) JSON.parse(query));
        query = "{$lookup:{from:'oilFactory', localField:'_id', foreignField:'factory', as:'oil'}}";
        pipeline.add((DBObject) JSON.parse(query));
        query = "{$match:{name:'"+factoryName+"'}}";
        pipeline.add((DBObject) JSON.parse(query));
        query = "{$unwind: \"$orders\"}";
        pipeline.add((DBObject) JSON.parse(query));
        query = "{$unwind: \"$oil\"}";
        pipeline.add((DBObject) JSON.parse(query));
        query = "{$project: {\"price\":\"$oil.price\",\"Year\":{ $substr:[ \"$orders.date\", 0, 4] }, \"_id\":0}}";
        pipeline.add((DBObject) JSON.parse(query));
        query = "{$group:{\"_id\":\"$Year\", \"sum\":{$sum: \"$price\"}}}";
        pipeline.add((DBObject) JSON.parse(query));
        return this.getCollectionByName("Заводы").aggregate(pipeline).results().iterator();
    }
}
