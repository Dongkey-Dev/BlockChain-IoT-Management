package d.somewheres.uieosio;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

// 다이얼로그를 커스텀한 클래스
public class CustomDialog extends Dialog implements View.OnClickListener {
    private EditText ed1,ed2; //이름과 설명을 넣을 텍스트창
    private Button btn1,btn2; // 확인과 취소 버튼
    private CustomDialogListener customDialogListener; //다이얼로그이벤트를 청취할 리스너
    private Context context; //액티비티 컨택스트를 가져올 변수

    //다이얼로그의 생성자 액티비티 컨택스트를 넘겨받는다
    public CustomDialog(Context context) {
        super(context);
        this.context = context;
    }


    //인터페이스 설정 확인, 취소
    interface CustomDialogListener{
        void onPositiveClicked(String name, String desc);
        void onNegativeClicked();
    }

        //호출할 리스너 초기화
    public void setDialogListener(CustomDialogListener customDialogListener){
            this.customDialogListener = customDialogListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog);


        //각각의 뷰 id 값을 가져온다 값을 넘겨주기위해
        ed1 = (EditText) findViewById(R.id.createText);
        ed2 = (EditText) findViewById(R.id.description);
        btn1 = (Button) findViewById(R.id.btn_ok);
        btn2 = (Button) findViewById(R.id.btn_cancel);

        //버튼에 클릭 이벤트 리스너를 달아준다
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok: //확인 클릭시
                String name = ed1.getText().toString(); //이름에 사용자가 입력한 값 저장
                String desc = ed2.getText().toString(); //설명에도 저장

                customDialogListener.onPositiveClicked(name,desc); //다이얼로그 확인클릭시 이름과 설명을 메인액티비티로 넘겨준다
                dismiss(); //다이얼로그안보이게
                break;
            case R.id.btn_cancel:
                cancel(); //취소
                break;
        }
    }
}


