package US.bittiez.MobArenaSigns;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
/**
 * Info for me:
 * Config file will use the getX + getY + getZ in one string for the name
 *
 * Color the signs
 */


/**
 * Created by bittiez on 21/04/16.
 */
public class main extends JavaPlugin implements Listener {
    private static FileConfiguration signData;
    private static Logger log;
    private static String signFileYml = "signData.yml";

    /*
    Possible config items later:
     */
    private String joinTitle = "[Join Arena]";
    private String exitTitle = "[Exit Arena]";



    /*

    Overrides

     */
    @Override
    public void onEnable(){
        log = getLogger();
        loadSignData();

        getServer().getPluginManager().registerEvents(this, this);
    }




    /*

    Events

     */
    @EventHandler
    public void main(SignChangeEvent event){
        Block block = event.getBlock();
        Sign sign = (Sign)event.getBlock().getState();
        if(isMASSign(sign, true)){
            if(event.getPlayer().hasPermission("MAS.create")){
                signData.set(block.getX() + "" + block.getY() + "" + block.getZ(), "MAS SIGN");
                saveSignData();
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6MAS &3Sign created successfully!"));
            }
        }
    }
    @EventHandler
    public void main(BlockBreakEvent event){
        Block block = event.getBlock();
        if(block != null && block.getState() instanceof Sign){
            Sign sign = (Sign)block.getState();
            if(isMASSign(sign)){
                if(!event.getPlayer().hasPermission("MAS.break"))
                    event.setCancelled(true);
                else {
                    signData.set(block.getX() + "" + block.getY() + "" + block.getZ(), null);
                    saveSignData();
                }
            }
        }
    }
    @EventHandler
    public void main(PlayerInteractEvent event){
        Block block = event.getClickedBlock();
        if(block != null && block.getState() instanceof Sign){
            Sign sign = (Sign)block.getState();
            if(isMASSign(sign)){
                if(event.getPlayer().hasPermission("MAS.use")){
                    String arena = sign.getLine(SIGNLINES.ARENA);
                    String title = sign.getLine(SIGNLINES.TITLE);

                    if(title.equals(joinTitle))
                        if(arena != null && !arena.isEmpty())
                            event.getPlayer().performCommand("ma join " + arena);
                    if(title.equals(exitTitle))
                        event.getPlayer().performCommand("ma leave");
                }
            }
        }
    }





    /*

    Private functions

     */
    private boolean isMASSign(Sign sign){
        return isMASSign(sign, false);
    }
    private boolean isMASSign(Sign sign, boolean newCheck){
        String title = sign.getLine(SIGNLINES.TITLE);
        String arena = sign.getLine(SIGNLINES.ARENA);
        boolean isMAS = false;

        if(title.equals(joinTitle) || title.equals(exitTitle)){
            isMAS = true;
            Block signBlock = sign.getBlock();
            if(!newCheck && signData.getString(signBlock.getX() + "" + signBlock.getY() + "" + signBlock.getZ()) == null){
                isMAS = false;
            }
        }


        return isMAS;
    }
    private void saveSignData(){
        try {
            signData.save(new File(this.getDataFolder(), signFileYml));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadSignData(){
        File signFile = new File(this.getDataFolder(), signFileYml);

        if(!this.getDataFolder().exists())
            this.getDataFolder().mkdirs();

        if(!signFile.exists()) {
            try {
                signFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(signFile.exists())
            signData = YamlConfiguration.loadConfiguration(signFile);
        else
            getServer().getPluginManager().disablePlugin(this);
    }
}
