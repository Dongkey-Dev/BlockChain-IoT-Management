package d.somewheres.uieosio;

        import android.content.Context;
        import android.content.Intent;
        import android.database.Cursor;
        import android.graphics.drawable.Drawable;
        import android.net.wifi.WifiManager;
        import android.os.AsyncTask;
        import android.os.Handler;
        import android.os.Message;
        import android.support.v4.content.ContextCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.support.v7.widget.Toolbar;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.AdapterView;
        import android.widget.BaseAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.jcraft.jsch.ChannelExec;
        import com.jcraft.jsch.JSch;
        import com.jcraft.jsch.Session;

        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.InputStream;
        import java.net.DatagramPacket;
        import java.net.DatagramSocket;
        import java.net.InetAddress;
        import java.net.MulticastSocket;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Properties;

//IoT관리 페이지의 액티비티이다.
public class mijia_connect_Activity extends AppCompatActivity {

    String title;
    DatabaseHelper databaseHelper;
    SSHThread sshThread;
    HTTPThread httpThread;
    String networkname;
    String p_key;
    String account;
    private static final String TAG = "APITEST";
    private static final int MSG_SHOWLOG = 0;
    private static final int MSG_FOUND_DEVICE = 1;
    private static final int MSG_DISCOVER_FINISH = 2;
    private static final int MSG_STOP_SEARCH = 3;

