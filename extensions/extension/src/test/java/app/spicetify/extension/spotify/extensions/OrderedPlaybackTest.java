package app.spicetify.extension.spotify.extensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Test;

public class OrderedPlaybackTest {
    @Test
    public void acceptsOnlyTrackCollectionsNotIndividualSongOrRadio() {
        assertTrue(OrderedPlayback.supportedContext("spotify:playlist:example"));
        assertTrue(OrderedPlayback.supportedContext("spotify:album:example"));
        assertFalse(OrderedPlayback.supportedContext("spotify:track:example"));
        assertFalse(OrderedPlayback.supportedContext("spotify:station:example"));
        assertFalse(OrderedPlayback.supportedContext(null));
    }

    @Test
    public void explicitOrderPlayRequestRetainsTheOriginalTrackSequence() {
        String first = "spotify:track:AAAA";
        String second = "spotify:track:BBBB";
        String third = "spotify:track:CCCC";
        byte[] wire = Esperanto.playOrder("spotify:playlist:example", List.of(first, second, third));
        String debug = new String(wire, StandardCharsets.ISO_8859_1);
        assertTrue(debug.indexOf(first) >= 0);
        assertTrue(debug.indexOf(second) > debug.indexOf(first));
        assertTrue(debug.indexOf(third) > debug.indexOf(second));
        assertEquals(0x0A, wire[0] & 0xff);
    }
}
