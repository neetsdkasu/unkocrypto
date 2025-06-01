package neetsdkasu.fencapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import neetsdkasu.misc.ActivityResultManager;
import neetsdkasu.misc.Utils;
import neetsdkasu.misc.ValueListener;

public class MainActivity extends Activity {

    private FencViewModel fencViewModel;

    private final ActivityResultManager activityResultManager;
    private final ActivityResultManager.Launcher<Void> pickDecFileLauncher;
    private final ActivityResultManager.Launcher<String> exportOrigFileLauncher;
    private final ActivityResultManager.Launcher<Void> pickSourceFileLauncher;
    private final ActivityResultManager.Launcher<String> exportFencFileLauncher;

    {
        this.activityResultManager = new ActivityResultManager(this);
        ActivityResultManager manager = this.activityResultManager;
        this.pickDecFileLauncher = manager.register(this.new PickDecFileCondacts());
        this.exportOrigFileLauncher = manager.register(this.new ExportOrigFileCondacts());
        this.pickSourceFileLauncher = manager.register(this.new PickSourceFileCondacts());
        this.exportFencFileLauncher = manager.register(this.new ExportFencFileCondacts());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Utils.setSecure(this);

        AppContainer appContainer = ((MyApplication) this.getApplication()).appContainer;
        this.fencViewModel = new FencViewModel(appContainer.fencRepository);

        this.fencViewModel.origFileName.setListener(this, new ValueListener<String>() {
            @Override
            public void onUpdate(String filename) {
                MainActivity.this.logMessage(R.string.log_message_export);
                MainActivity.this.exportOrigFileLauncher.launch(filename);
            }
        }); 

        this.fencViewModel.decError.setListener(this, new ValueListener<String>() {
            @Override
            public void onUpdate(String msg) {
                MainActivity.this.logMessage(msg);
                MainActivity.this.setUiEnabled(true);
            }
        });

        this.fencViewModel.exportOrigFileResult.setListener(this, new ValueListener<Boolean>() {
            @Override
            public void onUpdate(Boolean result) {
                if (result) {
                    MainActivity.this.logMessage(R.string.log_message_dec_success);
                } else {
                    MainActivity.this.logMessage(R.string.log_message_unknown_error);
                }
                MainActivity.this.setUiEnabled(true);
            }
        });

        this.fencViewModel.fencFileName.setListener(this, new ValueListener<String>() {
            @Override
            public void onUpdate(String filename) {
                MainActivity.this.logMessage(R.string.log_message_export);
                MainActivity.this.exportFencFileLauncher.launch(filename);
            }
        }); 

        this.fencViewModel.encError.setListener(this, new ValueListener<String>() {
            @Override
            public void onUpdate(String msg) {
                MainActivity.this.logMessage(msg);
                MainActivity.this.setUiEnabled(true);
            }
        });

        this.fencViewModel.exportFencFileResult.setListener(this, new ValueListener<Boolean>() {
            @Override
            public void onUpdate(Boolean result) {
                if (result) {
                    MainActivity.this.logMessage(R.string.log_message_enc_success);
                } else {
                    MainActivity.this.logMessage(R.string.log_message_unknown_error);
                }
                MainActivity.this.setUiEnabled(true);
            }
        });
    }

    void logMessage(String msg) {
        TextView log = findViewById(R.id.log_message);
        log.setText(msg);
    }

    void logMessage(int resId) {
        TextView log = findViewById(R.id.log_message);
        log.setText(resId);
    }

    void hideInputMethod() {
        TextView keyword = findViewById(R.id.keyword);
        Utils.hideInputMethod(this, keyword);
    }

    void beIdleWithUnknownErrorMessage() {
        logMessage(R.string.log_message_unknown_error);
        setUiEnabled(true);
    }

    void beIdleByCanceled() {
        logMessage(R.string.log_message_canceled);
        setUiEnabled(true);
    }

    void setUiEnabled(boolean enabled) {
        findViewById(R.id.keyword).setEnabled(enabled);
        findViewById(R.id.enc_button).setEnabled(enabled);
        findViewById(R.id.dec_button).setEnabled(enabled);
    }

    String getFencPassword() {
        TextView keyword = findViewById(R.id.keyword);
        CharSequence password = keyword.getText();
        if (password == null) {
            return "";
        }
        return password.toString().trim();
    }

    public void onClickEncButton(View view) {
        this.hideInputMethod();
        if (this.getFencPassword().isEmpty()) {
            final TextView log = findViewById(R.id.log_message);
            log.setText(R.string.log_message_require_password);
            return;
        }
        this.logMessage(R.string.log_message_pick_source_file);
        this.setUiEnabled(false);
        this.pickSourceFileLauncher.launch();
    }

