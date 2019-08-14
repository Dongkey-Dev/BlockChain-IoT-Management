package d.somewheres.uieosio;

import android.graphics.drawable.Drawable;

public class ListViewIoTtime {
    private Drawable iconDrawable ; //아이콘
    private String titleIoT ; //제목

    private String data;
    private String time;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    public void setIconDrawable(Drawable iconDrawable) {
        this.iconDrawable = iconDrawable;
    }

    public String getTitleIoT() {
        return titleIoT;
    }

    public void setTitleIoT(String titleIoT) {
        this.titleIoT = titleIoT;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }



}
