package edu.wisc.ece.avalonforchromecast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * Global utility methods.
 */
public class Utils {

    /**
     * Shows an error dialog.
     *
     * @param errorMessage The message to show in the dialog.
     */
    public static void showErrorDialog(final Activity activity, final String errorMessage) {
        if (!activity.isDestroyed()) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Show a error dialog along with error messages.
                    AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                    alertDialog
                            .setTitle(activity.getString(R.string.game_connection_error_message));
                    alertDialog.setMessage(errorMessage);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,
                            activity.getString(R.string.game_dialog_ok_button_text),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            });
        }
    }

}