    public void onClickDecButton(View view) {
        this.hideInputMethod();
        if (this.getFencPassword().isEmpty()) {
            final TextView log = findViewById(R.id.log_message);
            log.setText(R.string.log_message_require_password);
            return;
        }
        this.logMessage(R.string.log_message_pick_fenc_file);
        this.setUiEnabled(false);
        this.pickDecFileLauncher.launch();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        this.activityResultManager.onActivityResult(requestCode, resultCode, data);
    }

    private final class PickDecFileCondacts extends ActivityResultManager.Condacts<Void> {
        @Override
        public Intent onCreate(Void obj) {
            return new Intent(Intent.ACTION_GET_CONTENT)
                .setType("*/*")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        }
        @Override
        public void onFailedToStart() {
            MainActivity.this.beIdleWithUnknownErrorMessage();
        }
        @Override
        public void onCanceled(Intent data) {
            MainActivity.this.beIdleByCanceled();
        }
        @Override
        public void onOk(Intent data) {
            if (data == null) {
                MainActivity.this.beIdleWithUnknownErrorMessage();
                return;
            }
            Uri uri = data.getData();
            if (data == null) {
                MainActivity.this.beIdleWithUnknownErrorMessage();
                return;
            }
            String password = MainActivity.this.getFencPassword();
            if (password.isEmpty()) {
                MainActivity.this.beIdleWithUnknownErrorMessage();
                return;
            }
            try {
                MainActivity.this.logMessage(R.string.log_message_dec_processing);
                MainActivity.this.fencViewModel.dec(password, uri);
            } catch (Exception ex) {
                MainActivity.this.logMessage(ex.getMessage());
            }
        }
    }

    private final class ExportOrigFileCondacts extends ActivityResultManager.Condacts<String> {
        @Override
        public Intent onCreate(String filename) {
            return new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .setType("application/octet-stream")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_TITLE, filename);
        }
        @Override
        public void onFailedToStart() {
            MainActivity.this.beIdleWithUnknownErrorMessage();
        }
        @Override
        public void onCanceled(Intent data) {
            MainActivity.this.beIdleByCanceled();
        }
        @Override
        public void onOk(Intent data) {
            if (data == null) {
                MainActivity.this.beIdleWithUnknownErrorMessage();
                return;
            }
            Uri uri = data.getData();
            if (data == null) {
                MainActivity.this.beIdleWithUnknownErrorMessage();
                return;
            }
            try {
                MainActivity.this.logMessage(R.string.log_message_exporting);
                MainActivity.this.fencViewModel.exportOrigFile(uri);
            } catch (Exception ex) {
                MainActivity.this.logMessage(ex.getMessage());
            }
        }
    }

    private final class PickSourceFileCondacts extends ActivityResultManager.Condacts<Void> {
        @Override
        public Intent onCreate(Void obj) {
            return new Intent(Intent.ACTION_GET_CONTENT)
                .setType("*/*")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        }
        @Override
        public void onFailedToStart() {
            MainActivity.this.beIdleWithUnknownErrorMessage();
        }
        @Override
        public void onCanceled(Intent data) {
            MainActivity.this.beIdleByCanceled();
        }
        @Override
        public void onOk(Intent data) {
            if (data == null) {
                MainActivity.this.beIdleWithUnknownErrorMessage();
                return;
            }
            Uri uri = data.getData();
            if (data == null) {
                MainActivity.this.beIdleWithUnknownErrorMessage();
                return;
            }
            String password = MainActivity.this.getFencPassword();
            if (password.isEmpty()) {
                MainActivity.this.beIdleWithUnknownErrorMessage();
                return;
            }
            try {
                MainActivity.this.logMessage(R.string.log_message_enc_processing);
                MainActivity.this.fencViewModel.enc(password, uri);
            } catch (Exception ex) {
                MainActivity.this.logMessage(ex.getMessage());
            }
        }
    }

    private final class ExportFencFileCondacts extends ActivityResultManager.Condacts<String> {
        @Override
        public Intent onCreate(String filename) {
            return new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .setType("application/octet-stream")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_TITLE, filename);
        }
        @Override
        public void onFailedToStart() {
            MainActivity.this.beIdleWithUnknownErrorMessage();
        }
        @Override
        public void onCanceled(Intent data) {
            MainActivity.this.beIdleByCanceled();
        }
        @Override
        public void onOk(Intent data) {
            if (data == null) {
                MainActivity.this.beIdleWithUnknownErrorMessage();
                return;
            }
            Uri uri = data.getData();
            if (data == null) {
                MainActivity.this.beIdleWithUnknownErrorMessage();
                return;
            }
            try {
                MainActivity.this.logMessage(R.string.log_message_exporting);
                MainActivity.this.fencViewModel.exportFencFile(uri);
            } catch (Exception ex) {
                MainActivity.this.logMessage(ex.getMessage());
            }
        }
    }
}
