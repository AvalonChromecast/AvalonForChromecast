package edu.wisc.ece.avalonforchromecast;

import com.google.android.gms.cast.games.GameManagerClient;
import com.google.android.gms.cast.games.GameManagerState;
import com.google.android.gms.cast.games.PlayerInfo;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


/**
 * Fragment for drawing.
 * Based on https://github.com/playgameservices/8bitartist
 */
public class GameOverFragment extends GameFragment{

    private static final String TAG = "LobbyFragment";

    private TextView mGameOverView;
    private Button mLobbyButton;

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
        View view = inflater.inflate(R.layout.game_over_fragment, container, false);

        mGameOverView = (TextView) view.findViewById(R.id.gameOverView);

        mLobbyButton = (Button) view.findViewById(R.id.lobbyButton);

        mLobbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {onLobbyButtonClicked();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        if(gameManagerClient == null){
            Log.d(TAG, "gamemanagerClient is somehow null.");
            return;
        }
        GameManagerState state = gameManagerClient.getCurrentState();
        JSONObject gameData = state.getGameData();

        boolean goodWins = false;
        try {
            goodWins = gameData.getBoolean("goodWins");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String loyalty = ((MainActivity) getActivity()).getLoyalty();
        boolean isGood = false;
        if(loyalty.equals("good")){
            isGood = true;
        }
        else if(loyalty.equals("evil")){
            isGood = false;
        }

        if(goodWins == isGood){
            mGameOverView.setText("You are on the winning team");
        }
        else{
            mGameOverView.setText("You are on the losing team");
        }
    }

    /**
     * Game state callback handler.
     */
    @Override
    public void onStateChanged(GameManagerState newState,
                               GameManagerState oldState) {
        if(newState.hasGameDataChanged(oldState)) {
            if(newState.getGameData() != null) {
                ((MainActivity) getActivity()).updateFragments();
            }
        }
    }

    /**
     * Move the players back to lobby.
     */
    public void onLobbyButtonClicked(){
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        GameManagerState state = gameManagerClient.getCurrentState();
        JSONObject gameData = state.getGameData();

        JSONObject reset = new JSONObject();
        try {
            reset.put("reset", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PendingResult<GameManagerClient.GameManagerResult> result =
                gameManagerClient.sendGameRequest(reset);
        result.setResultCallback(new ResultCallback<GameManagerClient.GameManagerResult>() {
            @Override
            public void onResult(final GameManagerClient.GameManagerResult gameManagerResult) {
                if (gameManagerResult.getStatus().isSuccess()) {
                    ((MainActivity) getActivity()).setPlayerState(gameManagerClient.getCurrentState().getPlayer(
                            gameManagerResult.getPlayerId()).getPlayerState());
                }
                else {

                }
            }
        });

    }
}
