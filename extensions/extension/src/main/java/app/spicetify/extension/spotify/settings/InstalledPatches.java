package app.spicetify.extension.spotify.settings;

public final class InstalledPatches {
    private InstalledPatches() {}

    // Selected patches replace these method bodies in the injected extension.
    public static boolean cleanSharing() {
        return false;
    }

    public static boolean themeColors() {
        return false;
    }

    public static boolean homePins() {
        return false;
    }

    public static boolean serverFiles() {
        return false;
    }

    public static boolean hidePremiumTab() {
        return false;
    }

    public static boolean hideBrandAds() {
        return false;
    }

    public static boolean hidePlayerAdCards() {
        return false;
    }

    public static boolean extensions() {
        return false;
    }

    /** Opt-in experimental audio ad slot control (not an entitlement bypass). */
    public static boolean disableAudioAdBreaks() {
        return false;
    }

    /** Opt-in popup marketing filter, independent of the Premium tab. */
    public static boolean blockPremiumPromoPopups() {
        return false;
    }
}
