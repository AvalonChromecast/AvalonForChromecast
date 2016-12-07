package edu.wisc.ece.avalonforchromecast;

import com.google.android.gms.cast.games.GameManagerClient;
import com.google.android.gms.cast.games.GameManagerState;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * A fragment displayed while the player is in the game lobby.
 */
public class LobbyFragment extends GameFragment {

    private static final String TAG = "LobbyFragment";

    private EditText mNameEditText;
    private Button mJoinButton;
    private ProgressBar mSpinner;

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

        mNameEditText = (EditText) view.findViewById(R.id.name);
        mSpinner = (ProgressBar) view.findViewById(R.id.spinner);
        mJoinButton = (Button) view.findViewById(R.id.button_join);

        mJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onJoinClicked();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    @Override
    public void onDestroy(){
        //sendPlayerQuitRequest();
        super.onDestroy();
    }

    /**
     * Button click handler. Set the new player state based on the current player state.
     */
    private void onJoinClicked() {
        GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        GameManagerState state = gameManagerClient.getCurrentState();

        int playerState = ((MainActivity) getActivity()).getPlayerState();
        if (playerState == GameManagerClient.PLAYER_STATE_AVAILABLE) {
            //if lobby state is closed
            if(state.getLobbyState() == GameManagerClient.LOBBY_STATE_CLOSED){
                Toast.makeText(getActivity(), "Please wait until the game is finished", Toast.LENGTH_SHORT).show();
            }
            else{
                ((MainActivity) getActivity()).setPlayerName(mNameEditText.getText().toString());
                sendPlayerReadyRequest();
            }
        } else  {
            Log.e(TAG, "Somehow player state was not available???");
        }
        updateView();
    }

    /**
     * Change the player state to PLAYER_STATE_READY.
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
                        ((MainActivity) getActivity())
                                .setPlayerState(gameManagerClient.getCurrentState().getPlayer(
                                        gameManagerResult.getPlayerId()).getPlayerState());
                    } else {
                        mCastConnectionManager.disconnectFromReceiver(false);
                        Utils.showErrorDialog(getActivity(),
                                gameManagerResult.getStatus().getStatusMessage());
                    }
                    updateView();
                }
            });
        }
        updateView();
    }



    /**
     *  Change the player state to PLAYER_STATE_QUIT.
     */
    public void sendPlayerQuitRequest() {
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        if (mCastConnectionManager.isConnectedToReceiver()) {
            PendingResult<GameManagerClient.GameManagerResult> result =
                    gameManagerClient.sendPlayerQuitRequest(null);
            result.setResultCallback(new ResultCallback<GameManagerClient.GameManagerResult>() {
                @Override
                public void onResult(final GameManagerClient.GameManagerResult gameManagerResult) {
                    if (gameManagerResult.getStatus().isSuccess()) {
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

    /**
     * Update the UI based on the current lobby and player state. The player has to first join
     * the lobby and then start the game.
     */
    private void updateView() {
        if (((MainActivity) getActivity()).getPlayerName() == null) {
            mNameEditText.setText("");
        }
        int playerState = ((MainActivity) getActivity()).getPlayerState();
        GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        if (mCastConnectionManager.isConnectedToReceiver()) {
            GameManagerState gameManagerState = gameManagerClient.getCurrentState();
            if (gameManagerState.getLobbyState() == GameManagerClient.LOBBY_STATE_OPEN) {
                mJoinButton.setVisibility(View.VISIBLE);
                mSpinner.setVisibility(View.GONE);
            } else {
                mJoinButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onStateChanged(GameManagerState newState, GameManagerState oldState){
        //Log.d(TAG, "Enter lobbyfragment's onStateChanged");
        String playerId = ((MainActivity) getActivity()).getPlayerId();
        if(newState.hasPlayerStateChanged(playerId, oldState)){
            ((MainActivity) getActivity()).setPlayerState(newState.getPlayer(
                    playerId).getPlayerState());
            //Log.d(TAG, "Lobbyfragment's updatefragment");
        }
    }

}
