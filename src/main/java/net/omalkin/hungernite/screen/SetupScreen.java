package net.omalkin.hungernite.screen;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.appliedenergistics.yoga.YogaFlexDirection;

@OnlyIn(Dist.CLIENT)
public class SetupScreen extends Screen {
    public SetupScreen() {
        super(Component.literal("Constructor text"));
    }

    @Override
    protected void init() {
        super.init();
        var modularUI = createModularUI();
        modularUI.setScreenAndInit(this);
        this.addRenderableWidget(modularUI.getWidget());
    }

    private static ModularUI createModularUI() {
        // create a root element
        var root = new UIElement();
        // add an element to display an image based on a resource location
        var image = new UIElement().layout(layout -> layout.width(80).height(80))
                .style(style -> style.background(
                        SpriteTexture.of("ldlib2:textures/gui/icon.png"))
                );
        root.addChildren(
                // add a label to display text
                new Label().setText("Interaction")
                        // center align text
                        .textStyle(textStyle -> textStyle.textAlignHorizontal(Horizontal.CENTER)),
                image,
                // add a container with the row flex direction
                new UIElement().layout(layout -> layout.flexDirection(YogaFlexDirection.ROW)).addChildren(
                        // a button to rotate the image -45°
                        new Button().setText("-45°")
                                .setOnClick(e -> image.transform(transform ->
                                        transform.rotation(transform.rotation()-45))),
                        new UIElement().layout(layout -> layout.flex(1)), // occupies the remaining space
                        // a button to rotate the image 45°
                        new Button().setText("+45°")
                                .setOnClick(e -> image.transform(transform ->
                                        transform.rotation(transform.rotation() + 45)))
                )
        ).style(style -> style.background(Sprites.BORDER)); // set a background for the root element
        // set padding and gap for children elements
        root.layout(layout -> layout.paddingAll(7).gapAll(5));
        // create a UI
        var ui = UI.of(root);
        // return a modular UI for runtime instance
        return ModularUI.of(ui);
    }
}
