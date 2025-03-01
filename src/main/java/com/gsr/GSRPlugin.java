package com.gsr;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import java.util.HashSet;
import java.util.Set;
import net.runelite.api.KeyCode;
import java.util.stream.Collectors;
import net.runelite.client.events.ConfigChanged;
import net.runelite.api.ItemContainer;
import net.runelite.api.Item;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemID;
import java.awt.image.BufferedImage;
import net.runelite.api.events.ItemContainerChanged;

@Slf4j
@PluginDescriptor(
    name = "GIM Storage Reminders",
    description = "Reminds you to return Group Iron Man items to storage",
    tags = {"gim", "group", "iron", "storage"}
)
public class GSRPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private GSRConfig config;

    @Inject
    private ItemManager itemManager;

    @Inject
    private InfoBoxManager infoBoxManager;

    @Inject
    private OverlayManager overlayManager;

    private GIMItemOverlay itemOverlay;
    private Set<String> taggedItems;
    private GIMItemInfoBox infoBox;

    @Override
    protected void startUp()
    {
        taggedItems = loadTaggedItems();
        itemOverlay = new GIMItemOverlay(itemManager, this, config);
        overlayManager.add(itemOverlay);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(itemOverlay);
        infoBoxManager.removeIf(GIMItemInfoBox.class::isInstance);
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (!client.isKeyPressed(KeyCode.KC_SHIFT))
        {
            return;
        }

        // Only add menu entry for inventory items and only for the first menu option
        if (event.getType() != MenuAction.CC_OP.getId() || 
            event.getActionParam1() != 9764864 || 
            !event.getOption().equals("Drop"))  // Only add our option on the "Drop" menu entry
        {
            return;
        }

        final int itemId = event.getItemId();
        final String target = event.getTarget();
        final String option = taggedItems.contains(itemId) ? "Untag GIM item" : "Tag as GIM item";
        
        client.createMenuEntry(-1)
            .setOption(option)
            .setTarget(target)
            .setIdentifier(itemId)
            .setType(MenuAction.RUNELITE)
            .setParam0(event.getActionParam0())
            .setParam1(event.getActionParam1())
            .onClick(e -> toggleTag(itemId));
    }

    private void toggleTag(int itemId)
    {
        String itemName = itemManager.getItemComposition(itemId).getName();
        if (taggedItems.contains(itemName))
        {
            taggedItems.remove(itemName);
            client.addChatMessage(
                net.runelite.api.ChatMessageType.GAMEMESSAGE,
                "",
                "Untagged GIM item: " + itemName,
                null
            );
        }
        else
        {
            taggedItems.add(itemName);
            client.addChatMessage(
                net.runelite.api.ChatMessageType.GAMEMESSAGE,
                "",
                "Tagged GIM item: " + itemName,
                null
            );
        }
        config.setTaggedItems(String.join(",", taggedItems));

        // Check containers after toggling tag
        boolean hasGIMItems = false;
        
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory != null)
        {
            hasGIMItems |= checkContainerForGIMItems(inventory);
        }

        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment != null)
        {
            hasGIMItems |= checkContainerForGIMItems(equipment);
        }

        ItemContainer bank = client.getItemContainer(InventoryID.BANK);
        if (bank != null)
        {
            hasGIMItems |= checkContainerForGIMItems(bank);
        }

        // Update info box
        if (hasGIMItems)
        {
            if (infoBox == null)
            {
                final BufferedImage image = itemManager.getImage(ItemID.GROUP_IRONMAN_HELM);
                infoBox = new GIMItemInfoBox(image, this);
                infoBoxManager.addInfoBox(infoBox);
            }
        }
        else if (infoBox != null)
        {
            infoBoxManager.removeInfoBox(infoBox);
            infoBox = null;
        }
    }

    public boolean isGIMItem(int itemId)
    {
        String itemName = itemManager.getItemComposition(itemId).getName();
        return taggedItems.contains(itemName);
    }

    private Set<String> loadTaggedItems()
    {
        Set<String> items = new HashSet<>();
        String itemList = config.taggedItems();
        if (itemList != null && !itemList.isEmpty())
        {
            for (String itemName : itemList.split(","))
            {
                items.add(itemName.trim());
            }
        }
        return items;
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (event.getGroup().equals("gimstorage") && event.getKey().equals("taggedItems"))
        {
            taggedItems = loadTaggedItems();
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        // Check if container is one we care about
        if (event.getContainerId() != InventoryID.INVENTORY.getId() &&
            event.getContainerId() != InventoryID.EQUIPMENT.getId() &&
            event.getContainerId() != InventoryID.BANK.getId())
        {
            return;
        }

        boolean hasGIMItems = false;
        ItemContainer container = event.getItemContainer();

        if (container == null)
        {
            return;
        }

        // Check inventory
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory != null)
        {
            hasGIMItems |= checkContainerForGIMItems(inventory);
        }

        // Check equipment
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment != null)
        {
            hasGIMItems |= checkContainerForGIMItems(equipment);
        }

        // Check bank
        ItemContainer bank = client.getItemContainer(InventoryID.BANK);
        if (bank != null)
        {
            hasGIMItems |= checkContainerForGIMItems(bank);
        }

        // Manage the info box based on GIM item presence
        if (hasGIMItems)
        {
            if (infoBox == null)
            {
                final BufferedImage image = itemManager.getImage(ItemID.GROUP_IRONMAN_HELM);
                infoBox = new GIMItemInfoBox(image, this);
                infoBoxManager.addInfoBox(infoBox);
            }
        }
        else if (infoBox != null)
        {
            infoBoxManager.removeInfoBox(infoBox);
            infoBox = null;
        }
    }

    private boolean checkContainerForGIMItems(ItemContainer container)
    {
        for (Item item : container.getItems())
        {
            if (item == null)
            {
                continue;
            }

            if (isGIMItem(item.getId()))
            {
                return true;
            }
        }
        return false;
    }

    @Provides
    GSRConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(GSRConfig.class);
    }
}