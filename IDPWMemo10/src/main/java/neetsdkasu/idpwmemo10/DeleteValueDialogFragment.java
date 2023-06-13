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
import android.widget.TextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

public class DeleteValueDialogFragment extends DialogFragment
        implements
            CompoundButton.OnCheckedChangeListener,
            DialogInterface.OnShowListener,
            DialogInterface.OnClickListener {

    static final String TAG = "DeleteValueDialogFragment";

    private static final String SERVICE_INDEX = "serviceIndex";
    private static final String VALUE_INDEX = "valueIndex";
    private static final String IS_SECRET = "isSecret";
    private static final String VALUE = "value";

    static interface Listener {
        void doDeleteValue(int serviceIndex, int valueIndex, boolean isSecret);
    }

    static DeleteValueDialogFragment newInstance(int serviceIndex, int valueIndex, boolean isSecret, String value) {
        DeleteValueDialogFragment f = new DeleteValueDialogFragment();
        Bundle args = new Bundle();
        args.putInt(SERVICE_INDEX, serviceIndex);
        args.putInt(VALUE_INDEX, valueIndex);
        args.putBoolean(IS_SECRET, isSecret);
        args.putString(VALUE, value);
        f.setArguments(args);
        return f;
    }

    int getServiceIndex() {
        return getArguments().getInt(SERVICE_INDEX);
    }

    boolean isSecretValue() {
        return getArguments().getBoolean(IS_SECRET);
    }

    // android.app.DialogInterface.OnClickListener.onClick
    public void onClick(DialogInterface dialog, int witchButton) {
        int serviceIndex = getServiceIndex();
        int valueIndex = getArguments().getInt(VALUE_INDEX);
        boolean isSecret = isSecretValue();
        ((Listener)getActivity()).doDeleteValue(serviceIndex, valueIndex, isSecret);
    }

    // android.app.DialogInterface.OnShowListener.onShow
    public void onShow(DialogInterface dialog) {
        AlertDialog aDialog = (AlertDialog) dialog;
        if (aDialog == null) return;
        Button btn = aDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (btn == null) return;
        SwitchCompat swt = (SwitchCompat) aDialog.findViewById(R.id.delete_value_dialog_switch);
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
        View view = inflater.inflate(R.layout.delete_value_dialog, null);

        SwitchCompat swt = (SwitchCompat) view.findViewById(R.id.delete_value_dialog_switch);
        swt.setOnCheckedChangeListener(this);

        TextView tv = (TextView) view.findViewById(R.id.delete_value_dialog_value);
        tv.setText(getArguments().getString(VALUE));

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.delete_value_dialog_title)
            .setView(view)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null)
            .create();

        dialog.setOnShowListener(this);

        return dialog;
    }
}

