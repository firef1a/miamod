package mia.miamod.features.impl.general.title;

public enum DFIcons implements QuickJoinIcon {
    DIAMONDFIRE("diamondfire"),
    PRIDE("pridefire"),
    TRANS("transfire"),
    MELON("melon"),
    MELON_KING("melon_king"),
    SITE03("site03"),
    MACE("mace");


    private final String path;
    private DFIcons(String path) {
        this.path = path;
    }

    public String getAsString() { return this.toString(); }

    public String getDisabledPath() { return path + "/" + path + "_disabled.png"; }
    public String getEnabledPath() { return path + "/" + path + "_enabled.png"; }
}
