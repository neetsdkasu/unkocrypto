package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;

final class ActivityResultManager {

    private static interface IntentCreator<T> {
        Intent onCreate(T arg);
        void onFailedToStart();
    }

    private static interface ResultListener {
        void onCanceled();
        void onOk(Intent data);
        void onUserResult(int resultCode, Intent data);
    }

    static abstract class Condacts<T> implements ActivityResultManager.IntentCreator<T>,
                                                 ActivityResultManager.ResultListener {
        public abstract Intent onCreate(T arg);
        public void onFailedToStart() {}
        public void onCanceled() {}
        public void onOk(Intent data) {}
        public void onUserResult(int resultCode, Intent data) {}
    }

    private final class Flag {
        private boolean enabled = true;
        boolean isEnabled() { return this.enabled; }
        void disabled() { this.enabled = false; }
    }

    static final class Launcher<T> {
        private final Activity activity;
        private final int requestCode;
        private final ActivityResultManager.IntentCreator<T> creator;
        private final Flag flag;

        private Launcher(Activity activity, int requestCode, ActivityResultManager.IntentCreator<T> creator, Flag flag) {
            this.activity = activity;
            this.requestCode = requestCode;
            this.creator = creator;
            this.flag = flag;
        }

        boolean launch() { return launch(null); }

        boolean launch(T arg) {
            if (!this.flag.isEnabled()) {
                return false;
            }
            Intent intent = this.creator.onCreate(arg);
            if (intent == null) {
                this.creator.onFailedToStart();
                return false;
            }
            if (intent.resolveActivity(activity.getPackageManager()) == null) {
                this.creator.onFailedToStart();
                return false;
            }
            this.activity.startActivityForResult(intent, this.requestCode);
            return true;
        }
    }

    private final Activity activity;

    ActivityResultManager(Activity activity) {
        this.activity = activity;
    }

    private List<ActivityResultManager.ResultListener> listenerList = new ArrayList<>();
    private List<Flag> flagList = new ArrayList<>();

    <T> ActivityResultManager.Launcher<T> register(ActivityResultManager.Condacts<T> condacts) {
        Flag flag = new Flag();
        this.listenerList.add(condacts);
        this.flagList.add(flag);
        int requestCode = this.listenerList.size();
        return new ActivityResultManager.Launcher<>(this.activity, requestCode, condacts, flag);
    }

    <T> boolean unregister(ActivityResultManager.Condacts<T> condacts) {
        if (condacts == null) {
            return false;
        }
        for (int i = 0; i < this.listenerList.size(); i++) {
            ActivityResultManager.ResultListener listener = this.listenerList.get(i);
            if (listener == condacts) {
                Flag oldFlag = this.flagList.set(i, null);
                if (oldFlag != null) {
                    oldFlag.disabled();
                }
                this.listenerList.set(i, null);
                return true;
            }
        }
        return false;
    }

    boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!Utils.inRange(requestCode, 1, this.listenerList.size())) {
            return false;
        }
        ActivityResultManager.ResultListener listener = this.listenerList.get(requestCode - 1);
        if (listener == null) {
            return true;
        }
        if (resultCode == Activity.RESULT_OK) {
            listener.onOk(data);
        } else if (resultCode == Activity.RESULT_CANCELED) {
            listener.onCanceled();
        } else {
            listener.onUserResult(resultCode, data);
        }
        return true;
    }
}