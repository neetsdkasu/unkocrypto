package neetsdkasu.idpwmemo10;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.fragment.app.DialogFragment;
import java.util.Arrays;

public class NewServiceDialogFragment extends DialogFragment
        implements InputFilter, DialogInterface.OnShowListener, DialogInterface.OnClickListener {

    static final String TAG = "NewServiceDialogFragment";

    static interface Listener {
        void createNewService(String name);
    }

    static NewServiceDialogFragment newInstance() {
        NewServiceDialogFragment f = new NewServiceDialogFragment();
        return f;
    }

    // android.text.InputFilter.filter
    public CharSequence filter (CharSequence source, int start, int end, android.text.Spanned dest, int dstart, int dend) {
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null) return null;
        Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (btn == null) return null;
        int len = dest.length() - (dend - dstart) + (end - start);
        btn.setEnabled(len > 0);
        return null;
    }

    boolean firstTime = true;

    // android.app.DialogInterface.OnShowListener.onShow
    public void onShow(DialogInterface dialog) {
        AlertDialog aDialog = (AlertDialog) dialog;
        if (aDialog == null) return;
        Button btn = aDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (btn == null) return;
        EditText e = (EditText) aDialog.findViewById(R.id.new_service_dialog_name);
        if (e == null) {
            btn.setEnabled(false);
        } else {
            btn.setEnabled(e.length() > 0);
            if (firstTime) {
                firstTime = false;
                InputFilter[] fs = e.getFilters();
                if (fs == null) {
                    fs = new InputFilter[1];
                } else {
                    fs = Arrays.copyOf(fs, fs.length + 1);
                }
                fs[fs.length-1] = this;
                e.setFilters(fs);
            }
        }
    }

    // android.app.DialogInterface.OnClickListener.onClick
    public void onClick(DialogInterface dialog, int witchButton) {
        EditText e = (EditText) ((Dialog)dialog).findViewById(R.id.new_service_dialog_name);
        String name = e.getText().toString();
        ((Listener)getActivity()).createNewService(name);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.new_service_dialog, null);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.new_service_dialog_title)
            .setView(view)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null)
            .create();

        dialog.setOnShowListener(this);

        return dialog;
    }
}