package edu.wisc.ece.avalonforchromecast;

import com.google.android.gms.cast.games.GameManagerClient;
import com.google.android.gms.cast.games.GameManagerState;
import com.google.android.gms.cast.games.PlayerInfo;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
 * Fragment for drawing.
 * Based on https://github.com/playgameservices/8bitartist
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

    private final int MERLIN_INDEX = 0;
    private final int ASSASSIN_INDEX = 1;
    private final int PERCIVAL_INDEX = 2;
    private final int MORDRED_INDEX = 3;
    private final int OBERON_INDEX = 4;
    private final int MORGANA_INDEX = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    // This be the real onCreate function where we do lots of setup
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
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
     * Game state callback handler.
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

                ((MainActivity) getActivity()).updateFragments();
            }
        }
    }

    /**
     * Called the first time selection phase is called. Initializes info on the screen, such as
     * loyalty, role, etc.
     */
    private void initialize(GameManagerState gameState, JSONObject gameData) {
        //display loyalty
        PlayerInfo player = gameState.getPlayer(((MainActivity) getActivity()).getPlayerId());
        if(player == null){
            Log.d(TAG, "player is somehow null in initialize()");
            return;
        }
        List<PlayerInfo> players = gameState.getPlayersInState(GameManagerClient.PLAYER_STATE_PLAYING);

        String loyalty = "";
        String role = "";
        String myPlayerId = ((MainActivity) getActivity()).getPlayerId();

        if(player.getPlayerData() == null) return;
        try {
            loyalty = player.getPlayerData().getString("loyalty");
            role = player.getPlayerData().getString("role");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ((MainActivity) getActivity()).setLoyalty(loyalty);

        mPlayerRoleTextView.setText(loyalty + " - " + role);
        mMissionTeamSizeView.setText("");
        mExtraInfoContainer.removeAllViews();

        boolean isEvil = loyalty.equals("evil");
        boolean isMerlin = role.equals(MERLIN);
        boolean isPercival = role.equals(PERCIVAL);
        boolean isMordred = role.equals(MORDRED);
        boolean isOberon = role.equals(OBERON);
        boolean isMorgana = role.equals(MORGANA);

        //boolean[] rolesArray= Arrays.copyOf(((MainActivity)getActivity()).getRolesArray(),6);

        TextView header = new TextView(getActivity());
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
                        TextView evils = new TextView(getActivity());
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
                TextView tv = new TextView(getActivity());
                tv.setText(merlinName);
                mExtraInfoContainer.addView(tv);
            }else {
                header.setText("Merlin or Morgana");

                Random rng = new Random();
                boolean merlinFirst = rng.nextBoolean();

                if(merlinFirst) {
                    TextView tv = new TextView(getActivity());
                    tv.setText(merlinName);
                    mExtraInfoContainer.addView(tv);

                    tv = new TextView(getActivity());
                    tv.setText(morganaName);
                    mExtraInfoContainer.addView(tv);
                } else {
                    TextView tv = new TextView(getActivity());
                    tv.setText(morganaName);
                    mExtraInfoContainer.addView(tv);

                    tv = new TextView(getActivity());
                    tv.setText(merlinName);
                    mExtraInfoContainer.addView(tv);
                }
            }
        }
    }

    /**
     * Handle selection phase.
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
        String playerId = ((MainActivity) getActivity()).getPlayerId();
        if(leaderId.equals(playerId)) {
            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 250 milliseconds
            v.vibrate(250);

            Toast.makeText(getActivity(), "You are the leader", Toast.LENGTH_LONG).show();
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
                ToggleButton playerButton = new ToggleButton(getActivity());
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
     * Handle voting phase.
     */
    public void votingPhase(GameManagerState gameState, JSONObject gameData){
        mMissionTeamSizeView.setVisibility(View.GONE);
        mPlayHintView.setVisibility(View.GONE);

        Toast.makeText(getActivity(), "Selected mission team is shown on the TV", Toast.LENGTH_LONG).show();

        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 250 milliseconds
        v.vibrate(250);

        mApproveSelectionButton.setVisibility(View.VISIBLE);
        mRejectSelectionButton.setVisibility(View.VISIBLE);
        mSubmitSelectionButton.setVisibility(View.GONE);
        mPlayerButtonsContainer.setVisibility(View.GONE);
    }

    /**
     * Handle mission phase.
     */
    public void missionPhase(GameManagerState gameState, JSONObject gameData) {

        //get players in mission team
        //if you're on the mission team, display pass or fail buttons
        mApproveSelectionButton.setVisibility(View.GONE);
        mRejectSelectionButton.setVisibility(View.GONE);
        try {
            JSONArray missionTeam = gameData.getJSONArray("missionTeam");

            String playerId = ((MainActivity) getActivity()).getPlayerId();
            boolean onMission = false;
            for (int i = 0; i < missionTeam.length(); i++) {
                if(playerId.equals(missionTeam.getString(i))){
                    onMission = true;
                    break;
                }
            }

            if (onMission) {
                Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 250 milliseconds
                v.vibrate(250);

                mPlayHintView.setVisibility(View.GONE);

                Toast.makeText(getActivity(), "Choose whether or not pass the mission", Toast.LENGTH_LONG).show();
                mPassMissionButton.setVisibility(View.VISIBLE);
                PlayerInfo player = gameState.getPlayer(((MainActivity) getActivity()).getPlayerId());
                String loyalty = player.getPlayerData().getString("loyalty");
                if(loyalty.equals("evil")) {
                    mFailMissionButton.setVisibility(View.VISIBLE);
                }
            } else {
                mPlayHintView.setText("You are not on the mission. Wait for the mission team make their selections");
                mPlayHintView.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), "You are not on the mission", Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     *Handle assassin phase.
     */
    public void assassinPhase(GameManagerState gameState, JSONObject gameData){
        mApproveSelectionButton.setVisibility(View.GONE);
        mRejectSelectionButton.setVisibility(View.GONE);
        mPassMissionButton.setVisibility(View.GONE);
        mFailMissionButton.setVisibility(View.GONE);

        PlayerInfo myPlayerInfo = gameState.getPlayer(((MainActivity) getActivity()).getPlayerId());
        String myRole = "";
        try {
            myRole = myPlayerInfo.getPlayerData().getString("role");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(myRole.equals(ASSASSIN)) {
            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 250 milliseconds
            v.vibrate(250);

            Toast.makeText(getActivity(), "You are the assassin", Toast.LENGTH_LONG).show();
            //leader view
            //remove extra buttons when entering this phase again
            mPlayHintView.setVisibility(View.GONE);

            mMissionTeamSizeView.setVisibility(View.VISIBLE);
            mPlayerButtonsContainer.removeAllViews();
            mPlayerButtonsContainer.setVisibility(View.VISIBLE);
            mSubmitSelectionButton.setVisibility(View.GONE);
            mSubmitAssassinButton.setVisibility(View.VISIBLE);

            mTargetsContainer = new RadioGroup(getActivity());
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
                    RadioButton playerButton = new RadioButton(getActivity());
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

    public void onShowHideButtonClicked(){
        if(mShowHideButton.getText().equals("Show")){
            mShowHideButton.setText("Hide");
            mPlayerRoleTextView.setVisibility(View.VISIBLE);
            mExtraInfoContainer.setVisibility(View.VISIBLE);
        }
        else{
            mShowHideButton.setText("Show");
            mPlayerRoleTextView.setVisibility(View.GONE);
            mExtraInfoContainer.setVisibility(View.GONE);
        }
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
            Toast.makeText(getActivity(), "You selected too few players", Toast.LENGTH_SHORT).show();
            return;
        } else if(selectedPlayers.size() > teamSize){
            Toast.makeText(getActivity(), "You selected too many players", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getActivity(), "You submitted a selection", Toast.LENGTH_SHORT).show();
                }
                else {

                }
            }
        });

    }

    /**
     * Button click handler. Approve team.
     */
    public void onApproveSelectionClicked(){
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        GameManagerState state = gameManagerClient.getCurrentState();
        JSONObject gameData = state.getGameData();

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
                    Toast.makeText(getActivity(), "You voted to approve", Toast.LENGTH_SHORT).show();
                    mApproveSelectionButton.setVisibility(View.GONE);
                    mRejectSelectionButton.setVisibility(View.GONE);
                }
                else {
                    mPlayHintView.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Button click handler. Reject team.
     */
    public void onRejectSelectionClicked(){
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        GameManagerState state = gameManagerClient.getCurrentState();
        JSONObject gameData = state.getGameData();

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
                    Toast.makeText(getActivity(), "You voted to reject", Toast.LENGTH_SHORT).show();
                    mApproveSelectionButton.setVisibility(View.GONE);
                    mRejectSelectionButton.setVisibility(View.GONE);
                }
                else {
                    mPlayHintView.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Button click handler. Pass mission.
     */
    public void onPassMissionClicked(){
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        GameManagerState state = gameManagerClient.getCurrentState();
        JSONObject gameData = state.getGameData();

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
                    Toast.makeText(getActivity(), "You passed the mission", Toast.LENGTH_SHORT).show();
                    mPassMissionButton.setVisibility(View.GONE);
                    mFailMissionButton.setVisibility(View.GONE);
                }
                else {
                    mPlayHintView.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Button click handler. Fail mission.
     */
    public void onFailMissionClicked(){
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        GameManagerState state = gameManagerClient.getCurrentState();
        JSONObject gameData = state.getGameData();

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
                    Toast.makeText(getActivity(), "You failed the mission", Toast.LENGTH_SHORT).show();
                    mPassMissionButton.setVisibility(View.GONE);
                    mFailMissionButton.setVisibility(View.GONE);
                }
                else {
                    mPlayHintView.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Button click handler. Submit assassination target
     */
    public void onSubmitAssassinClicked(){
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        GameManagerState state = gameManagerClient.getCurrentState();
        JSONObject gameData = state.getGameData();

        RadioButton targetButton = (RadioButton) getActivity().
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
                    Toast.makeText(getActivity(), "You assassinate someone", Toast.LENGTH_SHORT).show();
                }
                else {

                }
            }
        });
    }
}
