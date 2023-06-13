package neetsdkasu.idpwmemo10;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.fragment.app.DialogFragment;

public class MemoMenuDialogFragment extends DialogFragment
        implements View.OnClickListener {

    static final String TAG = "MemoMenuDialogFragment";

    static final String MEMO_NAME = "memoName";

    static interface Listener {
        void openMemo(String memoName);
        void exportMemo(String memoName);
        void changeMemoPassword(String memoName);
        void deleteMemo(String memoName);
    }

    static MemoMenuDialogFragment newInstance(MemoFile memoFile) {
        MemoMenuDialogFragment f = new MemoMenuDialogFragment();
        Bundle args = new Bundle();
        args.putString(MEMO_NAME, memoFile.name);
        f.setArguments(args);
        return f;
    }

    // android.view.View.OnClickListener.onClick
    public void onClick (View v) {
        Listener listener = (Listener) getActivity();
        String memoName = getArguments().getString(MEMO_NAME);
        int id = v.getId();
        if (id == R.id.memo_menu_dialog_open) {
            listener.openMemo(memoName);
        } else if (id == R.id.memo_menu_dialog_export) {
            listener.exportMemo(memoName);
        } else if (id == R.id.memo_menu_dialog_change_password) {
            listener.changeMemoPassword(memoName);
        } else if (id == R.id.memo_menu_dialog_delete) {
            listener.deleteMemo(memoName);
        }
        dismiss();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.memo_menu_dialog, null);

        view.findViewById(R.id.memo_menu_dialog_open).setOnClickListener(this);
        view.findViewById(R.id.memo_menu_dialog_export).setOnClickListener(this);
        view.findViewById(R.id.memo_menu_dialog_change_password).setOnClickListener(this);
        view.findViewById(R.id.memo_menu_dialog_delete).setOnClickListener(this);

        String title = getArguments().getString(MEMO_NAME);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setView(view)
            .create();

        return dialog;
    }
}
