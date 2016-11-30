package edu.wisc.ece.avalonforchromecast;

import android.os.Bundle;
import android.util.Log;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cast_connection_fragment, container, false);
        mConnectLabel = view.findViewById(R.id.connect_label);
        mSpinner = view.findViewById(R.id.spinner);

        Log.d(TAG, "Avalon should be visible");
        mConnectLabel.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void update(Observable object, Object data) {
        if (getView() == null) {
            return;
        }
        // Once the user has selected a Cast device, show a progress indicator.
        // MainActivity will load the lobby fragment next.
        if (mCastConnectionManager.getSelectedDevice() != null) {
            mConnectLabel.setVisibility(View.GONE);
            mSpinner.setVisibility(View.VISIBLE);
        } else {
            mConnectLabel.setVisibility(View.VISIBLE);
            mSpinner.setVisibility(View.GONE);
        }
    }
}
