package neetsdkasu.idpwmemo10;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

public class DeleteServiceDialogFragment extends DialogFragment
        implements
            CompoundButton.OnCheckedChangeListener,
            DialogInterface.OnShowListener,
            DialogInterface.OnClickListener {

    static final String TAG = "DeleteServiceDialogFragment";

    private static final String SERVICE_NAME = "serviceName";
    private static final String SERVICE_INDEX = "serviceIndex";

    static interface Listener {
        void doDeleteService(int serviceIndex);
    }

    static DeleteServiceDialogFragment newInstance(String serviceName, int serviceIndex) {
        DeleteServiceDialogFragment f = new DeleteServiceDialogFragment();
        Bundle args = new Bundle();
        args.putString(SERVICE_NAME, serviceName);
        args.putInt(SERVICE_INDEX, serviceIndex);
        f.setArguments(args);
        return f;
    }

    // android.app.DialogInterface.OnClickListener.onClick
    public void onClick(DialogInterface dialog, int witchButton) {
        int serviceIndex = getArguments().getInt(SERVICE_INDEX);
        ((Listener)getActivity()).doDeleteService(serviceIndex);
    }

    // android.app.DialogInterface.OnShowListener.onShow
    public void onShow(DialogInterface dialog) {
        AlertDialog aDialog = (AlertDialog) dialog;
        if (aDialog == null) return;
        Button btn = aDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (btn == null) return;
        SwitchCompat swt = (SwitchCompat) aDialog.findViewById(R.id.delete_service_dialog_switch);
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
        View view = inflater.inflate(R.layout.delete_service_dialog, null);

        SwitchCompat swt = (SwitchCompat) view.findViewById(R.id.delete_service_dialog_switch);
        swt.setOnCheckedChangeListener(this);

        String title = getActivity().getString(R.string.delete_service_dialog_title)
            + ": " + getArguments().getString(SERVICE_NAME);

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

