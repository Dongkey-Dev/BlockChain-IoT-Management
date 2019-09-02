package d.somewheres.uieosio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class Network_join_fragment extends Fragment {
    String account;

    public Network_join_fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.network_join_fragment,container,false);
        EditText joinnetworktext = (EditText) v.findViewById(R.id.join_network_text);
        Button joinbutton = (Button) v.findViewById(R.id.btn_networkjoin);

        //리스너에 getaccount로 네트워크 계정이 있는지 파악한 뒤, 있으면 생성, 없으면 오류 (구현해야한다)
        joinbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                account = "사용자";



                Intent intent = new Intent(getActivity(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });


        return v;
    }
}
