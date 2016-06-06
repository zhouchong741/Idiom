package com.zc741.idiom;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NewIdiomHistoryActivity extends AppCompatActivity {
    private List<SearchWord> mList;
    private SQLiteDatabase mDb;
    private String mDeleteName;

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

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        MyAdapter myAdapter = new MyAdapter(mList);
        recyclerView.setAdapter(myAdapter);

        select();
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private List<Integer> mWidth;

        public MyAdapter(List<SearchWord> list) {
            super();
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(NewIdiomHistoryActivity.this).inflate(R.layout.idiom_history, parent, false);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(final MyAdapter.ViewHolder holder, final int position) {
            //瀑布流的做法 暂时不做
            mWidth = new ArrayList<>();
            for (int i = 0; i < mList.size(); i++) {
                mWidth.add((int) (100 + Math.random() * 300));
            }

            ViewGroup.LayoutParams lp = holder.mIdiom_word.getLayoutParams();
            //lp.width = mWidth.get(position);

            holder.mIdiom_word.setLayoutParams(lp);
            holder.mIdiom_word.setText(mList.get(position).toString());

            //点击操作会 时有时无
            /*holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("position = " + position);
                    String word = holder.mIdiom_word.getText().toString();
                    startActivity(new Intent(getApplication(), Main2Activity.class));
                }
            });*/

            //长按删除
            holder.mIdiom_word.setOnLongClickListener(new View.OnLongClickListener() {

                private int mPos;

                @Override
                public boolean onLongClick(View v) {
                    mPos = holder.getLayoutPosition();

                    mDeleteName = holder.mIdiom_word.getText().toString();//要删除的条目

                    AlertDialog.Builder builder = new AlertDialog.Builder(NewIdiomHistoryActivity.this);
                    builder.setMessage("要删除这条成语吗？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            deleteIdiom();
                            removeData(mPos);
                            mList.clear();
                            Cursor cursor = mDb.query("idiom", null, null, null, null, null, null);
                            while (cursor.moveToNext()) {
                                String name = cursor.getString(cursor.getColumnIndex("searchWord"));
                                SearchWord searchWord = new SearchWord(name);
                                mList.add(searchWord);
                            }
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setNeutralButton("查询", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            /*ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText("Label", mDeleteName);//
                            cmb.setPrimaryClip(clipData);*///复制到剪切板
                            Intent intent = new Intent(NewIdiomHistoryActivity.this, Main2Activity.class);
                            intent.putExtra("idiomWord", mDeleteName);
                            startActivity(intent);
                        }
                    });
                    builder.create().show();
                    return true;
                }
            });
        }

        public void removeData(int position) {
            mList.remove(position);
            notifyItemRemoved(position);
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView mIdiom_word;

            public ViewHolder(View itemView) {
                super(itemView);
                mIdiom_word = (TextView) itemView.findViewById(R.id.idiom_word);
            }
        }
    }

    private void select() {
        Cursor cursor1 = mDb.query("idiom", null, null, null, null, null, null);
        while (cursor1.moveToNext()) {
            String idiomName = cursor1.getString(cursor1.getColumnIndex("searchWord"));
            SearchWord word = new SearchWord(idiomName);
            mList.add(word);
            //System.out.println("searchWord = " + word);
        }
    }

    private void deleteIdiom() {
        mDb.delete("idiom", "searchWord=?", new String[]{mDeleteName});
    }
}
