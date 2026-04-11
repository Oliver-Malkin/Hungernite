package net.omalkin.hungernite.screen.custom;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.omalkin.hungernite.gamemechanics.GameModes;
import net.omalkin.hungernite.gamemechanics.GenerationOptions;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SetupScreen extends Screen {
    public SetupScreen() {
        super(Component.literal("Lobby setup"));
    }

    @Override
    protected void init() {
        super.init();
        var modularUI = createModularUI();
        modularUI.setScreenAndInit(this);
        this.addRenderableWidget(modularUI.getWidget());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // Define the UI
    private static ModularUI createModularUI() {
        var root = new UIElement().layout(layoutStyle -> layoutStyle
                .paddingAll(1)
                .gapAll(7)
        );
        var body = new UIElement().layout(layoutStyle -> layoutStyle
                .flexDirection(FlexDirection.ROW)
                .display(TaffyDisplay.FLEX)
        );
        var leftPanel = new UIElement().layout(layoutStyle -> layoutStyle
                .paddingAll(2)
                .gapAll(7)
                .minWidth(120)
        );
        var rightPanel = new UIElement().layout(layoutStyle -> layoutStyle
                .paddingAll(2)
                .gapAll(7)
                .minWidth(120)
        );

        var startingKits = new ToggleGroupElement().layout(layoutStyle -> layoutStyle
                .flexDirection(FlexDirection.COLUMN)
        );
        startingKits.addChild(new Toggle().setText(Component.translatable("screen.hungernite.on")));
        startingKits.addChild(new Toggle().setText(Component.translatable("screen.hungernite.off")).setOn(true));
        startingKits.addChild(new Toggle().setText(Component.translatable("screen.hungernite.random")));

        var mapTypeLabel = new Label()
                .setText(GenerationOptions.DEFAULT.getDescKey().getString())
                .textStyle(textStyle -> textStyle
                        .textWrap(TextWrap.WRAP)
                        .adaptiveHeight(true)
                        .textColor(ChatFormatting.BLUE.getColor())
                        .textShadow(false)
                        .fontSize(7)
                );

        var mapTypeSelector = new Selector<GenerationOptions>();
        mapTypeSelector.layout(layoutStyle -> layoutStyle
                .width(90)
                .minHeight(15)
        );
        mapTypeSelector.setCandidates(List.of(GenerationOptions.values()));
        mapTypeSelector.setSelected(GenerationOptions.DEFAULT);
        mapTypeSelector.setOnValueChanged(value -> {
            mapTypeLabel.setText(value.getDescKey());
        });

        leftPanel.addChildren(
                // Generation settings
                new UIElement().addChildren(
                        new Label().setText(Component.translatable("screen.hungernite.map_type"))
                                .textStyle(textStyle -> textStyle.adaptiveWidth(true).textColor(ChatFormatting.GOLD.getColor()))
                                .addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                                    event.hoverTooltips = HoverTooltips.empty()
                                            .append(Component.translatable("screen.hungernite.map_type.desc"));
                                }).layout(layoutStyle -> {
                                    layoutStyle.flexShrink(1);
                                }),
                        mapTypeSelector,
                        mapTypeLabel.layout(layoutStyle -> layoutStyle.paddingAll(2))
                ).layout(layoutStyle -> layoutStyle
                        .gapAll(1)
                ),

                // Starting kits
                new UIElement().addChildren(
                        new Label().setText(Component.translatable("screen.hungernite.starting_kits"))
                                .textStyle(textStyle -> textStyle.adaptiveWidth(true).textColor(ChatFormatting.GOLD.getColor()))
                                .addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                                    event.hoverTooltips = HoverTooltips.empty()
                                            .append(Component.translatable("screen.hungernite.starting_kits.desc"));
                                }).layout(layoutStyle -> layoutStyle
                                        .gapAll(2)),
                        startingKits
                ).layout(layoutStyle -> layoutStyle
                        .gapAll(1)
                )
        );

        var gameModeToggles = new UIElement();

        for (GameModes options : GameModes.values()) {
            var toAdd = new Toggle().setText(options.getName());
            toAdd.toggleLabel.textStyle(textStyle -> textStyle.adaptiveWidth(true).textColor(ChatFormatting.DARK_PURPLE.getColor()).textShadow(false));
            toAdd.toggleLabel.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                event.hoverTooltips = HoverTooltips.empty()
                        .append(options.getDescKey());
            });
            gameModeToggles.addChild(toAdd);
        }

        rightPanel.addChildren(
                // Game modes
                new UIElement().addChildren(
                        new Label().setText(Component.translatable("screen.hungernite.game_modes"))
                                .textStyle(textStyle -> textStyle.adaptiveWidth(true).textColor(ChatFormatting.GOLD.getColor()))
                                .addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                                    event.hoverTooltips = HoverTooltips.empty()
                                            .append(Component.translatable("screen.hungernite.game_modes.desc"));
                                }),
                        gameModeToggles.layout(layoutStyle -> layoutStyle.paddingAll(2))
                ).layout(layoutStyle -> layoutStyle
                        .gapAll(1)
                )
        );

        body.addChildren(
                leftPanel,
                rightPanel
        );
        body.addClass("panel_bg");

        root.addChildren(
                new Label().setText(Component.translatable("screen.hungernite.setup_title"))
                        .textStyle(textStyle -> textStyle.textAlignHorizontal(Horizontal.CENTER)),
                body
        );

        return ModularUI.of(UI.of(root, StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.MC)));
    }
}
