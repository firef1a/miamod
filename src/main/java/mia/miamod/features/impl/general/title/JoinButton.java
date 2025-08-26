package mia.miamod.features.impl.general.title;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.EnumDropdownControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.parameters.ParameterDataField;
import mia.miamod.features.parameters.ParameterIdentifier;
import mia.miamod.features.parameters.impl.BooleanDataField;
import mia.miamod.features.parameters.impl.IntegerDataField;
import mia.miamod.features.parameters.impl.StringDataField;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class JoinButton extends Feature {
    private static StringDataField serverAddress;
    private static IntegerDataField serverPort;
    private static ParameterDataField<DFIcons> joinIcon;

    public JoinButton(Categories category) {
        super(category, "Menu Join Button", "quickjoin", "Title menu join button");

        serverAddress = new StringDataField("Server Address", ParameterIdentifier.of(this, "server_address"), "node1.mcdiamondfire.com");
        serverPort = new IntegerDataField("Server Port", ParameterIdentifier.of(this, "server_port"), 25565);
        joinIcon = new ParameterDataField<>("Join Icon", ParameterIdentifier.of(this, "join_icon"), DFIcons.PRIDE) {
            @Override
            public void serialize(JsonObject jsonObject) {
                jsonObject.addProperty(identifier.getIdentifier(), dataField.toString());
            }

            @Override
            public DFIcons deserialize(JsonElement jsonObject) {
                return DFIcons.valueOf(jsonObject.getAsString());
            }
        };
    }

    public static String getCustomServerAddress() { return serverAddress.getValue(); }
    public static int getCustomServerPort() { return serverPort.getValue(); }
    public static DFIcons getJoinIcon() { return joinIcon.getValue(); }
}
