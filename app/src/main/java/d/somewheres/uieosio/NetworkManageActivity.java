package d.somewheres.uieosio;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class NetworkManageActivity extends AppCompatActivity {

    ListView listview; //리스트뷰 객체 생성
    ListViewAdapterIoT adapter; //어댑터 생성
    String networkname; //네트워크 이름을 받아온다.
    HTTPThread httpThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_manage);

        //rest api 가져올 url 세팅
        String url = "http://192.168.0.13:8888/v1/history/get_actions";
        //메인액티비티에서 보낸 값을 받는다.
        Intent intent = getIntent();

        //툴바 제목에 리스트뷰에 클릭한 네트워크이름 보여줌
        TextView networktitle = (TextView)findViewById(R.id.toolbar_title2);
        networkname = intent.getStringExtra("name");
        networktitle.setText(intent.getStringExtra("name"));

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("pos","-1"); //post할 값을 넣어준다
            jsonObject.put("offset","-20");
            jsonObject.put("account_name",networkname);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //네트워크이름에 대한 공용키를 얻어온다.
        httpThread = new HTTPThread(url,jsonObject);
        httpThread.start(); //시작
        try {
            httpThread.join(); //쓰레드 끝날떄까지 멈춤
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    public class HTTPThread extends Thread {
        Context context; //액티비티의 컨택스트를 저장할 변수 현재 사용 x
        String url; //url을 저장할 변수
        String result; //결과값을 저장해 리턴하기위한 변수
        TextView setText; //값을 나타낼 텍스트뷰 id 값
        JSONObject value; //json으로 블록체인 서버에 보낼 값을 저장
        Search_Json jsonvalue; // json추출을 위한 객체


        //적용할 url, 나타낼 ui textview, JSONOBject값 입력해 사용 생성자
        HTTPThread(String m_url, TextView m_keyvalue, JSONObject m_values) {
            url = m_url;
            this.setText = m_keyvalue;
            this.value = m_values;

        }
        HTTPThread(String m_url, JSONObject m_values) {
            url = m_url;
            this.value = m_values;

        }

        //쓰레드 실행시 실행
        public void run() {

            NetworkTask networkTask = new NetworkTask(url, value); //url과 json값을 넘겨주어서 rest api 연결 객체 생성
            networkTask.execute(); //실행
        }

        //연결을위한 클래스
        public class NetworkTask extends AsyncTask<Void, Void, String> {

            private String url; //url저장할 생성자
            private JSONObject values; //json값 저장할 변수

            //생성자
            public NetworkTask(String url, JSONObject values) {

                this.url = url;
                this.values = values;
            }

            @Override
            protected String doInBackground(Void... params) {

                String result; // 요청 결과를 저장할 변수.
                RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection(); //RequestHttpURLConnection.java 파일의 객체 생성
                result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.

                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                Search_Json sj = new Search_Json();
                List ss = sj.Parsing_Response(s,"attachdevice");
                String a = String.valueOf(ss);
                super.onPostExecute(s);
                TextView test = (TextView)findViewById(R.id.networkID);
                test.setText(a);
            }
        }


    }


}
