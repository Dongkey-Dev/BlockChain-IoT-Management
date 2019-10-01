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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class user_list_fragment extends ListFragment {

    ListView listview; //리스트뷰 객체 생성
    ListViewuserAdapter adapter; //어댑터 생성
    String networkname;
    HTTPThread httpThread;
    JSONObject jsonObject;
    View v;

    public user_list_fragment() {

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.user_list_fragment, container, false);

        String url = "http://192.168.0.13:8888/v1/history/get_actions";
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.user_toolbar1);
        AppCompatActivity activity = (AppCompatActivity) getActivity();

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


        //리스트뷰 추가, 어댑터추가
        //사용자 목록을 받아 리스트뷰로 나타내는 코드 구현
        adapter = new ListViewuserAdapter();
        setListAdapter(adapter);


        adapter.notifyDataSetChanged();

        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return v;
    }

    //설정 이미지 추가 및 누르면 삭제버튼나오게
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.toolbar,menu);
    }




    // user목록을 사용하게 쓰레드 수정하기
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

                List<String> result = new ArrayList<>();
                Search_Json sj = new Search_Json();
                result = sj.Recent_user_device(s,"recentuser");

                for (int i = 0 ; i < result.size() ; i++) {
                    username = result.get(i);

                    adapter.addItem(ContextCompat.getDrawable(getActivity(), R.drawable.user), username);
                    adapter.notifyDataSetChanged();
                }
                super.onPostExecute(s);


                TextView controllist = (TextView)v.findViewById(R.id.nousercontrollist);
                controllist.setVisibility(View.GONE);
            }
        }


    }
    public class ListViewuserAdapter extends BaseAdapter {
        // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
        private ArrayList<ListViewuserItem> listViewItemList = new ArrayList<ListViewuserItem>() ;

        // ListViewAdapter의 생성자
        public ListViewuserAdapter() {

        }

        // Adapter에 사용되는 데이터의 개수를 리턴
        @Override
        public int getCount() {
            return listViewItemList.size() ;
        }

        // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            final Context context = parent.getContext();

            // "listview_item" Layout을 inflate하여 convertView 참조 획득.
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_useritem_simple, parent, false);
            }

            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            ImageView iconImageView = (ImageView) convertView.findViewById(R.id.user_image) ;
            TextView titleTextView = (TextView) convertView.findViewById(R.id.user_text) ;

            // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
            ListViewuserItem listViewItem = listViewItemList.get(position);

            // 아이템 내 각 위젯에 데이터 반영
            iconImageView.setImageDrawable(listViewItem.getIcon());
            titleTextView.setText(listViewItem.getTitle());

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

        // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
        public void addItem(Drawable icon, String title) {
            ListViewuserItem item = new ListViewuserItem();

            item.setIcon(icon);
            item.setTitle(title);

            listViewItemList.add(item);
        }
        //cursor를 이용해서 리스트뷰에 아이템 추가
        public void addItem(Drawable icon, Cursor cursor) {

            while (cursor.moveToNext()) {
                ListViewuserItem item = new ListViewuserItem();
                item.setIcon(icon);
                item.setTitle(cursor.getString(1));

                listViewItemList.add(item);
            }
            cursor.close();
        }
    }

}
