package videopager.wilsonmanzano.globaluz.videopagerpc.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Vector;

/**
 * Created by HP on 22/03/2017.
 */

public class DataBaseManager extends SQLiteOpenHelper {

    //@Params that save the name of the database and the script for create the table for first time
    public static String dataBaseName="OrderDataBase";
    public static int version=1;
    public static String SCRIPTS_FOR_DEFINE_TABLES[]
            ={
            "CREATE TABLE IF NOT EXISTS " +
                    "orders(" +
                    "order_code integer primary key autoincrement," +
                    "order_order integer," +
                    "order_pager intenger" +
                    ")"

    };

    public static String SCRIPTS_FOR_DELETE_TABLES[]
            ={"DROP TABLE IF EXISTS orders"};

    public SQLiteDatabase sqLiteDatabase;

    public DataBaseManager(Context context) {
        super(context, dataBaseName, null, version);
        initializeDataBase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //android.os.Debug.waitForDebugger();
        for (String currentScript:SCRIPTS_FOR_DEFINE_TABLES){
            db.execSQL(currentScript);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion>oldVersion){
            for (String currentScript:SCRIPTS_FOR_DELETE_TABLES){
                db.execSQL(currentScript);
            }
            this.onCreate(db);
        }
    }

    public void initializeDataBase(){
        try {
            sqLiteDatabase = this.getWritableDatabase();
        }catch (Exception error){
            Log.e("DataBaseManager","initializeDataBase: error - "+error.toString());
        }
    }

    public void closeDataBaseConnection(){
        if(sqLiteDatabase!=null){
            if(sqLiteDatabase.isOpen()){
                sqLiteDatabase.close();
            }
        }
    }

    //Insert Object in to Table

    public  String insertIntoTable(String tableName,ContentValues contentValues){
        String result="";
        try{
            result="Rows affected: "+sqLiteDatabase.insertOrThrow(tableName, null, contentValues );
        }catch(Exception error){
            result=error.toString();
        }
        return result;
    }

    //Update a object in to table

    public  String updateTable(String tableName,ContentValues contentValues,String whereClause){
        String result="";
        try{
            result="Rows affected: "+sqLiteDatabase.update(tableName,  contentValues,whereClause,null );
        }catch(Exception error){
            result=error.toString();
        }
        return result;
    }


    //Delete object from table ( this function delete the object with its ID) the ids that we use is the order number
    public  String deleteFromTable(String tableName,String whereClause){
        String result="";
        try{
            result="Rows affected: "+sqLiteDatabase.delete(tableName,  whereClause,null );
        }catch(Exception error){
            result=error.toString();
        }
        return result;
    }

    //Excute some order with database commands

    public  String executeThisSQL(String sql){
        String result="";
        try{
            sqLiteDatabase.execSQL(sql);
        }catch(Exception error){
            result=error.toString();
        }
        return result;
    }

    public void test(){
        this.executeThisSQL("insert into orders (order_order,order_pager) values('dummy','123')");
    }

    //Get all the objects storaged in the database
    public Vector<Object[]> executeQuery(String queryClause){
        Vector<Object[]> result=new Vector<Object[]>();
        Cursor cursor=null;
        try{
            cursor=sqLiteDatabase.rawQuery(queryClause,null);
            String[] columnNames=cursor.getColumnNames();
            result.add(columnNames);
            while (cursor.moveToNext()) {
                Object[] row = new Object[columnNames.length];
                for (int columnIndex = 0; columnIndex < cursor.getColumnCount(); columnIndex++) {
                    int type=cursor.getType (columnIndex);
                    if(Cursor.FIELD_TYPE_INTEGER==type){
                        row[columnIndex]=cursor.getInt(columnIndex);
                    }
                    if(Cursor.FIELD_TYPE_FLOAT==type){
                        row[columnIndex]=cursor.getFloat(columnIndex);
                    }
                    if(Cursor.FIELD_TYPE_STRING==type){
                        row[columnIndex]=cursor.getString(columnIndex);
                    }
                    if(Cursor.FIELD_TYPE_BLOB==type){
                        row[columnIndex]=cursor.getBlob(columnIndex);
                    }
                }
                result.add(row);
            }
        }catch(Exception error){
            result=null;
        }
        finally{
            if(cursor!=null){
                if(!cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
        return result;
    }
}