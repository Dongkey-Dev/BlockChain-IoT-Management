package d.somewheres.uieosio;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Network_join_fragment extends Fragment {
    String account;
    DatabaseHelper databaseHelper;
    JSONObject jsonObject;
    HTTPThread httpThread;
    String name;

    public Network_join_fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.network_join_fragment,container,false);
        EditText joinnetworktext = (EditText) v.findViewById(R.id.join_network_text);
        Button cencelbutton = (Button) v.findViewById(R.id.btn_networkcancel2);
        Button joinbutton = (Button) v.findViewById(R.id.btn_networkjoin);
        String url = "http://192.168.0.13:8888/v1/chain/get_account";


        //리스너에 getaccount로 네트워크 계정이 있는지 파악한 뒤, 있으면 생성, 없으면 오류 (구현해야한다)
        joinbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                databaseHelper = new DatabaseHelper(getActivity(),"eos.db",null,1);
                account = "사용자";

                name = joinnetworktext.getText().toString();

                jsonObject = new JSONObject();
                System.out.println("테스트" + name);
                try {
                    jsonObject.put("account_name",name);
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
            }
        });
        cencelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });


        return v;
    }

    // iot 목록 거른거 json 사용하기 수정필요
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

            //여기에 getaccount로 네트워크 계정이있는지 파악한당
            @Override
            protected void onPostExecute(String s) {
                String tmp= null;
                Search_Json sj = new Search_Json();
                System.out.println("테스트 :" + s);
                tmp = sj.Get_Account_ID(s);
                System.out.println("테스트: " + sj.Get_Account_ID(s));
                if(name.equals(tmp)) {
                    //db에 추가
                    Network network = new Network();
                    network.setName(name);
                    network.setAccount(account);
                    databaseHelper.addNetworklist(network);

                    Intent intent = new Intent(getActivity(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    //동일한 네트워크가 없다고 알림
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                    alertDialogBuilder.setTitle("알림");
                    alertDialogBuilder.setMessage("해당되는 네트워크가 없습니다.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                    alertDialogBuilder.show();
                }
                super.onPostExecute(s);

            }
        }


    }
}
