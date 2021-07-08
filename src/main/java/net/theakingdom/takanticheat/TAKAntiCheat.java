package net.theakingdom.takanticheat;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class TAKAntiCheat extends JavaPlugin implements Listener {
    //Temp/RAM player data
    ArrayList<UUID> silentJoin = new ArrayList<>();
    Player latestDetect;
    HashMap<Player, Boolean> speedDetected = new HashMap<>();
    HashMap<Player, Boolean> goneDown = new HashMap<>();
    HashMap<PlayerMoveEvent, Integer> i = new HashMap<>();
    HashMap<Player, Location> moved = new HashMap<>();

    public void flag(Player player, String reason){
        getServer().getOnlinePlayers().forEach(opl -> {
            if(opl.isOp()){
                opl.sendMessage("§4[§cTAKAntiCheat§4] §cDetected player §4" + player.getName() + "§c with the exploit §4" + reason + "§c.");
                opl.sendMessage("§4[§cTAKAntiCheat§4] §cTo teleport to player, write §4/ldtp §cor §4/lastdetectiontp§c.");
            }
        });
        latestDetect = player;
    }

    //Get bans
    /*
    public HashMap<UUID, String> getBans(){
        File file = new File(this.getDataFolder(), "bans.yml");
        if(!file.exists()){
            try {
                file.createNewFile();
                FileConfiguration tempConfig = new YamlConfiguration();
                try { tempConfig.load(file); } catch (InvalidConfigurationException e) {}
                HashMap<UUID, String> tempBans = new HashMap<>();
                tempConfig.set("bans", tempBans);
                tempConfig.save(file);
            } catch (IOException e) {}
        }
        FileConfiguration fileConfig = new YamlConfiguration();
        try {
            fileConfig.load(file);
        } catch (IOException e) {} catch (InvalidConfigurationException e) {}
        HashMap<UUID, String> bans = (HashMap<UUID, String>) fileConfig.get("bans");

        return bans;
    }

    public void ban(OfflinePlayer p, String reason){
        File file = new File(this.getDataFolder(), "bans.yml");
        if(!file.exists()){
            try {
                file.createNewFile();
                FileConfiguration tempConfig = new YamlConfiguration();
                try { tempConfig.load(file); } catch (InvalidConfigurationException e) {}
                HashMap<UUID, String> tempBans = new HashMap<>();
                tempConfig.set("bans", tempBans);
                tempConfig.save(file);
            } catch (IOException e) {}
        }
        FileConfiguration fileConfig = new YamlConfiguration();
        try {
            fileConfig.load(file);
        } catch (IOException e) {} catch (InvalidConfigurationException e) {}
        HashMap<UUID, String> bans = (HashMap<UUID, String>) fileConfig.get("bans");

        bans.put(p.getUniqueId(), reason);
        Player onp = getServer().getPlayer(p.getUniqueId());
        if(onp != null) {
            onp.kickPlayer("§4Error: §cI'm afraid you have been banned for the following reason: §7\n");
        }
    }

    public void unban(OfflinePlayer p){
        File file = new File(this.getDataFolder(), "bans.yml");
        if(!file.exists()){
            try {
                file.createNewFile();
                FileConfiguration tempConfig = new YamlConfiguration();
                try { tempConfig.load(file); } catch (InvalidConfigurationException e) {}
                HashMap<UUID, String> tempBans = new HashMap<>();
                tempConfig.set("bans", tempBans);
                tempConfig.save(file);
            } catch (IOException e) {}
        }
        FileConfiguration fileConfig = new YamlConfiguration();
        try {
            fileConfig.load(file);
        } catch (IOException e) {} catch (InvalidConfigurationException e) {}
        HashMap<UUID, String> bans = (HashMap<UUID, String>) fileConfig.get("bans");
        bans.remove(p.getUniqueId());
    }

    public boolean isOnGround(Player p){
        Location playerLoc = p.getLocation();
        Location belowPlayer = new Location(playerLoc.getWorld(), playerLoc.getX(), playerLoc.getY()-2, playerLoc.getZ());
        if(belowPlayer.getBlock().getType().equals(Material.AIR)) return true;
        return false;
    }*/

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    public double getDistance(double x1, double y1, double x2, double y2){
        double xDist;
        if(x1 >= x2){
            xDist = x1-x2;
        } else{
            xDist = x2-x1;
        }
        double yDist;
        if(y1 >= y2){
            yDist = y1-y2;
        } else{
            yDist = y2-y1;
        }
        double dist;
        if(xDist >= yDist){
            dist = xDist;
        } else{
            dist = yDist;
        }
        System.out.println("Speed: " + Double.toString(dist));
        return dist;
    }

    //Move event for checking movement cheats
    @EventHandler
    public void onMove(PlayerMoveEvent ev){
        Player p = ev.getPlayer();
        Entity e = (Entity) p;

        //Speed
        console.log("On ground: " + Boolean.toString(p.isOnGround()));
        if(getDistance(ev.getFrom().getX(), ev.getFrom().getZ(), ev.getTo().getX(), ev.getTo().getZ()) >= 0.7d && p.isOnGround()){
            if(speedDetected.get(p) == null || speedDetected.get(p) == false) {
                flag(ev.getPlayer(), "speed");
            }
            p.teleport(ev.getFrom());
            speedDetected.put(p, true);
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                if(speedDetected.get(p)){
                    speedDetected.put(p, false);
                }
            }, 100);
        }
        //System.out.println(getDistance(ev.getFrom().getX(), ev.getFrom().getZ(), ev.getTo().getX(), ev.getTo().getZ()));
        moved.put(ev.getPlayer(), ev.getTo());
    }

    /*
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent ev){
        if(getBans().containsKey((OfflinePlayer) ev.getPlayer())){
            ev.disallow(ev.getResult(), getBans().get((OfflinePlayer) ev.getPlayer()));
        }
    }*/

    //Silent join engine
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent ev){
        if(silentJoin.contains(ev.getPlayer().getUniqueId())){
            ev.setJoinMessage(null);
            ev.getPlayer().sendMessage("§aJoined silently.");
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent ev){
        if(silentJoin.contains(ev.getPlayer().getUniqueId())){
            ev.setQuitMessage(null);
        }
    }

    //Staff chat engine
    @EventHandler
    public void onChat(PlayerChatEvent ev){
        //I don't give a shit about deprecated functions as long as they're not exploitation vulnerabilities
        if(ev.getMessage().startsWith("# ") && ev.getPlayer().hasPermission("tak.sendstaffchat")){
            ev.setCancelled(true);
            String message = ev.getMessage();
            message = message.replace("# ", "");
            Player p = ev.getPlayer();
            String finalMessage = message;
            getServer().getOnlinePlayers().forEach(onl -> {
                if(onl.hasPermission("tak.seestaffchat")){
                    onl.sendMessage("§4[§cStaffChat§4] §7" + p.getDisplayName() + "§r: " + finalMessage);
                }
            });
        }
    }

    //Command engine
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Teleport to detected cheater
        if(command.getName().equals("ldtp")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                Player victim = latestDetect;
                if (p.isOp()) {
                    if (victim != null) {
                        p.teleport(victim);
                        p.sendMessage("§aSending you to the latest detection.");
                    } else {
                        p.sendMessage("§4Error: §cThere have been no detections.");
                    }
                } else {
                    p.sendMessage("§4Error: §cYou don't have permission to run this command.");
                }
            } else {
                sender.sendMessage("A non player cannot use this command.");
            }
        }

        //Ban command
        /*
        if(command.getName().equals("ban")){
            if(sender.hasPermission("tak.banPlayer")){
                if(args.length > 0){
                    OfflinePlayer ofp = getServer().getOfflinePlayerIfCached(args[0]);
                    if(ofp == null){
                        sender.sendMessage("§4Error: §cThat player wasn't found in the offline player cache.");
                    } else{
                        if(args.length > 1){
                            String reason = String.join(" ", args);
                            reason = reason.replace(args[0] + " ", "");
                            ban(ofp, reason);
                        } else{
                            ban(ofp, "Le banne de hammer has been struck opun u idot");
                        }
                        sender.sendMessage("§aSuccessfully banned §2" + ofp.getName() + "§a.");
                    }
                } else{
                    sender.sendMessage("§4Error: §cNo player name specified.");
                }
            }
        }*/

        //Kick command
        if(command.getName().equals("kick")){
            if(sender.hasPermission("tak.kickPlayer")){
                if(args.length > 0){
                    Player p = getServer().getPlayer(args[0]);
                    if(p == null){
                        sender.sendMessage("§4Error: §cPlayer isn't online or not found.");
                    } else{
                        if(args.length > 1){
                            String reason = String.join(" ", args);
                            reason = reason.replace(args[0] + " ", "");
                            p.kickPlayer(reason);
                        } else{
                            p.kickPlayer("U got drievbye kciked of " + sender.getName() + ". xdd");
                        }
                        sender.sendMessage("§aSuccessfully kicked §2" + p.getName() + "§a.");
                    }
                } else{
                    sender.sendMessage("§4Error: §cNo player specified to kick.");
                }
            } else{
                sender.sendMessage("§4Error: §cYou don't have permission to run this command.");
            }
        }

        //Unban command
        /*
        if(command.getName().equals("unban")){
            if(sender.hasPermission("tak.unBanPlayer")){
                if(args.length > 0){
                    OfflinePlayer p = getServer().getOfflinePlayerIfCached(args[0]);
                    if(p == null){
                        sender.sendMessage("§4Error: §cThat player wasn't found in the offline player cache.");
                    } else{
                        unban(p);
                        sender.sendMessage("§aSuccessfully unbanned §2" + p.getName() + "§a.");
                    }
                } else{
                    sender.sendMessage("§4Error: §cNo player specified to kick.");
                }
            } else{
                sender.sendMessage("§4Error: §cYou don't have permission to run this command.");
            }
        }*/

        //Toggle silent join command
        if(command.getName().equals("silentjoin")){
            if(sender instanceof Player){
                Player p = (Player) sender;
                if(p.hasPermission("tak.silentJoin")){
                    if(silentJoin.contains(p.getUniqueId())){
                        silentJoin.remove(p.getUniqueId());
                        p.sendMessage("§aToggled silent join to §2false§a.");
                    } else{
                        silentJoin.add(p.getUniqueId());
                        p.sendMessage("§aToggled silent join to §2true§a.");
                    }
                } else{
                    p.sendMessage("§4Error: §cYou don't have permission to use this command.");
                }
            } else{
                sender.sendMessage("§4Error: §cThis command can only be run by players.");
            }
        }
        return true;
    }

}
