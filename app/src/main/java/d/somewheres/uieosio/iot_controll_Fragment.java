package d.somewheres.uieosio;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class iot_controll_Fragment extends Fragment {
    private String TAG = "Control";

    private static final int MSG_CONNECT_SUCCESS = 0;
    private static final int MSG_CONNECT_FAILURE = 1;
    private static final String CMD_TOGGLE = "{\"id\":%id,\"method\":\"toggle\",\"params\":[]}\r\n" ;
    private static final String CMD_ON = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"on\",\"smooth\",500]}\r\n" ;
    private static final String CMD_OFF = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"off\",\"smooth\",500]}\r\n" ;
    private static final String CMD_CT = "{\"id\":%id,\"method\":\"set_ct_abx\",\"params\":[%value, \"smooth\", 500]}\r\n";
    private static final String CMD_HSV = "{\"id\":%id,\"method\":\"set_hsv\",\"params\":[%value, 100, \"smooth\", 200]}\r\n";
    private static final String CMD_BRIGHTNESS = "{\"id\":%id,\"method\":\"set_bright\",\"params\":[%value, \"smooth\", 200]}\r\n";
    private static final String CMD_BRIGHTNESS_SCENE = "{\"id\":%id,\"method\":\"set_bright\",\"params\":[%value, \"smooth\", 500]}\r\n";
    private static final String CMD_COLOR_SCENE = "{\"id\":%id,\"method\":\"set_scene\",\"params\":[\"cf\",1,0,\"100,1,%color,1\"]}\r\n";


    View v;
    SSHThread sshThread;
    HTTPThread httpThread;
    String networkname;
    String name;
    JSONObject jsonObject;
    Yeelight_Launchar yeelight_launchar = new Yeelight_Launchar();
    Context context;

    write write;
    private int mCmdId;
    private Socket mSocket;
    private String mBulbIP;
    private int mBulbPort;
    private ProgressDialog mProgressDialog;
    private SeekBar mBrightness;
    private SeekBar mCT;
    private SeekBar mColor;
    private Button mBtnOn;
    private Button mBtnOff;
    private Button mBtnMusic;
    private BufferedOutputStream mBos;
    private BufferedReader mReader;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_CONNECT_FAILURE:
                    mProgressDialog.dismiss();
                    break;
                case MSG_CONNECT_SUCCESS:
                    mProgressDialog.dismiss();
                    break;
            }
        }
    };


    //rest api로 블록에서 ip랑 port를 얻어와야한다.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.iot_control_fragment,container,false);
        context = getActivity();
        if(getArguments() != null) {
            //네트워크 이름을 파라메터로 받음
            name = getArguments().getString("name");
            networkname = getArguments().getString("networkname");
        }

        String url = "http://192.168.0.13:8888/v1/history/get_actions";
        jsonObject = new JSONObject();

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
        System.out.println("테스트" + mBulbIP);
        System.out.println("테스트" + mBulbPort);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("Connecting...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        mBrightness = (SeekBar) v.findViewById(R.id.brightness);
        mColor = (SeekBar) v.findViewById(R.id.color);
        mCT = (SeekBar) v.findViewById(R.id.ct);
        mCT.setMax(4800);
        mColor.setMax(360);
        mBrightness.setMax(100);

        mBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                write = new write(parseBrightnessCmd(seekBar.getProgress()));
                write.start();
                try {
                    yeelight_launchar.yeelight_launchar(parseBrightnessCmd(seekBar.getProgress()),networkname,name,context);
                } catch (ParseException e) {
                    System.out.println("예외발생");
                    e.printStackTrace();;
                }
            }
        });
        mCT.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                write = new write(parseCTCmd(seekBar.getProgress() + 1700));
                write.start();;
                try {
                    yeelight_launchar = new Yeelight_Launchar();
                    yeelight_launchar.yeelight_launchar(parseCTCmd(seekBar.getProgress() + 1700),networkname,name,context);
                } catch (ParseException e) {
                    System.out.println("예외발생");
                    e.printStackTrace();;
                }
            }
        });
        mColor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                write = new write(parseColorCmd(seekBar.getProgress()));
                write.start();
                try {
                    yeelight_launchar.yeelight_launchar(parseColorCmd(seekBar.getProgress()),networkname,name,context);
                } catch (ParseException e) {
                    System.out.println("예외발생");
                    e.printStackTrace();;
                }
            }
        });
        mBtnOn = (Button) v.findViewById(R.id.btn_on);
        mBtnOff = (Button) v.findViewById(R.id.btn_off);
        mBtnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                write = new write(parseSwitch(true));
                write.start();
                try {
                    yeelight_launchar.yeelight_launchar(parseSwitch(true),networkname,name,context);
                } catch (ParseException e) {
                    System.out.println("예외발생");
                    e.printStackTrace();;
                }
            }
        });
        mBtnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                write = new write(parseSwitch(false));
                write.start();
                write = new write(parseSwitch(false));
                write.start();
                try {
                    yeelight_launchar.yeelight_launchar(parseSwitch(true),networkname,name,context);
                } catch (ParseException e) {
                    System.out.println("예외발생");
                    e.printStackTrace();;
                }
            }
        });


        return v;
    }

    private boolean cmd_run = true;
    private void connect(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cmd_run = true;
                    mSocket = new Socket(mBulbIP,mBulbPort);
                    mSocket.setKeepAlive(true);
                    mBos= new BufferedOutputStream(mSocket.getOutputStream());
                    mHandler.sendEmptyMessage(MSG_CONNECT_SUCCESS);
                    mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    while (cmd_run){
                        try {
                            String value = mReader.readLine();
                            Log.d(TAG, "value = "+value);
                        }catch (Exception e){

                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(MSG_CONNECT_FAILURE);
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
            cmd_run = false;
            if (mSocket!=null)
                mSocket.close();
        }catch (Exception e){

        }

    }
    private String parseSwitch(boolean on){
        String cmd;
        if (on){
            cmd = CMD_ON.replace("%id", String.valueOf(++mCmdId));
        }else {
            cmd = CMD_OFF.replace("%id", String.valueOf(++mCmdId));
        }
        return cmd;
    }
    private String parseCTCmd(int ct){
        return CMD_CT.replace("%id",String.valueOf(++mCmdId)).replace("%value",String.valueOf(ct+1700));
    }
    private String parseColorCmd(int color){
        return CMD_HSV.replace("%id",String.valueOf(++mCmdId)).replace("%value",String.valueOf(color));
    }
    private String parseBrightnessCmd(int brightness){
        return CMD_BRIGHTNESS.replace("%id",String.valueOf(++mCmdId)).replace("%value",String.valueOf(brightness));
    }

    public class write extends Thread {

        String cmd;
        write(String cmd) {
            this.cmd = cmd;
        }

        //쓰레드 실행시 실행
        public void run() {
            if (mBos != null && mSocket.isConnected()){
                try {
                    mBos.write(cmd.getBytes());
                    mBos.flush();
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG,"mBos = null or mSocket is closed");
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


    public class HTTPThread extends Thread {
        Context context; //액티비티의 컨택스트를 저장할 변수 현재 사용 x
        String url; //url을 저장할 변수
        String result; //결과값을 저장해 리턴하기위한 변수
        TextView setText; //값을 나타낼 텍스트뷰 id 값
        org.json.JSONObject value; //json으로 블록체인 서버에 보낼 값을 저장
        Search_Json jsonvalue; // json추출을 위한 객체


        //적용할 url, 나타낼 ui textview, JSONOBject값 입력해 사용 생성자
        HTTPThread(String m_url, TextView m_keyvalue, org.json.JSONObject m_values) {
            url = m_url;
            this.setText = m_keyvalue;
            this.value = m_values;

        }
        HTTPThread(String m_url, org.json.JSONObject m_values) {
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
            private org.json.JSONObject values; //json값 저장할 변수

            //생성자
            public NetworkTask(String url, org.json.JSONObject values) {

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

                List<String> result = new ArrayList<>();
                Search_Json sj = new Search_Json();
                result = sj.Device_Ip_Port(s,name);
                mBulbIP = result.get(0);
                mBulbPort = Integer.parseInt(result.get(1));
                super.onPostExecute(s);
                System.out.println("테스트:" + mBulbIP + mBulbPort);
                connect();

            }
        }


    }

}
