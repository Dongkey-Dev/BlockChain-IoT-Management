package d.somewheres.uieosio;

import android.graphics.drawable.Drawable;

public class ListViewuserItem {
    private Drawable iconDrawable ; //아이콘
    private String titleuser ; //제목



    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        titleuser = title ;
    }



    public Drawable getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() {
        return this.titleuser ;
    }


}

