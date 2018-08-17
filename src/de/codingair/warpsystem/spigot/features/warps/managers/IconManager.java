package de.codingair.warpsystem.spigot.features.warps.managers;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.serializable.SerializableLocation;
import de.codingair.codingapi.server.Color;
import de.codingair.codingapi.tools.Location;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.warpsystem.spigot.WarpSystem;
import de.codingair.warpsystem.spigot.commands.CWarp;
import de.codingair.warpsystem.spigot.commands.CWarps;
import de.codingair.warpsystem.spigot.features.FeatureType;
import de.codingair.warpsystem.spigot.features.globalwarps.guis.affiliations.GlobalWarp;
import de.codingair.warpsystem.spigot.features.warps.guis.affiliations.Category;
import de.codingair.warpsystem.spigot.features.warps.guis.affiliations.DecoIcon;
import de.codingair.warpsystem.spigot.features.warps.guis.affiliations.Warp;
import de.codingair.warpsystem.spigot.features.warps.guis.affiliations.utils.Action;
import de.codingair.warpsystem.spigot.features.warps.guis.affiliations.utils.ActionIconHelper;
import de.codingair.warpsystem.spigot.features.warps.guis.affiliations.utils.ActionObject;
import de.codingair.warpsystem.spigot.features.warps.guis.affiliations.utils.Icon;
import de.codingair.warpsystem.spigot.features.warps.importfilter.CategoryData;
import de.codingair.warpsystem.spigot.features.warps.importfilter.WarpData;
import de.codingair.warpsystem.utils.Manager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class IconManager implements Manager {
    private static ItemBuilder STANDARD_ITEM() {
        return new ItemBuilder(Material.GRASS);
    }

    private List<Warp> warps = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private List<GlobalWarp> globalWarps = new ArrayList<>();
    private List<DecoIcon> decoIcons = new ArrayList<>();

    private int userSize = 54;
    private int adminSize = 54;
    private String adminPermission = "WarpSystem.Admin";

    public static IconManager getInstance() {
        return ((IconManager) WarpSystem.getInstance().getDataManager().getManager(FeatureType.WARPS));
    }

    public boolean load() {
        if(WarpSystem.getInstance().getFileManager().getFile("ActionIcons") == null) WarpSystem.getInstance().getFileManager().loadFile("ActionIcons", "/Memory/");

        //Load
        boolean success = true;

        WarpSystem.log("  > Loading Icons");
        ActionIconHelper.load = true;

        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("Config");
        FileConfiguration config = file.getConfig();
        this.userSize = config.getInt("WarpSystem.GUI.User.Size", 54);
        switch(this.userSize) {
            case 9:
            case 18:
            case 27:
            case 36:
            case 45:
            case 54:
                break;
            default:
                this.userSize = 54;
                config.set("WarpSystem.GUI.User.Size", 54);
                file.saveConfig();
                break;
        }

        this.adminSize = config.getInt("WarpSystem.GUI.Admin.Size", 54);
        switch(this.adminSize) {
            case 9:
            case 18:
            case 27:
            case 36:
            case 45:
            case 54:
                break;
            default:
                this.adminSize = 54;
                config.set("WarpSystem.GUI.Admin.Size", 54);
                file.saveConfig();
                break;
        }

        this.adminPermission = config.getString("WarpSystem.GUI.Admin.Permission", "WarpSystem.Admin");

        this.warps.clear();
        this.categories.clear();
        this.globalWarps.clear();
        this.decoIcons.clear();
        ActionIconHelper.CONVERTED_ICONS = 0;

        file = WarpSystem.getInstance().getFileManager().getFile("ActionIcons");
        config = file.getConfig();

        WarpSystem.log("    > Loading Categories");
        List<String> categories = config.getStringList("Categories");
        for(String s : categories) {
            Category category = ActionIconHelper.fromString(s);

            if(category != null) {
                if(category.getName().contains("@")) category.setName(category.getName().replace("@", "(at)"));
                this.categories.add(category);
            } else success = false;
        }

        WarpSystem.log("    > Loading Warps");
        List<String> warps = config.getStringList("Warps");
        for(String s : warps) {
            Warp warp = ActionIconHelper.fromString(s);

            if(warp != null) {
                if(warp.getName().contains("@")) warp.setName(warp.getName().replace("@", "(at)"));
                this.warps.add(warp);
            } else success = false;
        }

        WarpSystem.log("      > Check each Category of all Warps");
        for(Warp warp : this.warps) {
            if(warp.getCategory() == null) continue;
            if(!existsCategory(warp.getCategory().getName())) {
                this.categories.add(warp.getCategory());
            }
        }

        WarpSystem.log("    > Loading GlobalWarps");
        List<String> gWarps = config.getStringList("GlobalWarps");
        for(String s : gWarps) {
            GlobalWarp warp = ActionIconHelper.fromString(s);

            if(warp != null) {
                if(warp.getName().contains("@")) warp.setName(warp.getName().replace("@", "(at)"));
                this.globalWarps.add(warp);
            } else success = false;
        }

        WarpSystem.log("    > Loading Deco");
        List<String> decoIcons = config.getStringList("DecoIcons");
        for(String s : decoIcons) {
            DecoIcon deco = ActionIconHelper.fromString(s);

            if(deco != null) this.decoIcons.add(deco);
            else success = false;
        }

        WarpSystem.log("    > " + ActionIconHelper.CONVERTED_ICONS + " Icon(s) imported");

        ActionIconHelper.load = false;

        //Import old
        if(WarpSystem.getInstance().isOld()) {
            WarpSystem.log("    > Import old icons");

            WarpSystem.getInstance().getFileManager().loadFile("Categories", "Memory/");
            WarpSystem.getInstance().getFileManager().loadFile("Warps", "Memory/");

            ConfigFile oldFile = WarpSystem.getInstance().getFileManager().getFile("Categories");
            FileConfiguration oldConfig = oldFile.getConfig();

            for(String key : oldConfig.getKeys(false)) {
                Category category = new Category(key, ImportHelper.getItem(oldConfig.getString(key + ".Item")), oldConfig.getInt(key + ".Slot"), oldConfig.getString(key + ".Permission", null));

                category.setItem(new ItemBuilder(category.getItem()).setHideStandardLore(true).setAmount(1).setName("§b§n" + category.getName()).setLore(oldConfig.getStringList(key + ".Lore")).getItem());

                this.categories.add(category);
            }

            oldFile = WarpSystem.getInstance().getFileManager().getFile("Warps");
            oldConfig = oldFile.getConfig();

            for(String key : oldConfig.getKeys(false)) {
                Warp warp = new Warp(key, ImportHelper.getItem(oldConfig.getString(key + ".Item")), oldConfig.getInt(key + ".Slot"), oldConfig.getString(key + ".Permission", null), getCategory(oldConfig.getString(key + ".Category", null))
                        , new ActionObject(Action.TELEPORT_TO_WARP, new SerializableLocation(ImportHelper.stringToLoc(oldConfig.getString(key + ".Location")))));

                warp.setItem(new ItemBuilder(warp.getItem()).setHideStandardLore(true).setAmount(1).setName("§b" + warp.getName()).setLore(oldConfig.getStringList(key + ".Lore")).getItem());

                this.warps.add(warp);
            }

            WarpSystem.getInstance().getFileManager().getFile("Categories").delete();
            WarpSystem.getInstance().getFileManager().getFile("Warps").delete();
        }

        if(WarpSystem.getInstance().getFileManager().getFile("Config").getConfig().getBoolean("WarpSystem.Functions.Warps", true)) {
            CWarp cWarp = new CWarp();
            CWarps cWarps = new CWarps();

            WarpSystem.getInstance().getCommands().add(cWarp);
            WarpSystem.getInstance().getCommands().add(cWarps);

            cWarp.register(WarpSystem.getInstance());
            cWarps.register(WarpSystem.getInstance());
        }

        return success;
    }

    public void save(boolean saver) {
        //Save
        if(!saver) WarpSystem.log("  > Saving Icons");

        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("ActionIcons");
        FileConfiguration config = file.getConfig();

        if(!saver) WarpSystem.log("    > Saving Warps");
        List<String> warps = new ArrayList<>();
        for(Warp warp : this.warps) {
            warps.add(ActionIconHelper.toString(warp));
        }

        if(!saver) WarpSystem.log("    > Saving Categories");
        List<String> categories = new ArrayList<>();
        for(Category category : this.categories) {
            categories.add(ActionIconHelper.toString(category));
        }

        if(!saver) WarpSystem.log("    > Saving GlobalWarps");
        List<String> gWarps = new ArrayList<>();
        for(GlobalWarp warp : this.globalWarps) {
            gWarps.add(ActionIconHelper.toString(warp));
        }

        if(!saver) WarpSystem.log("    > Saving Deco");
        List<String> decoIcons = new ArrayList<>();
        for(DecoIcon deco : this.decoIcons) {
            decoIcons.add(ActionIconHelper.toString(deco));
        }

        config.set("Warps", warps);
        config.set("Categories", categories);
        config.set("GlobalWarps", gWarps);
        config.set("DecoIcons", decoIcons);
        file.saveConfig();
    }

    private int getNextFreeSlot(Category category) {
        int slot = 0;
        List<Integer> unavailable = new ArrayList<>();
        unavailable.add(0);
        unavailable.add(8);
        unavailable.add(45);
        unavailable.add(53);

        boolean available;

        do {
            available = true;

            if(slot > 53) break;

            if(unavailable.contains(slot)) {
                slot++;
                available = false;
            } else {
                if(category == null) {
                    for(Category c : this.categories) {
                        if(c.getSlot() == slot) {
                            slot++;
                            available = false;
                            break;
                        }
                    }
                }

                for(Warp warp : getWarps(category)) {
                    if(warp.getSlot() == slot) {
                        slot++;
                        available = false;
                        break;
                    }
                }

                for(GlobalWarp warp : getGlobalWarps(category)) {
                    if(warp.getSlot() == slot) {
                        slot++;
                        available = false;
                        break;
                    }
                }

                for(DecoIcon deco : getDecoIcons(category)) {
                    if(deco.getSlot() == slot) {
                        slot++;
                        available = false;
                        break;
                    }
                }
            }
        } while(!available);

        if(available) return slot;
        else return -999;
    }

    public boolean importCategoryData(CategoryData categoryData) {
        int slot = getNextFreeSlot(null);

        if(slot == -999) return false;
        if(this.existsCategory(categoryData.getName())) return false;

        Category c = new Category(categoryData.getName(), STANDARD_ITEM().setName(categoryData.getName()).getItem(), slot, categoryData.getPermission());
        this.categories.add(c);

        boolean result = true;

        for(WarpData warpData : categoryData.getWarps()) {
            if(!importWarpData(warpData)) result = false;
        }

        return result;
    }

    public boolean importWarpData(WarpData warpData) {
        if(warpData.getCategory() != null && !existsCategory(warpData.getCategory())) return false;
        Category category = warpData.getCategory() == null ? null : getCategory(warpData.getCategory());

        int slot = getNextFreeSlot(category);

        if(slot == -999) return false;
        if(this.existsWarp(warpData.getName(), category)) return false;

        Location loc = new Location(new org.bukkit.Location(Bukkit.getWorld(warpData.getWorld()), warpData.getX(), warpData.getY(), warpData.getZ(), warpData.getYaw(), warpData.getPitch()));

        Warp warp = new Warp(warpData.getName(), STANDARD_ITEM().setName(warpData.getName()).getItem(), slot, warpData.getPermission(), category, new ActionObject(Action.TELEPORT_TO_WARP, new SerializableLocation(loc)));
        this.warps.add(warp);
        return true;
    }

    public boolean existsWarp(String name, Category category) {
        if(name == null) return false;
        name = Color.removeColor(name);

        return getWarp(name, category) != null;
    }

    public Warp getWarp(String name, Category category) {
        if(name == null) return null;
        name = Color.removeColor(name);

        for(Warp warp : getWarps(category)) {
            if(warp.getNameWithoutColor().equalsIgnoreCase(name)) return warp;
        }

        return null;
    }

    public boolean existsCategory(String name) {
        if(name == null) return false;
        name = Color.removeColor(name);

        return getCategory(name) != null;
    }

    public boolean existsGlobalWarp(String name) {
        if(name == null) return false;

        return getGlobalWarp(name) != null;
    }

    public Category getCategory(String name) {
        if(name == null) return null;
        name = Color.removeColor(name);

        for(Category c : this.categories) {
            if(c.getNameWithoutColor().equalsIgnoreCase(name)) return c;
        }

        return null;
    }

    public List<Warp> getWarps() {
        return warps;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public List<Warp> getWarps(Category category) {
        List<Warp> icons = new ArrayList<>();

        for(Warp icon : this.warps) {
            if((icon.getCategory() == null && category == null) || ((icon.getCategory() != null && category != null) && icon.getCategory().getName().equals(category.getName()))) icons.add(icon);
        }

        return icons;
    }

    public GlobalWarp getGlobalWarp(String name) {
        for(GlobalWarp icon : this.globalWarps) {
            if(icon.getName().equalsIgnoreCase(name)) return icon;
        }

        return null;
    }

    public List<GlobalWarp> getGlobalWarps(Category category) {
        List<GlobalWarp> icons = new ArrayList<>();

        for(GlobalWarp icon : this.globalWarps) {
            if((icon.getCategory() == null && category == null) || ((icon.getCategory() != null && category != null) && icon.getCategory().getName().equals(category.getName()))) icons.add(icon);
        }

        return icons;
    }

    public List<DecoIcon> getDecoIcons(Category category) {
        List<DecoIcon> icons = new ArrayList<>();

        for(DecoIcon icon : this.decoIcons) {
            if((icon.getCategory() == null && category == null) || ((icon.getCategory() != null && category != null) && icon.getCategory().getName().equals(category.getName()))) icons.add(icon);
        }

        return icons;
    }

    public void remove(Icon icon) {
        if(icon instanceof Category) {
            Category category = (Category) icon;
            List<Warp> warps = getWarps(category);

            for(Warp warp : warps) {
                remove(warp);
            }

            this.categories.remove(icon);
        } else if(icon instanceof Warp) {
            this.warps.remove(icon);
        } else if(icon instanceof GlobalWarp) {
            this.globalWarps.remove(icon);
        } else if(icon instanceof DecoIcon) {
            this.decoIcons.remove(icon);
        }
    }

    public List<GlobalWarp> getGlobalWarps() {
        return globalWarps;
    }

    public List<DecoIcon> getDecoIcons() {
        return decoIcons;
    }

    public int getUserSize() {
        return userSize;
    }

    public void setUserSize(int userSize) {
        this.userSize = userSize;
    }

    public int getAdminSize() {
        return adminSize;
    }

    public void setAdminSize(int adminSize) {
        this.adminSize = adminSize;
    }

    public String getAdminPermission() {
        return adminPermission;
    }

    public void setAdminPermission(String adminPermission) {
        this.adminPermission = adminPermission;
    }
}