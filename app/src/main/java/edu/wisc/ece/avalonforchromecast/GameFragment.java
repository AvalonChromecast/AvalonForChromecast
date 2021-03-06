package edu.wisc.ece.avalonforchromecast;

import com.google.android.gms.cast.games.GameManagerClient;
import com.google.android.gms.cast.games.GameManagerState;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import java.util.Observable;
import java.util.Observer;

/**
 * A base class for all the fragments in the game.
 */
public class GameFragment extends Fragment implements Observer, GameManagerClient.Listener {

    private static final String TAG = "GameFragment";

    private Activity mActivity;

    protected CastConnectionManager mCastConnectionManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCastConnectionManager = ((MainActivity) mActivity).getCastConnectionManager();
        mCastConnectionManager.addObserver(this);
        if (mCastConnectionManager.getGameManagerClient() != null) {
            mCastConnectionManager.getGameManagerClient().setListener(this);
        }
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCastConnectionManager.deleteObserver(this);
    }

    /**
     * GameManagerClient observer callback.
     */
    @Override
    public void update(Observable object, Object data) {
        // no-op
    }

    /**
     * Game state callback handler.
     */
    @Override
    public void onStateChanged(GameManagerState newState,
            GameManagerState oldState) {
        ((MainActivity) mActivity)
                .setPlayerState(newState.getPlayer(
                        ((MainActivity) mActivity).getPlayerId()).getPlayerState());
    }

    /**
     * Game message callback handler.
     */
    @Override
    public void onGameMessageReceived(String playerId, JSONObject message) {
        // no-op
    }
}
