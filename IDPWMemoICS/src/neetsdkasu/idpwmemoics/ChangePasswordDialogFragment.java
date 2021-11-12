package neetsdkasu.idpwmemoics;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class ChangePasswordDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener {

    static final String TAG = "ChangePasswordDialogFragment";

    private static final String MEMO_NAME = "memoName";

    static interface Listener {
        void changePassword(String memoName, String oldPassword, String newPassword);
    }

    static ChangePasswordDialogFragment newInstance(String memoName) {
        ChangePasswordDialogFragment f = new ChangePasswordDialogFragment();
        Bundle args = new Bundle();
        args.putString(MEMO_NAME, memoName);
        f.setArguments(args);
        return f;
    }

    // android.app.DialogInterface.OnClickListener.onClick
    public void onClick(DialogInterface dialog, int witchButton) {
        EditText e1 = (EditText) ((Dialog)dialog).findViewById(R.id.change_password_dialog_old_password);
        EditText e2 = (EditText) ((Dialog)dialog).findViewById(R.id.change_password_dialog_new_password);
        String oldPassword = e1.getText().toString();
        String newPassword = e2.getText().toString();
        String memoName = getArguments().getString(MEMO_NAME);
        ((Listener)getActivity()).changePassword(memoName, oldPassword, newPassword);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.change_password_dialog, null);

        String title = getArguments().getString(MEMO_NAME);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setView(view)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null)
            .create();

        dialog.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);

        return dialog;
    }
}

