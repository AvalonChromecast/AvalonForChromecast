package edu.wisc.ece.avalonforchromecast;

import com.google.android.gms.cast.games.GameManagerClient;
import com.google.android.gms.cast.games.GameManagerState;
import com.google.android.gms.cast.games.PlayerInfo;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Fragment displayed while the game is in progress, and the player is in game.
 */
public class PlayingFragment extends GameFragment{

    private static final String TAG = "PlayingFragment";

    private boolean initialized;

    private TextView mPlayerRoleTextView;
    private TextView mMissionTeamSizeView;
    private TextView mPlayHintView;

    private Button mShowHideButton;
    private Button mSubmitSelectionButton;
    private Button mApproveSelectionButton;
    private Button mRejectSelectionButton;
    private Button mPassMissionButton;
    private Button mFailMissionButton;
    private Button mSubmitAssassinButton;

    private LinearLayout mPlayerButtonsContainer;
    private LinearLayout mExtraInfoContainer;
    private RadioGroup mTargetsContainer;

    // enums for phases during the game
    private final int SELECTION_PHASE = 2;
    private final int VOTING_PHASE = 3;
    private final int MISSION_PHASE = 4;
    private final int ASSASSIN_PHASE = 5;

    private final String MERLIN = "merlin";
    private final String ASSASSIN = "assassin";
    private final String PERCIVAL = "percival";
    private final String MORDRED = "mordred";
    private final String OBERON = "oberon";
    private final String MORGANA = "morgana";

