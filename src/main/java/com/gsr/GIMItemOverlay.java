package com.gsr;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import javax.inject.Inject;
import net.runelite.client.util.ImageUtil;
import net.runelite.api.Point;

public class GIMItemOverlay extends WidgetItemOverlay
{
    private final ItemManager itemManager;
    private final GSRPlugin plugin;
    private final GSRConfig config;
    private final BufferedImage gimIcon;

    @Inject
    GIMItemOverlay(ItemManager itemManager, GSRPlugin plugin, GSRConfig config)
    {
        this.itemManager = itemManager;
        this.plugin = plugin;
        this.config = config;
        showOnInventory();
        showOnBank();
        
        // Load and scale the GIM icon
        BufferedImage originalIcon = ImageUtil.loadImageResource(getClass(), "/gim_icon.png");
        gimIcon = ImageUtil.resizeImage(originalIcon, 12, 12);
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem itemWidget)
    {
        if (!config.showOverlay() || !plugin.isGIMItem(itemId))
        {
            return;
        }

        Point location = itemWidget.getCanvasLocation();
        graphics.drawImage(
            gimIcon,
            location.getX() + itemWidget.getCanvasBounds().width - gimIcon.getWidth(),
            location.getY() + itemWidget.getCanvasBounds().height - gimIcon.getHeight(),
            null
        );
    }
}