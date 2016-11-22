package edu.wisc.ece.avalonforchromecast;

import com.google.android.gms.cast.games.GameManagerClient;
import com.google.android.gms.cast.games.GameManagerState;
import com.google.android.gms.cast.games.PlayerInfo;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Fragment for drawing.
 * Based on https://github.com/playgameservices/8bitartist
 */
public class PlayingFragment extends GameFragment{


    private static final String TAG = "LobbyFragment";

    private TextView mPlayerRoleTextView;
    private TextView mOtherInfoTextView;
    private Button mSubmitSelectionButton;
    private Button mApproveSelectionButton;
    private Button mRejectSelectionButton;
    private Button mPassMissionButton;
    private Button mFailMissionButton;

    private ScrollView mPlayerButtonsContainer;

    private static final int SELECTION_PHASE = 0;
    private static final int VOTING_PHASE = 1;
    private static final int MISSION_PHASE = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

//        GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
//        GameManagerState gameState = gameManagerClient.getCurrentState();
//        JSONObject gameData = gameState.getGameData();
//        selectionPhase(gameState, gameData);
    }

    // This be the real onCreate function where we do lots of setup
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.playing_fragment, container, false);

        mPlayerRoleTextView = (TextView) view.findViewById(R.id.roleTextView);
        mOtherInfoTextView = (TextView) view.findViewById(R.id.otherInfoPlaceholderTextView);
        mSubmitSelectionButton = (Button) view.findViewById(R.id.submitSelectionButton);
        mApproveSelectionButton = (Button) view.findViewById(R.id.approveSelectionButton);
        mRejectSelectionButton = (Button) view.findViewById(R.id.rejectSelectionButton);
        mPassMissionButton = (Button) view.findViewById(R.id.passMissionButton);
        mFailMissionButton = (Button) view.findViewById(R.id.failMissionButton);

        mPlayerButtonsContainer = (ScrollView) view.findViewById(R.id.playerButtonsContainer);

        mSubmitSelectionButton.setVisibility(View.GONE);
        mApproveSelectionButton.setVisibility(View.GONE);
        mRejectSelectionButton.setVisibility(View.GONE);
        mPassMissionButton.setVisibility(View.GONE);
        mFailMissionButton.setVisibility(View.GONE);


        mSubmitSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitSelectionClicked();
            }
        });
        mApproveSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {onApproveSelectionClicked();
            }
        });
        mRejectSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {onRejectSelectionClicked();
            }
        });
        mPassMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {onPassMissionClicked();
            }
        });
        mFailMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {onFailMissionClicked();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    /**
     * Game state callback handler.
     */
    @Override
    public void onStateChanged(GameManagerState newState,
                               GameManagerState oldState) {
        Log.d(TAG, "Inside onStateChanged");
        if(newState.hasGameDataChanged(oldState)){
            Log.d(TAG, "hasGameDataChanged");
            if(newState.getGameData() != null){
                JSONObject gameData = newState.getGameData();
                Log.d(TAG, "getGameData != null");
                try {
                    int gamePhase = gameData.getInt("phase");
                    if(gamePhase == SELECTION_PHASE){
                        Log.d(TAG, "Entering selection phase");
                        selectionPhase(newState, gameData);
                    }
                    else if(gamePhase == VOTING_PHASE){
                        votingPhase(newState, gameData);
                    }
                    else if(gamePhase == MISSION_PHASE){
                        missionPhase(newState, gameData);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Handle selection phase.
     */
    public void selectionPhase(GameManagerState gameState, JSONObject gameData){
        Log.d(TAG, "gameData: " + gameData.toString());
        //TODO: gameData only contains {"phase":0}
        try {
            String leaderId = gameData.getString("leader");
            String playerId = ((MainActivity) getActivity()).getPlayerId();
            if(leaderId.equals(playerId)) {
                //leader view
                mSubmitSelectionButton.setVisibility(View.VISIBLE);

                //get list of playing players
                List<PlayerInfo> players = gameState.getPlayersInState(GameManagerClient.PLAYER_STATE_PLAYING);
                //make radio button for each player
                for (int i = 0; i < players.size(); i++) {
                    PlayerInfo player = players.get(i);
                    String playerName = "";
                    try {
                        playerName = player.getPlayerData().getString("name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Unable to access 'name' from PlayerInfo");
                    }
                    ToggleButton playerButton = new ToggleButton(getActivity());
                    playerButton.setText(playerName);
                    playerButton.setTag(player.getPlayerId());
                    mPlayerButtonsContainer.addView(playerButton);
                }
            }
            else{
                //do nothing
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle voting phase.
     */
    public void votingPhase(GameManagerState gameState, JSONObject gameData){
        mApproveSelectionButton.setVisibility(View.VISIBLE);
        mRejectSelectionButton.setVisibility(View.VISIBLE);

    }

    /**
     * Handle mission phase.
     */
    public void missionPhase(GameManagerState gameState, JSONObject gameData){

    }

    /**
     * Button click handler. Submit player selection.
     */
    //find out how many buttons in playerButtonsContainer are toggled on
    //get current missionTeamSize from gameData
    //if that missionTeamSize equals num playerButtons selected,
    //create array missionTeam of playerId's of selected playerButtons
    //sendRequest json 'missionTeam': missionTeam
    public void onSubmitSelectionClicked(){
        //TODO: count how many players are selected


    }

    /**
     * Button click handler. Approve team.
     */
    public void onApproveSelectionClicked(){

    }

    /**
     * Button click handler. Reject team.
     */
    public void onRejectSelectionClicked(){

    }

    /**
     * Button click handler. Pass mission.
     */
    public void onPassMissionClicked(){

    }

    /**
     * Button click handler. Fail mission.
     */
    public void onFailMissionClicked(){

    }

}
