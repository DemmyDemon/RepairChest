package com.webkonsept.bukkit.repairchest.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.webkonsept.bukkit.repairchest.RepairChestPlugin;

public class RepairChestList {
	protected RepairChestPlugin plugin;
	protected HashMap<Location,RepairChest> chestLocation 	= new HashMap<Location,RepairChest>();
	protected ArrayList<RepairChest> chests 				= new ArrayList<RepairChest>();
	protected File source;
	
	protected byte[] signTranslate = new byte[13];
	protected BlockFace[] chestSide = new BlockFace[13];
	protected HashMap<BlockFace,BlockFace> signSide = new HashMap<BlockFace,BlockFace>();
	
	public RepairChestList (File chestSource, RepairChestPlugin instance){
		plugin = instance;
		source = chestSource;
		
		signTranslate[0] 	= 3;
		signTranslate[4] 	= 4;
		signTranslate[8] 	= 2;
		signTranslate[12] 	= 5;
		
		// As SIGN_POST
		chestSide[0] 	= BlockFace.EAST;
		chestSide[4] 	= BlockFace.SOUTH;
		chestSide[8] 	= BlockFace.WEST;
		chestSide[12]	= BlockFace.NORTH;
		
		// As WALL_SIGN
		chestSide[2]	= BlockFace.WEST;
		chestSide[3]	= BlockFace.EAST;
		// 4 is SOUTH, but that's the same as for SIGN_POST
		chestSide[5]	= BlockFace.NORTH;
		
		// Translating "Chest is to the NORTH of the sign" into "Sign is to the SOUTH of the chest"
		signSide.put(BlockFace.NORTH,BlockFace.SOUTH);
		signSide.put(BlockFace.SOUTH,BlockFace.NORTH);
		signSide.put(BlockFace.EAST,BlockFace.WEST);
		signSide.put(BlockFace.WEST,BlockFace.EAST);
		
	}
	
	// PUBLIC METHODS
	
	public byte signTranslate(byte b){
		if (b < signTranslate.length){
			return signTranslate[b];
		}
		else {
			return 0;
		}
	}
	
	public BlockFace findChest(byte b){
		if (b < chestSide.length){
			return chestSide[b];
		}
		else {
			return null;
		}
	}
	
	public void add(Block chest,BlockFace signFace){
		//FIXME Stub method!
	}
	
	
	public void load(){
		if (source.exists()){
			try {
				BufferedReader in = new BufferedReader(new FileReader(source));
				String line = "";
				int chestCount = 0;
				while (line != null){
					line = in.readLine();
					RepairChest thisChest = new RepairChest(line);
					chests.add(thisChest);
					chestLocation.put(thisChest.getLocation(),thisChest);
					plugin.verbose(thisChest.getOwnerName() + "' RepairChest loaded!");
					chestCount++;
				}
				in.close();
				plugin.verbose(chestCount + " repair chests!");
			}
			catch (FileNotFoundException e) {
				// Stupid Java... We'll NEVER GET HERE, unless the file stops existing in under a nanosecond!
				e.printStackTrace();
				plugin.error("How the hell did THAT happen?!");
			}
			catch (IOException e) {
				e.printStackTrace();
				plugin.error("IOException reading chest file " + source.getAbsolutePath());
				if (!source.canRead()){
					plugin.error("CAN'T READ FROM " + source.getAbsolutePath());
				}
			} catch (ParseException e) {
				e.printStackTrace();
				plugin.error("Parse Exception while reading chest file: " + e.getMessage());
			}
		}
		else {
			plugin.out("RepairChestFile "+source.getAbsolutePath()+" does not exist:  Creating new!");
			try {
				source.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				plugin.error("IOException creating chest file " + source.getAbsolutePath());
				if (!source.canWrite()){
					plugin.error("CAN'T WRITE TO " + source.getAbsolutePath());
				}
			}
		}
	}
	public void save(){
		plugin.verbose("Saving chests");
		try {
			if (!source.exists()){
				source.createNewFile();
			}
			BufferedWriter out = new BufferedWriter(new FileWriter(source));
			for (RepairChest chest : chests){
				out.write(chest.toString());
				out.newLine();
			}
			out.close();
		}
		catch (IOException e){
			e.printStackTrace();
			plugin.error("IOException writing chest file " + source.getAbsolutePath());
			if (!source.canWrite()){
				plugin.error("CAN'T WRITE TO " + source.getAbsolutePath());
			}
		}
		plugin.verbose("Saved!");
	}
	public void save(File alternativeChestFile){
		source = alternativeChestFile;
		save();
	}
	public boolean contains(){
		return true;
	}
	public ArrayList<RepairChest> get(Player player){
		ArrayList<RepairChest> returnChests = new ArrayList<RepairChest>();
		for (RepairChest chest : chests){
			if (chest.getOwnerPlayer().equals(player)){
				returnChests.add(chest);
			}
		}
		return returnChests;
	}
	public ArrayList<RepairChest> get(String playerName){
		ArrayList<RepairChest> returnChests = new ArrayList<RepairChest>();
		for (RepairChest chest : chests){
			if (chest.getOwnerName().equals(playerName)){
				returnChests.add(chest);
			}
		}
		return returnChests;
	}
	
	public RepairChest get(Location location){
		if (chestLocation.containsKey(location)){
			return chestLocation.get(location);
		}
		else {
			return null;
		}
	}
}	