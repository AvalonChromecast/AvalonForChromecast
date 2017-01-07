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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A fragment displayed while the player is in the game lobby.
 */
public class LobbyFragment extends GameFragment {

    private static final String TAG = "LobbyFragment";

    private TextView mTextLabel;
    private EditText mNameEditText;
    private Button mJoinSetButton;
    private Button mBackButton;
    private Button mStartButton;

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
        View view = inflater.inflate(R.layout.lobby_fragment, container, false);

        mTextLabel = (TextView) view.findViewById(R.id.text_label);
        mNameEditText = (EditText) view.findViewById(R.id.name);
        mJoinSetButton = (Button) view.findViewById(R.id.join_set_button);
        mBackButton = (Button) view.findViewById(R.id.back_button);
        mStartButton = (Button) view.findViewById(R.id.start_button);

        mJoinSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onJoinSetClicked();
            }
        });
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackClicked();
            }
        });
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartClicked();
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
        updateView();
    }

    @Override
    public void onStart() {
        super.onStart();
        mJoinSetButton.setText(R.string.button_join);
        updateView();
    }

    @Override
    public void onDestroy(){
        //sendPlayerQuitRequest();
        super.onDestroy();
    }

    /**
     * Join/Set button click handler. Set player state to ready.
     */
    private void onJoinSetClicked() {
        GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        GameManagerState state = gameManagerClient.getCurrentState();

        int playerState = ((MainActivity) mActivity).getPlayerState();
        if (playerState == GameManagerClient.PLAYER_STATE_AVAILABLE || playerState == GameManagerClient.PLAYER_STATE_READY) {
            //if lobby state is closed
            if (state.getLobbyState() == GameManagerClient.LOBBY_STATE_CLOSED) {
                Toast.makeText(mActivity, "Please wait until the game is finished", Toast.LENGTH_SHORT).show();
            } else {
                ((MainActivity) mActivity).setPlayerName(mNameEditText.getText().toString());
                sendPlayerReadyRequest();
            }
        }
        updateView();
    }

    /**
     * Back button click handler. Quits player from game and returns them to main screen.
     */
    private void onBackClicked(){
        sendPlayerQuitRequest();
        mCastConnectionManager.disconnectFromReceiver(false);
    }

    /**
     * Start button click handler. Starts the game and sets player as setup leader
     */
    private void onStartClicked(){
        GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        GameManagerState state = gameManagerClient.getCurrentState();

        int playerState = ((MainActivity) mActivity).getPlayerState();
        if (playerState == GameManagerClient.PLAYER_STATE_AVAILABLE) {
            Toast.makeText(mActivity, "Enter your name first.", Toast.LENGTH_SHORT).show();
        } else if (playerState == GameManagerClient.PLAYER_STATE_READY) {
            if(state.getLobbyState() == GameManagerClient.LOBBY_STATE_CLOSED){
                Toast.makeText(mActivity, "Please wait until the game is finished", Toast.LENGTH_SHORT).show();
            }
            else{
                ((MainActivity) mActivity).setPlayerName(mNameEditText.getText().toString());
                sendStartGameRequest();
            }
        }
        updateView();
    }

    /**
     * Change the player state to PLAYER_STATE_READY while setting player name.
     */
    public void sendPlayerReadyRequest() {
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        if (mCastConnectionManager.isConnectedToReceiver()) {
            // Send player name to the receiver
            final JSONObject jsonMessage = new JSONObject();
            try {
                jsonMessage.put("playerName", mNameEditText.getText().toString());
            } catch (JSONException e) {
                Log.e(TAG, "Error creating JSON message", e);
                return;
            }
            PendingResult<GameManagerClient.GameManagerResult> result =
                    gameManagerClient.sendPlayerReadyRequest(jsonMessage);
            result.setResultCallback(new ResultCallback<GameManagerClient.GameManagerResult>() {
                @Override
                public void onResult(final GameManagerClient.GameManagerResult gameManagerResult) {
                    if (gameManagerResult.getStatus().isSuccess()) {
                        final JSONObject jsonMessage2 = new JSONObject();
                        try {
                            jsonMessage2.put("updatePlayerList", true);
                        } catch (JSONException e){
                            Log.e(TAG, "Error creating JSON message", e);
                            return;
                        }
                        Log.d(TAG, "sent updatePlayerList signal");
                        gameManagerClient.sendGameMessage(jsonMessage2);
                        ((MainActivity) mActivity)
                                .setPlayerState(gameManagerClient.getCurrentState().getPlayer(
                                        gameManagerResult.getPlayerId()).getPlayerState());
                    } else {
                        mCastConnectionManager.disconnectFromReceiver(false);
                        Utils.showErrorDialog(mActivity,
                                gameManagerResult.getStatus().getStatusMessage());
                    }
                    updateView();
                }
            });
        }
        updateView();
    }

    /**
     * Change the player state to PLAYER_STATE_PLAYING. Also will signal to receiver to start the
     * game.
     */
    public void sendStartGameRequest() {
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        if (mCastConnectionManager.isConnectedToReceiver()) {

            // Send player name to the receiver
            JSONObject jsonMessage = new JSONObject();
            try {
                jsonMessage.put("startGame", "true");
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
                        //Toast.makeText(mActivity, "Start Game success, you're setup leader", Toast.LENGTH_SHORT).show();
                        ((MainActivity) mActivity)
                                .setPlayerState(gameManagerClient.getCurrentState().getPlayer(
                                        gameManagerResult.getPlayerId()).getPlayerState());
                    } else if(gameManagerResult.getStatus().getStatusCode() == GameManagerClient.STATUS_TOO_MANY_PLAYERS){
                        Toast.makeText(mActivity, "Please have 5 to 10 players", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        mCastConnectionManager.disconnectFromReceiver(false);
                        Utils.showErrorDialog(mActivity,
                                gameManagerResult.getStatus().getStatusMessage());
                    }
                    updateView();
                }
            });
        }
        updateView();
    }

    /**
     * Disconnects player from the chromecast.
     */
    public void sendPlayerQuitRequest(){
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        if (mCastConnectionManager.isConnectedToReceiver()) {
            // Send player name to the receiver
            final JSONObject jsonMessage = new JSONObject();
            try {
                jsonMessage.put("playerName", mNameEditText.getText().toString());
            } catch (JSONException e) {
                Log.e(TAG, "Error creating JSON message", e);
                return;
            }
            PendingResult<GameManagerClient.GameManagerResult> result =
                    gameManagerClient.sendPlayerQuitRequest(jsonMessage);
            result.setResultCallback(new ResultCallback<GameManagerClient.GameManagerResult>() {
                @Override
                public void onResult(final GameManagerClient.GameManagerResult gameManagerResult) {
                    if (gameManagerResult.getStatus().isSuccess()) {
                        ((MainActivity) mActivity)
                                .setPlayerState(gameManagerClient.getCurrentState().getPlayer(
                                        gameManagerResult.getPlayerId()).getPlayerState());
                    } else {
                        mCastConnectionManager.disconnectFromReceiver(false);
                        Utils.showErrorDialog(mActivity,
                                gameManagerResult.getStatus().getStatusMessage());
                    }
                    updateView();
                }
            });
        }
        updateView();
    }

    /**
     * Update the UI based on the current lobby and player state. The player has to first join
     * the lobby and then start the game.
     */
    private void updateView() {
        if (((MainActivity) mActivity).getPlayerName() == null) {
            mNameEditText.setText("");
        }
        int playerState = ((MainActivity) mActivity).getPlayerState();
        GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        if (mCastConnectionManager.isConnectedToReceiver()) {
            GameManagerState gameManagerState = gameManagerClient.getCurrentState();
            if (gameManagerState.getLobbyState() == GameManagerClient.LOBBY_STATE_OPEN) {
                mJoinSetButton.setEnabled(true);
                mTextLabel.setText(R.string.lobby_open);
                if (playerState == GameManagerClient.PLAYER_STATE_AVAILABLE) {
                    mJoinSetButton.setText(R.string.button_join);
                    mStartButton.setEnabled(false);
                } else if (playerState == GameManagerClient.PLAYER_STATE_READY) {
                    mJoinSetButton.setText(R.string.button_set);
                    mStartButton.setEnabled(true);
                }
            } else {
                mJoinSetButton.setEnabled(false);
                mStartButton.setEnabled(false);
                mTextLabel.setText(R.string.lobby_closed);
            }
        }
    }

    /**
     * listener for if player state has changed or game data has changed
     */
    @Override
    public void onStateChanged(GameManagerState newState, GameManagerState oldState){
        Log.d(TAG, "Enter lobbyfragment's onStateChanged");
        String playerId = ((MainActivity) mActivity).getPlayerId();
        if(newState.hasPlayerStateChanged(playerId, oldState)){
            ((MainActivity) mActivity).setPlayerState(newState.getPlayer(
                    playerId).getPlayerState());
        }
        updateView();
    }
}
