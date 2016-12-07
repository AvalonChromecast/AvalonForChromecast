package edu.wisc.ece.avalonforchromecast;

import com.google.android.gms.cast.games.GameManagerClient;
import com.google.android.gms.cast.games.GameManagerState;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

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

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitClicked();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy(){
        //sendPlayerQuitRequest();
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
        GameManagerState state = gameManagerClient.getCurrentState();
        JSONObject gameData = state.getGameData();

        String setupLeaderId = "";
        String setupLeaderName = "";
        try {
            setupLeaderId = gameData.getString("setupLeader");
            setupLeaderName = gameData.getString("setupLeaderName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String playerId = ((MainActivity) getActivity()).getPlayerId();
        if(!playerId.equals(setupLeaderId)){
            mTitleTextView.setText("Wait for " + setupLeaderName + " to start the game.");
            mMerlinCheckBox.setVisibility(View.GONE);
            mAssassinCheckBox.setVisibility(View.GONE);
            mPercivalCheckBox.setVisibility(View.GONE);
            mMorganaCheckBox.setVisibility(View.GONE);
            mOberonCheckBox.setVisibility(View.GONE);
            mMordredCheckBox.setVisibility(View.GONE);
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    /**
     * Submit selected roles to receiver
     */
    private void onSubmitClicked() {
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        if (mCastConnectionManager.isConnectedToReceiver()) {
            // create array of selected roles
            ArrayList<String> selectedRoles = new ArrayList<>();
            ArrayList<View> roleButtons;
            roleButtons = mCheckboxLayout.getTouchables();

            for(int i = 0; i < roleButtons.size(); i++){
                CheckBox currRole = (CheckBox) roleButtons.get(i);
                if(currRole.isChecked()){
                    selectedRoles.add(((String)currRole.getText()).toLowerCase());
                }
            }
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
                        Toast.makeText(getActivity(), "Start Game was successful", Toast.LENGTH_SHORT);
                        ((MainActivity) getActivity())
                                .setPlayerState(gameManagerClient.getCurrentState().getPlayer(
                                        gameManagerResult.getPlayerId()).getPlayerState());

                    } else {
                        mCastConnectionManager.disconnectFromReceiver(false);
                        Utils.showErrorDialog(getActivity(),
                                gameManagerResult.getStatus().getStatusMessage());
                    }
                }
            });
        }
    }
}
