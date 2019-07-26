package d.somewheres.uieosio;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

//리스트뷰에 값을 넣어줄수 있도록 도와주는 어댑터이다.
public class ListViewAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<ListViewnetworkItem> listViewItemList = new ArrayList<ListViewnetworkItem>() ;

    // ListViewAdapter의 생성자
    public ListViewAdapter() {

    }

    // Adapter에 사용되는 데이터의 개수를 리턴
    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position; //클릭한 리스트의 위치
        final Context context = parent.getContext(); //부모 컨택스트를 얻어와 뷰를 사용

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_networkitem, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView1) ;
        TextView titleTextView = (TextView) convertView.findViewById(R.id.textView1) ;
        TextView descTextView = (TextView) convertView.findViewById(R.id.textView2) ;
        TextView accountTextView = (TextView) convertView.findViewById(R.id.account) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        ListViewnetworkItem listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        iconImageView.setImageDrawable(listViewItem.getIcon());
        titleTextView.setText(listViewItem.getTitle());
        descTextView.setText(listViewItem.getDesc());
        accountTextView.setText(listViewItem.getAccountStr());

        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }

    //리스트뷰 지정된 위치에있는 이름을 얻어올수 있다.
    public String getname(int position) { return listViewItemList.get(position).getTitle();}
    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(Drawable icon, String title, String desc, String account) {
        ListViewnetworkItem item = new ListViewnetworkItem();

        item.setIcon(icon);
        item.setTitle(title);
        item.setDesc(desc);
        item.setaccount(account);

        listViewItemList.add(item);
    }

    //cursor를 이용해서 리스트뷰에 아이템 추가
    public void addItem(Drawable icon, Cursor cursor) {

        while (cursor.moveToNext()) {
            ListViewnetworkItem item = new ListViewnetworkItem();
            item.setIcon(icon);
            item.setTitle(cursor.getString(1));
            item.setDesc(cursor.getString(2));
            item.setaccount(cursor.getString(3));

            listViewItemList.add(item);
        }
        cursor.close();
    }
}
