package me.milese3.NanoParkour;
import java.io.File;

import net.minecraft.server.v1_6_R3.NBTTagList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin implements Listener{
	FileConfiguration config;
    File cfile;
    public Player checkpointP;
    public String Map;
	public String prefix = ChatColor.AQUA + "[" + ChatColor.RED + ChatColor.BOLD + "NanoParkour" + ChatColor.RESET + ChatColor.AQUA + "] " + ChatColor.GRAY;
	public boolean running;
	public boolean checkpoint;
	public int point;
	
	@Override
	public void onEnable (){
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		cfile = new File(getDataFolder(), "config.yml");
		config = YamlConfiguration.loadConfiguration(cfile);
		this.saveDefaultConfig();
		getLogger().info("[NanoParkour] by milese3 and JoelyMo101 has been enabled version 0.0.1A");
		running = false;
	}
	@Override
	public void onDisable (){
		getLogger().info("[NanoParkour] by milese3 and JoelyMo101 has been disabled version 0.0.1A");
		running = false;
		saveConfig();
	}
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(!(sender instanceof Player)){
			getLogger().info("You must be a player to do that!");
			return false;
		}
		Player s = (Player) sender;
		String map;
		if(cmd.getName().equalsIgnoreCase("pk")){
			if(args.length == 0){
				//Display Help command
				return true;
			}
			if(args[0].equalsIgnoreCase("help")){
				if(args[1].equalsIgnoreCase("admin")){
					if(s.hasPermission("pk.help.admin")){
						//Display Admin Help command
						return true;
					}
					s.sendMessage(prefix + ChatColor.RED + "You do not have permission to view the Admin comamnds.");
					return true;

				}
				//display Help command
				return true;
			}
			if(args[0].equalsIgnoreCase("create")){
				if(s.hasPermission("pk.create")){
					if(args.length != 2){
						s.sendMessage(prefix + "Usage: /Pk create [ArenaName]");
						return true;
					}
					if(this.getConfig().get("Lobby.Spawn") == null){
						s.sendMessage(prefix + ChatColor.RED + "You must set a spawn for the lobby first!");
						return true;
					}
					map = args[1];
					//make sure the arena does not already exist
					if(this.getConfig().get("Maps." + map) != null){
						s.sendMessage(prefix + "That name is already taken by an exsisting Map, Please choose a new name.");
						return true;
					}
					//create arena
					if(this.getConfig().get("MapIDs") == null){
						this.getConfig().set("MapIDs", 0);
						this.saveConfig();
					}
					this.getConfig().set("Maps." + map + ".Mapname", map);
					this.getConfig().set("Maps." + map + ".Creator", null);
					this.getConfig().set("Maps." + map + ".Disabled", true);
					this.getConfig().set("Maps." + map + ".Checkpoints", null);
					this.getConfig().set("Maps." + map + ".CheckpointsNum", null);
					this.getConfig().set("Maps." + map + ".Spawn", null);
					this.getConfig().set("MapIDs", (this.getConfig().getInt("MapIDs") + 1));
					this.getConfig().set("Maps." + map + ".MapID", this.getConfig().getInt("MapIDs"));
					this.getConfig().set("Maps." + map + ".Edit", true);
					s.sendMessage(prefix + ChatColor.GREEN + "Map " + ChatColor.AQUA + map + ChatColor.GREEN + "(Map " + this.getConfig().getInt("Maps." + map +".MapID") +  ") created!");
					s.sendMessage(prefix + "Now, Set the maps properties (Spawn, Checkpoints and creator). When you are done, issue the command: /pk done [MapName]");
					this.saveConfig();
					return true;
				}
				s.sendMessage(prefix + ChatColor.RED + "You do not have permission to create a Map.");
				return true;

			}
			if(args[0].equalsIgnoreCase("setlobbyspawn")){
				if(s.hasPermission("pk.setlobbyspawn")){
					
				if(args.length != 1){
					s.sendMessage(prefix + "Usage: /Pk setlobbyspawn");
				}
				//Set the location
				this.getConfig().set("Lobby.Spawn.world",  s.getLocation().getWorld().getName());
				this.getConfig().set("Lobby.Spawn.x", s.getLocation().getX());
				this.getConfig().set("Lobby.Spawn.y", s.getLocation().getY());
				this.getConfig().set("Lobby.Spawn.z", s.getLocation().getZ());
				this.getConfig().set("Lobby.Spawn.yaw", s.getLocation().getYaw());
				this.getConfig().set("Lobby.Spawn.pitch", s.getLocation().getPitch());
				s.sendMessage(prefix + ChatColor.GREEN + "Success! The lobby spawn has been set to your current position.");
				this.saveConfig();
				return true;
			}
			s.sendMessage(prefix + ChatColor.RED + "You do not have permission to set the spawn of a Map.");
			return true;
				
			}
			if(args[0].equalsIgnoreCase("setspawn")){
				if(s.hasPermission("pk.setspawn")){
					if(args.length != 2){
						s.sendMessage(prefix + "Usage: /Pk setspawn [Mapname]");
					}
					map = args[1];
					//Check if Map exists
					if(this.getConfig().get("Maps." + map) == null){
						s.sendMessage(prefix + ChatColor.RED + "The Map " + ChatColor.AQUA + map + ChatColor.RED + " does not exist!");
						return true;
					}
					//Check for edit mode
					if(this.getConfig().getBoolean("Maps." + map + ".Disabled") == false){
						s.sendMessage(prefix + ChatColor.RED + "The Map must be disabled for you to do that!");
						return true;
					}
					//Set the location
					this.getConfig().set("Maps." + map + ".Spawn.world",  s.getLocation().getWorld().getName());
					this.getConfig().set("Maps." + map + ".Spawn.x", s.getLocation().getX());
					this.getConfig().set("Maps." + map + ".Spawn.y", s.getLocation().getY());
					this.getConfig().set("Maps." + map + ".Spawn.z", s.getLocation().getZ());
					this.getConfig().set("Maps." + map + ".Spawn.yaw", s.getLocation().getYaw());
					this.getConfig().set("Maps." + map + ".Spawn.pitch", s.getLocation().getPitch());
					s.sendMessage(prefix + ChatColor.GREEN + "Success! The spawn for Map " + ChatColor.AQUA + map + ChatColor.GREEN + " has been set to your current position.");
					this.saveConfig();
					return true;
				}
				s.sendMessage(prefix + ChatColor.RED + "You do not have permission to set the spawn of a Map.");
				return true;
			}
			if(args[0].equalsIgnoreCase("enable")){
				if(s.hasPermission("pk.enable")){
					if(args.length != 2){
						s.sendMessage(prefix + "Usage: /Pk enable [MapName]");
						return true;
					}
					map = args[1];
					if(this.getConfig().getBoolean("Maps." + map + ".Edit") == true){
						s.sendMessage(prefix + ChatColor.RED + "You cannot enable Maps which have not been published.");
						return true;
					}
					if(this.getConfig().get("Maps." + map) == null){
						s.sendMessage(prefix + ChatColor.RED + "The Map " + ChatColor.AQUA + map + ChatColor.RED + " does not exist!");
						return true;
					}
					if(this.getConfig().getBoolean("Maps." + map + ".Disabled") == false){
							s.sendMessage(prefix + "This Map is already enabled!");
							return true;
					}
					s.sendMessage(prefix + ChatColor.GREEN + "Enabled" + ChatColor.GRAY + " map " + ChatColor.AQUA + map + ChatColor.GRAY + "!");
					this.getConfig().set("Maps." + map + ".Disabled", false);
					this.saveConfig();
					return true;
				}
				sender.sendMessage(prefix + ChatColor.RED + "You do not have permission to enable Maps!");
			}
			if(args[0].equalsIgnoreCase("disable")){
				if(s.hasPermission("pk.disable")){
					if(args.length != 2){
						s.sendMessage(prefix + "Usage: /Pk disable [MapName]");
						return true;
					}
					map = args[1];
					if(this.getConfig().getBoolean("Maps." + map + ".Edit") == true){
						s.sendMessage(prefix + ChatColor.RED + "You cannot enable Maps which have not been published.");
						return true;
					}
					if(this.getConfig().get("Maps." + map) == null){
						s.sendMessage(prefix + ChatColor.RED + "The Map " + ChatColor.AQUA + map + ChatColor.RED + " does not exist!");
						return true;
					}
					if(this.getConfig().getBoolean("Mpas." + map + ".Disabled") == true){
							s.sendMessage(prefix + "This Map is already disabled!");
							return true;
					}
					s.sendMessage(prefix + ChatColor.RED + "Disabled" + ChatColor.GRAY + " map " + ChatColor.AQUA + map + ChatColor.GRAY + "!");
					this.getConfig().set("Maps." + map + ".Disabled", true);
					this.saveConfig();
					return true;
				}
				sender.sendMessage(prefix + ChatColor.RED + "You do not have permission to enable Maps!");
			}
			if(args[0].equalsIgnoreCase("done")){
				if(s.hasPermission("pk.done")){
					
					map = args[1];
					if(args.length != 2){
						s.sendMessage(prefix + "Usage: /Pk done [MapName]");
						return true;
					}
					if(this.getConfig().get("Maps." + map) == null){
						s.sendMessage(prefix + ChatColor.RED + "The Map " + ChatColor.AQUA + map + ChatColor.RED + " does not exist!");
						return true;
					}
					if(this.getConfig().getBoolean("Maps." + map + ".Edit") == false){
						s.sendMessage(prefix + ChatColor.RED + "You can only publish new Maps.");
						return true;
					}
					if(this.getConfig().get("Maps." + map + ".Creator") == null){
						s.sendMessage(prefix + "Your Map is missing something! Use /Map " + map + " info, for more info!");
						return true;
					}
					if(this.getConfig().get("Maps." + map + ".CheckpointsNum") == null || this.getConfig().getInt("Maps." + map + ".CheckpointsNum") < 2){
						s.sendMessage(prefix + "Error! You need at least one checkpoint!");
						return true;
					}
					if(this.getConfig().get("Maps." + map + ".Spawn") == null){
						s.sendMessage(prefix + "Your Map is missing something! Use /Map " + map + " info, for more info!");
						return true;
					}
					this.getConfig().set("Maps." + map + ".Disabled", false);
					this.getConfig().set("Maps." + map + ".Edit", false);
					this.saveConfig();
					s.sendMessage(prefix + ChatColor.GREEN + "Sucsess! Map " + ChatColor.AQUA + map + ChatColor.GREEN + " created! /Pk join " + map + "!");
					return true;
				}
				s.sendMessage(prefix + ChatColor.RED + "You do not have permission to publish an arena!");
				return true;
			}
			if(args[0].equalsIgnoreCase("setcreator")){
				if(s.hasPermission("pk.setcreator")){
					if(args.length != 3){
						s.sendMessage(prefix + "Usage: /Pk setcreator [MapName] [PlayerName]");
						return true;
					}
					map = args[1];
					String creator = args[2];
					//Check if Map exists
					if(this.getConfig().get("Maps." + map) == null){
						s.sendMessage(prefix + ChatColor.RED + "The Map " + ChatColor.AQUA + map + ChatColor.RED + " does not exist!");
						return true;
					}
					//Check for edit mode
					if(this.getConfig().getBoolean("Maps." + map + ".Disabled") == false){
						s.sendMessage(prefix + ChatColor.RED + "The Map must be disabled for you to do that!");
						return true;
					}
					////REMEBER MULTI AUTHORS////
					//Set author
					s.sendMessage(prefix + ChatColor.GREEN + "Success! Creator set to " + ChatColor.AQUA + creator + ChatColor.GREEN + "!");
					this.getConfig().set("Maps." + map + ".Creator", creator);
					this.saveConfig();
					return true;
				}
				s.sendMessage(prefix + ChatColor.RED + "You do not have permission to set the creator of a Map.");
				return true;
			}
			if(args[0].equalsIgnoreCase("setcheckpoints")){
				if(s.hasPermission("pk.setcheckpoints")){
					if(args.length != 2){
						s.sendMessage(prefix + "Usage: /Pk setcheckpoints [ArenaName]");
						return true;
					}
					map = args[1];
					//New Map
					if(this.getConfig().getBoolean("Maps." + map + ".Edit") == false){
						s.sendMessage(prefix + ChatColor.RED + "You can only set checkpoints for Maps being set up.");
						return true;
					}
					//Check if Map exists
					if(this.getConfig().get("Maps." + map) == null){
						s.sendMessage(prefix + ChatColor.RED + "The Map " + ChatColor.AQUA + map + ChatColor.RED + " does not exist!");
						return true;
					}
					//Check for edit mode
					if(this.getConfig().getBoolean("Maps." + map + ".Disabled") == false){
						s.sendMessage(prefix + ChatColor.RED + "The Map must be disabled for you to do that!");
						return true;
					}
					//Run checkpoint loop
					checkpoint = true;
					this.getConfig().set("Maps." + map + ".CheckpointsNum", 0);
					s.sendMessage(ChatColor.AQUA + "Checkpoint edit mode enabled! Right click a Stone/Wood pressure plate with a blaze rod to set it as a checkpoint(The start and finish are checkpoints too)!");
					s.sendMessage(ChatColor.AQUA + "When you are done, Type /finish");
					PlayerInventory inv = s.getInventory();
					ItemStack item = new ItemStack(Material.BLAZE_ROD, 1);
					ItemMeta meta = item.getItemMeta();
				    meta.setDisplayName("§6Checkpoint Setup Stick");
				    item.setItemMeta(meta);
					inv.addItem(item);
					point = 1;
					checkpointP = s;
					Map = map;
					return true;
				}
				s.sendMessage(prefix + ChatColor.RED + "You do not have permission to set checkpoints for a Map.");
				return true;
			}
			if(args[0].equalsIgnoreCase("check")){
				if(s.hasPermission("pk.check")){
					if(args.length != 2){
						s.sendMessage(prefix + "Usage: /Pk check [MapName]");
						return true;
					}
					map = args[1];
					if(this.getConfig().get("Maps." + map) == null){
						s.sendMessage(prefix + ChatColor.RED + "The Map " + ChatColor.AQUA + map + ChatColor.RED + " does not exist!");
						return true;
					}
					s.sendMessage(prefix + "Infomation about Map " + ChatColor.AQUA + map + ChatColor.GRAY + "!");
					s.sendMessage(ChatColor.AQUA + "Map Name: " + ChatColor.GRAY + this.getConfig().getString("Maps." + map + ".Mapname"));
					s.sendMessage(ChatColor.AQUA + "Map ID: " + ChatColor.GRAY + this.getConfig().getInt("Maps." + map + "MapID") + "/" + this.getConfig().getInt("MapIDs"));
					s.sendMessage(ChatColor.AQUA + "Map Creator: " + ChatColor.GRAY + this.getConfig().getString("Maps." + map + ".Creator"));
					s.sendMessage(ChatColor.AQUA + "Map Disabled: " + ChatColor.GRAY + this.getConfig().getString("Maps." + map + ".Disabled"));
					s.sendMessage(ChatColor.AQUA + "Map Checkpoints: " + ChatColor.GRAY + this.getConfig().getString("Maps." + map + ".CheckpointsNum"));
					s.sendMessage(ChatColor.AQUA + "Map Spawn World: " + ChatColor.GRAY + this.getConfig().get("Maps." + map + ".Spawn.world"));
					s.sendMessage(ChatColor.AQUA + "Map Spawn X: " + ChatColor.GRAY + this.getConfig().get("Maps." + map + ".Spawn.x"));
					s.sendMessage(ChatColor.AQUA + "Map Spawn Y: " + ChatColor.GRAY + this.getConfig().get("Maps." + map + ".Spawn.y"));
					s.sendMessage(ChatColor.AQUA + "Map Spawn Z: " + ChatColor.GRAY + this.getConfig().get("Maps." + map + ".Spawn.z"));
					return true;
				}
				s.sendMessage(prefix + ChatColor.RED + "You do not have permission to check the propertys of a Map.");
				return true;
			}
			if(args[0].equalsIgnoreCase("join")){
				map = args[1];
				if(args.length > 2){
					s.sendMessage(prefix + "Usage: /Pk join " + map);
					return true;
				} 
				if(this.getConfig().get("Maps." + map) == null){
					s.sendMessage(prefix + "The Map " + ChatColor.AQUA + map + ChatColor.GRAY + " does not exist!");
					return true;
				}
				if(this.getConfig().getBoolean("Maps." + map + ".Disabled") == true){
					s.sendMessage(prefix + "The Map " + ChatColor.AQUA + map + ChatColor.GRAY + " is currently disabled!");
					return true;
				}
				if(this.getConfig().getBoolean("Maps." + map + ".Edit") == true){
					s.sendMessage(prefix + "The Map " + ChatColor.AQUA + map + ChatColor.GRAY + " is currently being set up!");
					return true;
				}
				if(args.length == 1){
					World w = Bukkit.getServer().getWorld(this.getConfig().getString("Lobby.Spawn.World"));
					double x = this.getConfig().getDouble("Lobby.Spawn.x");
					double y = this.getConfig().getDouble("Lobby.Spawn.y");
					double z = this.getConfig().getDouble("Lobby.Spawn.z");
					int yaw = this.getConfig().getInt("Lobby.Spawn.yaw");
					int pitch = this.getConfig().getInt("Lobby.Spawn.pitch");
					s.teleport(new Location(w, x, y, z, yaw, pitch));
					s.sendMessage(prefix + "Welcome to the Parkour lobby!");
					return true;
				}
				this.getConfig().set("PlayerData." + s + ".Map", map);
				this.getConfig().set("PlayerData." + s + ".Checkpoint", 0);
				this.getConfig().set("PlayerData." + s + ".Playing", true);
				this.getConfig().set("PlayerData." + s + ".Alive", false);
				this.saveConfig();
				s.sendMessage(prefix + "You Joined " + ChatColor.AQUA + map + ChatColor.GRAY + " (Map " + ChatColor.AQUA + this.getConfig().getInt("Maps." + map + ".MapID") + ChatColor.GRAY + ") by" + ChatColor.AQUA + this.getConfig().getString("Maps." + map + ".Creator") + ChatColor.GRAY + "!");
				World w = Bukkit.getServer().getWorld(this.getConfig().getString("Maps." + map + ".Spawn.world"));
				double x = this.getConfig().getDouble("Maps." + map + ".Spawn.x");
				double y = this.getConfig().getDouble("Maps." + map + ".Spawn.y");
				double z = this.getConfig().getDouble("Maps." + map + ".Spawn.z");
				int yaw = this.getConfig().getInt("Maps." + map + "Spawn.yaw");
				int pitch = this.getConfig().getInt("Maps." + map + ".Spawn.pitch");
				s.teleport(new Location(w, x, y, z, yaw, pitch));
				s.setGameMode(GameMode.ADVENTURE);
				((CraftPlayer)s).getHandle().inventory.b(new NBTTagList());
				return true;
			}
			if(args[0].equalsIgnoreCase("leave")){
				if(args.length != 1){
					sender.sendMessage(prefix + "Usage: /pk leave");
					return true;
				}
				if(this.getConfig().get("PlayerData." + s) == null || this.getConfig().getBoolean("PlayerData." + s + ".Playing") == false){
					s.sendMessage(prefix + ChatColor.RED + "You are not in a game so you cannot leave one!");
					return true;
				}
				String playing = this.getConfig().getString("PlayerData." + s + "Map");
				s.sendMessage(prefix + "You left the Map " + ChatColor.AQUA + playing + ChatColor.GRAY + " (Map " + ChatColor.AQUA + this.getConfig().getInt("Maps." + playing + ".MapID") + ChatColor.GRAY + ") by" + ChatColor.AQUA + this.getConfig().getString("Maps." + playing + ".Creator") + ChatColor.GRAY + "!");
				World w = Bukkit.getServer().getWorld(this.getConfig().getString("Lobby.Spawn.World"));
				double x = this.getConfig().getDouble("Lobby.Spawn.x");
				double y = this.getConfig().getDouble("Lobby.Spawn.y");
				double z = this.getConfig().getDouble("Lobby.Spawn.z");
				int yaw = this.getConfig().getInt("Lobby.Spawn.yaw");
				int pitch = this.getConfig().getInt("Lobby.Spawn.pitch");
				s.teleport(new Location(w, x, y, z, yaw, pitch));
				s.setGameMode(GameMode.SURVIVAL);
				s.sendMessage(prefix + "Welcome to the Parkour lobby!");
				this.getConfig().set("PlayerData." + s, null);
				this.saveConfig();
				return true;
			}
		}
		if(cmd.getName().equalsIgnoreCase("finish")){
			if(s.hasPermission("pk.setcheckpoints")){
				if(!checkpoint){
					s.sendMessage(prefix + ChatColor.RED + "You must be in checkpoint edit mode, for more info Type /setcheckpoints");
					return true;
				}
				checkpoint = false;
				s.sendMessage(prefix + ChatColor.AQUA + this.getConfig().getInt("Maps." + Map + ".CheckpointsNum") + ChatColor.GREEN + " checkpoints set for Map " + ChatColor.AQUA + Map + ChatColor.GREEN + "!");	
				checkpointP = null;
				Map = null;
				point = 0;
				return true;
			}
			s.sendMessage(prefix + ChatColor.RED + "You do not have permission to issue this command.");
			return true;
	}
		return false;
	}
	@EventHandler
	 public void onCheckpointManager(PlayerInteractEvent event){
		 String map = Map;
		 if(checkpoint){
			 if(event.getPlayer() == checkpointP){
		        if(event.getAction() == Action.RIGHT_CLICK_BLOCK && checkpointP.getInventory().getItemInHand().getType() == Material.BLAZE_ROD) {
		        event.getClickedBlock();
		        if(event.getClickedBlock().getType() == Material.STONE_PLATE);
		        	checkpointP.sendMessage(ChatColor.AQUA + "Checkpoint " + ChatColor.GREEN + point + ChatColor.AQUA + " set!");
		        	String world = event.getClickedBlock().getLocation().getWorld().getName();
		        	double x = event.getClickedBlock().getLocation().getX();
		        	double y = event.getClickedBlock().getLocation().getY();
		        	double z = event.getClickedBlock().getLocation().getZ();
		        	this.getConfig().set("Maps." + map + ".Checkpoints." + point + ".world", world);
		        	this.getConfig().set("Maps." + map + ".Checkpoints." + point + ".x", x);
		        	this.getConfig().set("Maps." + map + ".Checkpoints." + point + ".y", y);
		        	this.getConfig().set("Maps." + map + ".Checkpoints." + point + ".z", z);
		        	this.getConfig().set("Maps." + map + ".CheckpointsNum", point);
		        	point++;
		        	this.saveConfig();
		        } else if(event.getClickedBlock().getType() == Material.WOOD_PLATE){
		        	checkpointP.sendMessage(ChatColor.AQUA + "Checkpoint " + ChatColor.GREEN + point + ChatColor.AQUA + " set!");
		        	String world = event.getClickedBlock().getLocation().getWorld().getName();
		        	double x = event.getClickedBlock().getLocation().getX();
		        	double y = event.getClickedBlock().getLocation().getY();
		        	double z = event.getClickedBlock().getLocation().getZ();
		        	this.getConfig().set("Maps." + map + ".Checkpoints." + point + ".world", world);
		        	this.getConfig().set("Maps." + map + ".Checkpoints." + point + ".x", x);
		        	this.getConfig().set("Maps." + map + ".Checkpoints." + point + ".y", y);
		        	this.getConfig().set("Maps." + map + ".Checkpoints." + point + ".z", z);
		        	this.getConfig().set("Maps." + map + ".CheckpointsNum", point);
		        	point++;
		        	this.saveConfig();
		        } else {
		        	checkpointP.sendMessage(ChatColor.RED + "That is not a pressure plate so it could not be set as a checkpoint!");
		        }
			 }
		 }
    }
	@EventHandler
	public void onFalling(PlayerMoveEvent e) {
	    Material m = e.getPlayer().getLocation().getBlock().getType();
	    Player s = e.getPlayer();
	    String map = this.getConfig().getString("PlayerData." + s + ".Map");
	    if(map != null){
		    if(this.getConfig().getBoolean("PlayerData." + map + ".Playing") == true){
			    if (m == Material.STATIONARY_WATER || m == Material.WATER || m == Material.LAVA || m == Material.STATIONARY_LAVA) {
			    	this.getConfig().set("PlayerData." + s + ".Alive", false);
			    	this.getConfig().set("PlayerData." + s + ".Checkpoint", 0);
			    	this.saveConfig();
			    	s.sendMessage(prefix + "You fell! Teleporting to start!");
					World w = Bukkit.getServer().getWorld(this.getConfig().getString("Maps." + map + ".Spawn.world"));
					double x = this.getConfig().getDouble("Maps." + map + ".Spawn.x");
					double y = this.getConfig().getDouble("Maps." + map + ".Spawn.y");
					double z = this.getConfig().getDouble("Maps." + map + ".Spawn.z");
					int yaw = this.getConfig().getInt("Maps." + map + "Spawn.yaw");
					int pitch = this.getConfig().getInt("Maps." + map + ".Spawn.pitch");
					s.teleport(new Location(w, x, y, z, yaw, pitch));
			    }
		    }
	    }


	}
    @EventHandler
    public void onStepOnPressurePlate(PlayerInteractEvent event){
    	Player s = event.getPlayer();
    	String map = this.getConfig().getString("PlayerData." + s + ".Map");
    	if(map != null){
    		if(this.getConfig().getBoolean("PlayerData." + s + ".Playing") == true){
    			if(event.getAction().equals(Action.PHYSICAL)){
    	            if(event.getClickedBlock().getType() == Material.STONE_PLATE){
    	            	int checkpoint = this.getConfig().getInt("PlayerData." + s + ".Checkpoint");
    	            	int checkpoints = (this.getConfig().getInt("Maps." + map + ".CheckpointsNum") -1);
    	            	World w = Bukkit.getServer().getWorld(this.getConfig().getString("Maps." + map + ".Checkpoints." + checkpoint + ".world"));
    	            	double x = this.getConfig().getDouble("Maps." + map + ".Checkpoints." + checkpoint + ".x");
    	            	double y = this.getConfig().getDouble("Maps." + map + ".Checkpoints." + checkpoint + ".x");
    	            	double z = this.getConfig().getDouble("Maps." + map + ".Checkpoints." + checkpoint + ".x");
    	            	Location loc = new Location(w, x, y, z);
    	            	Location pos = event.getClickedBlock().getLocation();
    	            	if(loc == pos){
    	            		if(checkpoint == 0){
    	            			this.getConfig().set("PlayerData." + s + ".Checkpoint", (checkpoint + 1));
    	            			this.getConfig().set("PlayerData." + s + ".Alive", true);
    	            			s.sendMessage(prefix + "Started map " + ChatColor.AQUA + map + ChatColor.GRAY + "!");
    	            			s.sendMessage(prefix + "Checkpoint " + ChatColor.RED + (checkpoint + 1) + "/" + this.getConfig().getInt("Maps." + map + "CheckpointsNum"));
    	            			this.saveConfig();
    	            		} else if(checkpoint <= checkpoints){
    	            			this.getConfig().set("PlayerData." + s + ".Checkpoint", (checkpoint + 1));
    	            			s.sendMessage(prefix + "Checkpoint " + ChatColor.RED + (checkpoint + 1) + "/" + this.getConfig().getInt("Maps." + map + "CheckpointsNum"));
    	            			s.sendMessage(prefix + ChatColor.GREEN + "Congratulations! You Completed the Map " + ChatColor.AQUA + map + ChatColor.GREEN + " (Map " + ChatColor.AQUA + this.getConfig().getInt("Maps." + map + "MapID") + ChatColor.GREEN + ")!");
    	    					World wLobby = Bukkit.getServer().getWorld(this.getConfig().getString("Lobby.Spawn.World"));
    	    					double xLobby = this.getConfig().getDouble("Lobby.Spawn.x");
    	    					double yLobby = this.getConfig().getDouble("Lobby.Spawn.y");
    	    					double zLobby = this.getConfig().getDouble("Lobby.Spawn.z");
    	    					int yaw = this.getConfig().getInt("Lobby.Spawn.yaw");
    	    					int pitch = this.getConfig().getInt("Lobby.Spawn.pitch");
    	    					s.teleport(new Location(wLobby, xLobby, yLobby, zLobby, yaw, pitch));
    	    					s.sendMessage(prefix + "Welcome to the Parkour lobby!");
    	    					this.getConfig().set("PlayerData." + s, null);
    	            			this.saveConfig();
    	            		} else {
    	            			this.getConfig().set("PlayerData." + s + ".Checkpoint", (checkpoint + 1));
    	            			s.sendMessage(prefix + "Started map " + ChatColor.AQUA + map + ChatColor.GRAY + "!");
    	            			s.sendMessage(prefix + "Checkpoint " + ChatColor.RED + (checkpoint + 1) + "/" + this.getConfig().getInt("Maps." + map + "CheckpointsNum"));
    	            			this.saveConfig();
    	            		}
    	            	}

    	            }
    	            else if(event.getClickedBlock().getType() == Material.WOOD_PLATE){
    	            	int checkpoint = this.getConfig().getInt("PlayerData." + s + ".Checkpoint");
    	            	int checkpoints = (this.getConfig().getInt("Maps." + map + ".CheckpointsNum") -1);
    	            	World w = Bukkit.getServer().getWorld(this.getConfig().getString("Maps." + map + ".Checkpoints." + checkpoint + ".world"));
    	            	double x = this.getConfig().getDouble("Maps." + map + ".Checkpoints." + checkpoint + ".x");
    	            	double y = this.getConfig().getDouble("Maps." + map + ".Checkpoints." + checkpoint + ".x");
    	            	double z = this.getConfig().getDouble("Maps." + map + ".Checkpoints." + checkpoint + ".x");
    	            	Location loc = new Location(w, x, y, z);
    	            	World checkWorld = event.getClickedBlock().getLocation().getWorld();
    	            	double checkX = event.getClickedBlock().getLocation().getX();
    	            	double checkY = event.getClickedBlock().getLocation().getY();
    	            	double checkZ = event.getClickedBlock().getLocation().getZ();
    	            	Location pos = new Location(checkWorld, checkX, checkY, checkZ);
    	            	if(loc == pos){
    	            		if(checkpoint == 0){
    	            			this.getConfig().set("PlayerData." + s + ".Checkpoint", (checkpoint + 1));
    	            			this.getConfig().set("PlayerData." + s + ".Alive", true);
    	            			s.sendMessage(prefix + "Started map " + ChatColor.AQUA + map + ChatColor.GRAY + "!");
    	            			s.sendMessage(prefix + "Checkpoint " + ChatColor.RED + (checkpoint + 1) + "/" + this.getConfig().getInt("Maps." + map + "CheckpointsNum"));
    	            			this.saveConfig();
    	            		} else if(checkpoint <= checkpoints){
    	            			this.getConfig().set("PlayerData." + s + ".Checkpoint", (checkpoint + 1));
    	            			s.sendMessage(prefix + "Checkpoint " + ChatColor.RED + (checkpoint + 1) + "/" + this.getConfig().getInt("Maps." + map + "CheckpointsNum"));
    	            			s.sendMessage(prefix + ChatColor.GREEN + "Congratulations! You Completed the Map " + ChatColor.AQUA + map + ChatColor.GREEN + " (Map " + ChatColor.AQUA + this.getConfig().getInt("Maps." + map + "MapID") + ChatColor.GREEN + ")!");
    	    					World wLobby = Bukkit.getServer().getWorld(this.getConfig().getString("Lobby.Spawn.World"));
    	    					double xLobby = this.getConfig().getDouble("Lobby.Spawn.x");
    	    					double yLobby = this.getConfig().getDouble("Lobby.Spawn.y");
    	    					double zLobby = this.getConfig().getDouble("Lobby.Spawn.z");
    	    					int yaw = this.getConfig().getInt("Lobby.Spawn.yaw");
    	    					int pitch = this.getConfig().getInt("Lobby.Spawn.pitch");
    	    					s.teleport(new Location(wLobby, xLobby, yLobby, zLobby, yaw, pitch));
    	    					s.sendMessage(prefix + "Welcome to the Parkour lobby!");
    	    					this.getConfig().set("PlayerData." + s, null);
    	            			this.saveConfig();
    	            		} else {
    	            			this.getConfig().set("PlayerData." + s + ".Checkpoint", (checkpoint + 1));
    	            			s.sendMessage(prefix + "Started map " + ChatColor.AQUA + map + ChatColor.GRAY + "!");
    	            			s.sendMessage(prefix + "Checkpoint " + ChatColor.RED + (checkpoint + 1) + "/" + this.getConfig().getInt("Maps." + map + "CheckpointsNum"));
    	            			this.saveConfig();
    	            		}
    	            	}
    	            }
    	        }
    		}
    	}
    }
    @EventHandler
    public void onJoinEvent(PlayerJoinEvent e){
    	Player s = e.getPlayer();
    	String map = this.getConfig().getString("PlayerData." + s + ".Map");
    	if(map != null){
    		if(this.getConfig().getBoolean("PlayerData." + s + ".Playing") == true){
    			this.getConfig().set("PlayerData." + s, null);
    			this.saveConfig();
				World w = Bukkit.getServer().getWorld(this.getConfig().getString("Lobby.Spawn.World"));
				double x = this.getConfig().getDouble("Lobby.Spawn.x");
				double y = this.getConfig().getDouble("Lobby.Spawn.y");
				double z = this.getConfig().getDouble("Lobby.Spawn.z");
				int yaw = this.getConfig().getInt("Lobby.Spawn.yaw");
				int pitch = this.getConfig().getInt("Lobby.Spawn.pitch");
				s.teleport(new Location(w, x, y, z, yaw, pitch));
				s.setGameMode(GameMode.SURVIVAL);
				s.sendMessage(prefix + "Welcome to the Parkour lobby!");
    		}
    	}

    }
    public void onBreakEvent(BlockBreakEvent e){
    	Player s = e.getPlayer();
    	if(this.getConfig().getBoolean("PlayerData." + s + ".Playing") == true){
    		e.setCancelled(true);
    		s.sendMessage(prefix + ChatColor.RED + "You are not allowed to break blocks whilst playing Parkour!");
    	}
    }
    public void onPlaceEvent(BlockPlaceEvent e){
    	Player s = e.getPlayer();
    	if(this.getConfig().getBoolean("PlayerData." + s + ".Playing") == true){
    		e.setCancelled(true);
    		s.sendMessage(prefix + ChatColor.RED + "You are not allowed to place blocks whilst playing Parkour!");
    	}
    }
}
