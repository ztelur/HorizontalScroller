package com.carpediem.randy.horizontalscroller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.carpediem.randy.lib.HorizontalScroller;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private HorizontalScroller mHSContainer;

    private Button mBtnChangeData;

    private MainAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHSContainer = (HorizontalScroller) findViewById(R.id.scroller);
        mAdapter = new MainAdapter(this);

        mHSContainer.setAdapter(mAdapter);
        mAdapter.setData(generateInitList());


        mBtnChangeData = (Button)findViewById(R.id.button);
        mBtnChangeData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.setData(generateTestList());
            }
        });
    }

    private List<String> generateInitList() {
        List<String> result = new ArrayList<>();
        result.add("dddd");
        result.add("dddd1");
        result.add("dddd2");
        result.add("dddd3");
        result.add("dddd4");
        return  result;
    }
    private List<String> generateTestList() {
          List<String> result = new ArrayList<>();
        result.add("dddd");
        result.add("dddd");
        result.add("dddd");
        result.add("dddd");
        result.add("dddd");
        return  result;
    }

}
