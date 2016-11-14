package edu.wisc.ece.avalonforchromecast;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.cast.games.GameManagerClient;
import com.google.android.gms.cast.games.GameManagerState;
import com.google.android.gms.common.api.ResultCallback;

import org.json.JSONObject;

public class LobbyActivity extends AppCompatActivity {


    private static final String TAG = LobbyActivity.class.getSimpleName();

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


