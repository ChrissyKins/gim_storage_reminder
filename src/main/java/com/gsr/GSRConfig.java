package com.gsr;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("gimstorage")
public interface GSRConfig extends Config
{
	@ConfigItem(
		keyName = "showOverlay",
		name = "Show Overlay",
		description = "Show an overlay on GIM items"
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "taggedItems",
		name = "Tagged Items",
		description = "CSV list of item IDs that are tagged as GIM items"
	)
	default String taggedItems()
	{
		return "";
	}

	@ConfigItem(
		keyName = "taggedItems",
		name = "Tagged Items",
		description = "CSV list of item IDs that are tagged as GIM items"
	)
	void setTaggedItems(String items);
}
