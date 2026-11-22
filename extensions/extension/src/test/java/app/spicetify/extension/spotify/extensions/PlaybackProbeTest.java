package app.spicetify.extension.spotify.extensions;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class PlaybackProbeTest {
    @Test
    public void directShuffleOffIsExplicitFalse() {
        assertArrayEquals(new byte[] {0x08, 0x00}, PlaybackProbe.directShuffleOffRequest());
    }

    @Test
    public void parsesShuffleOffAndForceSuppressionWithoutSharingIdentifiers() throws Exception {
        Wire.Writer options = new Wire.Writer();
        options.bool(1, false);
        Wire.Writer state = new Wire.Writer();
        state.message(17, options);
        state.string(19, "mft/force_shuffle");
        state.string(19, "ad");
        state.string(2, "spotify:playlist:private-example");
        Esperanto.PlayerState parsed = Esperanto.parseState(state.toByteArray());
        assertEquals(Boolean.FALSE, parsed.shuffleEnabled);
        assertTrue(parsed.forceShuffleSuppression);
    }

    @Test
    public void parsesShuffleOnWithoutForceSuppression() throws Exception {
        Wire.Writer options = new Wire.Writer();
        options.bool(1, true);
        Wire.Writer state = new Wire.Writer();
        state.message(17, options);
        state.string(19, "ad");
        Esperanto.PlayerState parsed = Esperanto.parseState(state.toByteArray());
        assertEquals(Boolean.TRUE, parsed.shuffleEnabled);
        assertFalse(parsed.forceShuffleSuppression);
    }

    @Test
    public void missingPlayerOptionsIsUnknownRatherThanAssumedOff() throws Exception {
        Esperanto.PlayerState parsed = Esperanto.parseState(new byte[0]);
        assertNull(parsed.shuffleEnabled);
        assertFalse(parsed.forceShuffleSuppression);
    }
}
