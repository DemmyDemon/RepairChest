package com.webkonsept.bukkit.repairchest.storage;

import java.text.ParseException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class RepairChest {
	
	private static final String sep = ",";  
	private Location location;
	private String ownerName;
	private Integer repairsDone;
	private Integer currencySaved;
	private BlockFace signDirection;
	
	private int expectedFieldsInChestString = 8;
	
	RepairChest (String string) throws ParseException {
		String[] elements = string.split(sep);
		if (elements.length != expectedFieldsInChestString){
			throw new ParseException ("Incorrect number of fields in RC string: "+string, 0);
		}
		
		World world = Bukkit.getServer().getWorld(elements[0]);
		if (world == null) throw new ParseException ("World "+elements[0]+" does not exist, from RC string: "+string,0);
		
		try {
			double locX = Double.parseDouble(elements[1]);
			double locY = Double.parseDouble(elements[2]);
			double locZ = Double.parseDouble(elements[3]);
			location = new Location(world,locX,locY,locZ);
			ownerName = elements[4];
			repairsDone = Integer.parseInt(elements[5]);
			currencySaved = Integer.parseInt(elements[6]);
			signDirection = BlockFace.valueOf(elements[7]);
		}
		catch (NumberFormatException e){
			throw new ParseException ("Malformed numbers in RC string: "+string,0);
		}
		if (signDirection == null){
			throw new ParseException ("Invalid Sign Direction '"+elements[7]+"' in RC string!",0);
		}
	}
	RepairChest (Location place, String owner, Integer repairs,Integer currency,BlockFace sign){
		location = place;
		ownerName = owner;
		repairsDone = repairs;
		currencySaved = currency;
		signDirection = sign;
		
	}
	public String toString(){
		
		String worldName = location.getWorld().getName();
		Integer x = (int) location.getX();
		Integer y = (int) location.getY();
		Integer z = (int) location.getX();
		String sign = signDirection.toString();
		
		return worldName+sep+x+sep+y+sep+z+sep+ownerName+sep+repairsDone+sep+currencySaved+sep+sign;
	}
	
	public int addRepair(){
		return ++repairsDone;
	}
	public int addRepair(int repairs){
		repairsDone += repairs;
		return repairsDone;
	}
	public int getRepairs(){
		return repairsDone;
	}
	public String getOwnerName(){
		return ownerName;
	}
	public Player getOwnerPlayer(){
		return Bukkit.getServer().getPlayer(ownerName);
	}
	public void setOwner(Player player){
		if (player == null) return;
		ownerName = player.getName();
	}
	public Location getLocation(){
		return location;
	}
	public void setLocation(Location loc){
		location = loc;
	}
	public BlockFace getSignDirection(){
		return signDirection;
	}
	public void setSignDirection(BlockFace newDirection){
		signDirection = newDirection;
	}
}