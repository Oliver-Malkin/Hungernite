package net.omalkin.hungernite.gui;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ScreenTest extends Screen {
    protected ScreenTest() {
        super(Component.literal("Screen test title"));
    }

    @Override
    public void init() {
        super.init();
        var modularUI = createModularUI();
        modularUI.setScreenAndInit(this);
        this.addRenderableWidget(modularUI.getWidget());
    }

    private static ModularUI createModularUI() {
        // create a root element
        var root = new UIElement();
        root.addChildren(
                // add a label to display text
                new Label().setText("My First UI"),
                // add a button with text
                new Button().setText("Click Me!"),
                // add an element to display an image based on a resource location
                new UIElement().layout(layout -> layout.width(80).height(80))
                        .style(style -> style.background(
                                SpriteTexture.of("ldlib2:textures/gui/icon.png"))
                        )
        ).style(style -> style.background(Sprites.BORDER)); // set a background for the root element
        // create a UI
        var ui = UI.of(root);
        // return a modular UI for runtime instance
        return ModularUI.of(ui);
    }
}
