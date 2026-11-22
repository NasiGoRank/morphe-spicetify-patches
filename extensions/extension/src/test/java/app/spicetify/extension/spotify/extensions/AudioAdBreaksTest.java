package app.spicetify.extension.spotify.extensions;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

/** The ad slot request encodes an explicit false, not a missing proto field. */
public class AudioAdBreaksTest {
    @Test
    public void streamSlotGetsExplicitDisabledBoolean() {
        assertArrayEquals(new byte[] {
            0x0A, 0x06, 's', 't', 'r', 'e', 'a', 'm', 0x10, 0x00
        }, AudioAdBreaks.disableSlotRequest("stream"));
    }

    @Test
    public void prerollSlotGetsExplicitDisabledBoolean() {
        assertArrayEquals(new byte[] {
            0x0A, 0x07, 'p', 'r', 'e', 'r', 'o', 'l', 'l', 0x10, 0x00
        }, AudioAdBreaks.disableSlotRequest("preroll"));
    }
}
