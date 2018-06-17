package com.example.administrator.slide;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.right1:
                    break;
                case R.id.bt1:
                    mSlide.setScale(1.2f);
                    break;
                case R.id.bt2:
                    break;

                default:
                    break;
            }
        }
    };
    private SlideView mSlide;
    private View mRight1;
    private Button mBt1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final View slide = findViewById(R.id.slide);
        mBt1 = (Button) findViewById(R.id.bt1);

        mRight1 = findViewById(R.id.right1);
        mRight1.setOnClickListener(mListener);
        findViewById(R.id.bt2).setOnClickListener(mListener);
        mSlide = (SlideView) findViewById(R.id.slide);
        mBt1.setOnClickListener(mListener);

    }
}
