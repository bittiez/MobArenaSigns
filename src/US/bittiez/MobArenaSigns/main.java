package US.bittiez.MobArenaSigns;

import US.bittiez.UtilityAPI.UpdateChecker.UpdateChecker;
import US.bittiez.UtilityAPI.UpdateChecker.UpdateStatus;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;


public class main extends JavaPlugin implements Listener {
    private static FileConfiguration signData;
    private static Logger log;
    private static String signFileYml = "signData.yml";
    private String joinTitle = "[Join Arena]";
    private String exitTitle = "[Exit Arena]";
    private ChatColor titleColor = ChatColor.DARK_PURPLE;
    private boolean hasUpdate = false;

    @Override
    public void onEnable() {
        log = getLogger();
        loadSignData();

        UpdateStatus updater = new UpdateChecker("https://github.com/bittiez/MobArenaSigns/raw/master/src/plugin.yml", getDescription().getVersion()).IsUpToDate();
        if(updater.HasUpdate){
            hasUpdate = true;
            log.warning("[Mob Arena Signs] has an update! Check it out at https://github.com/bittiez/MobArenaSigns/releases or https://www.spigotmc.org/resources/mobarena-signs.37001/");
            log.warning("Local Version: " + updater.LocalVersion + "  |  Remote Version: " + updater.RemoteVersion);
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event){
        if(hasUpdate) {
            for (Player player : getServer().getOnlinePlayers()) {
                if (player.isOp() || player.hasPermission("MAS.create")) {
                    player.sendMessage(
                            ChatColor.GOLD + "[Mob Arena Signs] has an update! Check it out at https://github.com/bittiez/MobArenaSigns/releases or https://www.spigotmc.org/resources/mobarena-signs.37001/");
                }
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player who = event.getPlayer();
        Block block = event.getBlock();
        if (!event.isCancelled()) {
            if (event.getLine(SIGNLINES.TITLE).equalsIgnoreCase(joinTitle) || event.getLine(SIGNLINES.TITLE).equalsIgnoreCase(exitTitle)) {
                int signType;
                if (event.getLine(SIGNLINES.TITLE).equalsIgnoreCase(joinTitle))
                    signType = SIGNTYPE.JOIN;
                else
                    signType = SIGNTYPE.EXIT;

                if (who.hasPermission("MAS.create")) {
                    if (signType == SIGNTYPE.JOIN)
                        event.setLine(SIGNLINES.TITLE, titleColor + joinTitle);
                    else
                        event.setLine(SIGNLINES.TITLE, titleColor + exitTitle);
                    signData.set(block.getX() + "" + block.getY() + "" + block.getZ(), "MAS SIGN");
                    saveSignData();
                    who.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6MAS &3Sign created successfully!"));
                } else
                    who.sendMessage("You do not have permission to create Mob Arena Signs!");
            }
        }
    }

    @EventHandler
    public void main(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block != null && block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            if (isMASSign(sign)) {
                if (!event.getPlayer().hasPermission("MAS.break"))
                    event.setCancelled(true);
                else {
                    signData.set(block.getX() + "" + block.getY() + "" + block.getZ(), null);
                    saveSignData();
                }
            }
        }
    }

    @EventHandler
    public void main(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block != null && block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            if (isMASSign(sign)) {
                if (event.getPlayer().hasPermission("MAS.use")) {
                    String arena = sign.getLine(SIGNLINES.ARENA);
                    String title = sign.getLine(SIGNLINES.TITLE);

                    if (title.equals(titleColor + joinTitle))
                        if (arena != null && !arena.isEmpty())
                            event.getPlayer().performCommand("ma join " + arena);
                    if (title.equals(titleColor + exitTitle))
                        event.getPlayer().performCommand("ma leave");
                }
            }
        }
    }


    private boolean isMASSign(Sign sign) {
        String title = sign.getLine(SIGNLINES.TITLE);
        boolean isMAS = false;

        if (title.equals(titleColor + joinTitle) || title.equals(titleColor + exitTitle)) {
            isMAS = true;
            Block signBlock = sign.getBlock();
            if (signData.getString(signBlock.getX() + "" + signBlock.getY() + "" + signBlock.getZ()) == null) {
                isMAS = false;
            }
        }
        return isMAS;
    }

    private void saveSignData() {
        try {
            signData.save(new File(this.getDataFolder(), signFileYml));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSignData() {
        File signFile = new File(this.getDataFolder(), signFileYml);

        if (!this.getDataFolder().exists())
            this.getDataFolder().mkdirs();

        if (!signFile.exists()) {
            try {
                signFile.createNewFile();
                signData = YamlConfiguration.loadConfiguration(signFile);
            } catch (IOException e) {
                getServer().getPluginManager().disablePlugin(this);
                e.printStackTrace();
            }
        }
    }
}
