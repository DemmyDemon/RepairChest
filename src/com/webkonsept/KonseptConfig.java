package com.webkonsept;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class KonseptConfig {
    private final String version = "v0.1";
    private Plugin plugin;
    private HashMap<String,String> strings = new HashMap<String,String>();
    private Logger log = Logger.getLogger("Minecraft");
    private boolean verbose = false;
    private File cfgFile;
    private YamlConfiguration config;
    
    public KonseptConfig(Plugin plugin){
        this.plugin = plugin;
        log = plugin.getLogger();
        
    }
    
    public void refresh(){
        cfgFile = new File(plugin.getDataFolder(),"config.yml");
        config = YamlConfiguration.loadConfiguration(cfgFile);
        if (!cfgFile.exists()){
            config.options().copyDefaults(true);
            try {
                config.save(cfgFile);
            } catch (IOException e) {
                log.severe("Failed to save configuration to "+cfgFile+": "+e.getMessage());
            }
        }
        verbose = plugin.getConfig().getBoolean("verbose",false);
        verbose("Loading Configuration using KonseptConfig "+version);
        
        Map<String,Object> cfgStrings = config.getConfigurationSection("strings").getValues(false);
        if (cfgStrings != null){
            verbose(cfgStrings.size()+" strings found");
            for (String item : cfgStrings.keySet()){
                Object rawValue = cfgStrings.get(item);
                if (rawValue instanceof String){
                    String itemValue = (String)rawValue;
                    verbose(item+"="+itemValue);
                    strings.put(item,itemValue);
                }
                else {
                    verbose("strings in configuration contained a "+rawValue.getClass().toString());
                }
            }
        }
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
