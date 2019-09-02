package d.somewheres.uieosio;

import android.graphics.drawable.Drawable;

//네트워크 생성시 리스트뷰로 나타내기위한 커스텀리스트뷰이다.
public class ListViewnetworkItem {
    private Drawable iconDrawable ;
    private String titleStr ;
    private String accountStr;

    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        titleStr = title ;
    }
    public void setaccount(String account) {
        accountStr = account ;
    }

    public Drawable getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() {
        return this.titleStr ;
    }
    public String getAccountStr() {
        return this.accountStr;
    }
}