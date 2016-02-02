package uk.co.chatlonger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;

/**
 * Created by Ben Freke on 31/01/2016.
 */
public class DatabaseConnector extends SQLiteOpenHelper{

    SQLiteDatabase db_read;
    SQLiteDatabase db_write;

    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE config " +
                        "(id INT PRIMARY KEY, name TEXT, email TEXT, apiKey TEXT)"
        );

        db.execSQL(
                "CREATE TABLE conversations " +
                        "(id BIGINT PRIMARY KEY, user1 INTEGER, user2 INTEGER, name TEXT)"
        );

        db.execSQL(
                "CREATE TABLE messages " +
                        "(id BIGINT PRIMARY KEY, sender INTEGER,receiver INTEGER, timestamp DATETIME, content TEXT)"
        );

    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {}

    public DatabaseConnector(Context context){
        super(context,"ChatLonger",null,1);
        db_read = this.getReadableDatabase();
        db_write = this.getWritableDatabase();
        //setTestData();
    }

    public String getConversationName(int id){
        Cursor res =  db_read.rawQuery( "SELECT * FROM conversations WHERE id="+id+"", null );
        res.moveToFirst();
        if (res.getCount() == 0) return null;
        return res.getString(res.getColumnIndex("name"));
    }

    public String getConversationRecipient(int id){
        Cursor res =  db_read.rawQuery( "SELECT * FROM conversations WHERE id="+id+"", null );
        res.moveToFirst();
        if (res.getCount() == 0) return null;
        return res.getString(res.getColumnIndex("user2"));
    }

    public void message(int id, int sender, int recipient, String message, String timestamp){
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("sender", sender);
        contentValues.put("receiver", recipient);
        contentValues.put("timestamp", timestamp);
        contentValues.put("content", message);
        db_write.insert("messages", null, contentValues);
    }

    public void setTestData(){
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", 1);
        contentValues.put("user1", 1);
        contentValues.put("user2", 2);
        contentValues.put("name", "Joe Bloggs");
        db_write.insert("conversations", null, contentValues);

        contentValues = new ContentValues();
        contentValues.put("id", 2);
        contentValues.put("user1", 2);
        contentValues.put("user2", 1);
        contentValues.put("name", "Ben Freke");
        db_write.insert("conversations", null, contentValues);

        contentValues = new ContentValues();
        contentValues.put("id", 2);
        contentValues.put("name", "John Smith");
        contentValues.put("email", "john.smith@gmail.com");
        contentValues.put("apiKey", "VzMk8S89UfBDJnqYJFxxtVGIH7FZVin4ZOq4MZcz2qcPsaLYv865cKuA67HuRa4b");
        db_write.insert("config", null, contentValues);

    }

    public int getUserID(){
        Cursor res =  db_read.rawQuery( "SELECT * FROM config LIMIT 1", null );
        res.moveToFirst();
        if (res.getCount() == 0) return -1;
        return Integer.parseInt(res.getString(res.getColumnIndex("id")));
    }

    public String getUserAPI(){
        Cursor res =  db_read.rawQuery( "SELECT * FROM config LIMIT 1", null );
        res.moveToFirst();
        if (res.getCount() == 0) return null;
        return (res.getString(res.getColumnIndex("apiKey")));
    }

    public String[][] getMessages(int conversationID){
        Cursor res =  db_read.rawQuery( "SELECT * FROM conversations WHERE id="+conversationID+"", null );
        res.moveToFirst();
        if (res.getCount() == 0) return null;
        int user1 =  Integer.parseInt(res.getString(res.getColumnIndex("user1")));
        int user2 =  Integer.parseInt(res.getString(res.getColumnIndex("user2")));

        res =  db_read.rawQuery( "SELECT * FROM messages WHERE (sender="+user1+" AND receiver="+user2+") OR (sender="+user2+" AND receiver="+user1+") ORDER BY id ASC", null );
        if (res.getCount() == 0) return null;
        int i = 0;
        String messages[][] = new String[res.getCount()][3];
        while (res.moveToNext()) {
            messages[i][0] = res.getString(res.getColumnIndex("sender"));
            messages[i][1] = res.getString(res.getColumnIndex("receiver"));
            messages[i][2] = res.getString(res.getColumnIndex("content"));
            i++;
        }
        res.close();
        return messages;
    }

    public void initaliseDatabase(){

    }

    private boolean tableExists(String tableName){
        Cursor cursor = db_read.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }


}
