package edu.wisc.ece.avalonforchromecast;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }


    /* submits a selection of players to go on a mission */
    public void submitSelection(View view){
        // send to chromecast the selection of players to go on mission

        // chromecast will send signal to all players to move to vote on selection phase
    }


    /* vote to approve current selection of players to go on mission */
    public void approveSelection(View view){
        // send to chromecast vote to approve selection

        // once all players have voted chromecast will then decide to either
        //   move to go on mission phase or
        //   increment rejected selection counter and move to select players phase
    }


    /* vote to reject current selection of players to go on mission */
    public void rejectSelection(View view){
        // send to chromecast vote to reject selection
    }


    /* pass the mission */
    public void passMission(View view){
        // send to chromecast action to pass mission

        // once all players in mission have performed action for mission, chromecast will determine
        // whether mission was a success or a failure
        // chromecast will update the big screen, choose next leader, and move to select players phase

    }

    /* fail the mission */
    public void failMission(View view){
        // send to chromecast action to fail mission
    }
}
