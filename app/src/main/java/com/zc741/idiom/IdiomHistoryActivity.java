package com.zc741.idiom;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class IdiomHistoryActivity extends AppCompatActivity {
    private List<SearchWord> mList;
    private SQLiteDatabase mDb;
    private BaseAdapter mBaseAdapter;
    private String mClickDeleteName;
    private PopupWindow mPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idiom_history);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back_white_18dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        mList = new ArrayList<>();

        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(this, "idiom_db");
        mDb = helper.getWritableDatabase();

        select();
    }

    private void select() {
        Cursor cursor1 = mDb.query("idiom", null, null, null, null, null, null);
        while (cursor1.moveToNext()) {
            String idiomName = cursor1.getString(cursor1.getColumnIndex("searchWord"));
            SearchWord word = new SearchWord(idiomName);
            mList.add(word);
            //System.out.println("searchWord = " + word);
        }

        initList();
    }

    private void initList() {
        ListView idiomListView = (ListView) findViewById(R.id.idiomListView);
        mBaseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return mList.size();
            }

            @Override
            public Object getItem(int position) {
                return mList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder viewHolder = null;
                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    convertView = LayoutInflater.from(IdiomHistoryActivity.this).inflate(R.layout.idiom_history, null);
                    viewHolder.idiom_word = (TextView) convertView.findViewById(R.id.idiom_word);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                viewHolder.idiom_word.setText(mList.get(position).getSearchWord());
                return convertView;
            }
            class ViewHolder {
                TextView idiom_word;
            }
        };
        assert idiomListView != null;
        idiomListView.setAdapter(mBaseAdapter);

        idiomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //不做了

            }
        });


        idiomListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mClickDeleteName = mList.get(position).getSearchWord();
                delete();
                return false;
            }
        });
    }



    private void delete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("要删除这条成语吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                deleteIdiom();

                mList.clear();
                Cursor cursor = mDb.query("idiom",null,null,null,null,null,null);
                while (cursor.moveToNext()){
                    String name = cursor.getString(cursor.getColumnIndex("searchWord"));
                    SearchWord searchWord = new SearchWord(name);
                    mList.add(searchWord);
                }
                mBaseAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void deleteIdiom() {
        mDb.delete("idiom","searchWord=?",new String[]{mClickDeleteName});
    }


}
