package app.spicetify.extension.spotify.ads;

import android.util.Log;
import app.spicetify.extension.spotify.settings.PatchSettings;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Opt-in filter for Pendragon fullscreen promotional messages.
 * Adapted from the GPL-3.0 pablobaro/pablobaro-morphe-patches
 * technique, with explicit evaluation of Spotify's existing RxJava fallback.
 */
public final class BlockPromoPopups {
    private static final String TAG = "SpicetifyPromo";
    private static final AtomicInteger SUPPRESSED = new AtomicInteger();
    private BlockPromoPopups() {}

    /** Runs at the return of a Pendragon FetchMessage/FetchMessageList request. */
    public static Object replaceRequest(Object request) {
        if (!PatchSettings.blockPremiumPromoPopupsEnabled() || request == null
                || !isOnErrorReturn(request.getClass().getName())) return request;
        try {
            ClassLoader loader = request.getClass().getClassLoader();
            boolean rx3 = request.getClass().getName().startsWith("io.reactivex.rxjava3.");
            String fName = rx3 ? "io.reactivex.rxjava3.functions.Function" : "io.reactivex.functions.Function";
            String sName = rx3 ? "io.reactivex.rxjava3.core.Single" : "io.reactivex.Single";
            Class<?> fnClass = Class.forName(fName, false, loader);
            Class<?> singleClass = Class.forName(sName, false, loader);
            if (!singleClass.isInstance(request)) return request;

            Field supplierField = findSupplier(request.getClass(), fnClass);
            if (supplierField == null) return request;
            supplierField.setAccessible(true);
            Object supplier = supplierField.get(request);
            if (supplier == null) return request;

            // Calling the fallback is necessary: Single.just(supplier) would emit
            // the Function itself rather than the expected Spotify response.
            Method apply = fnClass.getMethod("apply", Object.class);
            Object fallback = apply.invoke(supplier, new IOException("message unavailable"));
            if (fallback == null) return request;
            Object safeSingle = singleClass.getMethod("just", Object.class).invoke(null, fallback);
            SUPPRESSED.incrementAndGet();
            return safeSingle;
        } catch (Throwable ex) {
            // Fail open on unknown RxJava shape; do not log content or identifiers.
            Log.w(TAG, "Popup fallback unavailable; request left unchanged: " + ex.getClass().getSimpleName());
            return request;
        }
    }

    static boolean isOnErrorReturn(String className) {
        return className != null && className.endsWith(".SingleOnErrorReturn")
                && (className.startsWith("io.reactivex.rxjava3.")
                        || className.startsWith("io.reactivex."));
    }

    private static Field findSupplier(Class<?> type, Class<?> functionClass) {
        for (Class<?> cls = type; cls != null; cls = cls.getSuperclass()) {
            for (Field f : cls.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers())
                        && functionClass.isAssignableFrom(f.getType())) return f;
            }
        }
        return null;
    }

    /** Counts request substitutions, NOT actual displayed promo popups. */
    public static int suppressedRequestCount() { return SUPPRESSED.get(); }
}
