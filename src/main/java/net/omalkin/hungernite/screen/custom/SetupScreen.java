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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.omalkin.hungernite.gamemechanics.GameModes;
import net.omalkin.hungernite.gamemechanics.GenerationOptions;
import net.omalkin.hungernite.gamemechanics.StartingKits;
import net.omalkin.hungernite.network.packets.UpdateGameModePacket;
import net.omalkin.hungernite.network.packets.UpdateMapTypePacket;
import net.omalkin.hungernite.network.packets.UpdateStartingKitPacket;
import net.omalkin.hungernite.util.EnumTranscoder;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class SetupScreen extends Screen {
    private final String lobbyId;
    private final EnumSet<GameModes> gameModes;
    private final GenerationOptions mapType;
    private final StartingKits startingKits;
    private final Set<Toggle> enabledOptions = new HashSet<>();

    public SetupScreen(String lobbyId, int gameModes, String mapType, String startingKits) {
        super(Component.translatable("screen.hungernite.setup_title", lobbyId));
        this.lobbyId = lobbyId;
        this.gameModes = EnumTranscoder.decode(gameModes, GameModes.class);
        this.mapType = GenerationOptions.valueOf(mapType);
        this.startingKits = StartingKits.valueOf(startingKits);
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
    private ModularUI createModularUI() {
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

        // ---------------- //
        // Left panel stuff //
        // ---------------- //

        // Generation type selector
        var mapTypeLabel = new Label()
                .setText(this.mapType.getDescKey().getString())
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
        mapTypeSelector.setSelected(mapType);
        mapTypeSelector.setOnValueChanged(value -> {
            mapTypeLabel.setText(value.getDescKey());
            PacketDistributor.sendToServer(new UpdateMapTypePacket(value.name()));
        });

        // Starting kit toggles
        var startingKitsGroup = new ToggleGroupElement().layout(layoutStyle -> layoutStyle
                .flexDirection(FlexDirection.COLUMN)
        );
        for(StartingKits kits : StartingKits.values()){
            var kit = new Toggle().setText(kits.toString());
            kit.toggleLabel.textStyle(textStyle -> textStyle
                    .textColor(ChatFormatting.DARK_GRAY.getColor()).textShadow(false));
            kit.setOn(kits.name().equals(this.startingKits.name()));
            kit.setOnToggleChanged(isOn -> {
                if(isOn){
                    PacketDistributor.sendToServer(new UpdateStartingKitPacket(kits.name()));
                }
            });
            startingKitsGroup.addChild(kit);
        }

        leftPanel.addChildren(
                // Generation settings
                new UIElement().addChildren(
                        new Label().setText(Component.translatable("screen.hungernite.map_type"))
                                .textStyle(textStyle -> textStyle
                                        .adaptiveWidth(true)
                                        .textColor(ChatFormatting.DARK_PURPLE.getColor())
                                        .textShadow(false))
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
                                .textStyle(textStyle -> textStyle
                                        .adaptiveWidth(true)
                                        .textColor(ChatFormatting.DARK_PURPLE.getColor())
                                        .textShadow(false))
                                .addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                                    event.hoverTooltips = HoverTooltips.empty()
                                            .append(Component.translatable("screen.hungernite.starting_kits.desc"));
                                }).layout(layoutStyle -> layoutStyle
                                        .gapAll(2)),
                        startingKitsGroup
                ).layout(layoutStyle -> layoutStyle
                        .gapAll(1)
                )
        );

        // ----------------- //
        // Right panel stuff //
        // ----------------- //

        var gameModeToggles = new UIElement();

        for (GameModes options : GameModes.values()) {
            var toAdd = new Toggle().setText(options.getName());
            toAdd.setOn(gameModes.contains(options));
            toAdd.toggleLabel.textStyle(textStyle -> textStyle
                    .adaptiveWidth(true)
                    .textColor(ChatFormatting.DARK_GRAY.getColor())
                    .textShadow(false)
            );
            toAdd.toggleLabel.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                event.hoverTooltips = HoverTooltips.empty()
                        .append(options.getDescKey());
            });
            toAdd.setOnToggleChanged(isOn -> {
                PacketDistributor.sendToServer(new UpdateGameModePacket(options.name(), isOn));
            });
            this.enabledOptions.add(toAdd);
            gameModeToggles.addChild(toAdd);
        }

        var gameModeButtons = new UIElement().layout(layoutStyle -> layoutStyle.flexDirection(FlexDirection.ROW).paddingAll(3).gapAll(2));
        gameModeButtons.addChildren(
                new Button().setText(Component.translatable("screen.hungernite.on"))
                        .textStyle(textStyle -> textStyle.fontSize(8))
                        .setOnClick(e -> setGameModes(1)),
                new Button().setText(Component.translatable("screen.hungernite.off"))
                        .textStyle(textStyle -> textStyle.fontSize(8))
                        .setOnClick(e -> setGameModes(2)),
                new Button().setText(Component.translatable("screen.hungernite.randomise"))
                        .textStyle(textStyle -> textStyle.fontSize(8))
                        .setOnClick(e -> setGameModes(3))
        );

        rightPanel.addChildren(
                // Game modes
                new UIElement().addChildren(
                        new Label().setText(Component.translatable("screen.hungernite.game_modes"))
                                .textStyle(textStyle -> textStyle
                                        .adaptiveWidth(true)
                                        .textColor(ChatFormatting.DARK_PURPLE.getColor())
                                        .textShadow(false))
                                .addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                                    event.hoverTooltips = HoverTooltips.empty()
                                            .append(Component.translatable("screen.hungernite.game_modes.desc"));
                                }),
                        gameModeToggles.layout(layoutStyle -> layoutStyle.paddingAll(2)),
                        gameModeButtons
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
                new Label().setText(Component.translatable("screen.hungernite.setup_title", this.lobbyId))
                        .textStyle(textStyle -> textStyle.textAlignHorizontal(Horizontal.CENTER)),
                body
        );

        return ModularUI.of(UI.of(root, StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.MC)));
    }

    private void setGameModes(int mode) {
        Random random = new Random();
        for (Toggle option : enabledOptions) {
            switch (mode) {
                case 1:
                    option.setOn(true);
                    break;
                case 2:
                    option.setOn(false);
                    break;
                case 3:
                    option.setOn(random.nextBoolean());
                    break;
                default:
                    return;
            }
        }
    }
}
