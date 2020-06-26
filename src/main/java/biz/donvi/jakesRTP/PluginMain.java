package biz.donvi.jakesRTP;

import biz.donvi.argsChecker.Util;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PluginMain extends JavaPlugin {

    public static PluginMain plugin;
    static Logger logger;
    static Map<String, Object> cmdMap;

    private String defaultConfigVersion = null;

    @Override
    public void onEnable() {
        //Set up the reference for some objects
        plugin = this;
        logger = plugin.getLogger();
        cmdMap = new Yaml().load(this.getClassLoader().getResourceAsStream("commandTree.yaml"));


        try {
            //If there is no config file, save the default one
            if (!Files.exists(Paths.get(this.getDataFolder().getPath(), "config.yml")))
                saveDefaultConfig();
            else if (!getCurrentConfigVersion().equals(getDefaultConfigVersion())
                     && !getConfig().getBoolean("run-old-configs")) {
                logger.log(Level.WARNING, "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
                logger.log(Level.WARNING, "A new plugin-level config file is available.");
                logger.log(Level.WARNING, "Automatically backing up the old config file, and using the new default one.");
                logger.log(Level.WARNING, "You may want to copy any values from the old config to the new one if you customized it at all.");
                logger.log(Level.WARNING, "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
                Files.move(
                        Paths.get(getDataFolder().getPath() + "\\config.yml"),
                        Paths.get(getDataFolder().getPath() + "\\config-" + getCurrentConfigVersion() + "-old.yml")
                );
                saveDefaultConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //Register commands
        getCommand("rtp-admin").setExecutor(new CmdRtpAdmin(Util.getImpliedMap(cmdMap,"rtp-admin")));
        loadRandomTeleporter(); //DON'T REMOVE THIS LINE, THE MAJORITY OF THE FUNCTIONALITY COMES FROM IT


        System.out.println("Loading complete.");
    }

    public static void infoLog(String msg) {
        logger.log(Level.INFO, msg);
    }

    @Override
    public void onDisable() {

    }

    public void loadRandomTeleporter() {
        this.reloadConfig();
        try {
            getCommand("rtp").setExecutor(new RandomTeleporter(this.getConfig()));
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "RTP Command could not be loaded!");
            e.printStackTrace();
        }
    }

    public String getDefaultConfigVersion() throws Exception {
        if (defaultConfigVersion != null) return defaultConfigVersion;
        String confVersionLine = "config-version: ";
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
                this.getClass().getClassLoader().getResourceAsStream("config.yml")
        )));
        try {
            String s;
            while ((s = bufferedReader.readLine()) != null) {
                if (s.startsWith(confVersionLine))
                    return defaultConfigVersion = s.substring(confVersionLine.length());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new Exception("Could not find version of the resource config.yml");
    }

    public String getCurrentConfigVersion() {
        return getConfig().getString("config-version");
    }

    public RandomTeleporter getRandomTeleporter() {
        return (RandomTeleporter) getCommand("rtp").getExecutor();
    }
}