    // enums for indexes of each role in a boolean array for whether a role is in play or not
    private final int MERLIN_INDEX = 0;
    private final int ASSASSIN_INDEX = 1;
    private final int PERCIVAL_INDEX = 2;
    private final int MORDRED_INDEX = 3;
    private final int OBERON_INDEX = 4;
    private final int MORGANA_INDEX = 5;

    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playing_fragment, container, false);

        initialized = false;

        mPlayerRoleTextView = (TextView) view.findViewById(R.id.roleTextView);
        mMissionTeamSizeView = (TextView) view.findViewById(R.id.missionTeamSizeView);
        mPlayHintView = (TextView) view.findViewById(R.id.playHintView);

        mShowHideButton = (Button) view.findViewById(R.id.showHideButton);
        mSubmitSelectionButton = (Button) view.findViewById(R.id.submitSelectionButton);
        mApproveSelectionButton = (Button) view.findViewById(R.id.approveSelectionButton);
        mRejectSelectionButton = (Button) view.findViewById(R.id.rejectSelectionButton);
        mPassMissionButton = (Button) view.findViewById(R.id.passMissionButton);
        mFailMissionButton = (Button) view.findViewById(R.id.failMissionButton);
        mSubmitAssassinButton = (Button) view.findViewById(R.id.submitAssassinButton);

        mPlayerButtonsContainer = (LinearLayout) view.findViewById(R.id.playerButtonsContainer);
        mExtraInfoContainer = (LinearLayout) view.findViewById(R.id.extraInfoContainer);

        mSubmitSelectionButton.setVisibility(View.GONE);
        mApproveSelectionButton.setVisibility(View.GONE);
        mRejectSelectionButton.setVisibility(View.GONE);
        mPassMissionButton.setVisibility(View.GONE);
        mFailMissionButton.setVisibility(View.GONE);
        mSubmitAssassinButton.setVisibility(View.GONE);

        mMissionTeamSizeView.setVisibility(View.GONE);
        mPlayHintView.setVisibility(View.GONE);

        mShowHideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShowHideButtonClicked();
            }
        });

        mSubmitSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {onSubmitSelectionClicked();
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
        mSubmitAssassinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {onSubmitAssassinClicked();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mActivity = activity;
    }

    /**
     * onStart contains a function that updates which phase the player should be in. Necessary if a
     * player navigates away from the app while game phases are changing.
     */
    @Override
    public void onStart() {
        super.onStart();

        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        if(gameManagerClient == null){
            Log.d(TAG, "gameManagerClient in onStart is somehow null.");
            return;
        }
        GameManagerState state = gameManagerClient.getCurrentState();
        JSONObject gameData = state.getGameData();

        if(!initialized){
            initialize(state, gameData);
            initialized = false;
        }
        int gamePhase = 0;
        try {
            gamePhase = gameData.getInt("phase");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(gamePhase == SELECTION_PHASE){
            selectionPhase(state, gameData);
        }
        else if(gamePhase == VOTING_PHASE){
            votingPhase(state, gameData);
        }
        else if(gamePhase == MISSION_PHASE){
            missionPhase(state, gameData);
        }
        else if(gamePhase == ASSASSIN_PHASE){
            assassinPhase(state, gameData);
        }
    }

    /**
     * Listener for changes in GameManagerState. Updates what the current phase should be.
     */
    @Override
    public void onStateChanged(GameManagerState newState,
                               GameManagerState oldState) {
        if(newState.hasGameDataChanged(oldState)){
            if(newState.getGameData() != null){
                JSONObject gameData = newState.getGameData();
                int gamePhase = -1;
                try {
                    gamePhase = gameData.getInt("phase");
                    Log.d(TAG, "game phase:" + gamePhase);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(gamePhase == SELECTION_PHASE){
                    selectionPhase(newState, gameData);
                }
                else if(gamePhase == VOTING_PHASE){
                    votingPhase(newState, gameData);
                }
                else if(gamePhase == MISSION_PHASE){
                    missionPhase(newState, gameData);
                }
                else if(gamePhase == ASSASSIN_PHASE){
                    assassinPhase(newState, gameData);
                }

                ((MainActivity) mActivity).updateFragments();
            }
        }
    }

    /**
     * Called the first time selection phase is called. Initializes info on the screen, such as
     * loyalty, role, etc.
     */
    private void initialize(GameManagerState gameState, JSONObject gameData) {
        //display loyalty
        PlayerInfo player = gameState.getPlayer(((MainActivity) mActivity).getPlayerId());
        if(player == null){
            Log.d(TAG, "player is somehow null in initialize()");
            return;
        }
        List<PlayerInfo> players = gameState.getPlayersInState(GameManagerClient.PLAYER_STATE_PLAYING);

        String loyalty = "";
        String role = "";
        String myPlayerId = ((MainActivity) mActivity).getPlayerId();

        if(player.getPlayerData() == null) return;
        try {
            loyalty = player.getPlayerData().getString("loyalty");
            role = player.getPlayerData().getString("role");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ((MainActivity) mActivity).setLoyalty(loyalty);

        mPlayerRoleTextView.setText(loyalty + " - " + role);
        mMissionTeamSizeView.setText("");
        mExtraInfoContainer.removeAllViews();

        boolean isEvil = loyalty.equals("evil");
        boolean isMerlin = role.equals(MERLIN);
        boolean isPercival = role.equals(PERCIVAL);
        boolean isMordred = role.equals(MORDRED);
        boolean isOberon = role.equals(OBERON);
        boolean isMorgana = role.equals(MORGANA);

        //boolean[] rolesArray= Arrays.copyOf(((MainActivity)mActivity).getRolesArray(),6);

        TextView header = new TextView(mActivity);
        mExtraInfoContainer.addView(header);


        if(isOberon){
            header.setText("You can't see your fellow traitors!");
        }

        // display who traitors are
        if((isEvil && !isOberon) || isMerlin){
            if(isEvil){
                header.setText("Fellow traitors: ");
            }
            else if(isMerlin){
                header.setText("Traitors: ");
            }
            for(int i=0; i<players.size(); i++){
                String currPlayerId = players.get(i).getPlayerId();
                String currPlayerLoyalty = "";
                String currPlayerName = "";
                String currPlayerRole = "";
                try {
                    currPlayerLoyalty = players.get(i).getPlayerData().getString("loyalty");
                    currPlayerName = players.get(i).getPlayerData().getString("name");
                    currPlayerRole = players.get(i).getPlayerData().getString("role");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "Current player's role: " + currPlayerRole);
                if(currPlayerLoyalty.equals("evil") && !myPlayerId.equals(currPlayerId)){
                    if( !((isEvil && currPlayerRole.equals(OBERON)) ||(isMerlin && currPlayerRole.equals(MORDRED)))) {
                        TextView evils = new TextView(mActivity);
                        evils.setText(currPlayerName);
                        mExtraInfoContainer.addView(evils);
                    }
                }
            }
        }

        //display who merlin is
        if(isPercival){
            String morganaName = "";
            String merlinName = "";
            try {
                morganaName = gameData.getString(MORGANA);
                merlinName = gameData.getString(MERLIN);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(morganaName.equals("")) {
                header.setText("Merlin:");
                TextView tv = new TextView(mActivity);
                tv.setText(merlinName);
                mExtraInfoContainer.addView(tv);
            }else {
                header.setText("Merlin or Morgana");

                Random rng = new Random();
                boolean merlinFirst = rng.nextBoolean();

                if(merlinFirst) {
                    TextView tv = new TextView(mActivity);
                    tv.setText(merlinName);
                    mExtraInfoContainer.addView(tv);

                    tv = new TextView(mActivity);
                    tv.setText(morganaName);
                    mExtraInfoContainer.addView(tv);
                } else {
                    TextView tv = new TextView(mActivity);
                    tv.setText(morganaName);
                    mExtraInfoContainer.addView(tv);

                    tv = new TextView(mActivity);
                    tv.setText(merlinName);
                    mExtraInfoContainer.addView(tv);
                }
            }
        }
    }

    /**
     * Display necessary UI for the selection phase. Leader displays a toggle button for each player
     * and a submit button. Non-leaders get no display.
     */
    public void selectionPhase(GameManagerState gameState, JSONObject gameData){
        Log.d(TAG, "gameData: " + gameData.toString());

        mApproveSelectionButton.setVisibility(View.GONE);
        mRejectSelectionButton.setVisibility(View.GONE);
        mPassMissionButton.setVisibility(View.GONE);
        mFailMissionButton.setVisibility(View.GONE);

        String leaderId = "";
        try {
            leaderId = gameData.getString("leader");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String playerId = ((MainActivity) mActivity).getPlayerId();
        if(leaderId.equals(playerId)) {
            Vibrator v = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 250 milliseconds
            v.vibrate(250);

            Toast.makeText(mActivity, "You are the leader", Toast.LENGTH_LONG).show();
            //leader view
            //remove extra buttons when entering this phase again
            mMissionTeamSizeView.setVisibility(View.VISIBLE);
            mPlayHintView.setVisibility(View.GONE);
            mPlayerButtonsContainer.removeAllViews();
            mPlayerButtonsContainer.setVisibility(View.VISIBLE);
            mSubmitSelectionButton.setVisibility(View.VISIBLE);

            //get list of playing players
            List<PlayerInfo> players = gameState.getPlayersInState(GameManagerClient.PLAYER_STATE_PLAYING);
            //make radio button for each player
            for (int i = 0; i < players.size(); i++) {
                PlayerInfo player = players.get(i);
                Log.d(TAG, "playerData: " + player.getPlayerData().toString());
                String playerName = "";
                try {
                    playerName = player.getPlayerData().getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Unable to access 'name' from PlayerInfo");
                }
                ToggleButton playerButton = new ToggleButton(mActivity);
                playerButton.setText(playerName);
                playerButton.setTextOn(playerName);
                playerButton.setTextOff(playerName);
                playerButton.setTag(player.getPlayerId());
                mPlayerButtonsContainer.addView(playerButton);
            }
            int teamSize = 0;
            try {
                teamSize = gameData.getInt("missionTeamSize");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mMissionTeamSizeView.setText("Select " + teamSize + " people for the mission");
        }
        else{
            mPlayHintView.setText("You are not the leader. Wait for the team leader selects mission team");
            mPlayHintView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Display necessary UI for during the voting phase.
     */
    public void votingPhase(GameManagerState gameState, JSONObject gameData){
        mMissionTeamSizeView.setVisibility(View.GONE);
        mPlayHintView.setVisibility(View.GONE);

        Toast.makeText(mActivity, "Selected mission team is shown on the TV", Toast.LENGTH_LONG).show();

        Vibrator v = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 250 milliseconds
        v.vibrate(250);

        PlayerInfo myPlayerInfo = gameState.getPlayer(((MainActivity) mActivity).getPlayerId());
        boolean hasVoted = false;
        try {
            hasVoted = myPlayerInfo.getPlayerData().getBoolean("hasVoted");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(hasVoted){
            mApproveSelectionButton.setVisibility(View.GONE);
            mRejectSelectionButton.setVisibility(View.GONE);
        }
        else{
            mApproveSelectionButton.setVisibility(View.VISIBLE);
            mRejectSelectionButton.setVisibility(View.VISIBLE);
        }
        mSubmitSelectionButton.setVisibility(View.GONE);
        mPlayerButtonsContainer.setVisibility(View.GONE);
    }

    /**
     * Display necessary UI for during the mission phase. Players on mission team get a pass or fail
     * button displayed. Players not on mission team do not get any buttons displayed.
     */
    public void missionPhase(GameManagerState gameState, JSONObject gameData) {

        //get players in mission team
        //if you're on the mission team, display pass or fail buttons
        mApproveSelectionButton.setVisibility(View.GONE);
        mRejectSelectionButton.setVisibility(View.GONE);
        JSONArray missionTeam = new JSONArray();
        try {
            missionTeam = gameData.getJSONArray("missionTeam");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String playerId = ((MainActivity) mActivity).getPlayerId();
        boolean onMission = false;
        for (int i = 0; i < missionTeam.length(); i++) {
            String currPlayerId = "";
            try {
                currPlayerId = missionTeam.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(playerId.equals(currPlayerId)){
                onMission = true;
                break;
            }
        }

        if (onMission) {
            Vibrator v = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 250 milliseconds
            v.vibrate(250);

            mPlayHintView.setVisibility(View.GONE);

            Toast.makeText(mActivity, "Choose whether or not pass the mission", Toast.LENGTH_LONG).show();
            PlayerInfo player = gameState.getPlayer(((MainActivity) mActivity).getPlayerId());

            String loyalty = "";
            boolean hasMissioned = false;
            try {
                loyalty =  player.getPlayerData().getString("loyalty");
                hasMissioned = player.getPlayerData().getBoolean("hasMissioned");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(hasMissioned){
                mPassMissionButton.setVisibility(View.GONE);
                mFailMissionButton.setVisibility(View.GONE);
            }
            else{
                mPassMissionButton.setVisibility(View.VISIBLE);
                if(loyalty.equals("evil")) {
                    mFailMissionButton.setVisibility(View.VISIBLE);
                }
            }
        } else {
            mPlayHintView.setText("You are not on the mission. Wait for the mission team make their selections");
            mPlayHintView.setVisibility(View.VISIBLE);
            Toast.makeText(mActivity, "You are not on the mission", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Display necessary UI for during the assassin phase. Assassin gets a list of good players to
     * select from. Non-assassins get no buttons displayed.
     */
    public void assassinPhase(GameManagerState gameState, JSONObject gameData){
        mApproveSelectionButton.setVisibility(View.GONE);
        mRejectSelectionButton.setVisibility(View.GONE);
        mPassMissionButton.setVisibility(View.GONE);
        mFailMissionButton.setVisibility(View.GONE);

        PlayerInfo myPlayerInfo = gameState.getPlayer(((MainActivity) mActivity).getPlayerId());
        String myRole = "";
        try {
            myRole = myPlayerInfo.getPlayerData().getString("role");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(myRole.equals(ASSASSIN)) {
            Vibrator v = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 250 milliseconds
            v.vibrate(250);

            Toast.makeText(mActivity, "You are the assassin", Toast.LENGTH_LONG).show();
            //leader view
            //remove extra buttons when entering this phase again
            mPlayHintView.setVisibility(View.GONE);

            mMissionTeamSizeView.setVisibility(View.VISIBLE);
            mPlayerButtonsContainer.removeAllViews();
            mPlayerButtonsContainer.setVisibility(View.VISIBLE);
            mSubmitSelectionButton.setVisibility(View.GONE);
            mSubmitAssassinButton.setVisibility(View.VISIBLE);

            mTargetsContainer = new RadioGroup(mActivity);
            mPlayerButtonsContainer.addView(mTargetsContainer);

            //get list of playing players
            List<PlayerInfo> players = gameState.getPlayersInState(GameManagerClient.PLAYER_STATE_PLAYING);
            //make radio button for each player
            for (int i = 0; i < players.size(); i++) {
                PlayerInfo player = players.get(i);
                Log.d(TAG, "playerData: " + player.getPlayerData().toString());
                String playerLoyalty = "";
                String playerName = "";
                try {
                    playerLoyalty = player.getPlayerData().getString("loyalty");
                    playerName = player.getPlayerData().getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Unable to access 'name' from PlayerInfo");
                }
                if(playerLoyalty.equals("good")) {
                    RadioButton playerButton = new RadioButton(mActivity);
                    playerButton.setText(playerName);
                    playerButton.setTag(player.getPlayerId());
                    mTargetsContainer.addView(playerButton);
                }
            }

            mMissionTeamSizeView.setText("Choose your assassination target.");
        }
        else{
            mPlayHintView.setText("Wait for the assassin to choose the assassination target");
            mPlayHintView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show/hide button click listener. Toggles visibility of role and loyalty.
     */
    public void onShowHideButtonClicked(){
        if(mShowHideButton.getText().equals("Show")){
            mShowHideButton.setText("Hide");
            mPlayerRoleTextView.setVisibility(View.VISIBLE);
            mExtraInfoContainer.setVisibility(View.VISIBLE);
        }
        else{
            mShowHideButton.setText("Show");
            mPlayerRoleTextView.setVisibility(View.INVISIBLE);
            mExtraInfoContainer.setVisibility(View.GONE);
        }
    }

    /**
     * selection phase submit button click handler. Submit player selection to receiver application.
     */
    //find out how many buttons in playerButtonsContainer are toggled on
    //get current missionTeamSize from gameData
    //if that missionTeamSize equals num playerButtons selected,
    //create array missionTeam of playerId's of selected playerButtons
    //sendRequest json 'missionTeam': missionTeam
    public void onSubmitSelectionClicked(){
        //get number of players can be selected
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        GameManagerState state = gameManagerClient.getCurrentState();
        JSONObject gameData = state.getGameData();

        int teamSize = 0;
        try {
            teamSize = gameData.getInt("missionTeamSize");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //count how many players are selected
        ArrayList<String> selectedPlayers = new ArrayList<>();

        ArrayList<View> playerButtons;
        playerButtons = mPlayerButtonsContainer.getTouchables();

        for(int i=0; i<playerButtons.size(); i++){
            ToggleButton currPlayer = (ToggleButton) playerButtons.get(i);
            if(currPlayer.isChecked()){
                selectedPlayers.add(((String)currPlayer.getTag()));
            }
        }

        if(selectedPlayers.size() < teamSize){
            Toast.makeText(mActivity, "You selected too few players", Toast.LENGTH_SHORT).show();
            return;
        } else if(selectedPlayers.size() > teamSize){
            Toast.makeText(mActivity, "You selected too many players", Toast.LENGTH_SHORT).show();
            return;
        }


        JSONObject missionTeam = new JSONObject();
        try {
            missionTeam.put("missionTeam", new JSONArray(selectedPlayers));
            for(String player: selectedPlayers){
                Log.d(TAG, "selectedPlayers: " + player );
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //sending missionTeam array
        PendingResult<GameManagerClient.GameManagerResult> result =
                gameManagerClient.sendGameRequest(missionTeam);
        result.setResultCallback(new ResultCallback<GameManagerClient.GameManagerResult>() {
            @Override
            public void onResult(final GameManagerClient.GameManagerResult gameManagerResult) {
                if (gameManagerResult.getStatus().isSuccess()) {
                    Toast.makeText(mActivity, "You submitted a selection", Toast.LENGTH_SHORT).show();
                }
                else {

                }
            }
        });

    }

    /**
     * Approve button click handler. Submits an approve to receiver application.
     */
    public void onApproveSelectionClicked(){
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        GameManagerState state = gameManagerClient.getCurrentState();
        JSONObject gameData = state.getGameData();

        mApproveSelectionButton.setVisibility(View.GONE);
        mRejectSelectionButton.setVisibility(View.GONE);
        mPlayHintView.setText("Wait for everyone else to vote");
        mPlayHintView.setVisibility(View.VISIBLE);

        //send an approve message
        JSONObject jsonMessage = new JSONObject();
        try {
            jsonMessage.put("acceptReject", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PendingResult<GameManagerClient.GameManagerResult> result =
                gameManagerClient.sendGameRequest(jsonMessage);
        result.setResultCallback(new ResultCallback<GameManagerClient.GameManagerResult>() {
            @Override
            public void onResult(final GameManagerClient.GameManagerResult gameManagerResult) {
                if (gameManagerResult.getStatus().isSuccess()) {
                    Toast.makeText(mActivity, "You voted to approve", Toast.LENGTH_SHORT).show();
                }
                else {
                    mPlayHintView.setVisibility(View.GONE);
                    mApproveSelectionButton.setVisibility(View.VISIBLE);
                    mRejectSelectionButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * Reject button click handler. Sends a reject to the receiver application.
     */
    public void onRejectSelectionClicked(){
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        GameManagerState state = gameManagerClient.getCurrentState();
        JSONObject gameData = state.getGameData();

        mApproveSelectionButton.setVisibility(View.GONE);
        mRejectSelectionButton.setVisibility(View.GONE);
        mPlayHintView.setText("Wait for everyone else to vote");
        mPlayHintView.setVisibility(View.VISIBLE);

        //send a reject message
        JSONObject jsonMessage = new JSONObject();
        try {
            jsonMessage.put("acceptReject", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PendingResult<GameManagerClient.GameManagerResult> result =
                gameManagerClient.sendGameRequest(jsonMessage);
        result.setResultCallback(new ResultCallback<GameManagerClient.GameManagerResult>() {
            @Override
            public void onResult(final GameManagerClient.GameManagerResult gameManagerResult) {
                if (gameManagerResult.getStatus().isSuccess()) {
                    Toast.makeText(mActivity, "You voted to reject", Toast.LENGTH_SHORT).show();
                }
                else {
                    mPlayHintView.setVisibility(View.GONE);
                    mApproveSelectionButton.setVisibility(View.VISIBLE);
                    mRejectSelectionButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * Pass mission button click handler. Submits a pass to the receiver application.
     */
    public void onPassMissionClicked(){
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        GameManagerState state = gameManagerClient.getCurrentState();
        JSONObject gameData = state.getGameData();

        mPassMissionButton.setVisibility(View.GONE);
        mFailMissionButton.setVisibility(View.GONE);
        mPlayHintView.setText("Wait for others on the mission team to pass/fail the mission");
        mPlayHintView.setVisibility(View.VISIBLE);

        //send a reject message
        JSONObject jsonMessage = new JSONObject();
        try {
            jsonMessage.put("successFail", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PendingResult<GameManagerClient.GameManagerResult> result =
                gameManagerClient.sendGameRequest(jsonMessage);
        result.setResultCallback(new ResultCallback<GameManagerClient.GameManagerResult>() {
            @Override
            public void onResult(final GameManagerClient.GameManagerResult gameManagerResult) {
                if (gameManagerResult.getStatus().isSuccess()) {
                    Toast.makeText(mActivity, "You passed the mission", Toast.LENGTH_SHORT).show();
                }
                else {
                    mPlayHintView.setVisibility(View.GONE);
                    mPassMissionButton.setVisibility(View.VISIBLE);
                    mFailMissionButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * Fail mission button click handler. Submits a fail to the receiver application.
     */
    public void onFailMissionClicked(){
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        GameManagerState state = gameManagerClient.getCurrentState();
        JSONObject gameData = state.getGameData();

        mPassMissionButton.setVisibility(View.GONE);
        mFailMissionButton.setVisibility(View.GONE);
        mPlayHintView.setText("Wait for others on the mission team to pass/fail the mission");
        mPlayHintView.setVisibility(View.VISIBLE);

        //send a reject message
        JSONObject jsonMessage = new JSONObject();
        try {
            jsonMessage.put("successFail", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PendingResult<GameManagerClient.GameManagerResult> result =
                gameManagerClient.sendGameRequest(jsonMessage);
        result.setResultCallback(new ResultCallback<GameManagerClient.GameManagerResult>() {
            @Override
            public void onResult(final GameManagerClient.GameManagerResult gameManagerResult) {
                if (gameManagerResult.getStatus().isSuccess()) {
                    Toast.makeText(mActivity, "You failed the mission", Toast.LENGTH_SHORT).show();
                }
                else {
                    mPlayHintView.setVisibility(View.GONE);
                    mPassMissionButton.setVisibility(View.VISIBLE);
                    mFailMissionButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * Submit assassination target button click handler. Submits assassination target to the
     * receiver application.
     */
    public void onSubmitAssassinClicked(){
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        GameManagerState state = gameManagerClient.getCurrentState();
        JSONObject gameData = state.getGameData();

        RadioButton targetButton = (RadioButton) mActivity.
                findViewById(mTargetsContainer.getCheckedRadioButtonId());

        String targetId = (String) targetButton.getTag();

        JSONObject jsonMessage = new JSONObject();
        try {
            jsonMessage.put("assassinGuess", targetId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PendingResult<GameManagerClient.GameManagerResult> result =
                gameManagerClient.sendGameRequest(jsonMessage);
        result.setResultCallback(new ResultCallback<GameManagerClient.GameManagerResult>() {
            @Override
            public void onResult(final GameManagerClient.GameManagerResult gameManagerResult) {
                if (gameManagerResult.getStatus().isSuccess()) {
                    Toast.makeText(mActivity, "You assassinate someone", Toast.LENGTH_SHORT).show();
                }
                else {

                }
            }
        });
    }
}
