package mia.miamod.mixin.render;


import mia.miamod.features.FeatureManager;
import mia.miamod.features.impl.development.CodeSignColorer;
import mia.miamod.features.impl.internal.mode.LocationAPI;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignBlockEntity.class)
public abstract class MSignBlockEntity {
    @Shadow
    private SignText frontText;

    @Inject(method = "getFrontText", at = @At("HEAD"), cancellable = true)
    public void getFrontText(CallbackInfoReturnable<SignText> cir) {
        if (LocationAPI.getMode().canViewCode() && FeatureManager.getFeature(CodeSignColorer.class).getEnabled()) {
            SignText orig = this.frontText;
            Text line1 = orig.getMessage(0, false);
            Text line2 = orig.getMessage(1, false);
            Text line3 = orig.getMessage(2, false);
            Text line4 = orig.getMessage(3, false);

            line1 = Text.empty().append(line1).setStyle(Style.EMPTY.withColor(0xAAAAAA));
            line2 = Text.empty().append(line2).setStyle(Style.EMPTY.withColor(0xC5C5C5));
            line3 = Text.empty().append(line3).setStyle(Style.EMPTY.withColor(0xAAFFAA));
            line4 = Text.empty().append(line4).setStyle(Style.EMPTY.withColor(0xFF8800));

            cir.setReturnValue(
                    orig
                            .withMessage(0, line1)
                            .withMessage(1, line2)
                            .withMessage(2, line3)
                            .withMessage(3, line4)
            );

        }
    }
}