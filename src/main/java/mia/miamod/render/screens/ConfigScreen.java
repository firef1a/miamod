package mia.miamod.render.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import mia.miamod.ColorBank;
import mia.miamod.Mod;
import mia.miamod.config.ConfigStore;
import mia.miamod.core.SoundManager;
import mia.miamod.features.Categories;
import mia.miamod.features.Category;
import mia.miamod.features.Feature;
import mia.miamod.features.impl.internal.ConfigScreenFeature;
import mia.miamod.features.parameters.ParameterDataField;
import mia.miamod.features.parameters.impl.BooleanDataField;
import mia.miamod.features.parameters.impl.EnumDataField;
import mia.miamod.features.parameters.impl.IntegerSliderDataField;
import mia.miamod.features.parameters.impl.InternalDataField;
import mia.miamod.render.util.ARGB;
import mia.miamod.render.util.EasingFunctions;
import mia.miamod.render.util.RenderHelper;
import mia.miamod.render.util.data.*;
import mia.miamod.render.util.data.impl.VertexEnableButton;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Comparator;

public class ConfigScreen extends Screen {
    private static AnimationStage configScreenStage;
    private float animation;
    private static final float animationSpeed = 0.075F;//0.05F;
    private final Screen parent;
    private static final Matrix4f identityMatrix4f = new Matrix4f().identity();

    private VertexRect screen;
    private VertexRect sidebar;
    private VertexRect main;

    private ArrayList<CategoryButton> categoryButtons;
    private CategoryButton selectedCategoryButton;

    private ArrayList<VertexButton> configButtons;

    float   centerX, centerY,
            width, height,
            topLeftX, topLeftY;

    public ConfigScreen(Screen parent) {
        super(Text.literal("Config Screen"));
        this.parent = parent;

        centerX = (Mod.getScaledWindowWidth() / 2F);
        centerY = (Mod.getScaledWindowHeight() / 2F);

        width = 600;
        height = 400;
        topLeftX = -width/2;
        topLeftY = -height/2;

        initBuffer();
        openStage();
    }

    private static class CategoryButton extends VertexButton {
        private final Category category;

        public CategoryButton(Category category, Matrix4f matrix4f, float x, float y, float width, float height, float z, ARGB argb, ARGB highlightARGB, ARGB enabledARGB, ARGB highlighEnabledARGB, Runnable callback) {
            super(matrix4f, x, y, width, height, z, argb, highlightARGB, enabledARGB, highlighEnabledARGB, callback);
            this.category = category;
        }

        public Category getCategory() { return category; }
    }

    private static class FeatureButton extends VertexEnableButton {
        private final Feature feature;
        public FeatureButton(Feature feature, Matrix4f matrix4f, float x, float y, float width, float height, float z, Runnable callback) {
            super(matrix4f, x, y, width, height, z, callback);
            this.feature = feature;
        }

        @Override
        public void setEnabled(boolean enabled) { this.feature.setEnabled(enabled); }

        @Override
        public boolean getEnabled() { return this.feature.getEnabled(); }

        public Feature getFeature() { return feature; }
    }

    private class BooleanFieldButton extends VertexEnableButton {
        private BooleanDataField booleanDataField;
        public BooleanFieldButton(BooleanDataField booleanDataField, Matrix4f matrix4f, float x, float y, float width, float height, float z, Runnable callback) {
            super(matrix4f, x, y, width, height, z, callback);
            this.booleanDataField = booleanDataField;
        }

        @Override
        public void setEnabled(boolean enabled) { this.booleanDataField.setValue(enabled); }

        @Override
        public boolean getEnabled() { return this.booleanDataField.getValue(); }

        public BooleanDataField getBooleanDataField() { return booleanDataField; }
    }

    private class EnumParameterText extends TextBufferDrawable {
        public EnumDataField parameter;
        public EnumParameterText(EnumDataField parameter, Matrix4f matrix4f, Text text, float x, float y, float z, ARGB argb, boolean shadow) {
            super(matrix4f, text, x, y, z, argb, shadow);
            this.parameter = parameter;
        }

        @Override
        protected Text getText() {
            return this.parameter == null ? Text.empty() : Text.literal(this.parameter.getValue().name());
        }
    }

