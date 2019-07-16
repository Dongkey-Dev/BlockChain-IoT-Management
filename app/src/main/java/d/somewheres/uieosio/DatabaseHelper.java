package d.somewheres.uieosio;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "eosio.db";
    private static final int DATABASE_VERSION = 1;

    //생성자 - database 파일을 생성한다.
    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    //db 처음 만들때 호출. - 테이블생성등의 초기 처리
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table eos_tb(_id INTEGER PRIMARY KEY AUTOINCREMENT, name TXET, userkey TEXT);");
    }

    //db 업그레이드시 필요시 호출 newVersion에 따라 반응
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //사용자를 추가하기위한 함수
    public void addPerson(Person person) {
        //쓸 수있는 db객체를 가져온다
        SQLiteDatabase db = getWritableDatabase();

        // 데이터를 넣는다
        StringBuffer sb = new StringBuffer();
        sb.append(" INSERT INTO eos_tb( ");
        sb.append(" name, userkey )" );
        sb.append(" VALUES ( ?, ? ) ");

        db.execSQL(sb.toString(),
                new Object[]{
                        person.getName(),
                        person.getUserkey()});;
    }
}