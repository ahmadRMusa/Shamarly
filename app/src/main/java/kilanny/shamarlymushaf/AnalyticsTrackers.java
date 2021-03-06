package kilanny.shamarlymushaf;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A collection of Google Analytics trackers. Fetch the tracker you need using
 * {@code AnalyticsTrackers.getInstance().get(...)}
 * <p/>
 * This code was generated by Android Studio but can be safely modified by
 * hand at this point.
 * <p/>
 * TODO: Call {@link #initialize(Context)} from an entry point in your app
 * before using this!
 */
public final class AnalyticsTrackers {

    public enum Target {
        APP,
        // Add more trackers here if you need, and update the code in #get(Target) below
    }

    private static AnalyticsTrackers sInstance;

    private static synchronized void initialize(Context context) {
        if (sInstance != null) {
            throw new IllegalStateException("Extra call to initialize analytics trackers");
        }

        sInstance = new AnalyticsTrackers(context);
    }

    private static synchronized AnalyticsTrackers getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Call initialize() before getInstance()");
        }

        return sInstance;
    }

    public static String getDeviceInfo() {
        long vmHead = -1, recomendHeap = -1, totalMem = -1,
                freeMemory = -1, processors = -1;
        try {
            freeMemory = Runtime.getRuntime().freeMemory();
            processors = Runtime.getRuntime().availableProcessors();
            vmHead = Runtime.getRuntime().maxMemory();
            totalMem = Runtime.getRuntime().totalMemory();
            AnalyticsTrackers trackers = getInstance();
            ActivityManager am = (ActivityManager) trackers.mContext
                    .getSystemService(Context.ACTIVITY_SERVICE);
            recomendHeap = am.getMemoryClass();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return String.format("Version: %s, code: %d\nOs Version: %s\nSDK: %d\nDevice: %s\nModel: %s\nProduct: %s\n" +
                        "recomendHeap: %d\nfreeMemory: %d\nprocessors: %d\nvmHeap: %d\n" +
                        "Total Memory: %d",
                BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE,
                System.getProperty("os.version"), // OS version
                Build.VERSION.SDK_INT,      // API Level
                Build.DEVICE,           // Device
                Build.MODEL,            // Model
                Build.PRODUCT,          // Product,
                recomendHeap, freeMemory, processors, vmHead, totalMem
        );
    }

    public static void sendException(Context context, Throwable throwable) {
        sendFatalError(context, throwable.getMessage(),
                Arrays.toString(throwable.getStackTrace()));
        try {
            if (sInstance == null) initialize(context);
            AnalyticsTrackers instance = getInstance();
            instance.get(Target.APP).send(new HitBuilders.ExceptionBuilder()
                    .setDescription(new StandardExceptionParser(instance.mContext, null)
                            .getDescription(Thread.currentThread().getName(), throwable)
                            .concat("\n" + getDeviceInfo()))
                    .setFatal(false)
                    .build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendFatalError(Context context, String title, String message) {
        try {
            if (sInstance == null) initialize(context);
            getInstance().get(Target.APP)
                    .send(new HitBuilders.EventBuilder()
                            .setCategory("Fatal Error: " + title)
                            .setAction(message)
                            .setLabel(Arrays.toString(Thread.currentThread().getStackTrace())
                                    + "\n" + getDeviceInfo())
                            .setValue(1)
                            .build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendPageReadStats(Context context, HashSet<Integer> pages, long timeMs) {
        try {
            String val;
            timeMs /= 1000;
            if (timeMs < 60)
                val = timeMs + "s";
            else if (timeMs < 60 * 60)
                val = (timeMs / 60.0) + "m";
            else
                val = (timeMs / (60 * 60.0)) + "h";
            Object[] arr = pages.toArray();
            if (sInstance == null) initialize(context);
            getInstance().get(Target.APP)
                    .send(new HitBuilders.EventBuilder("Usage Stats", "readPages")
                            .setLabel(arr.length + ": " + Arrays.toString(arr) + ", in " + val)
                            .build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendTafseerStats(Context context, HashSet<String> strings) {
        try {
            if (sInstance == null) initialize(context);
            Object[] arr = strings.toArray();
            getInstance().get(Target.APP)
                    .send(new HitBuilders.EventBuilder("Usage Stats", "ViewTafseer")
                            .setLabel(arr.length + ": " + Arrays.toString(arr))
                            .build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendListenReciteStats(Context context, HashSet<String> strings) {
        try {
            if (sInstance == null) initialize(context);
            Object[] arr = strings.toArray();
            getInstance().get(Target.APP)
                    .send(new HitBuilders.EventBuilder("Usage Stats", "ListenRecites")
                            .setLabel(arr.length + ": " + Arrays.toString(arr))
                            .build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendComment(Context context, String comment) {
        try {
            if (sInstance == null) initialize(context);
            getInstance().get(Target.APP)
                    .send(new HitBuilders.EventBuilder("User Interactions", "Comment")
                            .setLabel(comment + "\n" + getDeviceInfo())
                            .build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendDownloadPages(Context context) {
        try {
            if (sInstance == null) initialize(context);
            getInstance().get(Target.APP)
                    .send(new HitBuilders.EventBuilder("Download", "Pages")
                            .setLabel(getDeviceInfo())
                            .build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendDownloadRecites(Context context,
                                           String reciter, HashSet<Integer> surahs) {
        try {
            if (sInstance == null) initialize(context);
            getInstance().get(Target.APP).send(new HitBuilders.EventBuilder("Download",
                    "Recites").setLabel(reciter + ": " + Arrays.toString(surahs.toArray()))
                    .build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendDownloadRecites(Context context,
                                           String reciter, int surah) {
        try {
            if (sInstance == null) initialize(context);
            getInstance().get(Target.APP).send(new HitBuilders.EventBuilder("Download",
                    "Recites").setLabel(reciter + ": " + surah).build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private final Map<Target, Tracker> mTrackers = new HashMap<>();
    private final Context mContext;

    /**
     * Don't instantiate directly - use {@link #getInstance()} instead.
     */
    private AnalyticsTrackers(Context context) {
        mContext = context.getApplicationContext();
    }

    public synchronized Tracker get(Target target) {
        if (!mTrackers.containsKey(target)) {
            Tracker tracker;
            switch (target) {
                case APP:
                    tracker = GoogleAnalytics.getInstance(mContext).newTracker(R.xml.app_tracker);
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled analytics target " + target);
            }
            mTrackers.put(target, tracker);
        }

        return mTrackers.get(target);
    }
}
