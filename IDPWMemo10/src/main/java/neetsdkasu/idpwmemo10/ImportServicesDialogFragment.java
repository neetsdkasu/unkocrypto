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

public class ImportServicesDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener {

    static final String TAG = "ImportServicesDialogFragment";

    private static final String PASSWORD = "password";
    private static final String DATA = "data";

    static interface Listener {
        void importServices(String data, String password);
    }

    static ImportServicesDialogFragment newInstance(String data, String password) {
        ImportServicesDialogFragment f = new ImportServicesDialogFragment();
        Bundle args = new Bundle();
        args.putString(PASSWORD, password);
        args.putString(DATA, data);
        f.setArguments(args);
        return f;
    }

    // android.app.DialogInterface.OnClickListener.onClick
    public void onClick(DialogInterface dialog, int witchButton) {
        EditText e1 = (EditText) ((Dialog)dialog).findViewById(R.id.import_services_dialog_password);
        String password = e1.getText().toString();
        EditText e2 = (EditText) ((Dialog)dialog).findViewById(R.id.import_services_dialog_data);
        String data = e2.getText().toString();
        ((Listener)getActivity()).importServices(data, password);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.import_services_dialog, null);

        EditText e1 = (EditText) view.findViewById(R.id.import_services_dialog_password);
        e1.setText(getArguments().getString(PASSWORD));

        EditText e2 = (EditText) view.findViewById(R.id.import_services_dialog_data);
        e2.setText(getArguments().getString(DATA));

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.import_services_dialog_title)
            .setView(view)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null)
            .create();

        return dialog;
    }
}