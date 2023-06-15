package neetsdkasu.idpwmemo10;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.fragment.app.DialogFragment;

public class ValueMenuDialogFragment extends DialogFragment
        implements View.OnClickListener {

    static final String TAG = "ValueMenuDialogFragment";

    private static final String SERVICE_INDEX = "serviceIndex";
    private static final String VALUE_INDEX = "valueIndex";
    private static final String IS_SECRET = "isSecret";
    private static final String IS_SERVICE_NAME = "isServiceName";

    static interface Listener {
        void copyValue(int serviceIndex, int valueIndex, boolean isSecret);
        void editValue(int serviceIndex, int valueIndex, boolean isSecret, boolean isServiceName);
        void deleteValue(int serviceIndex, int valueIndex, boolean isSecret, boolean isServiceName);
    }

    static ValueMenuDialogFragment newInstance(int serviceIndex, int valueIndex, boolean isSecret, boolean isServiceName) {
        ValueMenuDialogFragment f = new ValueMenuDialogFragment();
        Bundle args = new Bundle();
        args.putInt(SERVICE_INDEX, serviceIndex);
        args.putInt(VALUE_INDEX, valueIndex);
        args.putBoolean(IS_SECRET, isSecret);
        args.putBoolean(IS_SERVICE_NAME, isServiceName);
        f.setArguments(args);
        return f;
    }

    int getServiceIndex() {
        return getArguments().getInt(SERVICE_INDEX);
    }

    boolean isSecretValue() {
        return getArguments().getBoolean(IS_SECRET);
    }

    // android.view.View.OnClickListener.onClick
    public void onClick (View v) {
        Listener listener = (Listener) getActivity();
        int serviceIndex = this.getServiceIndex();
        int valueIndex = getArguments().getInt(VALUE_INDEX);
        boolean isSecret = this.isSecretValue();
        boolean isServiceName = getArguments().getBoolean(IS_SERVICE_NAME);
        int id = v.getId();
        if (id == R.id.value_menu_dialog_copy) {
            listener.copyValue(serviceIndex, valueIndex, isSecret);
        } else if (id == R.id.value_menu_dialog_edit) {
            listener.editValue(serviceIndex, valueIndex, isSecret, isServiceName);
        } else if (id == R.id.value_menu_dialog_delete) {
            listener.deleteValue(serviceIndex, valueIndex, isSecret, isServiceName);
        }
        dismiss();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.value_menu_dialog, null);

        view.findViewById(R.id.value_menu_dialog_copy).setOnClickListener(this);
        view.findViewById(R.id.value_menu_dialog_edit).setOnClickListener(this);
        Button delButton = (Button) view.findViewById(R.id.value_menu_dialog_delete);
        if (getArguments().getBoolean(IS_SERVICE_NAME)) {
            delButton.setEnabled(false);
        } else {
            delButton.setOnClickListener(this);
        }

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setView(view)
            .create();

        return dialog;
    }
}
