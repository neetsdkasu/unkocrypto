package neetsdkasu.misc;

import android.app.Activity;
import java.util.concurrent.atomic.AtomicReference;

public class Value<T> {

    class Pair {
        final Activity ui;
        final ValueListener<T> listener;
        Pair(Activity ui, ValueListener<T> listener) {
            this.ui = ui;
            this.listener = listener;
        }
    }

    final AtomicReference<Pair> target = new AtomicReference<>();
    final AtomicReference<T> value = new AtomicReference<>();

    public Value() {}

    public Value(T value) {
        this.value.set(value);
    }

    public boolean setListener(Activity activity, ValueListener<T> newListener) {
        // new Application.ActivityLifecycleCallbacks() ???
        // activity.registerActivityLifecycleCallbacks() ???
        // activity.unregisterActivityLifecycleCallbacks() ???
        Pair oldValue = this.target.get();
        if (activity == null || newListener == null) {
            return this.target.compareAndSet(oldValue, null);
        } else {
            return this.target.compareAndSet(oldValue, new Pair(activity, newListener));
        }
    }

    public T getValue() {
        return this.value.get();
    }

    public boolean setValue(final T newValue) {
        T oldValue = this.value.get();
        if (this.value.compareAndSet(oldValue, newValue)) {
            this.notifyUpdate(newValue);
            return true;
        }
        return false;
    }

    void notifyUpdate(final T newValue) {
        final Pair tmp = this.target.get();
        if (tmp == null) {
            return;
        }
        final Activity ui = tmp.ui;
        final ValueListener<T> listener = tmp.listener;
        if (ui == null || listener == null) {
            return;
        }
        if (ui.isDestroyed() || ui.isFinishing()) {
            this.target.compareAndSet(tmp, null);
            return;
        }
        ui.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listener.onUpdate(newValue);
            }
        });
    }
}
