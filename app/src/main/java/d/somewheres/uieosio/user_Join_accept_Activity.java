package d.somewheres.uieosio;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class user_Join_accept_Activity extends AppCompatActivity {

    String tmpdata1,tmpdata2;
    String username;
    SSHThread sshThread;
    String networkname;
    String account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_join_accept);

        Intent intent = getIntent();
        networkname = intent.getStringExtra("networkname");
        account = "관리자";

        EditText join_account_text = (EditText)findViewById(R.id.join_account_EditText);

        Button btn_create = (Button) findViewById(R.id.btn_join);
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = join_account_text.getText().toString();

                //SSH를 이용해 컨트랙트 실행해 사용자를 넣는다
                sshThread = new SSHThread("cleos push action " + networkname + " adduser [\"" + networkname + "\",\"" + username + "\"] -p " + username + "@active" );
                sshThread.start();
                try {
                    sshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(user_Join_accept_Activity.this, NetworkManageActivity.class);
                intent.putExtra("networkname",networkname);
                intent.putExtra("account", account);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
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
                try {
                    tmpdata1 = result.substring(133 + username.length(), 186 + username.length());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            } else if (trigger == 2) {
                //키를 생성해 프라이빗키와 퍼블릭키를 리턴
                try {
                    tmpdata1 = result.substring(12, 66);
                    tmpdata2 = result.substring(77, 130);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        public void run() {
            startcommand();
        }
    }
}
