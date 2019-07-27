package d.somewheres.uieosio;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

//IoT관리 페이지의 액티비티이다.
public class IoTdeviceManageActivity extends AppCompatActivity {

    //IoT폼을 위한 리스트뷰와 어댑터 JAVA파일을 생성해야한다.
    ListView listview; //리스트뷰 객체 생성
    ListViewAdapterIoT adapter; //어댑터 생성
    String networkname;
    private DatabaseHelper DatabaseHelper; //데이터베이스 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iot_device_manage);

        DatabaseHelper = new DatabaseHelper(IoTdeviceManageActivity.this,"eos.db",null,1);

        //전 액티비티에서 보낸 네트워크 이름을 넘겨받는다
        Intent intent = getIntent();
        networkname = intent.getStringExtra("name");

        //DB에서 IoT기기 내역을 받아 리스트뷰로 나타내는 코드 구현
        adapter = new ListViewAdapterIoT();
        listview = (ListView) findViewById(R.id.ioT_listview);
        Cursor cursor = DatabaseHelper.iotitem();
        adapter.addItem(ContextCompat.getDrawable(IoTdeviceManageActivity.this, R.drawable.ic_menu_camera),cursor);
        if(cursor.getCount() > 0) {
            TextView IoTNolist = (TextView)findViewById(R.id.IoTNolist);
            IoTNolist.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
        listview.setAdapter(adapter);

        //화면의 위에 제목및 뒤로가기를생성하는 툴바이다.
        Toolbar toolbar = findViewById(R.id.iot_manage_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //미완 IoT장치를 리스트뷰를 추가하기위해 사용할 클릭리스너이다.
        //1. SSH를 사용해 컨트랙트 실행해 IoT장치 이름으로 넣음(명령어)
        Button button = (Button) findViewById(R.id.IoTappend);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //버튼 클릭시 ioT 이름을 텍스트창에서 가져온다
                EditText iotcreate = (EditText)findViewById(R.id.iotcreate);
                String iotname = iotcreate.getText().toString();

                //SSH를 이용해 컨트랙트 실행해 iOt장치를 넣는다


                //목록은 리스트뷰에 추가한다
                TextView IoTNolist = (TextView)findViewById(R.id.IoTNolist);
                IoTNolist.setVisibility(View.GONE);
                adapter.addItem(ContextCompat.getDrawable(IoTdeviceManageActivity.this, R.drawable.ic_menu_camera), iotname);
                adapter.notifyDataSetChanged();

                //db에 저장한다.
                IoT iot = new IoT();
                iot.setName(iotname);
                DatabaseHelper.addIoTlist(iot);
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
