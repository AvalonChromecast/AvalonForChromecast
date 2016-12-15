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

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment displayed while the player is in the game lobby.
 */
public class SetupFragment extends GameFragment {

    private static final String TAG = "SetupFragment";

    private TextView mTitleTextView;

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
            public void onClick(View v){
                onMerlinClicked();
            }
        });
        mPercivalCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                onPercivalClicked();
            }
        });
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitClicked();
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
        //GameManagerState state = gameManagerClient.getCurrentState();
        //JSONObject gameData = state.getGameData();


        if(!getSetupLeader(gameManagerClient)){
            mTitleTextView.setText("Wait for setup leader to start the game.");
            mCheckboxLayout.setVisibility(View.GONE);
            mSubmitButton.setVisibility(View.GONE);
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
}
