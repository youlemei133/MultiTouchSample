package com.hudawei.multitouchsample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private MultiTouchView multiTouchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        multiTouchView = (MultiTouchView) findViewById(R.id.multiTouchView);
    }

    public void clickPre(View view) {
        multiTouchView.pre();
    }

    public void clickNext(View view) {
        multiTouchView.next();
    }
}
