package d.somewheres.uieosio;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//이 액티비티는 IoT이름을 전달받아서 IoT 장비의 시간대별 조작내역을 다 나타냄.
public class IoTinfoFragment extends ListFragment {
    String IoTname;
    String networkname;
    ListView listview; //리스트뷰 객체 생성
    ListViewAdapterIoTinfo adapter; //어댑터 생성
    HTTPThread httpThread;
    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.iotinfo_fragment,container,false);
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        if(getArguments() != null) {
            //네트워크 이름을 파라메터로 받음
            networkname = getArguments().getString("networkname");
            IoTname = getArguments().getString("name");
        }


        //rest api 가져올 url 세팅
        String url = "http://192.168.0.13:8888/v1/history/get_actions";
        //IoT이름을 받음(나중에 이름에따라서 REST API로 값 추출

        JSONObject jsonObject = new JSONObject();
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

        adapter = new ListViewAdapterIoTinfo();

        //장치 내용과 시간목록을 받아서 어댑터에 추가
        //adapter.addItem());
        setListAdapter(adapter);


        adapter.notifyDataSetChanged();


        return v;


    }


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

            HTTPThread.NetworkTask networkTask = new HTTPThread.NetworkTask(url, value); //url과 json값을 넘겨주어서 rest api 연결 객체 생성
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

            @Override
            protected void onPostExecute(String s) {
                List<String> iottmp = new ArrayList<>();
                List<String> iotdata = new ArrayList<>();
                List<String> iottime = new ArrayList<>();
                HashMap<String, List> result = new HashMap<String, List>();
                Search_Json sj = new Search_Json();
                result = sj.Lookup_device_detail2(s);
                TextView controllist = (TextView)v.findViewById(R.id.iotcontrollist);

                if(result.size() != 0) {
                    iottmp = result.get(IoTname);
                    for (int i = 0; i < result.get(IoTname).size(); i++) {
                        if (i % 2 == 0) {
                            iotdata.add(iottmp.get(i));
                        } else {
                            iottime.add(iottmp.get(i));
                        }
                    }

                    for (int i = 0; i < iotdata.size(); i++) {
                        String year = iottime.get(i).substring(0, 10);
                        String time = iottime.get(i).substring(11, 19);
                        String sum1 = year + "  " + time;
                        adapter.addItem(iotdata.get(i), sum1);
                        adapter.notifyDataSetChanged();
                        controllist.setVisibility(View.GONE);
                    }
                }
                super.onPostExecute(s);


            }
        }


    }

}
