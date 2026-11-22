package app.spicetify.extension.spotify.extensions;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class ExperimentalPlaybackControlsTest {
    @Test
    public void explicitFalseForAudioSlots() {
        assertArrayEquals(new byte[] {
            0x0A, 0x06, 's', 't', 'r', 'e', 'a', 'm', 0x10, 0x00
        }, AudioAdBreaks.disableSlotRequest("stream"));
        assertArrayEquals(new byte[] {
            0x0A, 0x07, 'p', 'r', 'e', 'r', 'o', 'l', 'l', 0x10, 0x00
        }, AudioAdBreaks.disableSlotRequest("preroll"));
    }

    @Test
    public void shuffleOffSendsExplicitOptionalFalse() {
        assertArrayEquals(new byte[] {0x1A, 0x02, 0x08, 0x00},
                ShuffleOff.shuffleOffRequest());
    }
}
