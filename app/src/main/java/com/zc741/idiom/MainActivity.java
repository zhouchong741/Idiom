package com.zc741.idiom;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText mEdit_search;
    private PopupWindow mPopupWindow;
    private String mSearchWord;

    @ViewInject(R.id.idiomWord)
    TextView idiomWord;
    @ViewInject(R.id.pinyin)
    TextView pinyin;
    @ViewInject(R.id.jieshi)
    TextView jieshi;
    @ViewInject(R.id.from)
    TextView from;
    @ViewInject(R.id.example)
    TextView example;
    @ViewInject(R.id.yinzhengjs)
    TextView yinzhengjs;
    @ViewInject(R.id.tongyi)
    TextView tongyi;
    @ViewInject(R.id.fanyi)
    TextView fanyi;
    private int mErrorCode;
    private SQLiteDatabase mDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        x.Ext.init(getApplication());
        x.view().inject(this);

        //数据库初始化
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(this, "idiom_db");
        mDb = helper.getWritableDatabase();

        //ToolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("成语查询");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.menu_items);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.search:
                        searchPopup();
                        break;
                    case R.id.more:
                        startActivity(new Intent(MainActivity.this, IdiomHistoryActivity.class));
                        break;
                }
                return true;
            }
        });
    }

    private void searchPopup() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.search_popue, null);
        mPopupWindow = new PopupWindow(view, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT, true);

        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setOutsideTouchable(true);
        WindowManager.LayoutParams params = getWindow().getAttributes();//创建当前界面的一个参数对象
        params.alpha = 0.8f;
        getWindow().setAttributes(params);//把该参数对象设置进当前界面中
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1.0f;//设置为不透明，即恢复原来的界面
                getWindow().setAttributes(params);
            }
        });

        //第一个参数为父View对象，即PopupWindow所在的父控件对象，第二个参数为它的重心，后面两个分别为x轴和y轴的偏移量
        mPopupWindow.showAtLocation(inflater.inflate(R.layout.activity_main, null), Gravity.TOP, 0, 0);

        //键盘弹出
        mEdit_search = (EditText) view.findViewById(R.id.edit_search);
        mEdit_search.setFocusable(true);
        mEdit_search.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) mEdit_search.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);


        mEdit_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    getData();
                }
                return false;
            }
        });
    }

    //EditText的返回和查询
    public void arrow_back(View view) {
        mPopupWindow.dismiss();
    }

    public void search(View view) {
        //getData
        getData();
    }

    private void getData() {
        //1.拿到EditText的字符
        mSearchWord = mEdit_search.getText().toString();
        //System.out.println("searchWord= " + searchWord);
        //String searchUrl = "http://apicloud.mob.com/appstore/idiom/query?key=134be95d20386&name=" + mSearchWord;
        String searchUrl = "http://v.juhe.cn/chengyu/query?key=330a5f17944e67eed98c4cab4e050edc&word=" + mSearchWord;
        RequestParams params = new RequestParams(searchUrl);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                String content = result;
                parseJson(content);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                System.out.println("error = " + ex);
                //弹出对话框提示查不到该成语相关信息
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("错误");
                builder.setMessage("查询不到该成语的相关信息,要重新查询吗？");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        searchPopup();
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

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                //请求结束 搜索框消失
                mPopupWindow.dismiss();
            }
        });
    }

    private void parseJson(String content) {
        Gson gson = new Gson();
        Idiom idiom = gson.fromJson(content, Idiom.class);

        //返回码
        mErrorCode = idiom.getError_code();
        //拼音
        String pinyinRes = idiom.getResult().getPinyin().trim();
        //解释
        String jieshiRes = idiom.getResult().getChengyujs().trim();
        //出处
        String fromRes = idiom.getResult().getFrom_().trim();
        //举例
        String exampleRes = idiom.getResult().getExample().trim();
        //引证解释
        String yinzhengjsRes = idiom.getResult().getYinzhengjs().trim();
        //同义词：
        List<String> toongyiRes = idiom.getResult().getTongyi();
        //反义
        List<String> fanyiRes = idiom.getResult().getFanyi();

        //绑定
        idiomWord.setText(mSearchWord);
        pinyin.setText(pinyinRes);
        jieshi.setText("解释：" + jieshiRes);
        from.setText("出处：" + fromRes);
        example.setText("例子：" + exampleRes);
        yinzhengjs.setText("引证解释：" + yinzhengjsRes);
        tongyi.setText("同义词：" + toongyiRes + "");
        fanyi.setText("反义词：" + fanyiRes + "");

        //保存到数据库
        saveToSQL();
    }

    private void saveToSQL() {
        //判断 1.只有返回码是0的时候才保存到数据库
        if (mErrorCode == 0) {
            checkExists();
        } else {
            System.out.println("错误 不保存");
        }
    }

    private void checkExists() {
        String existsSql = "select searchWord from idiom where searchWord = " + "\"" + mSearchWord + "\"";
        Cursor cursor = mDb.rawQuery(existsSql,null);
        cursor.moveToFirst();
        int numbers = cursor.getCount();
        if (numbers != 0){
            System.out.println("查询记录已经存在，可以直接查看历史纪录");
        }else {
            System.out.println("插入数据库");
            add();
        }
    }
    private void add() {
        //检出数据库是否已经存在
        String searchWord = "searchWord";
        String sqlName = "idiom";
        ContentValues values = new ContentValues();
        values.put(searchWord, mSearchWord);
        mDb.insert(sqlName, null, values);
        //select();
    }

}
