package d.somewheres.uieosio;

import android.graphics.drawable.Drawable;

//IoT장치를 리스트뷰로 나타내기위한 커스텀 리스트뷰 이다.
public class ListViewIoTItem {
    private Drawable iconDrawable ; //아이콘
    private String titleStr ; //제목
    private String descStr ; //설명


    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        titleStr = title ;
    }
    public void setDesc(String desc) {
        descStr = desc ;
    }


    public Drawable getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() {
        return this.titleStr ;
    }
    public String getDesc() {
        return this.descStr ;
    }

}