package d.somewheres.uieosio;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class userManageActivity extends AppCompatActivity {

    // 사용자참가 리스트뷰와 목록 리스트뷰를 나타내는 JAVA파일을 구현해야한다.
    ListView listview; //리스트뷰 객체 생성
    ListViewuserAdapter adapter; //어댑터 생성
    String networkname;
    private DatabaseHelper DatabaseHelper; //데이터베이스 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage);
        DatabaseHelper = new DatabaseHelper(userManageActivity.this,"eos.db",null,1);

        //전 액티비티에서 보낸 네트워크 이름을 넘겨받는다
        Intent intent = getIntent();
        networkname = intent.getStringExtra("name");


        //사용자 목록을 받아 리스트뷰로 나타내는 코드 구현
        adapter = new ListViewuserAdapter();
        listview = (ListView) findViewById(R.id.userlistview);
        Cursor cursor = DatabaseHelper.useritem();
        adapter.addItem(ContextCompat.getDrawable(userManageActivity.this, R.drawable.user),cursor);
        if(cursor.getCount() > 0) {
            TextView userNolist = (TextView)findViewById(R.id.controllist);
            userNolist.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
        listview.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.usermanage_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button button = (Button) findViewById(R.id.userappend);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //버튼 클릭시 사용자 이름을 텍스트창에서 가져온다
                EditText usercreate = (EditText)findViewById(R.id.usercreate);
                String username = usercreate.getText().toString();

                //SSH를 이용해 컨트랙트 실행해 사용자를 넣는다


                //목록은 리스트뷰에 추가한다
                TextView userNolist = (TextView)findViewById(R.id.controllist);
                userNolist.setVisibility(View.GONE);
                adapter.addItem(ContextCompat.getDrawable(userManageActivity.this, R.drawable.user), username);
                adapter.notifyDataSetChanged();

                //db에 저장한다.
                User user = new User();
                user.setName(username);
                DatabaseHelper.adduserlist(user);
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