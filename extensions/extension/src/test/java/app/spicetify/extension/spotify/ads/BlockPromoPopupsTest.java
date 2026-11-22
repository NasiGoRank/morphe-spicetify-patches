package app.spicetify.extension.spotify.ads;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class BlockPromoPopupsTest {
    @Test public void onlyMatchesKnownRxSingleFallbacks() {
        assertTrue(BlockPromoPopups.isOnErrorReturn("io.reactivex.rxjava3.internal.operators.single.SingleOnErrorReturn"));
        assertTrue(BlockPromoPopups.isOnErrorReturn("io.reactivex.internal.operators.single.SingleOnErrorReturn"));
        assertFalse(BlockPromoPopups.isOnErrorReturn("io.reactivex.rxjava3.internal.operators.single.SingleJust"));
        assertFalse(BlockPromoPopups.isOnErrorReturn("android.app.Dialog"));
        assertFalse(BlockPromoPopups.isOnErrorReturn("com.spotify.player.PremiumUpsell"));
        assertFalse(BlockPromoPopups.isOnErrorReturn(null));
    }
}
