package edu.wisc.ece.avalonforchromecast;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /* called when user clicks connect button */
    public void connectToCast(View view){
        // connect user to chromecast

        // move user to welcome screen
    }
}
