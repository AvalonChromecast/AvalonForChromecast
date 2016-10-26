package edu.wisc.ece.avalonforchromecast;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    /* adds player to the lobby */
    public void joinLobby(View view){
        // if the user entered a name, add them to the lobby

        // chromecast should update ui by adding player's name to list on the big screen

        // move to lobby ui
    }
}
