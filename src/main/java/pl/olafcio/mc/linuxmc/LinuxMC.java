package pl.olafcio.mc.linuxmc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public final class LinuxMC extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        super.onEnable();

        getConfig().options().copyDefaults(true);
        saveConfig();
        if (getConfig().getBoolean("enable-proxy")) {
            Runnable r = new Runnable() {
                public void run() {
                    Runtime run = Runtime.getRuntime();
                    try {
                        URL url = new URL("file:///home/container/proxy/server.jar");
                        URL[] urls = new URL[]{url};
                        ClassLoader classloader = new URLClassLoader (urls, ClassLoader.getSystemClassLoader());
                        Class<?> clazz = classloader.loadClass("net.md_5.bungee.Bootstrap");
                        System.setProperty("waterfall.packet-decode-logging", "true");
                        String[] args = {""};
                        clazz.getMethod("main",  String[].class).invoke(null, new Object[] {args});
                    } catch (Exception e) {
                        getLogger().info(e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            };

            new Thread(r).start();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        super.onDisable();
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("runcmd") || label.equalsIgnoreCase("linuxmc:runcmd")) {
            String fixedArgs = Arrays.toString(args).replace("[", "").replace("]", "").replace(",", "");
            if (getConfig().getBoolean("enable-proxy")) {
                System.setProperty("user.dir", "/home/container/proxy");
            }
            if (fixedArgs == "") {
                return false;
            }
            Runtime run = Runtime.getRuntime();
            Process pr = null;
            try {
                pr = run.exec(fixedArgs);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                pr.waitFor();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = "";
            while (true) {
                try {
                    if (!((line = buf.readLine()) != null)) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                sender.sendMessage(line);
            }
        } else if (label.equalsIgnoreCase("isProxyEnabled") || label.equalsIgnoreCase("linuxmc:isProxyEnabled")) {
            if (args.length > 0) {
                return false;
            }
            sender.sendMessage(String.valueOf(getConfig().getBoolean("enable-proxy")));
        }
        return true;
    }
}
