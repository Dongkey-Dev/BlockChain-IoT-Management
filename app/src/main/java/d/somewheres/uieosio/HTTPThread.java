package d.somewheres.uieosio;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

//rest api를 사용하기 위한 http 쓰레드
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

    //보안관계상 사용하는 핸들러
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            setText.setText(result);
        }
    };

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
            super.onPostExecute(s);
            String trans = jsonvalue.Get_Accout_Public_key(s);
            setText.setText(trans);
        }
    }


}
