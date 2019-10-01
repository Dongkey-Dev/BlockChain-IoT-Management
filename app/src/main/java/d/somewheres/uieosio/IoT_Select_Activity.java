package d.somewheres.uieosio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class IoT_Select_Activity extends AppCompatActivity {
    String networkname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iot_select);

        Intent intent = getIntent();
        networkname = intent.getStringExtra("networkname");

        ImageButton mijia = (ImageButton) findViewById(R.id.mijia);
        ImageButton razbery = (ImageButton) findViewById(R.id.razbery);

        mijia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), mijia_connect_Activity.class);
                intent.putExtra("networkname",networkname);
                startActivity(intent);
            }
        });

        //라즈베리파이 완성대는대로 작성
        razbery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
