package neetsdkasu.idpwmemo10;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;

public class ExportServiceDialogFragment extends DialogFragment {

    static final String TAG = "ExportServiceDialogFragment";

    private static final String SERVICE_NAME = "serviceName";
    private static final String DATA = "data";

    static ExportServiceDialogFragment newInstance(String serviceName, String data) {
        ExportServiceDialogFragment f = new ExportServiceDialogFragment();
        Bundle args = new Bundle();
        args.putString(SERVICE_NAME, serviceName);
        args.putString(DATA, data);
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.export_service_dialog, null);

        TextView e = (TextView) view.findViewById(R.id.export_service_dialog_data);
        e.setText(getArguments().getString(DATA));

        String title = getActivity().getString(R.string.export_service_dialog_title)
            + ": " + getArguments().getString(SERVICE_NAME);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setView(view)
            .setPositiveButton(android.R.string.ok, null)
            .create();

        return dialog;
    }
}