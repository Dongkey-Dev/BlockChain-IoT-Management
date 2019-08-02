package d.somewheres.uieosio;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class NetworkManageActivity extends AppCompatActivity {

    ListView listview; //리스트뷰 객체 생성
    ListViewAdapterIoT adapter; //어댑터 생성
    String networkname; //네트워크 이름을 받아온다.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_manage);

        //메인액티비티에서 보낸 값을 받는다.
        Intent intent = getIntent();

        //툴바 제목에 리스트뷰에 클릭한 네트워크이름 보여줌
        TextView networktitle = (TextView)findViewById(R.id.toolbar_title2);
        networkname = intent.getStringExtra("name");
        networktitle.setText(intent.getStringExtra("name"));
        // 여기 리스트뷰에서는 최근 조작한 IoT장치 값 받아서 나타냄(미완)
        adapter = new ListViewAdapterIoT();
        listview = (ListView) findViewById(R.id.listview2);
        listview.setAdapter(adapter);


        //iot 관리 파트
        Button button = (Button) findViewById(R.id.btn_IoTmanage);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),IoTdeviceManageActivity.class);
                //여기서도 인탠트 메인메뉴 타이틀을 넘겨줘야함.
                //iot참가 명령어를 보내기위해 네트워크 이름을 다음 액티비티로 전송
                intent.putExtra("name",networkname);
                startActivity(intent);

            }
        });
        Button button2 = (Button) findViewById(R.id.btn_usermanage);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),userManageActivity.class);
                //여기서도 인탠트 메인메뉴 타이틀을 넘겨줘야함.
                //user참가 명령어를 보내기위해 네트워크 이름을 다음 액티비티로 전송
                intent.putExtra("name",networkname);
                startActivity(intent);

            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


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
