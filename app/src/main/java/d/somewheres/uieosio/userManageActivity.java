package d.somewheres.uieosio;

import android.content.Context;
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

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class userManageActivity extends AppCompatActivity {

    // 사용자참가 리스트뷰와 목록 리스트뷰를 나타내는 JAVA파일을 구현해야한다.
    ListView listview; //리스트뷰 객체 생성
    ListViewuserAdapter adapter; //어댑터 생성
    String networkname;
    private DatabaseHelper DatabaseHelper; //데이터베이스 객체
    SSHThread sshThread;

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
                sshThread = new SSHThread("cleos push action " + networkname + " adduser [\"" + networkname + "\",\"" + username + "\"] -p " + networkname + "@active" );
                sshThread.start();
                try {
                    sshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

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
}