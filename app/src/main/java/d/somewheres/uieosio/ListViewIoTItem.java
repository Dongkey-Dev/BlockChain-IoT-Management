package d.somewheres.uieosio;

import android.graphics.drawable.Drawable;

//IoT장치를 리스트뷰로 나타내기위한 커스텀 리스트뷰 이다.
public class ListViewIoTItem {
    private Drawable iconDrawable ; //아이콘
    private String titleIoT ; //제목



    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        titleIoT = title ;
    }



    public Drawable getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() {
        return this.titleIoT ;
    }


}