    private class FeatureContainer extends VertexButton {
        private final Feature feature;
        public VertexButton featureEnableButton;
        public VertexRect featureParameterContainer;
        private final TextBufferDrawable featureNameText;
        public ArrayList<VertexRect> featureParameters;

        public FeatureContainer(Feature feature, Matrix4f matrix4f, float x, float y, float width, float height, float z) {
            super(matrix4f, x, y, width, height, z,
                    feature.getAlwaysEnabled() ? new ARGB(0x303a42,0.9) :new ARGB(0x11171c, 0.85),
                    feature.getAlwaysEnabled() ? new ARGB(0x303a42, 0.9) : new ARGB(0x212b33,0.85f),
                    feature.getAlwaysEnabled() ? new ARGB(0x303a42, 0.9) : new ARGB(0x303a42,0.9f),
                    feature.getAlwaysEnabled() ? new ARGB(0x303a42, 0.9) : new ARGB(0x3f4952,0.9f),
                    () -> {
                        if (!feature.getAlwaysEnabled()) {
                            feature.setEnabled(!feature.getEnabled());
                        }
                    }
            );
            this.feature = feature;
            this.featureParameters = new ArrayList<>();

            int buttonMargin = 6;
            float buttonSize = this.height - 6;
            float leftMargin = (this.height / 2) - ((this.height - buttonMargin) / 2) + 2;

            this.featureNameText = new TextBufferDrawable(
                    matrix4f,
                    Text.literal(this.feature.getName()).styled(style -> style.withItalic(true)),
                    leftMargin,
                    0,
                    z + 1,
                    new ARGB(ColorBank.WHITE, 1f),
                    true
            );

            addDrawable(featureNameText);
            featureNameText.setParentBinding(new DrawableBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
            featureNameText.setSelfBinding(new DrawableBinding(AxisBinding.NONE, AxisBinding.MIDDLE));

            if (!feature.getAlwaysEnabled()) {
                this.featureEnableButton = new FeatureButton(
                        feature,
                        matrix4f,
                        -leftMargin, 0,
                        buttonSize, buttonSize,
                        this.z + 1,
                        () -> {
                        }
                );
                featureEnableButton.setParentBinding(new DrawableBinding(AxisBinding.FULL, AxisBinding.MIDDLE));
                featureEnableButton.setSelfBinding(new DrawableBinding(AxisBinding.FULL, AxisBinding.MIDDLE));
                addDrawable(featureEnableButton);
            }

            featureParameterContainer = new VertexRect(
                    matrix4f,
                    0,0,
                    width,
                    0,
                    z,
                    new ARGB(this.argb.getRGB(), this.argb.getAlpha()*0.5)
            );
            featureParameterContainer.setParentBinding(new DrawableBinding(AxisBinding.NONE, AxisBinding.FULL));
            addDrawable(featureParameterContainer);

            float fieldY = 0;
            for (ParameterDataField<?> field : feature.getParameterDataFields()) {
                if (!field.isConfig()) continue;

                ArrayList<BufferDrawable> fieldRenderChildren = new ArrayList<>();
                Runnable clickCallback = null;

                if (field instanceof BooleanDataField booleanDataField) {
                    if (!(booleanDataField instanceof InternalDataField)) clickCallback = () -> { booleanDataField.setValue(!booleanDataField.getValue()); };
                    BooleanFieldButton parameterEnableButton = new BooleanFieldButton(
                            booleanDataField,
                            matrix4f,
                            -leftMargin, 0,
                            buttonSize, buttonSize,
                            featureParameterContainer.getZ()+2,
                            () -> { }
                    );
                    parameterEnableButton.setParentBinding(new DrawableBinding(AxisBinding.FULL, AxisBinding.MIDDLE));
                    parameterEnableButton.setSelfBinding(new DrawableBinding(AxisBinding.FULL, AxisBinding.MIDDLE));
                    fieldRenderChildren.add(parameterEnableButton);

                } else if (field instanceof EnumDataField<? extends Enum> enumDataField) {
                    Enum<?> icon = enumDataField.getValue();
                    Enum<?>[] enums = enumDataField.getDataClassType().getEnumConstants();

                    clickCallback = () -> { enumDataField.setValue(enumDataField.getValue().ordinal() + 1 == enums.length ? enums[0] : enums[enumDataField.getValue().ordinal()+1]); };

                    EnumParameterText enumText = new EnumParameterText(
                            enumDataField,
                            matrix4f,
                            Text.literal(icon.name()),
                            -leftMargin, 0,
                            featureParameterContainer.getZ() + 2,
                            new ARGB(ColorBank.WHITE, 1f),
                            true
                    );
                    enumText.setParentBinding(new DrawableBinding(AxisBinding.FULL, AxisBinding.MIDDLE));
                    enumText.setSelfBinding(new DrawableBinding(AxisBinding.FULL, AxisBinding.MIDDLE));
                    fieldRenderChildren.add(enumText);
                } else if (field instanceof IntegerSliderDataField integerSliderDataField) {

                }


                VertexRect fieldRect;
                if (clickCallback != null) {
                    fieldRect = new VertexButton(
                            matrix4f,
                            0, fieldY,
                            width,
                            this.height,
                            featureParameterContainer.getZ()+1,
                            this.argb,
                            this.highlightARGB,
                            this.enabledARGB,
                            this.highlighEnabledARGB,
                            clickCallback

                    ) {
                        @Override
                        protected ARGB colorWrapper(ARGB color) {
                            return new ARGB(color.getRGB(), color.getAlpha()*0.75);
                        }
                    };
                    configButtons.add((VertexButton) fieldRect);
                } else {
                     fieldRect = new VertexRect(
                            matrix4f,
                            0, fieldY,
                            width,
                            this.height,
                            featureParameterContainer.getZ()+1,
                            new ARGB(argb().getRGB(), argb().getAlpha()*0.75)
                    );
                }

                featureParameters.add(fieldRect);
                featureParameterContainer.addDrawable(fieldRect);

                TextBufferDrawable fieldText = new TextBufferDrawable(
                        matrix4f,
                        Text.literal(field.getIdentifier().parameter() + ": "),
                        leftMargin,0,
                        fieldRect.getZ()+1,
                        new ARGB(ColorBank.WHITE, 1f),
                        true
                );
                fieldRenderChildren.addFirst(fieldText);

                fieldText.setParentBinding(new DrawableBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
                fieldText.setSelfBinding(new DrawableBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
                fieldRect.addDrawable(fieldText);

                fieldRenderChildren.forEach(fieldRect::addDrawable);

                fieldY += fieldRect.getHeight();
            }
        }

        @Override
        protected ARGB colorWrapper(ARGB color) {
            return color;
            //return feature.getAlwaysEnabled() ? new ARGB(0x303a42,0.9) : color;
        }

        @Override
        public void setEnabled(boolean enabled) { this.feature.setEnabled(enabled); }

        @Override
        public boolean getEnabled() { return this.feature.getEnabled(); }

        public Feature getFeature() { return feature; }

        @Override
        protected void draw(DrawContext context, int mouseX, int mouseY) {
            super.draw(context, mouseX, mouseY);
            if (containsPoint((int) mouseX, (int) mouseY, true)) {
                if (ConfigScreen.configScreenStage.equals(AnimationStage.OPEN)) {
                    context.drawTooltip(Mod.MC.textRenderer, Text.literal(feature.getDescription()), mouseX, mouseY);
                }
            }
        }
    }



    private Matrix4f animationMatrix(float time) {
        Matrix4f animationMatrix = new Matrix4f().identity();
        animationMatrix.scale(EasingFunctions.easeInOutCubic(time), EasingFunctions.easeInOutCubic(time),1f);
        Vector3f axis = new Vector3f(-1, 1,0).normalize();
        float angle = (float) (Math.PI / 2);
        animationMatrix.rotate(RotationAxis.of(axis).rotation((-angle) * (1 - EasingFunctions.easeInOutCubic(time))));
        return animationMatrix;
    }



    private void initBuffer() {
        // render stuff
        configButtons = new ArrayList<>();

        int topBarHeight = 20;
        int sidebarWidth = 100;

        int backgroundColor = ColorBank.BLACK; //0xaf7cf2;

        // main thing
        screen = new VertexRect(
                identityMatrix4f,
                topLeftX,
                topLeftY,
                width,
                height,
                5,
                new ARGB(backgroundColor, 0));

        VertexRect topBar = new VertexRect(
                identityMatrix4f,
                0,
                0,
                width,
                topBarHeight,
                screen.getZ()+1,
                new ARGB(backgroundColor, 0.80));

        sidebar = new VertexRect(
                identityMatrix4f,
                0,
                topBar.getHeight(),
                sidebarWidth,
                height - (topBar.getHeight()),
                screen.getZ()+1,
                new ARGB(backgroundColor, 0.70));


        main = new VertexRect(
                identityMatrix4f,
                sidebar.getWidth(),
                0,
                width - (sidebar.getWidth()),
                height - (topBar.getHeight()),
                sidebar.getZ()+1,
                new ARGB(backgroundColor, 0.40));

        sidebar.addDrawable(main);
        topBar.addDrawable(sidebar);
        screen.addDrawable(topBar);

        TextBufferDrawable miamod_config_text = new TextBufferDrawable(
                identityMatrix4f,
                Text.literal("miamod config").styled((style -> style.withItalic(true))).withColor(ColorBank.MC_GRAY),
                (topBarHeight / 2F) - (Mod.MC.textRenderer.fontHeight / 2F),
                (topBarHeight / 2F) - (Mod.MC.textRenderer.fontHeight / 2F),
                topBar.getZ() - 1F,
                new ARGB(ColorBank.WHITE, 1F),
                true
        );
        topBar.addDrawable(miamod_config_text);

        // categories
        categoryButtons = new ArrayList<>();
        //selectedCategory = Categories.getCategories().getFirst();

        int i = 0;
        int categoryHeight = 15;

        for (Category category : Categories.getCategories()) {
            String categoryName = category.getName();

            float y = ((categoryHeight + 1) * i);
            CategoryButton button = new CategoryButton(
                    category,
                    identityMatrix4f,
                    0,
                    y,
                    sidebar.getWidth(),
                    categoryHeight,
                    sidebar.getZ() + 1,
                    new ARGB(0xcc8de0, 0.55F),
                    new ARGB(0xcc8de0, 0.7F),
                    new ARGB(0xdba9eb, 0.8F),
                    new ARGB(0xdba9eb, 0.9F),
                    () -> { }
            ) {
                @Override
                protected boolean onMouseDown(int mouseX, int mouseY) {
                    if (this.getEnabled()) return false;
                    categoryButtons.forEach(each -> each.setEnabled(false));
                    selectedCategoryButton.setEnabled(false);
                    selectedCategoryButton = this;
                    this.setEnabled(true);
                    setMenuCategory(this.getCategory());
                    return true;
                }
            };
            sidebar.addDrawable(button);
            categoryButtons.add(button);
            if (i == 0) {
                selectedCategoryButton = button;
                button.setEnabled(true);
            }

            float margin = (categoryHeight / 2) - (Mod.MC.textRenderer.fontHeight/2);

            TextBufferDrawable textPrimative = new TextBufferDrawable(
                    identityMatrix4f,
                    Text.literal(categoryName),
                    margin,
                    margin,
                    button.getZ()+1,
                    new ARGB(ColorBank.WHITE, 1F),
                    true
            );
            button.addDrawable(textPrimative);
            i++;
        }
        setMenuCategory(selectedCategoryButton.getCategory());
    }

    private void setMenuCategory(Category category) {
        ArrayList<FeatureContainer> featureContainers = new ArrayList<>();
        configButtons = new ArrayList<>();
        main.clearDrawables();

        VertexRect mainContainer = new VertexRect(
                identityMatrix4f,
                2,
                0,
                main.getWidth() - 4,
                main.getHeight(),
                main.getZ() + 1,
                new ARGB(ColorBank.BLACK, 0f)
        );

        main.addDrawable(mainContainer);

        for (Feature feature : category.getFeatures()) {
            int y = 2;
            FeatureContainer featureContainer = new FeatureContainer(
                    feature,
                    identityMatrix4f,
                    0,
                    y,
                    mainContainer.getWidth(),
                    15,
                    mainContainer.getZ()+1
            );
            configButtons.add(featureContainer);

            if (featureContainers.isEmpty()) {
                mainContainer.addDrawable(featureContainer);
            } else {
                featureContainer.setParentBinding(new DrawableBinding(AxisBinding.NONE, AxisBinding.FULL));

                if (featureContainers.getLast().featureParameters.isEmpty())  {
                    featureContainers.getLast().addDrawable(featureContainer);
                } else {
                    featureContainers.getLast().featureParameters.getLast().addDrawable(featureContainer);
                };

            }
            featureContainers.add(featureContainer);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (parent != null) parent.render(context, Integer.MAX_VALUE, Integer.MAX_VALUE, delta);
        //super.render(context, mouseX, mouseY, delta);
        this.applyBlur();
        //this.renderDarkening(context);

        // create stack
        Tessellator tessellator = Tessellator.getInstance();
        //VertexConsumerProvider.Immediate vertexConsumerProvider = context.vertexConsumers;
        RenderHelper renderHelper = new RenderHelper(tessellator, mouseX, mouseY, delta);
        MatrixStack matrices = context.getMatrices();

        // enable settings

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        //RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // start animation layer
        matrices.push();
        matrices.translate(centerX, centerY, 0);
        matrices.push();
        // open/close animation
        matrices.multiplyPositionMatrix(animationMatrix(animation));
        //matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) ((float) (((double) (mouseX - (Mod.getScaledWindowWidth()) / 2)) / Mod.getScaledWindowWidth()) * (-Math.PI))));
        //matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) ((float) (((double) (mouseY - (Mod.getScaledWindowHeight()) / 2)) / Mod.getScaledWindowHeight()) * (Math.PI))));
        matrices.push();
        // render screen objects
        Matrix4f renderMatrix4f = new Matrix4f(matrices.peek().getPositionMatrix());
        // end matrices
        matrices.pop();
        matrices.pop();
        matrices.pop();

        // global draw
        renderHelper.contextDraw(renderMatrix4f, context, screen);

        // disable settings
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        //RenderSystem.enableDepthTest();
        RenderHelper.clearStencil();

        // advance animation
        if (configScreenStage.equals(AnimationStage.OPENING)) {
            animation = Math.min(1F, animation + animationSpeed);
            if (animation >= 1F) configScreenStage = AnimationStage.OPEN;
        }
        if (configScreenStage.equals(AnimationStage.CLOSING)) {
            animation = Math.max(0F, animation - animationSpeed);
            if (animation <= 0F) {
                configScreenStage = AnimationStage.CLOSED;
                if (parent != null) {
                    Mod.MC.setScreen((Screen) parent);
                    ConfigScreenFeature.clearConfigScreen();
                }
            }
        }
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        if (configButtons != null) {
            configButtons.sort(Comparator.comparingInt(a -> (int) a.getZ()));

            ArrayList<VertexButton> buttons = new ArrayList<>();
            buttons.addAll(configButtons);
            buttons.addAll(categoryButtons);
            for (VertexButton configButton : buttons) {
                if (configButton.mouseDownEvent(mx, my)) {
                    SoundManager.playUIButtonClick();
                    break;
                }
            }

        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        if (configButtons != null) {
            configButtons.sort(Comparator.comparingInt(a -> (int) a.getZ()));

            ArrayList<VertexButton> buttons = new ArrayList<>();
            buttons.addAll(configButtons);
            buttons.addAll(categoryButtons);
            for (VertexButton configButton : buttons) {
                if (configButton.mouseUpEvent(mx, my)) {
                    break;
                }
            }

        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        if (configButtons != null) {
            configButtons.sort(Comparator.comparingInt(a -> (int) a.getZ()));

            ArrayList<VertexButton> buttons = new ArrayList<>();
            buttons.addAll(configButtons);
            buttons.addAll(categoryButtons);
            for (VertexButton configButton : buttons) {
                if (configButton.mouseDragEvent(mx, my)) {
                    break;
                }
            }

        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public void openStage() { configScreenStage = AnimationStage.OPENING; }
    public void closeStage() { configScreenStage = AnimationStage.CLOSING; }

    public AnimationStage getStage() {
        return configScreenStage;
    }

    @Override
    public void close() {
        ConfigStore.save();
        closeStage();
        if (parent == null) Mod.MC.setScreen((Screen) null);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {

        //if (parent == null) return;
        //parent.renderBackground(context, mouseX, mouseY, delta);
        //super.renderBackground(context, mouseX, mouseY, delta);
    }


}
