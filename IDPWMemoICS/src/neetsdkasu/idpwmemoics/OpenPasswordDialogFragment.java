package neetsdkasu.idpwmemoics;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class OpenPasswordDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener {

    static final String TAG = "OpenPasswordDialogFragment";

    private static final String MEMO_NAME = "memoName";

    static interface Listener {
        void openMemo(String password);
        void giveUpOpenPassword();
    }

    static OpenPasswordDialogFragment newInstance() {
        OpenPasswordDialogFragment f = new OpenPasswordDialogFragment();
        return f;
    }

    // android.app.DialogInterface.OnClickListener.onClick
    public void onClick(DialogInterface dialog, int witchButton) {
        if (witchButton == AlertDialog.BUTTON_POSITIVE) {
            EditText e = (EditText) ((Dialog)dialog).findViewById(R.id.open_password_dialog_password);
            String password = e.getText().toString();
            ((Listener)getActivity()).openMemo(password);
        } else if (witchButton == AlertDialog.BUTTON_NEGATIVE) {
            dialog.cancel();
        }
    }

    @Override
    public void	onCancel(DialogInterface dialog) {
        ((Listener)getActivity()).giveUpOpenPassword();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.open_password_dialog, null);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.open_password_dialog_title)
            .setView(view)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, this)
            .create();

        return dialog;
    }
}

