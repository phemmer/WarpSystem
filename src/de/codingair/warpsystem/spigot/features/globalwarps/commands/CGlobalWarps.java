package de.codingair.warpsystem.spigot.features.globalwarps.commands;

import de.codingair.codingapi.server.commands.BaseComponent;
import de.codingair.codingapi.server.commands.CommandBuilder;
import de.codingair.codingapi.server.commands.CommandComponent;
import de.codingair.codingapi.server.commands.MultiCommandComponent;
import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.features.FeatureType;
import de.codingair.warpsystem.spigot.features.globalwarps.guis.GGlobalWarpList;
import de.codingair.warpsystem.spigot.features.globalwarps.managers.GlobalWarpManager;
import de.codingair.warpsystem.spigot.base.language.Example;
import de.codingair.warpsystem.spigot.base.language.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CGlobalWarps extends CommandBuilder {
    public CGlobalWarps() {
        super("GlobalWarps", new BaseComponent(WarpSystem.PERMISSION_MODIFY_GLOBAL_WARPS) {
            @Override
            public void noPermission(CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("No_Permission", new Example("GER", "&cDu hast keine Berechtigungen für diese Aktion!"), new Example("ENG", "&cYou don't have permissions for that action!"), new Example("FRE", "&cDésolé mais vous ne possédez la permission pour exécuter cette action!")));
            }

            @Override
            public void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Only_For_Players"));
            }

            @Override
            public void unknownSubCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("GlobalWarp_Help", new Example("ENG", "&7Use: &e/GlobalWarp <create, delete>"), new Example("GER", "&7Benutze: &e/GlobalWarp <create, delete>")));
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("GlobalWarp_Help", new Example("ENG", "&7Use: &e/GlobalWarp <create, delete>"), new Example("GER", "&7Benutze: &e/GlobalWarp <create, delete>")));
                return false;
            }
        }, true);

        getBaseComponent().addChild(new CommandComponent("create") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("GlobalWarp_Create_Help", new Example("ENG", "&7Use: &e/GlobalWarp create <name>"), new Example("GER", "&7Benutze: &e/GlobalWarp create <Name>")));
                return false;
            }
        });

        getComponent("create").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, List<String> suggestions) {
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Player player = (Player) sender;
                ((GlobalWarpManager) WarpSystem.getInstance().getDataManager().getManager(FeatureType.GLOBAL_WARPS)).create(argument, player.getLocation(), new Callback<Boolean>() {
                    @Override
                    public void accept(Boolean created) {
                        if(created) {
                            sender.sendMessage(Lang.getPrefix() + Lang.get("GlobalWarp_Created", new Example("ENG", "&7The GlobalWarp '&b%GLOBAL_WARP%&7' was &acreated successfully&7."), new Example("GER", "&7Der GlobalWarp '&b%GLOBAL_WARP%&7' wurde &aerfolgreich erstellt&7.")).replace("%GLOBAL_WARP%", argument));
                        } else {
                            sender.sendMessage(Lang.getPrefix() + Lang.get("GlobalWarp_Create_Name_Already_Exists", new Example("ENG", "&7The GlobalWarp '&b%GLOBAL_WARP%&7' &calready exists&7."), new Example("GER", "&7Der GlobalWarp '&b%GLOBAL_WARP%&7' &cexistiert bereits&7.")).replace("%GLOBAL_WARP%", argument));
                        }
                    }
                });
                return false;
            }
        });

        getBaseComponent().addChild(new CommandComponent("delete") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("GlobalWarp_Delete_Help", new Example("ENG", "&7Use: &e/GlobalWarp delete <name>"), new Example("GER", "&7Benutze: &e/GlobalWarp delete <Name>")));
                return false;
            }
        });

        getComponent("delete").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, List<String> suggestions) {
                suggestions.addAll(((GlobalWarpManager) WarpSystem.getInstance().getDataManager().getManager(FeatureType.GLOBAL_WARPS)).getGlobalWarps().keySet());
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                ((GlobalWarpManager) WarpSystem.getInstance().getDataManager().getManager(FeatureType.GLOBAL_WARPS)).delete(argument, new Callback<Boolean>() {
                    @Override
                    public void accept(Boolean deleted) {
                        if(deleted) {
                            String name = ((GlobalWarpManager) WarpSystem.getInstance().getDataManager().getManager(FeatureType.GLOBAL_WARPS)).getCaseCorrectlyName(argument);

                            sender.sendMessage(Lang.getPrefix() + Lang.get("GlobalWarp_Deleted", new Example("ENG", "&7The GlobalWarp '&b%GLOBAL_WARP%&7' was &cdeleted&7."), new Example("GER", "&7Der GlobalWarp '&b%GLOBAL_WARP%&7' wurde &cgelöscht&7.")).replace("%GLOBAL_WARP%", name));
                        } else {
                            sender.sendMessage(Lang.getPrefix() + Lang.get("GlobalWarp_Not_Exists", new Example("ENG", "&7The GlobalWarp '&b%GLOBAL_WARP%&7' &cdoes not exist&7."), new Example("GER", "&7Der GlobalWarp '&b%GLOBAL_WARP%&7' &cexistiert nicht&7.")).replace("%GLOBAL_WARP%", argument));
                        }
                    }
                });
                return false;
            }
        });

        getBaseComponent().addChild(new CommandComponent("list") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                new GGlobalWarpList((Player) sender).open();
                return false;
            }
        });
    }
}