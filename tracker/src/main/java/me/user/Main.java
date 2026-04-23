package me.user;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class Main extends JavaPlugin implements CommandExecutor {

    private final Random random = new Random();
    private final String TRACKER_NAME = "§b§l플레이어 추적기";
    private final String NO_PLAYER_NAME = "§c§lNo Player";

    @Override
    public void onEnable() {
        getCommand("compass").setExecutor(this);
        
        // 1초(20틱)마다 실행
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateCompass(player);
            }
        }, 0L, 20L); 
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        ItemStack compass = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(TRACKER_NAME);
            meta.setLodestoneTracked(false);
            compass.setItemMeta(meta);
        }

        player.getInventory().addItem(compass);
        player.sendMessage("§a[알림] 추적 나침반을 지급했습니다!");
        return true;
    }

    private void updateCompass(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.COMPASS) return;

        CompassMeta meta = (CompassMeta) item.getItemMeta();
        // 현재 들고 있는 나침반이 추적기 혹은 No Player 상태인지 확인
        if (meta == null || (!meta.getDisplayName().equals(TRACKER_NAME) && !meta.getDisplayName().equals(NO_PLAYER_NAME))) return;

        Player nearest = null;
        double nearestDistance = 1000.0;

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player) || !other.getWorld().equals(player.getWorld())) continue;
            if (other.isSneaking()) continue; // 웅크리기 제외

            double distance = player.getLocation().distance(other.getLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = other;
            }
        }

        if (nearest != null) {
            // 1. 대상이 있을 때
            meta.setDisplayName(TRACKER_NAME); // 이름 복구
            meta.setLodestone(nearest.getLocation());
        } else {
            // 2. 대상이 없을 때 (1000블럭 내 없음 또는 모두 Shift 중)
            meta.setDisplayName(NO_PLAYER_NAME); // 이름 변경
            
            // 무작위 좌표를 찍어 바늘을 회전시킴
            Location randomLoc = player.getLocation().clone().add(
                random.nextInt(200) - 100, 
                0, 
                random.nextInt(200) - 100
            );
            meta.setLodestone(randomLoc);
        }
        
        item.setItemMeta(meta);
    }
}