package d.somewheres.uieosio;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

//이 액티비티는 IoT이름을 전달받아서 IoT 장비의 시간대별 조작내역을 다 나타냄.
public class IoTinfoActivity extends AppCompatActivity {
    String IoTname;
    ListView listview; //리스트뷰 객체 생성
    ListViewAdapterIoTinfo adapter; //어댑터 생성

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iotinfo);

        //IoT이름을 받음(나중에 이름에따라서 REST API로 값 추출
        Intent intent = getIntent();
        IoTname = intent.getStringExtra("name");

        adapter = new ListViewAdapterIoTinfo();
        listview = (ListView) findViewById(R.id.IoTinfolistview);

        //장치 내용과 시간목록을 받아서 어댑터에 추가
        //adapter.addItem());
        adapter.notifyDataSetChanged();
        listview.setAdapter(adapter);
    }
}
