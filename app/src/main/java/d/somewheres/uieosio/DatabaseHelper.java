package d.somewheres.uieosio;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "eos.db";
    private static final int DATABASE_VERSION = 1;
    private Context context;

    //생성자 - database 파일을 생성한다.
    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    //db 처음 만들때 호출. - 테이블생성등의 초기 처리
    @Override
    public void onCreate(SQLiteDatabase db) {

        //사용자 테이블
        StringBuffer sb = new StringBuffer();
        sb.append(" CREATE TABLE if not exists eos_tb ( ");
        sb.append(" _ID INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sb.append(" NAME TEXT, ");
        sb.append(" USERKEY TEXT, ");
        sb.append(" PASSWORD TEXT ) ");
        db.execSQL(sb.toString());

        //네트워크 리스트 테이블
        StringBuffer tb = new StringBuffer();
        tb.append(" CREATE TABLE if not exists network_tb ( ");
        tb.append(" _ID INTEGER PRIMARY KEY AUTOINCREMENT, ");
        tb.append(" NAME TEXT, ");
        tb.append(" DESC TEXT, ");
        tb.append(" ACCOUNT TEXT ) ");
        db.execSQL(tb.toString());


    }

    //db 업그레이드시 필요시 호출 newVersion에 따라 반응
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //사용자를 추가하기위한 함수
    public void addPerson(Person person) {
        String name = person.getName();
        String userkey = person.getUserkey();
        String password = person.getPassword();
        SQLiteDatabase db = getWritableDatabase();

        // 데이터를 넣는다
       String sql = String.format("INSERT INTO eos_tb(name, userkey, password) VALUES('" + name + "','" + userkey + "','" + password + "');");
        db.execSQL(sql);

    }

    public String getPersonpassword() {
        String password = null;
        SQLiteDatabase db = getReadableDatabase();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT password FROM eos_tb");
        // 디비 데이터를 가져온다
        Cursor cursor = db.rawQuery(sb.toString(), null);
        while(cursor.moveToNext())

        {
            password = cursor.getString(0);
        }
        cursor.close();
        return password;
    }

    public String getPersonname() {
        String username = null;
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT name FROM eos_tb;";
    // 디비 데이터를 가져온다
        Cursor cursor = db.rawQuery(sql, null);
        while(cursor.moveToNext())

    {
        username = cursor.getString(0);
    }
        cursor.close();
        return username;
}

    public String getPersonkey() {
        String userkey = null;
        SQLiteDatabase db = getReadableDatabase();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT userkey FROM eos_tb");
        // 디비 데이터를 가져온다
        Cursor cursor = db.rawQuery(sb.toString(), null);
        while(cursor.moveToNext())

        {
            userkey = cursor.getString(0);
        }
        cursor.close();
        return userkey;
    }

    //네트워크 생성파트 부분
    public void addNetworklist(Network network) {
        String name = network.getName();
        String desc = network.getDesc();
        String account = network.getAccount();
        SQLiteDatabase db = getWritableDatabase();

        // 데이터를 넣는다
        String sql = String.format("INSERT INTO network_tb(name, desc, account) VALUES('" + name + "','" + desc + "','" + account + "');");
        db.execSQL(sql);

    }

    public Cursor networkitem() {
        SQLiteDatabase db = getReadableDatabase();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM network_tb");
        Cursor cursor = db.rawQuery(sb.toString(), null);
        return cursor;
    }
}