package com.jty.backtrack.frame_monitor;

import android.os.Looper;
import android.util.Printer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jty
 * @date 2021/11/01
 */
public class LooperMonitor implements Printer {
    private static final String TAG = "LooperMonitor";
    private final List<LooperDispatchListener> listeners = new ArrayList<>();

    public LooperMonitor() {
        Looper.getMainLooper().setMessageLogging(this);
    }

    @Override
    public void println(String x) {
        boolean isValid = x.charAt(0) == '>' || x.charAt(0) == '<';
        if (isValid) {
            dispatch(x.charAt(0) == '>', x);
        }
    }

    private void dispatch(boolean isBegin, String log) {
        if (listeners.size() == 0) {
            return;
        }
        synchronized (listeners) {
            for (LooperDispatchListener listener : listeners) {
                if (isBegin) {
                    listener.onStart(log);
                } else {
                    listener.onEnd(log);
                }

            }
        }
    }

    public void addListener(LooperDispatchListener listener) {
        listeners.add(listener);
    }

    public interface LooperDispatchListener {
        void onStart(String log);

        void onEnd(String log);
    }
}
