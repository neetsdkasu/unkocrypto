package neetsdkasu.idpwmemo10;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.fragment.app.DialogFragment;

public class ExportPasswordDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener {

    static final String TAG = "ExportPasswordDialogFragment";

    private static final String SERVICE_NAME = "serviceName";
    private static final String SERVICE_INDEX = "serviceIndex";

    static interface Listener {
        void setExportPassword(int serviceIndex, String password);
    }

    static ExportPasswordDialogFragment newInstance(String serviceName, int serviceIndex) {
        ExportPasswordDialogFragment f = new ExportPasswordDialogFragment();
        Bundle args = new Bundle();
        args.putString(SERVICE_NAME, serviceName);
        args.putInt(SERVICE_INDEX, serviceIndex);
        f.setArguments(args);
        return f;
    }

    // android.app.DialogInterface.OnClickListener.onClick
    public void onClick(DialogInterface dialog, int witchButton) {
        EditText e = (EditText) ((Dialog)dialog).findViewById(R.id.export_password_dialog_password);
        String password = e.getText().toString();
        int serviceIndex = getArguments().getInt(SERVICE_INDEX);
        ((Listener)getActivity()).setExportPassword(serviceIndex, password);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.export_password_dialog, null);

        String title = getActivity().getString(R.string.export_password_dialog_title)
            + ": " + getArguments().getString(SERVICE_NAME);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setView(view)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null)
            .create();

        return dialog;
    }
}

