package d.somewheres.uieosio;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
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

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

//IoT관리 페이지의 액티비티이다.
public class IoTdeviceManageActivity extends AppCompatActivity {

    //IoT폼을 위한 리스트뷰와 어댑터 JAVA파일을 생성해야한다.
    ListView listview; //리스트뷰 객체 생성
    ListViewAdapterIoT adapter; //어댑터 생성
    String networkname;
    private DatabaseHelper DatabaseHelper; //데이터베이스 객체
    SSHThread sshThread; //ssh를 사용할 쓰레드 선언
    HTTPThread httpThread;
    String p_key;
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
        adapter.addItem(ContextCompat.getDrawable(IoTdeviceManageActivity.this, R.drawable.iot),cursor);
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
                String url = "http://192.168.0.12:8888/v1/chain/get_account"; //rest api rul을 지정
                //버튼 클릭시 ioT 이름을 텍스트창에서 가져온다
                EditText iotcreate = (EditText)findViewById(R.id.iotcreate);
                String iotname = iotcreate.getText().toString();

                JSONObject jsonObject = new JSONObject();

                try {
                    jsonObject.put("account_name",networkname); //post할 값을 넣어준다
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


                //SSH를 이용해  iOt장치를 생성한다
                sshThread = new SSHThread("cleos create account eosio " + iotname + " " + p_key + " " + p_key);
                sshThread.start();
                try {
                    sshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //ssh를 이용해 iot장치를 추가한다.
                sshThread = new SSHThread("cleos push action " + networkname + " attachdevice [\"" + iotname + "\"] -p " + iotname + "@active" );
                sshThread.start();
                try {
                    sshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //목록은 리스트뷰에 추가한다
                TextView IoTNolist = (TextView)findViewById(R.id.IoTNolist);
                IoTNolist.setVisibility(View.GONE);
                adapter.addItem(ContextCompat.getDrawable(IoTdeviceManageActivity.this, R.drawable.iot), iotname);
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
    public class SSHThread extends Thread {
        Context context;
        String Command;
        String result;
        int trigger=0; //서브스트링을 구분하기위한 트리거, 0이면 기본 명령값

        //텍스트뷰를 나타내기 위한 생성자


        //명령어를 단순 실행하기위한 생성자
        SSHThread(String m_command) {
            Command = m_command;
        }

        SSHThread(int trigger,String m_command) {
            this.trigger = trigger;
            Command = m_command;
        }




        public void startcommand() {
            ArrayList totalmsg = null;

            // String command1 = "ls"; // 여기안에 입력하고자 하는 EOS 명령어
            try {
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                JSch jsch = new JSch();
                // Create a JSch session to connect to the server
                Session session = jsch.getSession("gpc", "192.168.0.12", 22); //host:ip주소
                session.setPassword("1q2w3e4r");
                session.setConfig(config);
                // Establish the connection
                session.connect();
                System.out.println("Connected...");

                ChannelExec channel = (ChannelExec) session
                        .openChannel("exec");
                channel.setCommand(Command);
                channel.setErrStream(System.err);


                InputStream in = channel.getInputStream();
                System.out.println(in);
                channel.connect();
                byte[] tmp = new byte[1024];
                while (true) {
                    while (in.available() > 0) {
                        int i = in.read(tmp, 0, 1024);
                        if (i < 0) {
                            break;
                        }
                        System.out.print(new String(tmp, 0, i));
                        this.result = new String(tmp, 0, i);
                    }
                    if (channel.isClosed()) {
                        System.out.println("Exit Status: "
                                + channel.getExitStatus());
                        break;
                    }
                    Thread.sleep(1000);
                }
                channel.disconnect();
                session.disconnect();
                System.out.println("DONE!!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (trigger == 0) {
                //명령어만 입력시 실행만
            }
        }
        public void run() {
            startcommand();
        }
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
                String ss = sj.Get_Accout_Public_key(s);
                super.onPostExecute(ss);
                p_key = ss;
            }
        }


    }

}
