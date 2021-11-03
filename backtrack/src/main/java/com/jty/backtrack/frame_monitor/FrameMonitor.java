package com.jty.backtrack.frame_monitor;

import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Choreographer;


import com.jty.backtrack.utils.ReflectUtils;

import java.lang.reflect.Method;
import java.util.HashSet;

/**
 * @author jty
 * @date 2021/11/01
 */
public class FrameMonitor {
    private static final String TAG = "FrameMonitor";
    private static final String METHOD_ADD_CALLBACK = "addCallbackLocked";
    private static final int CALLBACK_INPUT = 0;
    private static final int CALLBACK_ANIMATION = 1;
    private static final int CALLBACK_TRAVERSAL = 2;
    private static final int CALLBACK_LAST = CALLBACK_TRAVERSAL;

    private static volatile FrameMonitor mInstance;
    private final Object mChoreographerLock;   //Choreographer的mLock锁对象
    private final Object[] mCallbackQueues;
    private final boolean[] mCallbackExist = new boolean[CALLBACK_LAST + 1];
    private Method mMethodAddTraversalQueue;
    private Method mMethodAddInputQueue;
    private Method mMethodAddAnimationQueue;
    private Choreographer mChoreographer;
    private final Object mDisplayEventReceiver;
    private long mFrameIntervalNanos = 16666666;//帧间隔 纳秒

    private final FrameInfo mFrameInfo = new FrameInfo();
    private final String mDoFrameCallbackStr; //调度doFrame的Handler消息callback

    private HashSet<FrameObserver> mFrameObservers = new HashSet<>();


    public synchronized static FrameMonitor getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("must be init before use!!!");
        }
        return mInstance;
    }

    public synchronized static void init() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalStateException("must be init in main thread!!!");
        }
        if (mInstance == null) {
            mInstance = new FrameMonitor();
        }
    }

    private FrameMonitor() {
        mChoreographer = Choreographer.getInstance();
        mChoreographerLock = ReflectUtils.reflectObject(mChoreographer, "mLock", new Object());
        mCallbackQueues = ReflectUtils.reflectObject(mChoreographer, "mCallbackQueues", null);
        if (null != mCallbackQueues) {
            mMethodAddInputQueue = ReflectUtils.reflectMethod(mCallbackQueues[CALLBACK_INPUT], METHOD_ADD_CALLBACK, long.class, Object.class, Object.class);
            mMethodAddAnimationQueue = ReflectUtils.reflectMethod(mCallbackQueues[CALLBACK_ANIMATION], METHOD_ADD_CALLBACK, long.class, Object.class, Object.class);
            mMethodAddTraversalQueue = ReflectUtils.reflectMethod(mCallbackQueues[CALLBACK_TRAVERSAL], METHOD_ADD_CALLBACK, long.class, Object.class, Object.class);
        }
        mFrameIntervalNanos = ReflectUtils.reflectObject(mChoreographer, "mFrameIntervalNanos", 16666666L);
        mDisplayEventReceiver = ReflectUtils.reflectObject(mChoreographer, "mDisplayEventReceiver", null);
        mDoFrameCallbackStr = mDisplayEventReceiver.toString();

        LooperMonitor looperMonitor = new LooperMonitor();
        looperMonitor.addListener(new LooperMonitor.LooperDispatchListener() {
            @Override
            public void onStart(String log) {
                String callbackStr = LooperLogParser.getCallbackStr(true, log);
                if (callbackStr != null && callbackStr.equals(mDoFrameCallbackStr)) {
                    long frameStartTime = getIntendedFrameTimeNs(System.nanoTime());
                    mFrameInfo.markDoFrameStart(frameStartTime);
                    hookDoFrame();
                }
            }

            @Override
            public void onEnd(String log) {
                String callbackStr = LooperLogParser.getCallbackStr(false, log);
                if (callbackStr != null && callbackStr.equals(mDoFrameCallbackStr)) {
                    mFrameInfo.markDoFrameEnd();
                    onFrameFinish();
                }
            }
        });
    }

    final Runnable mInputCallback = new Runnable() {
        @Override
        public void run() {
            mFrameInfo.markInputStart();
            synchronized (this) {
                mCallbackExist[CALLBACK_INPUT] = false;
            }
        }
    };
    final Runnable mAnimationCallback = new Runnable() {
        @Override
        public void run() {
            mFrameInfo.markAnimationStart();
            synchronized (this) {
                mCallbackExist[CALLBACK_ANIMATION] = false;
            }
        }
    };
    final Runnable mTraversalCallback = new Runnable() {
        @Override
        public void run() {
            mFrameInfo.markTraversalsStart();
            synchronized (this) {
                mCallbackExist[CALLBACK_TRAVERSAL] = false;
            }
        }
    };

    private void hookDoFrame() {
        addFrameCallback(CALLBACK_INPUT, mInputCallback, true);
        addFrameCallback(CALLBACK_ANIMATION, mAnimationCallback, true);
        addFrameCallback(CALLBACK_TRAVERSAL, mTraversalCallback, true);
    }

    private synchronized void addFrameCallback(int type, Runnable callback, boolean isAddHeader) {
        if (mCallbackExist[type]) {
            Log.w(TAG, "[addFrameCallback] this type " + type + " callback has exist! isAddHeader:" + isAddHeader);
            return;
        }
        try {
            synchronized (mChoreographerLock) {
                Method method = null;
                switch (type) {
                    case CALLBACK_INPUT:
                        method = mMethodAddInputQueue;
                        break;
                    case CALLBACK_ANIMATION:
                        method = mMethodAddAnimationQueue;
                        break;
                    case CALLBACK_TRAVERSAL:
                        method = mMethodAddTraversalQueue;
                        break;
                }
                if (null != method) {
                    method.invoke(mCallbackQueues[type], !isAddHeader ? SystemClock.uptimeMillis() : -1, callback, null);
                    mCallbackExist[type] = true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * 获取VSync信号到来时的帧时间
     */
    private long getIntendedFrameTimeNs(long defaultValue) {
        try {
            return ReflectUtils.reflectObject(mDisplayEventReceiver, "mTimestampNanos", defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        return defaultValue;
    }


    private void onFrameFinish() {
        if (mFrameObservers.size() == 0) {
            return;
        }
        long frameDurationNanos = mFrameInfo.getFrameDurationNanos();
        for (FrameObserver observer : mFrameObservers) {
            observer.onFrameFinish(mFrameIntervalNanos, frameDurationNanos);
        }

    }

    public void addFrameObserver(FrameObserver observer) {
        mFrameObservers.add(observer);
    }

    public void removeFrameObserver(FrameObserver observer) {
        mFrameObservers.remove(observer);
    }


    public interface FrameObserver {
        /**
         * 一帧结束
         *
         * @param frameIntervalNanos VSync 信号间隔
         * @param frameDurationNanos 一帧实际消耗时间
         */
        void onFrameFinish(long frameIntervalNanos, long frameDurationNanos);
    }

}
