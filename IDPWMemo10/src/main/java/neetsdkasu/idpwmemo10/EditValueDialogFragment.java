package neetsdkasu.idpwmemo10;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.fragment.app.DialogFragment;
import java.util.Arrays;

public class EditValueDialogFragment extends DialogFragment
        implements
            InputFilter,
            DialogInterface.OnShowListener,
            DialogInterface.OnClickListener {

    static final String TAG = "EditValueDialogFragment";

    private static final String SERVICE_INDEX = "serviceIndex";
    private static final String VALUE_INDEX = "valueIndex";
    private static final String IS_SECRET = "isSecret";
    private static final String IS_SERVICE_NAME = "isServiceName";
    private static final String VALUE_TYPE = "valueType";
    private static final String VALUE = "value";

    static interface Listener {
        void updateValue(int serviceIndex, int valueIndex, boolean isSecret, boolean isServiceName, idpwmemo.Value newValue);
    }

    static EditValueDialogFragment newInstance(int serviceIndex, int valueIndex, boolean isSecret, boolean isServiceName, idpwmemo.Value value) {
        EditValueDialogFragment f = new EditValueDialogFragment();
        Bundle args = new Bundle();
        args.putInt(SERVICE_INDEX, serviceIndex);
        args.putInt(VALUE_INDEX, valueIndex);
        args.putBoolean(IS_SECRET, isSecret);
        args.putBoolean(IS_SERVICE_NAME, isServiceName);
        args.putInt(VALUE_TYPE, (int)value.type);
        args.putString(VALUE, value.value);
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
        Spinner sp = (Spinner) ((Dialog)dialog).findViewById(R.id.edit_value_dialog_value_type);
        int valueType = sp.getSelectedItemPosition();
        EditText e = (EditText) ((Dialog)dialog).findViewById(R.id.edit_value_dialog_value);
        String value = e.getText().toString();
        idpwmemo.Value newValue = new idpwmemo.Value(valueType, value);
        int serviceIndex = this.getServiceIndex();
        int valueIndex = getArguments().getInt(VALUE_INDEX);
        boolean isSecret = this.isSecretValue();
        boolean isServiceName = this.getArguments().getBoolean(IS_SERVICE_NAME);
        ((Listener)getActivity()).updateValue(serviceIndex, valueIndex, isSecret, isServiceName, newValue);
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
        if (!firstTime) return;
        AlertDialog aDialog = (AlertDialog) dialog;
        if (dialog == null) return;
        Button btn = aDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (btn == null) return;
        firstTime = true;
        if (!getArguments().getBoolean(IS_SERVICE_NAME)) return;
        Spinner sp = (Spinner) ((Dialog)aDialog).findViewById(R.id.edit_value_dialog_value_type);
        if (sp != null) sp.setEnabled(false);
        EditText e = (EditText) ((Dialog)aDialog).findViewById(R.id.edit_value_dialog_value);
        if (e == null) return;
        InputFilter[] fs = e.getFilters();
        if (fs == null) {
            fs = new InputFilter[1];
        } else {
            fs = Arrays.copyOf(fs, fs.length + 1);
        }
        fs[fs.length-1] = this;
        e.setFilters(fs);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.edit_value_dialog, null);

        Spinner sp = (Spinner) view.findViewById(R.id.edit_value_dialog_value_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        for (int i = 0; i < 8; i++) {
            adapter.add(idpwmemo.Value.typeName(i));
        }
        sp.setAdapter(adapter);
        sp.setSelection(getArguments().getInt(VALUE_TYPE));

        EditText e = (EditText) view.findViewById(R.id.edit_value_dialog_value);
        e.setText(getArguments().getString(VALUE));

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.edit_value_dialog_title)
            .setView(view)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null)
            .create();

        dialog.setOnShowListener(this);

        return dialog;
    }
}