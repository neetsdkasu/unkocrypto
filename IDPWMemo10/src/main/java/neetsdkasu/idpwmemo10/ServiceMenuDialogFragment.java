package neetsdkasu.idpwmemo10;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.fragment.app.DialogFragment;

public class ServiceMenuDialogFragment extends DialogFragment
        implements View.OnClickListener {

    static final String TAG = "ServiceMenuDialogFragment";

    private static final String SERVICE_NAME = "serviceName";
    private static final String SERVICE_INDEX = "serviceIndex";

    static interface Listener {
        void showService(int serviceIndex);
        void exportService(int serviceIndex);
        void deleteService(int serviceIndex);
    }

    static ServiceMenuDialogFragment newInstance(int serviceIndex, String serviceName) {
        ServiceMenuDialogFragment f = new ServiceMenuDialogFragment();
        Bundle args = new Bundle();
        args.putString(SERVICE_NAME, serviceName);
        args.putInt(SERVICE_INDEX, serviceIndex);
        f.setArguments(args);
        return f;
    }

    // android.view.View.OnClickListener.onClick
    public void onClick (View v) {
        Listener listener = (Listener) getActivity();
        int serviceIndex = getArguments().getInt(SERVICE_INDEX);
        int id = v.getId();
        if (id == R.id.service_menu_dialog_show) {
            listener.showService(serviceIndex);
        } else if (id == R.id.service_menu_dialog_export) {
            listener.exportService(serviceIndex);
        } else if (id == R.id.service_menu_dialog_delete) {
            listener.deleteService(serviceIndex);
        }
        dismiss();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.service_menu_dialog, null);

        view.findViewById(R.id.service_menu_dialog_show).setOnClickListener(this);
        view.findViewById(R.id.service_menu_dialog_export).setOnClickListener(this);
        view.findViewById(R.id.service_menu_dialog_delete).setOnClickListener(this);

        String title = getArguments().getString(SERVICE_NAME);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setView(view)
            .create();

        return dialog;
    }
}
