package neetsdkasu.idpwmemo10;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.io.File;
import java.io.FilenameFilter;
import androidx.fragment.app.DialogFragment;

public class ImportMemoDialogFragment extends DialogFragment
        implements FilenameFilter, DialogInterface.OnShowListener, DialogInterface.OnClickListener {

    static final String TAG = "ImportMemoDialogFragment";

    static interface Listener {
        void importMemo(MemoFile memoFile);
    }

    static ImportMemoDialogFragment newInstance() {
        ImportMemoDialogFragment f = new ImportMemoDialogFragment();
        return f;
    }

    ArrayAdapter<MemoFile> importListAdapter = null;

    // android.app.DialogInterface.OnShowListener.onShow
    public void onShow(DialogInterface dialog) {
        AlertDialog aDialog = (AlertDialog) dialog;
        if (aDialog == null) return;
        Button btn = aDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (btn == null) return;
        ListView listView = aDialog.getListView();
        if (listView == null) {
            btn.setEnabled(false);
        } else {
            int pos = listView.getCheckedItemPosition();
            btn.setEnabled(pos != ListView.INVALID_POSITION);
        }
    }

    // android.app.DialogInterface.OnClickListener.onClick
    public void onClick(DialogInterface dialog, int witchButton) {
        AlertDialog aDialog = (AlertDialog) dialog;
        if (aDialog == null) return;
        ListView listView = aDialog.getListView();
        if (listView == null) return;
        int pos = listView.getCheckedItemPosition();
        if (witchButton == AlertDialog.BUTTON_POSITIVE) {
            MemoFile memoFile = this.importListAdapter.getItem(pos);
            ((Listener)getActivity()).importMemo(memoFile);
        } else {
            Button btn = aDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (btn == null) return;
            btn.setEnabled(pos != ListView.INVALID_POSITION);
        }
    }

    // java.io.FilenameFilter.accept
    public boolean accept(File d, String name) {
        if (!name.endsWith(Utils.MEMO_EXT)) return false;
        int len = name.length() - 5;
        if (!Utils.isValidMemoNameLength(len)) return false;
        return Utils.isValidMemoNameChars(name, 0, len);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        this.importListAdapter =  new ArrayAdapter<MemoFile>(getActivity(), android.R.layout.simple_list_item_single_choice);

        if (Utils.isExternalStorageReadable()) {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            dir.mkdirs();
            if (dir.isDirectory()) {
                File[] files = dir.listFiles(this);
                if (files != null) {
                    for (File f : files) {
                        this.importListAdapter.add(new MemoFile(f));
                    }
                }
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.import_memo_dialog_title)
            .setSingleChoiceItems (this.importListAdapter, -1, this)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null)
            .create();

        dialog.setOnShowListener(this);

        return dialog;
    }
}
