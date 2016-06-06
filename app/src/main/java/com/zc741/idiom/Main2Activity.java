package com.zc741.idiom;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.List;

public class Main2Activity extends AppCompatActivity {

    private String mTitle;
    private TextView mPinyin1;
    private TextView mJieshi1;
    private TextView mFrom1;
    private TextView mExample1;
    private TextView mYinzhengjs1;
    private TextView mTongyi1;
    private TextView mFanyi1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Intent intent = getIntent();
        mTitle = intent.getStringExtra("idiomWord");

        mPinyin1 = (TextView) findViewById(R.id.pinyin1);
        mJieshi1 = (TextView) findViewById(R.id.jieshi1);
        mFrom1 = (TextView) findViewById(R.id.from1);
        mExample1 = (TextView) findViewById(R.id.example1);
        mYinzhengjs1 = (TextView) findViewById(R.id.yinzhengjs1);
        mTongyi1 = (TextView) findViewById(R.id.tongyi1);
        mFanyi1 = (TextView) findViewById(R.id.fanyi1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mTitle);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back_white_18dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //
        initIdiom();

    }

    private void initIdiom() {
        String searchUrl = "http://v.juhe.cn/chengyu/query?key=330a5f17944e67eed98c4cab4e050edc&word=" + mTitle;
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
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private void parseJson(String content) {
        Gson gson = new Gson();
        Idiom idiom = gson.fromJson(content, Idiom.class);

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

        mPinyin1.setText(pinyinRes);
        mJieshi1.setText("解释：" + jieshiRes);
        mFrom1.setText("出处：" + fromRes);
        mExample1.setText("例子：" + exampleRes);
        mYinzhengjs1.setText("引证解释：" + yinzhengjsRes);
        mTongyi1.setText("同义词：" + toongyiRes + "");
        mFanyi1.setText("反义词：" + fanyiRes + "");
    }
}
