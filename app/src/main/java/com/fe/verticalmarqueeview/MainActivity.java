package com.fe.verticalmarqueeview;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    String[] contentArray = new String[] {
            "世界卫生组织WHO称：没有性生活认定为残疾...",
            "小龙女原型”夏梦离世 绝美旧照倾国倾城",
            "王楠老公删光王宝强相关微博 这是出了什么事？",
            "阿Sa自认桃花运不断：一向都有好多",
            "中国民营企业500强发布，华为超联想夺第一"};

    MarqueeView marqueeView;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        marqueeView = (MarqueeView) findViewById(R.id.marqueeview);
        marqueeView.setTextArray(contentArray);
        marqueeView.setOnItemClickListener(new MarqueeView.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("content", contentArray[position]);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                marqueeView.resume();
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        marqueeView.destory();
    }
}
