package com.gsr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.plugins.Plugin;

public class GIMItemInfoBox extends InfoBox
{
    public GIMItemInfoBox(BufferedImage image, Plugin plugin)
    {
        super(image, plugin);
        setTooltip("You have GIM items that need to be stored!");
    }

    @Override
    public String getText()
    {
        return "GIM";
    }

    @Override
    public Color getTextColor()
    {
        return Color.RED;
    }
} 