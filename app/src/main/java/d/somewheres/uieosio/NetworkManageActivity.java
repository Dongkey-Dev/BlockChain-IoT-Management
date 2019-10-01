package d.somewheres.uieosio;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
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
    JSONObject jsonObject;
    HTTPThread httpThread;
    DatabaseHelper databaseHelper;
    int frag=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_manage);



        //rest api 가져올 url 세팅
        String url = "http://192.168.0.13:8888/v1/history/get_actions";
        //메인액티비티에서 보낸 값을 받는다.
        Intent intent = getIntent();
        networkname = intent.getStringExtra("networkname");
        account = intent.getStringExtra("account");


        if(account.equals("사용자")) {
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
        }
        switchFragment1();
        iot_list_fragment fragment = new iot_list_fragment();
        Bundle bundle = new Bundle(1);
        bundle.putString("networkname",networkname);
        fragment.setArguments(bundle);

        invalidateOptionsMenu();
        Button iot_list_fragment_button = (Button) findViewById(R.id.btn_IoTmanage);
        Button user_list_fragment_button = (Button) findViewById(R.id.btn_usermanage);


        iot_list_fragment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchFragment1();
                frag=0;
            }
        });
        user_list_fragment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchFragment2();
                frag=1;
            }
        });



    }


    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.toolbar2, menu);
        if(account.equals("사용자")) {
            menu.findItem(R.id.toolbar_plus_button_imamge).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.toolbar_plus_button) {
            if(frag == 0) {
                Intent intent = new Intent(getApplicationContext(), IoT_Select_Activity.class);
                intent.putExtra("networkname", networkname);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getApplicationContext(), user_Join_accept_Activity.class);
                intent.putExtra("networkname", networkname);
                startActivity(intent);
            }
            //확인버튼 클릭시
            return true;
        }

        if (id == R.id.toolbar_setting_button) {
            if(frag == 0) {
                Intent intent = new Intent(getApplicationContext(), IoT_manage_Activity.class);
                intent.putExtra("networkname", networkname);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getApplicationContext(), userManageActivity.class);
                intent.putExtra("networkname", networkname);
                startActivity(intent);
            }
            //확인버튼 클릭시
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //리스트뷰 수정해야한다!!!!!!!!!!!!!!!!
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

    public void switchFragment1() {
        Fragment fr;
        fr = new iot_list_fragment();
        Bundle bundle = new Bundle(1);
        bundle.putString("networkname",networkname);
        fr.setArguments(bundle);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.user_iot_change, fr);
        fragmentTransaction.commit();
    }



    public void switchFragment2() {
        Fragment fr;
        fr = new user_list_fragment();
        Bundle bundle = new Bundle(1);
        bundle.putString("networkname",networkname);
        fr.setArguments(bundle);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.user_iot_change, fr);
        fragmentTransaction.commit();
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

            //여기에 동호가 가공한 user 추가한거만 나오는거 해야함
            @Override
            protected void onPostExecute(String s) {
                String username;
                int frag=0;
                databaseHelper = new DatabaseHelper(NetworkManageActivity.this,"eos.db",null,1);

                //(초기접속이 아닐시) 계정이 있으면 안드로이드 로컬db에 저장된 사용자 계정을 꺼내와 rest_api로 비교 키값 받아옴

                //db세팅

                String user = databaseHelper.getPersonname();
                List<String> result = new ArrayList<>();
                Search_Json sj = new Search_Json();
                result = sj.Recent_user_device(s,"recentuser");

                for (int i = 0 ; i < result.size() ; i++) {
                    username = result.get(i);
                    if(user.equals(username)){
                        frag=1;
                    }

                }
                if(frag==0) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NetworkManageActivity.this);

                    alertDialogBuilder.setTitle("알림");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setMessage("권한이없습니다. 관리자에게 요청하세요")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    finish();
                                }
                            });
                    alertDialogBuilder.show();


                }

                super.onPostExecute(s);

            }
        }


    }

}