    private static final String UDP_HOST = "239.255.255.250";
    private static final int UDP_PORT = 1982;
    private static final String message =
            "M-SEARCH * HTTP/1.1\r\n" +
                    "HOST:239.255.255.250:1982\r\n" +
                    "MAN:\"ssdp:discover\"\r\n" +
                    "ST:wifi_bulb\r\n";//用于发送的字符串
    private DatagramSocket mDSocket;
    private boolean mSeraching = true;
    private ListView mListView;
    private MyAdapter mAdapter;
    List<HashMap<String, String>> mDeviceList = new ArrayList<HashMap<String, String>>();
    private TextView mTextView;
    private Button mBtnSearch;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_FOUND_DEVICE:
                    mAdapter.notifyDataSetChanged();
                    break;
                case MSG_SHOWLOG:
                    Toast.makeText(mijia_connect_Activity.this, "" + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case MSG_STOP_SEARCH:
                    mSearchThread.interrupt();
                    mAdapter.notifyDataSetChanged();
                    mSeraching = false;
                    break;
                case MSG_DISCOVER_FINISH:
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    private WifiManager.MulticastLock multicastLock;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iot_device_manage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        networkname = intent.getStringExtra("networkname");
        account = "관리자";

        String url = "http://192.168.0.13:8888/v1/chain/get_account"; //rest api rul을 지정
        //버튼 클릭시 ioT 이름을 텍스트창에서 가져온다

        JSONObject jsonObject = new JSONObject();
        System.out.println("테스트 : " + networkname);
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

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        multicastLock = wm.createMulticastLock("test");
        multicastLock.acquire();
        mBtnSearch = (Button) findViewById(R.id.btn_search);
        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDevice();
            }

        });
        mListView = (ListView) findViewById(R.id.deviceList);
        mAdapter = new MyAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> bulbInfo = mDeviceList.get(position);
                Intent intent = new Intent(mijia_connect_Activity.this, NetworkManageActivity.class);
                intent.putExtra("networkname", networkname);
                intent.putExtra("account",account);
                String ipinfo = bulbInfo.get("Location").split("//")[1];
                String ip = ipinfo.split(":")[0];
                String port = ipinfo.split(":")[1];
                System.out.println("테스트 :"+ bulbInfo);
                String name = bulbInfo.get("model");

                System.out.println("테스트 :"+ name);
                //SSH를 이용해  iOt장치를 생성한다
                sshThread = new SSHThread("cleos create account eosio " + name + " " + p_key + " " + p_key);
                sshThread.start();
                try {
                    sshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("테스트:" + bulbInfo.toString());
                //ssh를 이용해 iot장치를 추가한다.
                sshThread = new SSHThread("cleos push action " + networkname + " attachdevice [\"" + name + "\",\"" + ip + "\",\"" + port + "\"] -p " + name + "@active" );
                sshThread.start();
                try {
                    sshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                databaseHelper = new DatabaseHelper(mijia_connect_Activity.this,"eos.db",null,1);
                IoT iot = new IoT();


                startActivity(intent);
            }
        });
    }
    private Thread mSearchThread = null;
    private void searchDevice() {

        mDeviceList.clear();
        mAdapter.notifyDataSetChanged();
        mSeraching = true;
        mSearchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mDSocket = new DatagramSocket();
                    DatagramPacket dpSend = new DatagramPacket(message.getBytes(),
                            message.getBytes().length, InetAddress.getByName(UDP_HOST),
                            UDP_PORT);
                    mDSocket.send(dpSend);
                    mHandler.sendEmptyMessageDelayed(MSG_STOP_SEARCH,2000);
                    while (mSeraching) {
                        byte[] buf = new byte[1024];
                        DatagramPacket dpRecv = new DatagramPacket(buf, buf.length);
                        mDSocket.receive(dpRecv);
                        byte[] bytes = dpRecv.getData();
                        StringBuffer buffer = new StringBuffer();
                        for (int i = 0; i < dpRecv.getLength(); i++) {
                            // parse /r
                            if (bytes[i] == 13) {
                                continue;
                            }
                            buffer.append((char) bytes[i]);
                        }
                        Log.d("socket", "got message:" + buffer.toString());
                        if (!buffer.toString().contains("yeelight")) {
                            mHandler.obtainMessage(MSG_SHOWLOG, "收到一条消息,不是Yeelight灯泡").sendToTarget();
                            return;
                        }
                        String[] infos = buffer.toString().split("\n");
                        HashMap<String, String> bulbInfo = new HashMap<String, String>();
                        for (String str : infos) {
                            int index = str.indexOf(":");
                            if (index == -1) {
                                continue;
                            }
                            String title = str.substring(0, index);
                            String value = str.substring(index + 1);
                            bulbInfo.put(title, value);
                        }
                        if (!hasAdd(bulbInfo)){
                            mDeviceList.add(bulbInfo);
                        }

                    }
                    mHandler.sendEmptyMessage(MSG_DISCOVER_FINISH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mSearchThread.start();

    }

    private boolean mNotify = true;
    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //DatagramSocket socket = new DatagramSocket(UDP_PORT);
                    InetAddress group = InetAddress.getByName(UDP_HOST);
                    MulticastSocket socket = new MulticastSocket(UDP_PORT);
                    socket.setLoopbackMode(true);
                    socket.joinGroup(group);
                    Log.d(TAG, "join success");
                    mNotify = true;
                    while (mNotify){
                        byte[] buf = new byte[1024];
                        DatagramPacket receiveDp = new DatagramPacket(buf,buf.length);
                        Log.d(TAG, "waiting device....");
                        socket.receive(receiveDp);
                        byte[] bytes = receiveDp.getData();
                        StringBuffer buffer = new StringBuffer();
                        for (int i = 0; i < receiveDp.getLength(); i++) {
                            // parse /r
                            if (bytes[i] == 13) {
                                continue;
                            }
                            buffer.append((char) bytes[i]);
                        }
                        if (!buffer.toString().contains("yeelight")){
                            Log.d(TAG,"Listener receive msg:" + buffer.toString()+" but not a response");
                            return;
                        }
                        String[] infos = buffer.toString().split("\n");
                        HashMap<String, String> bulbInfo = new HashMap<String, String>();
                        for (String str : infos) {
                            int index = str.indexOf(":");
                            if (index == -1) {
                                continue;
                            }
                            title = str.substring(0, index);
                            String value = str.substring(index + 1);
                            Log.d(TAG, "title = " + title + " value = " + value);
                            bulbInfo.put(title, value);
                        }
                        if (!hasAdd(bulbInfo)){
                            mDeviceList.add(bulbInfo);
                        }
                        mHandler.sendEmptyMessage(MSG_FOUND_DEVICE);
                        Log.d(TAG, "get message:" + buffer.toString());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNotify = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        multicastLock.release();
    }

    private class MyAdapter extends BaseAdapter {

        private LayoutInflater mLayoutInflater;
        private int mLayoutResource;

        public MyAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
            mLayoutResource = android.R.layout.simple_list_item_2;
        }

        @Override
        public int getCount() {
            return mDeviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            HashMap<String, String> data = (HashMap<String, String>) getItem(position);
            if (convertView == null) {
                view = mLayoutInflater.inflate(mLayoutResource, parent, false);
            } else {
                view = convertView;
            }
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText("Type = "+data.get("model") );

            Log.d(TAG, "name = " + textView.getText().toString());
            TextView textSub = (TextView) view.findViewById(android.R.id.text2);
            textSub.setText("location = " + data.get("Location"));
            return view;
        }
    }
    private boolean hasAdd(HashMap<String,String> bulbinfo){
        for (HashMap<String,String> info : mDeviceList){
            Log.d(TAG, "location params = " + bulbinfo.get("Location"));
            if (info.get("Location").equals(bulbinfo.get("Location"))){
                return true;
            }
        }
        return false;
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
                System.out.println("test" + s);
                Search_Json sj = new Search_Json();
                String ss = sj.Get_Accout_Public_key(s);
                super.onPostExecute(ss);
                p_key = ss;
            }
        }


    }
}