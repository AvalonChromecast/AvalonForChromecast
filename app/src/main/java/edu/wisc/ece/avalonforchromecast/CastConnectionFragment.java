package edu.wisc.ece.avalonforchromecast;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.MediaRouteButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Observable;

/**
 * A fragment displayed while this application is not yet connected to a cast device.
 */
public class CastConnectionFragment extends GameFragment {

    public static final String TAG = "CastConnectionFragment";

    private View mConnectLabel;
    private View mSpinner;
    private MediaRouteButton mMediaRouteButton;

    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cast_connection_fragment, container, false);
        mSpinner = view.findViewById(R.id.spinner);

        // Set the MediaRouteButton selector for device discovery.
        mMediaRouteButton = (MediaRouteButton) view.findViewById(R.id.media_route_button);
        ((MainActivity) mActivity).setMediaRouteButton(mMediaRouteButton);
        //mCastConnectionManager.setMediaRouteButton(mMediaRouteButton);

        return view;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void update(Observable object, Object data) {
        if (getView() == null) {
            return;
        }
        // Once the user has selected a Cast device, show a progress indicator.
        // MainActivity will load the lobby fragment next.
        if (mCastConnectionManager.getSelectedDevice() != null) {
            mSpinner.setVisibility(View.VISIBLE);
        } else {
            mSpinner.setVisibility(View.GONE);
        }
    }
}
