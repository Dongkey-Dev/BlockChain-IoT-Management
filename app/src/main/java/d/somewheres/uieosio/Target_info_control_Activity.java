package d.somewheres.uieosio;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Target_info_control_Activity extends AppCompatActivity {
    String networkname; //네트워크 이름을 받아온다.
    String iotname;
    String ip;
    String port;
    int frag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_info_control_);

        Intent intent = getIntent();

        networkname = intent.getStringExtra("networkname");
        iotname = intent.getStringExtra("name");

        Button iot_list_fragment_button = (Button) findViewById(R.id.btn_control_list);
        Button iot_control_fragment_button = (Button) findViewById(R.id.btn_control_device);

        switchFragment1();
        iot_list_fragment fragment = new iot_list_fragment();
        Bundle bundle = new Bundle(1);
        bundle.putString("networkname",networkname);
        fragment.setArguments(bundle);

        iot_list_fragment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchFragment1();
                frag=0;
            }
        });
        iot_control_fragment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchFragment2();
                frag=1;
            }
        });
    }

    public void switchFragment1() {
        Fragment fr;
        fr = new IoTinfoFragment();
        Bundle bundle = new Bundle(2);
        bundle.putString("networkname",networkname);
        bundle.putString("name",iotname);
        fr.setArguments(bundle);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.list_control_change, fr);
        fragmentTransaction.commit();
    }



    public void switchFragment2() {
        Fragment fr;
        fr = new iot_controll_Fragment();
        Bundle bundle = new Bundle(4);
        bundle.putString("networkname",networkname);
        bundle.putString("name",iotname);
        bundle.putString("ip",ip);
        bundle.putString("port",port);
        fr.setArguments(bundle);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.list_control_change, fr);
        fragmentTransaction.commit();
    }
}
