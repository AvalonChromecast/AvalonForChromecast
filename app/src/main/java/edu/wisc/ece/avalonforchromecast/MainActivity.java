package edu.wisc.ece.avalonforchromecast;

import com.google.android.gms.cast.games.GameManagerClient;
import com.google.android.gms.cast.games.GameManagerState;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

/**
 * The main activity. Fragments are loaded for the various stages in the UI.
 */
public class MainActivity extends AppCompatActivity implements Observer {

    private static final String TAG = "MainActivity";

    private CastConnectionFragment mCastConnectionFragment;
    private LobbyFragment mLobbyFragment;
    private PlayingFragment mPlayingFragment;
    private SetupFragment mSetupFragment;
    private GameOverFragment mGameOverFragment;
    private int mPlayerState = GameManagerClient.PLAYER_STATE_UNKNOWN;
    private String mPlayerName;
    private String mPlayerId;
    private String mLoyalty;

    private CastConnectionManager mCastConnectionManager;

    private static final int LOBBY_PHASE = 0;
    private static final int SETUP_PHASE = 1;
    private static final int GAMEOVER_PHASE = 6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Set up Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCastConnectionManager = new CastConnectionManager(this,
                getResources().getString(R.string.app_id));
        mCastConnectionFragment = new CastConnectionFragment();
        mPlayingFragment = new PlayingFragment();
        mLobbyFragment = new LobbyFragment();
        mSetupFragment = new SetupFragment();
        mGameOverFragment = new GameOverFragment();

        updateFragments();
    }

    public CastConnectionManager getCastConnectionManager() {
        return mCastConnectionManager;
    }

    /**
     * Called when the options menu is first created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        if (mediaRouteActionProvider == null) {
            Log.w(TAG, "mediaRouteActionProvider is null!");
            return false;
        }
        mediaRouteActionProvider.setRouteSelector(mCastConnectionManager.getMediaRouteSelector());
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCastConnectionManager.startScan();
        mCastConnectionManager.addObserver(this);
        updateFragments();
    }

    @Override
    protected void onPause() {
        mCastConnectionManager.stopScan();
        mCastConnectionManager.deleteObserver(this);
        super.onPause();
    }

    /**
     * Called when the cast connection changes.
     */
    @Override
    public void update(Observable object, Object data) {
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        if (mCastConnectionManager.isConnectedToReceiver()) {
            PendingResult<GameManagerClient.GameManagerResult> result =
                    gameManagerClient.sendPlayerAvailableRequest(null);
            result.setResultCallback(new ResultCallback<GameManagerClient.GameManagerResult>() {
                @Override
                public void onResult(final GameManagerClient.GameManagerResult gameManagerResult) {
                    if (gameManagerResult.getStatus().isSuccess()) {
                        Log.d(TAG, "Player ID: " + gameManagerResult.getPlayerId());
                        mPlayerId = gameManagerResult.getPlayerId();
                        mPlayerState = gameManagerClient.getCurrentState().getPlayer(
                                gameManagerResult.getPlayerId()).getPlayerState();
                    } else {
                        mCastConnectionManager.disconnectFromReceiver(false);
                        Utils.showErrorDialog(MainActivity.this,
                                gameManagerResult.getStatus().getStatusMessage());
                    }
                    updateFragments();
                }
            });
        }
        updateFragments();
    }

    public void updateFragments() {
        if (isChangingConfigurations() || isFinishing() || isDestroyed()) {
            return;
        }
        Fragment fragment;
        if (!mCastConnectionManager.isConnectedToReceiver()) {
            mPlayerName = null;
            fragment = mCastConnectionFragment;
        } else {
            GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
            if(gameManagerClient == null){
                return;
            }
            GameManagerState state = gameManagerClient.getCurrentState();
            JSONObject gameData = state.getGameData();

            int gamePhase = LOBBY_PHASE;

            try {
                gamePhase = gameData.getInt("phase");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "gamePhase is " + gamePhase);

            if (mPlayerState == GameManagerClient.PLAYER_STATE_PLAYING) {
                Log.d(TAG, "Player State is PLAYING");
                if(gamePhase == SETUP_PHASE){
                    fragment = new SetupFragment();
                }
                else if(gamePhase == GAMEOVER_PHASE){
                    fragment = mGameOverFragment;
                }
                else{
                    fragment = mPlayingFragment;
                }
            }
            //this happens when player state is ready or game phase is LOBBY_PHASE
            else {
                Log.d(TAG, "Player State is READY or AVAILABLE");
                    fragment = mLobbyFragment;
            }
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        // Do not pop any fragments, just act like the home button.
        moveTaskToBack(true);
    }

    public int getPlayerState() {
        return mPlayerState;
    }

    public void setPlayerState(int state) {
        Log.d(TAG, "setting player state: " + state);
        mPlayerState = state;
        updateFragments();
    }

    public String getPlayerName() {
        return mPlayerName;
    }

    public String getPlayerId(){
        return mPlayerId;
    }

    public void setPlayerName(String playerName) {
        mPlayerName = playerName;
    }

    public void setLoyalty(String loyalty){
        mLoyalty = loyalty;
    }

    public String getLoyalty(){
        return mLoyalty;
    }
}
