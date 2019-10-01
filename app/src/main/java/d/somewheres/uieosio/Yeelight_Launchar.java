package d.somewheres.uieosio;

import android.content.Context;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class Yeelight_Launchar {

    SSHThread sshThread;
    DatabaseHelper databaseHelper;

    public void yeelight_launchar(String jsonparse,String networkname, String name, Context context)  throws ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObj = (JSONObject) jsonParser.parse(jsonparse);
        Yeelight_Parser yeelight_parser = new Yeelight_Parser();
        String input_data = yeelight_parser.parser(jsonObj);
        databaseHelper = new DatabaseHelper(context,"eos.db",null,1);

        //(초기접속이 아닐시) 계정이 있으면 안드로이드 로컬db에 저장된 사용자 계정을 꺼내와 rest_api로 비교 키값 받아옴

        //db세팅

        String username = databaseHelper.getPersonname();
        System.out.println("cleos push action " + networkname + " pushdata [\"" + networkname + "\",\"" + name + "\",\"" + name + "\", \"" + input_data + "\"] -p " + name + "@active");
        sshThread = new SSHThread("cleos push action " + networkname + " pushdata [\"" + networkname + "\",\"" + username + "\",\"" + name + "\",\"" + input_data + "\"] -p " + username + "@active");
        sshThread.start();
        try {
            sshThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
}