package d.somewheres.uieosio;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class Network_create_fragment extends Fragment {

    String account;
    SSHThread sshThread;
    String tmpdata1, tmpdata2;
    DatabaseHelper DatabaseHelper;
    String username, userkey, userpassword;

    public Network_create_fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.network_create_fragment,container,false);
        EditText createnetworktext = (EditText) v.findViewById(R.id.create_network_text);
        Button createbutton = (Button) v.findViewById(R.id.btn_networkCreate);
        createbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                account = "관리자";

                DatabaseHelper = new DatabaseHelper(getActivity(),"eos.db",null,1);
                //db세팅

                username = DatabaseHelper.getPersonname();
                userkey = DatabaseHelper.getPersonkey();
                userpassword = DatabaseHelper.getPersonpassword();

                String name = createnetworktext.getText().toString();


                //네트워크를 생성할시 ssh로 지갑을 열고(명령어)
                sshThread = new SSHThread("cleos wallet unlock --name " + username + " --password " + userpassword);
                sshThread.start();
                try {
                    sshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //네트워크 계정을 생성할 키를 발급(명령어)
                sshThread = new SSHThread(2,"cleos create key --to-console");
                sshThread.start();
                try {
                    sshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String privatekey = tmpdata1;
                String publickey = tmpdata2;

                //지갑에 키를 넣는다(명령어)
                sshThread = new SSHThread("cleos wallet import -n "+ username + " --private-key " + privatekey);
                sshThread.start();
                try {
                    sshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //네트워크 이름을 넘겨받은 계정을 생성한다(명령어)
                sshThread = new SSHThread("cleos create account eosio "+ name + " " + publickey + " " + publickey);
                sshThread.start();
                try {
                    sshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                //abi를 설정한다(명령어)
                sshThread = new SSHThread("cleos set contract " + name + " /home/gpc/contracts/polman --abi polman.abi -p " + name + "@active");
                sshThread.start();
                try {
                    sshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //db에 추가
                Network network = new Network();
                network.setName(name);
                network.setAccount(account);
                DatabaseHelper.addNetworklist(network);


                //여기에다 이제 사용자의 username을 가져와 권한을 주는 adduser를 해야한다.(관리자자신) 구현해야함


                Intent intent = new Intent(getActivity(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        return v;


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
