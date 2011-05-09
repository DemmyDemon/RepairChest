package com.webkonsept.bukkit.repairchest;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;

public class RepairChestPlayerListener extends PlayerListener {
	private RepairChest plugin;
	
	RepairChestPlayerListener (RepairChest instance) {
		plugin = instance;
	}
	public void onPlayerCommandPreprocess (PlayerCommandPreprocessEvent event){
		if (! plugin.isEnabled()) return;
		if (event.getMessage().equalsIgnoreCase("/repairchesttest")){
			event.setCancelled(true);
			if (plugin.permit(event.getPlayer(), "repairchest.testing")){
				ItemStack inHand = event.getPlayer().getItemInHand();
				if(inHand.getMaxStackSize() == 1 && inHand.getType().getMaxDurability() > 100){
					inHand.setDurability((short)100);
				}
			}
			else {
				event.getPlayer().sendMessage(ChatColor.RED+"Sorry, you can't do that.");
			}
		}
	}
	
	public void onPlayerInteract (PlayerInteractEvent event){
		if (! plugin.isEnabled()) return;
		
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
			if (block != null){
			Material blockType = block.getType();
			if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
				if (blockType.equals(Material.WALL_SIGN)){
					Sign sign = (Sign) block.getState();
					if (sign.getLine(0).equalsIgnoreCase("[Repair]")){
						if (plugin.permit(player, "repairchest.use")){
							Block below = block.getFace(BlockFace.DOWN);
							if (below.getType().equals(Material.CHEST)){
								Chest chest = (Chest) below.getState();
								ItemStack[] inventory = chest.getInventory().getContents();
								if (calculateDamage(inventory) > 0){
									ItemStack inHand = player.getItemInHand();
									Material inHandType = inHand.getType();
									if (inHandType.equals(Material.getMaterial(plugin.currency))){
										int cost = calculateCost(inventory);
										if (cost < 1){
											cost = 1;
										}
										int goldPile = inHand.getAmount();
										int afterRepair = goldPile - cost;
										boolean charge = false;
										int repairCredits = (int)(goldPile / plugin.baseCost);
										
										if (afterRepair < 0 && ! plugin.partialRepair){
											player.sendMessage(ChatColor.DARK_RED+"You can't afford this repair!");
											return;
										}
										else if (afterRepair < 0 && plugin.partialRepair){
											afterRepair = 0;	
											if (plugin.distributePartialRepair){
												int itemsInChest = numberOfRepairableItems(inventory);
												int repairPerItem = repairCredits / itemsInChest;
												plugin.babble("Repair per item is "+repairPerItem+" for "+itemsInChest+" items, "+repairCredits+" credits.");
												for (int i = 0; i < inventory.length; i++){
													if (inventory[i] != null && inventory[i].getType().getMaxStackSize() == 1){
														if (inventory[i].getDurability() > 0){
															inventory[i].setDurability((short) (inventory[i].getDurability() - repairPerItem));
															charge = true;
												
														}
													}
												}
											}
											else {
												for (int i = 0; i < inventory.length;i++){
													if (inventory[i] != null && inventory[i].getType().getMaxStackSize() == 1){
														int damage = inventory[i].getDurability();
														if (damage <= repairCredits){
															repairCredits -= damage;
															charge = true;
															inventory[i].setDurability((short) 0);
														}
														else {
															inventory[i].setDurability((short) (inventory[i].getDurability() - repairCredits));
															charge = true;
															break;
														}
													}
												}
											}
										}
										else { // That is, partial repair is OFF, and the player can afford a full one
											for (int i = 0; i < inventory.length; i++){
												if (inventory[i] != null && inventory[i].getType().getMaxStackSize() == 1){
													if (inventory[i].getDurability() > 0){
														inventory[i].setDurability((short)0);
														charge = true;
													}
												}
											}
										}
										if (charge){
											plugin.babble("Charging for the repair...");
											player.sendMessage(ChatColor.GREEN+"Ding!  Repair complete!");
											if (afterRepair <= 0){
												plugin.babble("Poor bastard is now broke");
												player.setItemInHand(null);
											}
											else {
												plugin.babble("rich-- = "+afterRepair);
												player.setItemInHand(new ItemStack(Material.GOLD_INGOT,afterRepair));
											}
										}
										else {
											plugin.babble("Didn't fix anything.");
											player.setItemInHand(new ItemStack(Material.GOLD_INGOT,goldPile));
										}
									}
									else {
										player.sendMessage(ChatColor.DARK_RED+"Right-click the sign with a gold ingot in your hand to pay.");
									}
								}
								else {
									player.sendMessage(ChatColor.DARK_RED+"Nothing to repair!");
								}
							}
							else {
								player.sendMessage(ChatColor.DARK_RED+"Uh, there is no chest.");
							}
						}
						else {
							player.sendMessage(ChatColor.DARK_RED+"Sorry, you don't have permission to use repair chests.");
						}
					}
				}

			}
			else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)){
				if (blockType.equals(Material.WALL_SIGN)){
					Sign sign = (Sign) block.getState();
					if (sign.getLine(0).equalsIgnoreCase("[Repair]")){
						if (plugin.permit(player, "repairchest.use")){
							Block below = block.getFace(BlockFace.DOWN);
							if (below.getType().equals(Material.CHEST)){
								Chest chest = (Chest) below.getState();
								ItemStack[] inventory = chest.getInventory().getContents();
								int cost = this.calculateCost(inventory);
								int damage = this.calculateDamage(inventory);
								player.sendMessage(damage+" point"+plugin.plural(damage)+" of damage costs "+cost+plugin.currencyName+" to repair.");
							}
						}
						else {
							player.sendMessage(ChatColor.DARK_RED+"Sorry, you don't have permission to use repair chests.");
						}
					}
				}
			}
		}
	}
	private int numberOfRepairableItems(ItemStack[] inventory) {
		int repairable = 0;
		for (int i = 0; i < inventory.length; i++){
			if (inventory[i] != null && inventory[i].getType().getMaxStackSize() == 1){
				if (inventory[i].getDurability() > 0){
					repairable++;
				}
			}
		}
		return repairable;
	}

	private int calculateDamage(ItemStack[] inventory){
		int damage = 0;
		for (int i = 0; i < inventory.length; i++){
			if (inventory[i] != null && inventory[i].getType().getMaxStackSize() == 1){
				damage += inventory[i].getDurability();
			}
		}
		return damage;
		
	}
	private int calculateCost(ItemStack[] inventory) {
		int damage = calculateDamage(inventory);
		if (damage > 0){
			int price = (int) (damage * plugin.baseCost);
			if (price < 1){
				price = 1;
			}
			return price;
		}
		else {
			return 0;
		}
	}
}
