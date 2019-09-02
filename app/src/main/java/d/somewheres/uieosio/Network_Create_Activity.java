package d.somewheres.uieosio;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Network_Create_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_create);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.add(R.id.NetworkFragmentChange, new Network_create_fragment());
        fragmentTransaction.commit();

        Button Networkfragmentbutton1 = (Button) findViewById(R.id.NetworkfragmentButton1);
        Networkfragmentbutton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment1();
            }
        });

        Button Networkfragmentbutton2 = (Button) findViewById(R.id.NetworkfragmentButton2);
        Networkfragmentbutton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment2();
            }
        });
    }

    public void switchFragment1() {
        Fragment fr;
        fr = new Network_create_fragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.NetworkFragmentChange, fr);
        fragmentTransaction.commit();
    }

    public void switchFragment2() {
        Fragment fr;
        fr = new Network_join_fragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.NetworkFragmentChange, fr);
        fragmentTransaction.commit();
    }
}
