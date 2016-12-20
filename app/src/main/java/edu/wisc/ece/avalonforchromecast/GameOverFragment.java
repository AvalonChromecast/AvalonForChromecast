package edu.wisc.ece.avalonforchromecast;

import com.google.android.gms.cast.games.GameManagerClient;
import com.google.android.gms.cast.games.GameManagerState;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Fragment displayed during Game Over phase.
 */
public class GameOverFragment extends GameFragment{

    private static final String TAG = "GameOverFragment";

    private TextView mGameOverView;
    private Button mLobbyButton;
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
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mActivity = activity;
    }

    /**
     * display whether player won or not.
     */
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

        String loyalty = ((MainActivity) mActivity).getLoyalty();
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
     * Listener for GameManagerState change. Sets player state if player state has changed, which
     * also calls updateFragments.
     */
    @Override
    public void onStateChanged(GameManagerState newState,
                               GameManagerState oldState) {
        if(newState.hasGameDataChanged(oldState)) {
            if(newState.getGameData() != null) {
//                ((MainActivity) mActivity).updateFragments();
                ((MainActivity) mActivity).setPlayerState(newState.getPlayer(
                        ((MainActivity) mActivity).getPlayerId()).getPlayerState());
            }
        }
    }

    /**
     * Lobby button click listener. Move the players back to lobby.
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
                    ((MainActivity) mActivity).setPlayerState(gameManagerClient.getCurrentState().getPlayer(
                            gameManagerResult.getPlayerId()).getPlayerState());
                }
                else {

                }
            }
        });

    }
}
