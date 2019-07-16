package d.somewheres.uieosio;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.service.autofill.TextValueSanitizer;
import android.widget.TextView;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

//SSH연결을 위한 쓰레드
public class SSHThread extends Thread {
    Context context;
    String Command;
    String result;
    TextView keyvalue;

    SSHThread(String m_command, TextView m_keyvalue) {
        Command = m_command;
        this.keyvalue = m_keyvalue;
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            keyvalue.setText(result);
        }
    };

    public void run() {
        ArrayList totalmsg = null;

        // String command1 = "ls"; // 여기안에 입력하고자 하는 EOS 명령어
        try {
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            // Create a JSch session to connect to the server
            Session session = jsch.getSession("gpc", "211.205.68.69", 22); //host:ip주소
            session.setPassword("dong2307");
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
        result = result.substring(10, 15);
        Message msg = handler.obtainMessage();
        handler.sendMessage(msg);
    }
}