package d.somewheres.uieosio;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ListView;

public class userManageActivity extends AppCompatActivity {

    // 사용자참가 리스트뷰와 목록 리스트뷰를 나타내는 JAVA파일을 구현해야한다.
    ListView listview,listview2; //리스트뷰 객체 생성
    ListViewAdapterIoT adapter, adapter2; //어댑터 생성

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage);



        //사용자 목록을 받아 리스트뷰로 나타내는 코드 구현
        adapter = new ListViewAdapterIoT();
        listview = (ListView) findViewById(R.id.listview5);
        listview.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.toolbar4);
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