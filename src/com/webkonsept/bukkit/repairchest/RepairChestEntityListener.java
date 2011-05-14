package com.webkonsept.bukkit.repairchest;

import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

public class RepairChestEntityListener extends EntityListener {
	private RepairChest plugin;
	

	RepairChestEntityListener (RepairChest instance) {
		plugin = instance;
	}
	
	@Override
	public void onEntityExplode (EntityExplodeEvent event){
		if (!plugin.isEnabled()) return;
		if (event.isCancelled()) return;
		List<Block> blocks = event.blockList();
		Iterator<Block> iterator = blocks.iterator();
		boolean cancelEvent = false;
		while (iterator.hasNext()){
			Block block = iterator.next();
			if (plugin.blockListener.relevantSign(block)){ // Have no Player to offer here...
				cancelEvent = true;
				plugin.getServer().broadcastMessage(ChatColor.DARK_AQUA+"Everyone, please refrain from blowing up repair chests.  It's not nice.");
				break;
			}
		}
		event.setCancelled(cancelEvent);
	}
}
