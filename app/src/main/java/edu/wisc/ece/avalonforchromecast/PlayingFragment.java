package edu.wisc.ece.avalonforchromecast;

import com.google.android.gms.cast.games.GameManagerClient;
import com.google.android.gms.cast.games.GameManagerState;
import com.google.android.gms.cast.games.PlayerInfo;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Fragment for drawing.
 * Based on https://github.com/playgameservices/8bitartist
 */
public class PlayingFragment extends GameFragment{


    private static final String TAG = "LobbyFragment";

    private TextView mPlayerRoleTextView;
    private TextView mOtherInfoTextView;
    private Button mSubmitSelectionButton;
    private Button mApproveSelectionButton;
    private Button mRejectSelectionButton;
    private Button mPassMissionButton;
    private Button mFailMissionButton;

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

        mPlayerRoleTextView = (TextView) view.findViewById(R.id.roleTextView);
        mOtherInfoTextView = (TextView) view.findViewById(R.id.otherInfoPlaceholderTextView);
        mSubmitSelectionButton = (Button) view.findViewById(R.id.submitSelectionButton);
        mApproveSelectionButton = (Button) view.findViewById(R.id.approveSelectionButton);
        mRejectSelectionButton = (Button) view.findViewById(R.id.rejectSelectionButton);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    /**
     * Game state callback handler.
     */
    @Override
    public void onStateChanged(GameManagerState newState,
                               GameManagerState oldState) {
        if (newState.hasLobbyStateChanged(oldState)) {
            Log.d(TAG, "onLobbyStateChange: " + newState);
            mTextViewLobbyState.setText(getLobbyStateName(currentState.getLobbyState()));
        }
    }
}
