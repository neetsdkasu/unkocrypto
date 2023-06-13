package neetsdkasu.idpwmemo10;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

public class DeleteMemoDialogFragment extends DialogFragment
        implements
            CompoundButton.OnCheckedChangeListener,
            DialogInterface.OnShowListener,
            DialogInterface.OnClickListener {

    static final String TAG = "DeleteMemoDialogFragment";

    private static final String MEMO_NAME = "memoName";

    static interface Listener {
        void doDeleteMemo(String memoName);
    }

    static DeleteMemoDialogFragment newInstance(String memoName) {
        DeleteMemoDialogFragment f = new DeleteMemoDialogFragment();
        Bundle args = new Bundle();
        args.putString(MEMO_NAME, memoName);
        f.setArguments(args);
        return f;
    }

    // android.app.DialogInterface.OnClickListener.onClick
    public void onClick(DialogInterface dialog, int witchButton) {
        String memoName = getArguments().getString(MEMO_NAME);
        ((Listener)getActivity()).doDeleteMemo(memoName);
    }

    // android.app.DialogInterface.OnShowListener.onShow
    public void onShow(DialogInterface dialog) {
        AlertDialog aDialog = (AlertDialog) dialog;
        if (aDialog == null) return;
        Button btn = aDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (btn == null) return;
        SwitchCompat swt = (SwitchCompat) aDialog.findViewById(R.id.delete_memo_dialog_switch);
        if (swt == null) {
            btn.setEnabled(false);
        } else {
            btn.setEnabled(swt.isChecked());
        }
    }

    // android.widget.CompoundButton.OnCheckedChangeListener.onCheckedChanged
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Button btn = ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        if (btn == null) return;
        btn.setEnabled(isChecked);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.delete_memo_dialog, null);

        SwitchCompat swt = (SwitchCompat) view.findViewById(R.id.delete_memo_dialog_switch);
        swt.setOnCheckedChangeListener(this);

        String title = getActivity().getString(R.string.delete_memo_dialog_title)
            + ": " + getArguments().getString(MEMO_NAME);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setView(view)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null)
            .create();

        dialog.setOnShowListener(this);

        return dialog;
    }
}

