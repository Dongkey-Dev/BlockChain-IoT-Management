package d.somewheres.uieosio;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class iot_list_fragment extends ListFragment {

    View v;
    HTTPThread httpThread;
    ListViewAdapterIoTsimple adapter;
    JSONObject jsonObject;
    String networkname;
    ListView listview;

    public iot_list_fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.iot_list_fragment,container,false);
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.iot_toolbar1);
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        String url = "http://192.168.0.13:8888/v1/history/get_actions";
        if(getArguments() != null) {
            //네트워크 이름을 파라메터로 받음
            networkname = getArguments().getString("networkname");
        }
        jsonObject = new JSONObject();

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




        adapter = new ListViewAdapterIoTsimple();
        setListAdapter(adapter);


        adapter.notifyDataSetChanged();

        //아이템 클릭시 iot장치의 이름 넘겨준다.


        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        return v;
    }

    @Override
    public void onListItemClick (ListView l, View v, int position, long id) {
        ListViewIoTItem item = (ListViewIoTItem) l.getItemAtPosition(position);

        String iotname = item.getTitle();
        Intent intent = new Intent(getActivity(), Target_info_control_Activity.class);
        intent.putExtra("name",iotname);
        intent.putExtra("networkname",networkname);

        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.toolbar,menu);
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

            //여기에 iot이름,port, ip를 가져오게 해야한다 전체 수정해야함.
            @Override
            protected void onPostExecute(String s) {
                String iottitle;

                List<String> result = new ArrayList<>();
                Search_Json sj = new Search_Json();
                result = sj.Recent_user_device(s,"recentdevice");

                for (int i = 0 ; i < result.size() ; i++) {
                    iottitle = result.get(i);

                    adapter.addItem(ContextCompat.getDrawable(getActivity(), R.drawable.remote), iottitle);
                    adapter.notifyDataSetChanged();
                }
                super.onPostExecute(s);


                TextView controllist = (TextView)v.findViewById(R.id.noiotcontrollist);
                controllist.setVisibility(View.GONE);


            }
        }


    }

    public void addItem(Drawable icon, String title) {
        adapter.addItem(icon,title);
    }

}

