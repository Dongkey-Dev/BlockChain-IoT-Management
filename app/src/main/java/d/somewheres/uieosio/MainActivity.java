package d.somewheres.uieosio;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

//첫번쨰 메인액티비티, 네트워크 생성과 참여버튼이있다.
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BackPressCloseHandler backPressCloseHandler;
    Context context;
    ListView listview; //리스트뷰 객체 생성
    ListViewAdapter adapter; //어댑터 생성
    SSHThread sshThread; //ssh를 사용할 쓰레드 선언
    JSONObject jsonObject; //jsonObject형식을 저장할수 있는 변수{키:값}
    private DatabaseHelper DatabaseHelper; //데이터베이스 객체
    String tmpdata1, tmpdata2; //네트워크 연결로 받아온 데이터를 저장할 변수
    String username;
    String account;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //데이터베이스 작업을 도와주는 객체 생성, db이름은 eos
        File file = new File("/data/data/d.somewheres.uieosio/databases/eos.db");

        // 바탕화면을클릭시 꺼지는 거 고쳐야함
        // 데이터베이스에 존재하지 않으면 실행
        if (!file.exists()) {
            Intent intent = new Intent(getApplicationContext(),Create_Account_Activity.class);
            startActivity(intent);
        } else {
            DatabaseHelper = new DatabaseHelper(MainActivity.this,"eos.db",null,1);

            //(초기접속이 아닐시) 계정이 있으면 안드로이드 로컬db에 저장된 사용자 계정을 꺼내와 rest_api로 비교 키값 받아옴

            //db세팅

            String username = DatabaseHelper.getPersonname();
            String userkey = DatabaseHelper.getPersonkey();
            String userpassword = DatabaseHelper.getPersonpassword();

            //화면에보이는 유저이름과 공용키 설정(오류로 다시...)
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);



            setContentView(R.layout.activity_main);
            //ssh로 지갑 열기
            sshThread = new SSHThread("cleos wallet unlock --name " + username + " --password " + userpassword);
            sshThread.start();
            try {
                sshThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            adapter = new ListViewAdapter();
            listview = (ListView) findViewById(R.id.listview1);

            Cursor cursor = DatabaseHelper.networkitem();

            adapter.addItem(ContextCompat.getDrawable(MainActivity.this, R.drawable.network),cursor);
            if(cursor.getCount() > 0) {
                TextView NetworkNolist = (TextView)findViewById(R.id.NetworkNolist);
                NetworkNolist.setVisibility(View.GONE);
            }
            adapter.notifyDataSetChanged();
            listview.setAdapter(adapter);
            //아이템 클릭시 그 네트워크의 ioT및 사용자를 관리할수있는 액티비티로 전환 및 값을 넘겨준다
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getApplicationContext(), NetworkManageActivity.class);
                    intent.putExtra("name",adapter.getname(i));
                    intent.putExtra("account",account);


                    startActivity(intent);
                }
            });
        }

        //db에서 네트워크목록을 가져오는파트
        //네트워크 목록을 나타내기위한 리스트뷰 및 어댑터 장착



        //메인액티비티의 제목 및 네비게이션을 할수있는 상단의 툴바
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        backPressCloseHandler = new BackPressCloseHandler(this);

        if(file.exists()) {
            DatabaseHelper = new DatabaseHelper(MainActivity.this,"eos.db",null,1);

            String username = DatabaseHelper.getPersonname();
            String userkey = DatabaseHelper.getPersonkey();

            View nav_header_view = navigationView.getHeaderView(0);
            TextView title = (TextView)nav_header_view.findViewById(R.id.username);
            TextView keyvalue = (TextView)nav_header_view.findViewById(R.id.keyvalue);
            title.setText(username);
            keyvalue.setText(userkey);
        }

    }


    //아래 내용은 네비게이션에 관한것
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            backPressCloseHandler.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.toolbar_plus_button) {
            Intent intent = new Intent(getApplicationContext(), Network_Create_Activity.class);
            startActivity(intent);

            //확인버튼 클릭시
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

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
            } else if (trigger == 1) {
                //지갑을 생성하는 명령어, 패스워드 리턴
                tmpdata1 = result.substring(133 + username.length(), 186 + username.length());
            } else if (trigger == 2) {
                //키를 생성해 프라이빗키와 퍼블릭키를 리턴
                tmpdata1 = result.substring(12, 66);
                tmpdata2 = result.substring(77, 130);
            }
        }
        public void run() {
                startcommand();
        }
    }
}