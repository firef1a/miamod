package mia.miamod.mixin.hud;


import mia.miamod.ColorBank;
import mia.miamod.Mod;
import mia.miamod.features.FeatureManager;
import mia.miamod.features.impl.general.title.IconButtonWidget;
import mia.miamod.features.impl.general.title.JoinButton;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MTitleScreen extends Screen {
    public MTitleScreen(Text title) {
        super(title);
    }

    @Unique
    private static IconButtonWidget createIconButton(TexturedButtonWidget.PressAction onPress) {
        Tooltip tooltip = Tooltip.of(
                Text.literal("Join DF: ").append(Text.literal("\n" + JoinButton.getCustomServerAddress()).withColor(ColorBank.MC_GRAY))
        );

        IconButtonWidget buttonWidget = new IconButtonWidget(
                0,
                0,
                20,
                20,
                new ButtonTextures(
                        Identifier.tryParse(Mod.MOD_ID, "textures/gui/buttons/" + JoinButton.getJoinIcon().getDisabledPath()), // normal state
                        Identifier.tryParse(Mod.MOD_ID, "textures/gui/buttons/" + JoinButton.getJoinIcon().getEnabledPath())  // focused state
                ),
                onPress
        );
        buttonWidget.setTooltip(tooltip);

        return buttonWidget;
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addCustomButton(CallbackInfo ci) {
        if (!FeatureManager.getFeature(JoinButton.class).getEnabled()) return;

        IconButtonWidget textIconButtonWidget = this.addDrawableChild(createIconButton((button) -> {
            connectToServer(JoinButton.getCustomServerAddress(), JoinButton.getCustomServerPort());
        }));
        textIconButtonWidget.setPosition((this.width / 2) - 100 + 200 + 4, (this.height / 4) + 48);
    }

    @Unique
    private static void connectToServer(String address, int port) {
        if (Mod.MC.world != null) {
            Mod.MC.world.disconnect();
            Mod.MC.disconnect();
        }

        ServerAddress serverAddress = ServerAddress.parse(address + ":" + port);
        ServerInfo serverInfo = new ServerInfo(address, address + ":" + port, ServerInfo.ServerType.OTHER);

        net.minecraft.client.gui.screen.multiplayer.ConnectScreen.connect(Mod.getCurrentScreen(), Mod.MC, serverAddress, serverInfo, true, null);
    }
}
