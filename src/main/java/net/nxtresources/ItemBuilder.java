package net.nxtresources;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class ItemBuilder {

    private final ItemStack is;
    private final ItemMeta im;
    private SkullMeta sm;

    public ItemBuilder(Material m) {
        this.is = new ItemStack(m);
        this.im = is.getItemMeta();
        this.im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    }


    public ItemBuilder setAmount(int amount) {
        is.setAmount(amount);
        return this;
    }

    public ItemBuilder setDisplayName(Component str) {
        im.displayName((str));
        return this;
    }

    public ItemBuilder setEnchantment(Enchantment enchant, int lvl, boolean isVisible) {
        im.addEnchant(enchant, lvl, true);
        if (!isVisible) im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder setLore(Component...comp) {
        im.lore(Arrays.asList(comp));
        return this;
    }

    public ItemBuilder setArenaName(String name) {
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "arena_name");
        this.im.getPersistentDataContainer().set(key, PersistentDataType.STRING, name);
        return this;
    }

    /*public ItemBuilder setDataValue(short dataValue) {
        is.setDurability(dataValue);
        return this;
    }*/

    public ItemStack build() {
        if(sm != null) {
            is.setItemMeta(sm);
        } else
            is.setItemMeta(im);
        return is;
    }
//Lastcall (Ezt utoljara kell callolni a builderben.)
    public ItemBuilder setSkin(String skinData, String skinSig) {
        sm = (SkullMeta) im;
        if(is.getType() != Material.PLAYER_HEAD)
            throw new RuntimeException("INVALID CALL, YOU CAN ONLY SET SKIN FOR PLAYER HEADS.");

        PlayerProfile prof = Bukkit.createProfile("SheepHead");
        prof.setProperty(new ProfileProperty("textures", skinData, skinSig));
        sm.setPlayerProfile(prof);
        return this;
    }

    public ItemBuilder setPD(NamespacedKey key, String data) {
        im.getPersistentDataContainer().set(key, PersistentDataType.STRING, data);
        return this;
    }
}