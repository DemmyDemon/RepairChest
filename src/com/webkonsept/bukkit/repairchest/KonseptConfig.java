package com.webkonsept.bukkit.repairchest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class KonseptConfig {
    private final String version = "v0.1.2";
    private Plugin plugin;
    private HashMap<String,String> strings = new HashMap<String,String>();
    private Logger log = Logger.getLogger("Minecraft");
    private boolean verbose = false;
    private File cfgFile;
    private FileConfiguration config;
    
    public KonseptConfig(Plugin plugin){
        this.plugin = plugin;
        log = plugin.getLogger();
        
    }
    
    public int refresh(){
        int stringNumber = 0;
        plugin.reloadConfig();
        cfgFile = new File(plugin.getDataFolder(),"config.yml");
        config = plugin.getConfig();
        
        if (!cfgFile.exists()){
            config.options().copyDefaults(true);
            plugin.saveConfig();
        }
        verbose = plugin.getConfig().getBoolean("verbose",false);
        verbose("Loading Configuration using KonseptConfig "+version);
        ConfigurationSection stringSection = config.getConfigurationSection("strings");
        if (stringSection != null){
            Map<String,Object> cfgStrings = stringSection.getValues(false);
            if (cfgStrings != null){
                verbose(cfgStrings.size()+" strings found");
                for (String item : cfgStrings.keySet()){
                    Object rawValue = cfgStrings.get(item);
                    if (rawValue instanceof String){
                        String itemValue = (String)rawValue;
                        verbose(item+"="+itemValue);
                        strings.put(item,itemValue);
                        stringNumber++;
                    }
                    else {
                        verbose("strings in configuration contained a "+rawValue.getClass().toString());
                    }
                }
            }
            else {
                verbose("String section holds no values!");
            }
        }
        else {
            verbose("No strings section found!");
        }
        return stringNumber;
    }
    public String tr(String name){ // TRanslate, yeah?
        if (strings.containsKey(name)){
            return strings.get(name);
        }
        else {
            return "[Check config:  String '"+name+"' is missing]";
        }
    }
    public FileConfiguration get(){ // Just a passthroug for now, but this will come in handy, I hope.
        return config;
    }
    private void verbose(String message){
        if (verbose){
            log.info("[KonseptConfig "+version+"] "+message);
        }
    }
}
