package com.github.genju83.simplepreference.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // getter && setter
        int intValue = ShowCase.getInstance(this).getSampleInt(); // 1
        String stringValue = ShowCase.getInstance(this).getSampleString(); // "test"
        long longValue = ShowCase.getInstance(this).getSampleLong(); // 1L
        float floatValue = ShowCase.getInstance(this).getSampleFloat(); // 1f

        ShowCase.getInstance(this).setSampleInt(2);
        ShowCase.getInstance(this).setSampleString("new_test");
        ShowCase.getInstance(this).setSampleLong(2L);
        ShowCase.getInstance(this).setSampleFloat(2f);
    }
}
