package d.somewheres.uieosio;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

    Context context;
    private TextView keyvalue; //네비게이션에 키값을 받아와 나타낼수있게하는 텍스트뷰 변수이다.
    ListView listview; //리스트뷰 객체 생성
    ListViewAdapter adapter; //어댑터 생성
    SSHThread sshThread; //ssh를 사용할 쓰레드 선언
    HTTPThread httpThread; //http rest api를 사용할 쓰레드 선언
    JSONObject jsonObject; //jsonObject형식을 저장할수 있는 변수{키:값}
    private DatabaseHelper DatabaseHelper; //데이터베이스 객체


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        //데이터베이스 작업을 도와주는 객체 생성, db이름은 eos
        DatabaseHelper = new DatabaseHelper(MainActivity.this,"eos.db",null,1);
        File file = new File("/data/data/d.somewheres.uieosio/databases/eos.db");

        // 바탕화면을클릭시 꺼지는 거 고쳐야함
        // 데이터베이스에 존재하지 않으면 실행
        if (!file.exists()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this); //시작할떄다이얼로그 알림
            alert.setTitle("사용자 이름 입력"); //다이얼로그의 내용
            final EditText name = new EditText(this);
            alert.setView(name);

            //확인버튼 클릭시
            alert.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    //초기설정 메인액티비티 컨택스트, 연결할 eos 체인넷, 사용자가 작성한 이름을 가져옴
                    context = getApplicationContext();
                    String url = "http://124.80.247.31:8888/v1/chain/get_account"; //rest api rul을 지정
                    String username = name.getText().toString(); //username에 사용자가 입력한 값을 얻어옴

                    // (초기접속시) 계정을 생성할때 ssh로 먼저 지갑을 계정이름과 같게 만들고(명령어)
                    // 패스워드 추출해 줘야합니다.
                    sshThread = new SSHThread("cleos wallet create --name" + username + "--to-console");
                    sshThread.start();
                    try {
                        sshThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // db에 비밀번호를 저장
                    Person person = new Person();
                    person.setName();

                    // db에 저장된걸로 지갑을 먼저 연다음(명령어)
                    sshThread = new SSHThread("cleos wallet unlock --" + username + "--password " + person.getPassword());
                    sshThread.start();
                    try {
                        sshThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // 키를 발급받고(명령어)
                    // 비공개키는 지갑에넣음(명령어) 공개키는 계정생성할때 씀(공개키는 저장)
                    // (동시에진행)


                    //네비게이션 텍스트뷰 id값을 얻어와 거기에 텍스트를 나타냄
                    TextView userID = findViewById(R.id.username);
                    userID.setText(username);

                    //키값을 얻어오기위해 뷰의 key id얻어옴
                    keyvalue = findViewById(R.id.keyvalue);
                    jsonObject = new JSONObject(); // post 요청을하기위해 json형식의 변수 생성
                    try {
                        jsonObject.put("account_name",username); //post할 값을 넣어준다
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //http 연결을 위한 쓰레드 생성
                    context = getApplicationContext();
                    httpThread = new HTTPThread(url,keyvalue,jsonObject);
                    httpThread.start(); //시작
                    try {
                        httpThread.join(); //쓰레드 끝날떄까지 멈춤
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    person.setName(name.getText().toString());
                    person.setName(keyvalue.getText().toString());
                    DatabaseHelper.addPerson(person);
                    //intent로 메인 액티비티를 새로고침한다.
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);

                    // 여기에 백엔드에 계정키를 얻어와서 데이터베이스에 저장하고 매핑하는 함수? 작성
                }
            });
            alert.show(); //알림보여줌
        } else {
            //(초기접속이 아닐시) 계정이 있으면 안드로이드 로컬db에 저장된 사용자 계정을 꺼내와 rest_api로 비교 키값 받아옴
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //네트워크 목록을 나타내기위한 리스트뷰 및 어댑터 장착
        adapter = new ListViewAdapter();
        listview = (ListView) findViewById(R.id.listview1);
        listview.setAdapter(adapter);

        //아이템 클릭시 그 네트워크의 ioT및 사용자를 관리할수있는 액티비티로 전환 및 값을 넘겨준다
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), NetworkManageActivity.class);
                intent.putExtra("name",adapter.getname(i));


                startActivity(intent);
            }
        });


        //네트워크 생성 파트 버튼클릭시
        //네트워크를 생성할시 ssh로 지갑을 열고(명령어)
        //네트워크 계정을 생성할 키를 발급(명령어)
        //지갑에 키를 넣는다(명령어)
        //계정을 생성한다(명령어)
        //abi를 설정한다(명령어)
        Button button = (Button) findViewById(R.id.btn_networkCreate);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //커스텀다이얼로그 객체 생성해 보여준다.
                CustomDialog customDialog = new CustomDialog(MainActivity.this);
                customDialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                    @Override
                    public void onPositiveClicked(String name, String desc) {
                        //확인 클릭시 리스트뷰에 각각 내용을 넣어주고 새로고침한다.
                        String account = "관리자";
                        TextView NetworkNolist = (TextView)findViewById(R.id.NetworkNolist);
                        NetworkNolist.setVisibility(View.GONE);
                        adapter.addItem(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_menu_camera), name, desc, account);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNegativeClicked() {

                    }
                });
                customDialog.show();
            }
        });


        //네트워크 참가 파트 버튼 클릭시
        //1. 관리자측에서 클라이언트 계정이름을 REST API로 GET_ACCOUNT(명령어)를해 계정이름과 공개키를얻어와 SSH로 참가시키는 명령어를 완성해 컨트랙트실행(명령어)
        //2. 참여자 클라이언트측은 일단은 참가버튼클릭시 get account(명령어)로 네트워크 계정이 존재하면 대기중으로 나타남 이후 관리자가 계정을 참여시키면 사용자로 바뀜(질의할수있는 메소드 생성할것임)
        Button button2 = (Button) findViewById(R.id.btn_networkJoin);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //커스텀다이얼로그 객체 생성
                CustomDialog customDialog = new CustomDialog(MainActivity.this);
                customDialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                    @Override
                    public void onPositiveClicked(String name, String desc) {
                        //확인버튼 클릭시 사용자로 지정된 이름의 목록 생성
                        String account = "사용자";
                        adapter.addItem(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_menu_camera), name, desc, account);

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNegativeClicked() {

                    }
                });
                customDialog.show();
            }
        });

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

    }

    //아래 내용은 네비게이션에 관한것
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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

}