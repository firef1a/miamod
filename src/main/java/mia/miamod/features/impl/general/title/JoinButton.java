package mia.miamod.features.impl.general.title;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.parameters.ParameterDataField;
import mia.miamod.features.parameters.ParameterIdentifier;
import mia.miamod.features.parameters.impl.EnumDataField;
import mia.miamod.features.parameters.impl.IntegerDataField;
import mia.miamod.features.parameters.impl.StringDataField;

public final class JoinButton extends Feature {
    private static StringDataField serverAddress;
    private static IntegerDataField serverPort;
    private static EnumDataField<DFIcons> joinIcon;

    public JoinButton(Categories category) {
        super(category, "Menu Join Button", "quickjoin", "Title menu join button");
        serverAddress = new StringDataField("Server Address", ParameterIdentifier.of(this, "server_address"), "node1.mcdiamondfire.com", true);
        serverPort = new IntegerDataField("Server Port", ParameterIdentifier.of(this, "server_port"), 25565, true);
        joinIcon = new EnumDataField<>("Join Icon", ParameterIdentifier.of(this, "join_icon"), DFIcons.gay, true);
    }

    public static String getCustomServerAddress() { return serverAddress.getValue(); }
    public static int getCustomServerPort() { return serverPort.getValue(); }
    public static DFIcons getJoinIcon() { return joinIcon.getValue(); }
}
