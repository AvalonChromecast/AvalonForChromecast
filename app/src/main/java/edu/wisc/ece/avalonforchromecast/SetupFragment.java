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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment displayed while the player is in the game lobby.
 */
public class SetupFragment extends GameFragment {

    private static final String TAG = "SetupFragment";

    private int bestChange = -1;
    private String suggestion = "";

    private TextView mTitleTextView;
    private TextView mPredictionView;

    private LinearLayout mCheckboxLayout;
    private CheckBox mMerlinCheckBox;
    private CheckBox mAssassinCheckBox;
    private CheckBox mPercivalCheckBox;
    private CheckBox mMordredCheckBox;
    private CheckBox mOberonCheckBox;
    private CheckBox mMorganaCheckBox;

    private Button mSubmitButton;

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
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.setup_fragment, container, false);

        mTitleTextView = (TextView) view.findViewById(R.id.titleTextView);
        mPredictionView = (TextView) view.findViewById(R.id.predictionView);

        mCheckboxLayout = (LinearLayout) view.findViewById(R.id.checkboxLayout);
        mMerlinCheckBox = (CheckBox) view.findViewById(R.id.merlinCheckbox);
        mAssassinCheckBox = (CheckBox) view.findViewById(R.id.assassinCheckbox);
        mPercivalCheckBox = (CheckBox) view.findViewById(R.id.percivalCheckbox);
        mMordredCheckBox = (CheckBox) view.findViewById(R.id.mordredCheckbox);
        mOberonCheckBox = (CheckBox) view.findViewById(R.id.oberonCheckbox);
        mMorganaCheckBox = (CheckBox) view.findViewById(R.id.morganaCheckbox);

        mSubmitButton = (Button) view.findViewById(R.id.submitButton);

        mMerlinCheckBox.setTag(MERLIN_INDEX);
        mAssassinCheckBox.setTag(ASSASSIN_INDEX);
        mPercivalCheckBox.setTag(PERCIVAL_INDEX);
        mMordredCheckBox.setTag(MORDRED_INDEX);
        mOberonCheckBox.setTag(OBERON_INDEX);
        mMorganaCheckBox.setTag(MORGANA_INDEX);

        mMerlinCheckBox.setChecked(false);
        mAssassinCheckBox.setChecked(false);
        mPercivalCheckBox.setChecked(false);
        mMordredCheckBox.setChecked(false);
        mOberonCheckBox.setChecked(false);
        mMorganaCheckBox.setChecked(false);


        mAssassinCheckBox.setEnabled(false);
        mPercivalCheckBox.setEnabled(false);
        mMordredCheckBox.setEnabled(false);
        mMorganaCheckBox.setEnabled(false);

        mMerlinCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMerlinClicked();
                setupSuggestions();
            }
        });
        mPercivalCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                onPercivalClicked();
                setupSuggestions();
            }
        });
        mMordredCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                setupSuggestions();
            }
        });
        mMorganaCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                setupSuggestions();
            }
        });
        mOberonCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                setupSuggestions();
            }
        });
        mAssassinCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                setupSuggestions();
            }
        });
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitClicked();
            }
        });
        mPredictionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                displaySuggestion();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();

        // display buttons if setup leader, else display only title
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        if(gameManagerClient == null){
            Log.d(TAG, "gameManagerClient in onStart is somehow null.");
            return;
        }

        if(!getSetupLeader(gameManagerClient)){
            mTitleTextView.setText("Wait for setup leader to start the game.");
            mCheckboxLayout.setVisibility(View.GONE);
            mSubmitButton.setVisibility(View.GONE);
            mPredictionView.setVisibility(View.GONE);
        }
    }

    private boolean getSetupLeader(GameManagerClient gameManagerClient) {
        GameManagerState state = gameManagerClient.getCurrentState();
        JSONObject gameData = state.getGameData();

        String setupLeaderID = "";
        try {
            setupLeaderID = gameData.getString("setupLeader");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String playerID = ((MainActivity)mActivity).getPlayerId();
        Log.d(TAG,"playerID, setupLeaderID: " + playerID + ", " + setupLeaderID);
        return playerID.equals(setupLeaderID);

    }

    private void onMerlinClicked() {
        if(mMerlinCheckBox.isChecked()){
            mAssassinCheckBox.setEnabled(true);
            mPercivalCheckBox.setEnabled(true);
            mMordredCheckBox.setEnabled(true);
        } else {
            mAssassinCheckBox.setChecked(false);
            mPercivalCheckBox.setChecked(false);
            mMordredCheckBox.setChecked(false);
            mMorganaCheckBox.setChecked(false);
            mAssassinCheckBox.setEnabled(false);
            mPercivalCheckBox.setEnabled(false);
            mMordredCheckBox.setEnabled(false);
            mMorganaCheckBox.setEnabled(false);
        }
    }

    private void onPercivalClicked() {
        if(mPercivalCheckBox.isChecked()){
            mMorganaCheckBox.setEnabled(true);
        } else {
            mMorganaCheckBox.setChecked(false);
            mMorganaCheckBox.setEnabled(false);

        }
    }

    /**
     * Submit selected roles to receiver
     */
    private void onSubmitClicked() {
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        if (mCastConnectionManager.isConnectedToReceiver()) {
            // rolesArray
            boolean[] rolesArray = new boolean[6];
            // create array of selected roles
            ArrayList<String> selectedRoles = new ArrayList<>();
            ArrayList<View> roleButtons;
            roleButtons = mCheckboxLayout.getTouchables();

            // keep track of number of evil roles that are checked
            int numEvilChecked = 0;
            for(int i = 0; i < roleButtons.size(); i++){
                CheckBox currRole = (CheckBox) roleButtons.get(i);
                if(currRole.isChecked()){
                    String roleName = ((String)currRole.getText()).toLowerCase();
                    selectedRoles.add(roleName);
                    rolesArray[(int)currRole.getTag()] = true;
                    //increment numEvilChecked
                    if(roleName.equals("assassin") || roleName.equals("mordred") ||
                            roleName.equals("oberon") || roleName.equals("morgana")){
                        numEvilChecked++;
                    }
                }
            }
            //check if numEvilChecked is too large
            GameManagerState state = gameManagerClient.getCurrentState();
            List<PlayerInfo> players = state.getPlayersInState(GameManagerClient.PLAYER_STATE_PLAYING);
            int maxEvilChecked = (int)Math.ceil(players.size() / 3);
            Log.d(TAG, "numEvil, maxEvil: " + numEvilChecked + ", " + maxEvilChecked);
            if(numEvilChecked > maxEvilChecked){
                Toast.makeText(mActivity, "You've selected too many evil roles", Toast.LENGTH_SHORT).show();
                return;
            }
            //update mRolesArray
            //((MainActivity)mActivity).setRolesArray(rolesArray);
            // Send selected roles to the receiver
            JSONObject jsonMessage = new JSONObject();
            try {
                jsonMessage.put("selectedRoles", new JSONArray((selectedRoles)));
            } catch (JSONException e) {
                Log.e(TAG, "Error creating JSON message", e);
                return;
            }
            PendingResult<GameManagerClient.GameManagerResult> result =
                    gameManagerClient.sendGameRequest(jsonMessage);
            result.setResultCallback(new ResultCallback<GameManagerClient.GameManagerResult>() {
                @Override
                public void onResult(final GameManagerClient.GameManagerResult gameManagerResult) {
                    if (gameManagerResult.getStatus().isSuccess()) {
                        Toast.makeText(mActivity, "Start Game was successful", Toast.LENGTH_SHORT).show();
                        ((MainActivity) mActivity)
                                .setPlayerState(gameManagerClient.getCurrentState().getPlayer(
                                        gameManagerResult.getPlayerId()).getPlayerState());

                    } else {
                        Toast.makeText(mActivity, "Something wrong????", Toast.LENGTH_SHORT).show();
                        mCastConnectionManager.disconnectFromReceiver(false);
                        Utils.showErrorDialog(mActivity,
                                gameManagerResult.getStatus().getStatusMessage());
                    }
                }
            });
        }
    }

    private double[] merlin =   {.8, .85, .9, .95, .95, .95};
    private double[] assassin = {-.99, -.90, -.85, -.8, -.75, -.65};
    private double[] percival = {.4, .25, .43, .48, .52, .37};
    private double[] morgana =  {-.4, -.25, -.43, -.48, -.52, -.37};
    private double[] oberon =   {.6, .6, .5, .5, .5, .4};
    private double[] mordred =  {-.6, -.6, -.5, -.5, -.5, -.4};



    private void setupSuggestions() {
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        GameManagerState state = gameManagerClient.getCurrentState();
        JSONObject gameData = state.getGameData();

        int playerNum = state.getPlayersInState(GameManagerClient.PLAYER_STATE_PLAYING).size();
        if(playerNum < 5 || playerNum > 10){
            return;
        }

        double selectionBalance = 0.0;

        boolean[] roles = new boolean[6];

        ArrayList<View> roleButtons;
        roleButtons = mCheckboxLayout.getTouchables();

        ArrayList<View> selectedRoles;
        for (int i = 0; i < roleButtons.size(); i++) {
            CheckBox currRole = (CheckBox) roleButtons.get(i);
            if(currRole.isChecked()){
                switch ((int)currRole.getTag()) {
                    case MERLIN_INDEX:
                        roles[MERLIN_INDEX] = true;
                        selectionBalance += merlin[playerNum - 5];
                        break;
                    case ASSASSIN_INDEX:
                        roles[ASSASSIN_INDEX] = true;
                        selectionBalance += assassin[playerNum - 5];
                        break;
                    case PERCIVAL_INDEX:
                        roles[PERCIVAL_INDEX] = true;
                        selectionBalance += percival[playerNum - 5];
                        break;
                    case OBERON_INDEX:
                        roles[OBERON_INDEX] = true;
                        selectionBalance += oberon[playerNum - 5];
                        break;
                    case MORGANA_INDEX:
                        roles[MORGANA_INDEX] = true;
                        selectionBalance += morgana[playerNum - 5];
                        break;
                    case MORDRED_INDEX:
                        roles[MORDRED_INDEX] = true;
                        selectionBalance += mordred[playerNum - 5];
                        break;
                }
            }
        }

        double oldBalance = selectionBalance;
        double bestBalance = Math.abs(oldBalance);

        if (Math.abs(selectionBalance) > 0.2) {
            // Try adding role
            for ( int i = 0; i < roles.length; i++) {
                if (followsRules(roles, i)) {
                    switch (i) {
                        case MERLIN_INDEX:
                            if(roles[i])
                                selectionBalance -= merlin[playerNum - 5];
                            else
                                selectionBalance += merlin[playerNum - 5];
                            break;
                        case ASSASSIN_INDEX:
                            if(roles[i])
                                selectionBalance -= assassin[playerNum - 5];
                            else
                                selectionBalance += assassin[playerNum - 5];
                            break;
                        case PERCIVAL_INDEX:
                            if(roles[i])
                                selectionBalance -= percival[playerNum - 5];
                            else
                                selectionBalance += percival[playerNum - 5];
                            break;
                        case MORDRED_INDEX:
                            if(roles[i])
                                selectionBalance -= mordred[playerNum - 5];
                            else
                                selectionBalance += mordred[playerNum - 5];
                            break;
                        case MORGANA_INDEX:
                            if(roles[i])
                                selectionBalance -= morgana[playerNum - 5];
                            else
                                selectionBalance += morgana[playerNum - 5];
                            break;
                        case OBERON_INDEX:
                            if(roles[i])
                                selectionBalance -= oberon[playerNum - 5];
                            else
                                selectionBalance += oberon[playerNum - 5];
                            break;
                    }
                }
                if (Math.abs(selectionBalance) < bestBalance) {
                    bestBalance = Math.abs(selectionBalance);
                    bestChange = i;
                    switch (i) {
                        case MERLIN_INDEX:
                            if(roles[i])
                                suggestion = "Try deselecting Merlin";
                            else
                                suggestion = "Try selecting Merlin";
                            break;
                        case ASSASSIN_INDEX:
                            if(roles[i])
                                suggestion = "Try deselecting Assassin";
                            else
                                suggestion = "Try selecting Assassin";
                            break;
                        case PERCIVAL_INDEX:
                            if(roles[i])
                                suggestion = "Try deselecting Percival";
                            else
                                suggestion = "Try selecting Percival";
                            break;
                        case MORDRED_INDEX:
                            if(roles[i])
                                suggestion = "Try deselecting Mordred";
                            else
                                suggestion = "Try selecting Mordred";
                            break;
                        case MORGANA_INDEX:
                            if(roles[i])
                                suggestion = "Try deselecting Morgana";
                            else
                                suggestion = "Try selecting Morgana";
                            break;
                        case OBERON_INDEX:
                            if(roles[i])
                                suggestion = "Try deselecting Oberon";
                            else
                                suggestion = "Try selecting Oberon";
                            break;
                        default:
                            Log.e(TAG, "error???");
                            break;
                    }
                }
                selectionBalance = oldBalance;
            }
        }

        double winPercentage = Math.min(0.9999,(selectionBalance + 1.0)/2.0) * 100;

        DecimalFormat df = new DecimalFormat("#.##");
        mPredictionView.setText("Good wins " + df.format(winPercentage) + "% of the time with this setup");
    }

    private void displaySuggestion() {
        Toast.makeText(mActivity, suggestion, Toast.LENGTH_LONG).show();
    }

    private boolean followsRules(boolean[] roles, int i){
        if(i==MERLIN_INDEX && roles[i]){
            return !(roles[ASSASSIN_INDEX] || roles[MORDRED_INDEX]
                    || roles[PERCIVAL_INDEX] || roles[MORGANA_INDEX]);
        } else if((i==ASSASSIN_INDEX || i==MORDRED_INDEX || i==PERCIVAL_INDEX || i==MORGANA_INDEX) && !roles[i]){
            if(i==MORGANA_INDEX) return roles[MERLIN_INDEX] || roles[PERCIVAL_INDEX];
            return roles[MERLIN_INDEX];
        } else if(i==PERCIVAL_INDEX && roles[i]){
            return !roles[MORGANA_INDEX];
        } else {return true;}
    }

}

