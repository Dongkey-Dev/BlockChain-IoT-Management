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

public class Create_Account_Activity extends AppCompatActivity {

    private DatabaseHelper DatabaseHelper; //데이터베이스 객체
    String tmpdata1,tmpdata2;
    String username;
    SSHThread sshThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        EditText create_account_text = (EditText)findViewById(R.id.create_account_text);


        //확인버튼 클릭 시
        Button btn_create = (Button) findViewById(R.id.btn_create);
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper = new DatabaseHelper(Create_Account_Activity.this,"eos.db",null,1);
                username = create_account_text.getText().toString();

                // (초기접속시) 계정을 생성할때 ssh로 먼저 지갑을 계정이름과 같게 만들고(명령어)
                // tmp1변수에 패스워드 추출해 줘야합니다.
                sshThread = new SSHThread(1,"cleos wallet create -n " + username + " --to-console");
                sshThread.start();
                try {
                    sshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // db에 비밀번호를 저장 // 추출한 패스워드값 넣어야함
                Person person = new Person();
                person.setPassword(tmpdata1);

                // db에 저장된걸로 지갑을 먼저 연다음(명령어)
                sshThread = new SSHThread("cleos wallet unlock --name " + username + " --password " + tmpdata1);
                sshThread.start();
                try {
                    sshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 키를 발급받고(명령어) //여기서 프라이빗키(지갑에 넣기위해 얻어옴), 퍼블릭키(저장)를 받아와야한다
                sshThread = new SSHThread(2,"cleos create key --to-console");
                sshThread.start();
                try {
                    sshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String privatekey = tmpdata1;
                String publickey = tmpdata2;
                // 비공개키는 지갑에넣음(명령어) 공개키는 계정생성할때 씀(공개키는 저장)
                sshThread = new SSHThread("cleos wallet import -n "+ username + " --private-key " + privatekey);
                sshThread.start();
                try {
                    sshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // eosio 스태틱값 넣음
                sshThread = new SSHThread("cleos wallet import -n "+ username + " --private-key 5KQwrPbwdL6PhXujxW37FSSQZ1JiwsST4cqQzDeyXtP79zkvFD3");
                sshThread.start();
                try {
                    sshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //계정생성
                sshThread = new SSHThread("cleos create account eosio "+ username + " " + publickey + " " + publickey);
                sshThread.start();
                try {
                    sshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // (동시에진행)
                person.setName(username);
                person.setUserkey(tmpdata2);
                DatabaseHelper.addPerson(person); // db에 업데이트
                //intent로 메인 액티비티를 새로고침한다.
                Intent intent = new Intent(Create_Account_Activity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });
        Button btn_non = (Button) findViewById(R.id.btn_non);
        btn_non.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
