package edu.wisc.ece.avalonforchromecast;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A fragment displayed while the player is in the game lobby.
 */
public class PauseFragment extends GameFragment {

    private static final String TAG = "PauseFragment";

    private Button mRolesButton;
    private Button mRulesButton;
    private Button mResumeButton;
    private Button mEndButton;

    private Activity mActivity;

    // enums for pause fragments
    private static final int PAUSE_FRAGMENT = 0;
    private static final int ROLES_FRAGMENT = 1;
    private static final int RULES_FRAGMENT = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.pause_fragment, container, false);

        mRolesButton = (Button) view.findViewById(R.id.rolesButton);
        mRulesButton = (Button) view.findViewById(R.id.rulesButton);
        mResumeButton = (Button) view.findViewById(R.id.resumeButton);
        mEndButton = (Button) view.findViewById(R.id.endButton);

        mRolesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRolesClicked();
            }
        });
        mRulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRulesClicked();
            }
        });
        mResumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResumeClicked();
            }
        });
        mEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEndClicked();
            }
        });
        return view;
    }

    private void onRolesClicked() {
        //send to roles explanations fragment
        ((MainActivity)mActivity).updatePauseFragment(ROLES_FRAGMENT);
    }

    private void onRulesClicked() {
        //send to rules explanations fragment
        ((MainActivity)mActivity).updatePauseFragment(RULES_FRAGMENT);
    }

    private void onResumeClicked() {
        //send back to playing fragment
        ((MainActivity)mActivity).setPaused(false);
        ((MainActivity)mActivity).updateFragments();
    }

    private void onEndClicked() {
        //call reset function
        ((MainActivity)mActivity).setPaused(false);
        ((MainActivity)mActivity).reset();
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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy(){
        //sendPlayerQuitRequest();
        super.onDestroy();
    }

}
