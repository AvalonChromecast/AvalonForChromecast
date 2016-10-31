package edu.wisc.ece.avalonforchromecast;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /* called when pressing the start game button */
    public void connectToCast(View view){
        // connect to the chromecast

        // change activity to welcome activity
    }
}
