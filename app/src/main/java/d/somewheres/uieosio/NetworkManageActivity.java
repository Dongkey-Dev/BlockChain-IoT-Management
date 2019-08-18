package d.somewheres.uieosio;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NetworkManageActivity extends AppCompatActivity {

    ListView listview; //리스트뷰 객체 생성
    ListViewAdapterIoTtime adapter; //어댑터 생성
    String networkname; //네트워크 이름을 받아온다.
    String account;
    HTTPThread httpThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_manage);

        //rest api 가져올 url 세팅
        String url = "http://192.168.0.13:8888/v1/history/get_actions";
        //메인액티비티에서 보낸 값을 받는다.
        Intent intent = getIntent();

        //툴바 제목에 리스트뷰에 클릭한 네트워크이름 보여줌
        TextView networktitle = (TextView)findViewById(R.id.toolbar_title2);
        networkname = intent.getStringExtra("name");
        account = intent.getStringExtra("account");
        networktitle.setText(intent.getStringExtra("name"));

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("pos","-1"); //post할 값을 넣어준다
            jsonObject.put("offset","-20");
            jsonObject.put("account_name","network55");
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
        // 여기 리스트뷰에서는 최근 조작한 IoT장치 값 받아서 나타냄(미완)
        adapter = new ListViewAdapterIoTtime();
        listview = (ListView) findViewById(R.id.listview2);
        listview.setAdapter(adapter);
        //아이템 클릭시 iot장치의 이름 넘겨준다.
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), IoTinfoActivity.class);
                intent.putExtra("name",adapter.getname(i));
                intent.putExtra("networkname",networkname);


                startActivity(intent);
            }
        });


        //iot 관리 파트
        Button button = (Button) findViewById(R.id.btn_IoTmanage);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),IoTdeviceManageActivity.class);
                //여기서도 인탠트 메인메뉴 타이틀을 넘겨줘야함.
                //iot참가 명령어를 보내기위해 네트워크 이름을 다음 액티비티로 전송
                intent.putExtra("name",networkname);
                startActivity(intent);

            }
        });
        Button button2 = (Button) findViewById(R.id.btn_usermanage);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),userManageActivity.class);
                //여기서도 인탠트 메인메뉴 타이틀을 넘겨줘야함.
                //user참가 명령어를 보내기위해 네트워크 이름을 다음 액티비티로 전송
                intent.putExtra("name",networkname);
                startActivity(intent);

            }
        });

        if(account == "사용자") {
            button.setVisibility(View.GONE);
            button2.setVisibility(View.GONE);
        }

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


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

            @Override
            protected void onPostExecute(String s) {
                String iottitle;
                String iotdata;
                String iottime;
                List<String> device = new ArrayList<>();
                HashMap<String, List<List>> result = new HashMap<String, List<List>>();
                Search_Json sj = new Search_Json();
                result = sj.Lookup_device_detail(s);
                device.addAll(result.keySet());
                for (int i = 0 ; i < result.keySet().size() ; i++) {
                    iottitle = device.get(i);
                    iotdata = result.get(device.get(i)).get(0).get(0).toString();
                    iottime = result.get(device.get(i)).get(0).get(1).toString();
                    String year = iottime.substring(0,10);
                    String time = iottime.substring(11,19);
                    String sum1 = year + "  " + time;
                    adapter.addItem(ContextCompat.getDrawable(NetworkManageActivity.this, R.drawable.remote), iottitle, iotdata, sum1);
                    adapter.notifyDataSetChanged();
                }
                super.onPostExecute(s);


                TextView controllist = (TextView)findViewById(R.id.controllist);
                controllist.setVisibility(View.GONE);


            }
        }


    }
    //리스트뷰에 값을 넣어줄수 있도록 도와주는 어댑터이다.
    public class ListViewAdapterIoTtime extends BaseAdapter {
        // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
        private ArrayList<ListViewIoTtime> listViewItemList = new ArrayList<ListViewIoTtime>() ;

        // ListViewAdapter의 생성자
        public ListViewAdapterIoTtime() {

        }

        // Adapter에 사용되는 데이터의 개수를 리턴
        @Override
        public int getCount() {
            return listViewItemList.size() ;
        }

        // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position; //클릭한 리스트의 위치
            final Context context = parent.getContext(); //부모 컨택스트를 얻어와 뷰를 사용

            // "listview_item" Layout을 inflate하여 convertView 참조 획득.
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_iottime, parent, false);
            }

            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            ImageView iconImageView = (ImageView) convertView.findViewById(R.id.iotimage) ;
            TextView titleTextView = (TextView) convertView.findViewById(R.id.iottitle) ;
            TextView descTextView = (TextView) convertView.findViewById(R.id.iotdata) ;
            TextView timeTextView = (TextView) convertView.findViewById(R.id.iottime) ;

            // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
            ListViewIoTtime listViewItem = listViewItemList.get(position);

            // 아이템 내 각 위젯에 데이터 반영
            iconImageView.setImageDrawable(listViewItem.getIconDrawable());
            titleTextView.setText(listViewItem.getTitleIoT());
            descTextView.setText(listViewItem.getData());
            timeTextView.setText(listViewItem.getTime());

            return convertView;
        }

        // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
        @Override
        public long getItemId(int position) {
            return position ;
        }

        // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
        @Override
        public Object getItem(int position) {
            return listViewItemList.get(position) ;
        }

        //리스트뷰 지정된 위치에있는 이름을 얻어올수 있다.
        public String getname(int position) { return listViewItemList.get(position).getTitleIoT();}
        // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
        public void addItem(Drawable icon, String title, String desc, String time) {
            ListViewIoTtime item = new ListViewIoTtime();

            item.setIconDrawable(icon);
            item.setTitleIoT(title);
            item.setData(desc);
            item.setTime(time);

            listViewItemList.add(item);
        }

        //cursor를 이용해서 리스트뷰에 아이템 추가
        public void addItem(Drawable icon, Cursor cursor) {

            while (cursor.moveToNext()) {
                ListViewIoTtime item = new ListViewIoTtime();
                item.setIconDrawable(icon);
                item.setTitleIoT(cursor.getString(1));
                item.setData(cursor.getString(2));
                item.setTime(cursor.getString(3));


                listViewItemList.add(item);
            }
            cursor.close();
        }
    }

}
