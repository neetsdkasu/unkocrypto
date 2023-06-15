package neetsdkasu.idpwmemo10;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.fragment.app.DialogFragment;
import java.util.Arrays;

public class NewValueDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener {

    static final String TAG = "NewValueDialogFragment";

    private static final String SERVICE_INDEX = "serviceIndex";
    private static final String IS_SECRET = "isSecret";

    static interface Listener {
        void createNewValue(int serviceIndex, boolean isSecret, idpwmemo.Value newValue);
    }

    static NewValueDialogFragment newInstance(int serviceIndex, boolean isSecret) {
        NewValueDialogFragment f = new NewValueDialogFragment();
        Bundle args = new Bundle();
        args.putInt(SERVICE_INDEX, serviceIndex);
        args.putBoolean(IS_SECRET, isSecret);
        f.setArguments(args);
        return f;
    }

    // android.app.DialogInterface.OnClickListener.onClick
    public void onClick(DialogInterface dialog, int witchButton) {
        Spinner sp = (Spinner) ((Dialog)dialog).findViewById(R.id.new_value_dialog_value_type);
        int valueType = sp.getSelectedItemPosition();
        EditText e = (EditText) ((Dialog)dialog).findViewById(R.id.new_value_dialog_value);
        String value = e.getText().toString();
        idpwmemo.Value newValue = new idpwmemo.Value(valueType, value);
        int serviceIndex = getArguments().getInt(SERVICE_INDEX);
        boolean isSecret = getArguments().getBoolean(IS_SECRET);
        ((Listener)getActivity()).createNewValue(serviceIndex, isSecret, newValue);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.new_value_dialog, null);

        Spinner sp = (Spinner) view.findViewById(R.id.new_value_dialog_value_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        for (int i = 0; i < 8; i++) {
            adapter.add(idpwmemo.Value.typeName(i));
        }
        sp.setAdapter(adapter);
        if (getArguments().getBoolean(IS_SECRET)) {
            sp.setSelection(idpwmemo.Value.PASSWORD);
        } else {
            sp.setSelection(idpwmemo.Value.ID);
        }

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.new_value_dialog_title)
            .setView(view)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null)
            .create();

        return dialog;
    }

    int getServiceIndex() {
        return getArguments().getInt(SERVICE_INDEX);
    }

    boolean isSecretValue() {
        return getArguments().getBoolean(IS_SECRET);
    }
}