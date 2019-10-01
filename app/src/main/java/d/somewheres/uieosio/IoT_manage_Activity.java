package d.somewheres.uieosio;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class IoT_manage_Activity extends AppCompatActivity {

    ListView listview;
    ListViewAdapterIoT adapter;
    String networkname;
    SSHThread sshThread;
    HTTPThread httpThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iot_manage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.iot_toolbar_manage);
        Intent intent = getIntent();
        networkname = intent.getStringExtra("networkname");

        String url = "http://192.168.0.13:8888/v1/history/get_actions";

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
        adapter = new ListViewAdapterIoT();
        listview = (ListView) findViewById(R.id.ioT_listview);
        adapter.notifyDataSetChanged();
        listview.setAdapter(adapter);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

            //여기에 iot이름,port, ip를 가져오게 해야한다 전체 수정해야함.
            @Override
            protected void onPostExecute(String s) {
                String iottitle;

                List<String> result = new ArrayList<>();
                Search_Json sj = new Search_Json();
                result = sj.Recent_user_device(s,"recentdevice");

                for (int i = 0 ; i < result.size() ; i++) {
                    iottitle = result.get(i);

                    adapter.addItem(ContextCompat.getDrawable(IoT_manage_Activity.this, R.drawable.remote), iottitle);
                    adapter.notifyDataSetChanged();
                }
                super.onPostExecute(s);



                super.onPostExecute(s);
            }
        }
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
                Session session = jsch.getSession("gpc", "192.168.0.13", 22); //host:ip주소
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
    public class ListViewAdapterIoT extends BaseAdapter {
        // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
        private ArrayList<ListViewIoTItem> listViewItemList = new ArrayList<ListViewIoTItem>();

        // ListViewAdapter의 생성자
        public ListViewAdapterIoT() {

        }

        // Adapter에 사용되는 데이터의 개수를 리턴
        @Override
        public int getCount() {
            return listViewItemList.size();
        }

        // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            final Context context = parent.getContext();

            // "listview_item" Layout을 inflate하여 convertView 참조 획득.
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_iotitem, parent, false);
            }

            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView2);
            TextView titleTextView = (TextView) convertView.findViewById(R.id.textView3);

            // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
            ListViewIoTItem listViewItem = listViewItemList.get(position);

            // 아이템 내 각 위젯에 데이터 반영
            iconImageView.setImageDrawable(listViewItem.getIcon());
            titleTextView.setText(listViewItem.getTitle());
            Button button1 = (Button) convertView.findViewById(R.id.iotbutton);

            button1.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    ListViewIoTItem count = listViewItemList.get(pos);
                    listViewItemList.remove(count);
                    String deletename = count.getTitle();
                    //ssh를 이용해 iot장치를 추가한다.
                    sshThread = new SSHThread("cleos push action " + networkname + " removedevice [\"" + deletename + "\"] -p " + deletename + "@active" );
                    sshThread.start();
                    try {
                        sshThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    adapter.notifyDataSetChanged();
                }
            });
            return convertView;
        }

        // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
        @Override
        public long getItemId(int position) {
            return position;
        }

        // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
        @Override
        public Object getItem(int position) {
            return listViewItemList.get(position);
        }

        // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
        public void addItem(Drawable icon, String title) {
            ListViewIoTItem item = new ListViewIoTItem();

            item.setIcon(icon);
            item.setTitle(title);

            listViewItemList.add(item);
        }

        //cursor를 이용해서 리스트뷰에 아이템 추가
        public void addItem(Drawable icon, Cursor cursor) {

            while (cursor.moveToNext()) {
                ListViewIoTItem item = new ListViewIoTItem();
                item.setIcon(icon);
                item.setTitle(cursor.getString(1));

                listViewItemList.add(item);
            }
            cursor.close();
        }
    }

}
