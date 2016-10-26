package edu.wisc.ece.avalonforchromecast;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LobbyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
    }

    /* called when pressing the start game button */
    public void startGame(View view){
        // Send signal to chromecast to start the game

        // The chromecast should then signal all devices connected to it that the game is starting
        // When devices receive this signal they should move to the game activity
    }
}
