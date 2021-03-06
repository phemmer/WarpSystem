package de.codingair.warpsystem.spigot.features.warps.hiddenwarps;

import de.codingair.codingapi.serializable.SerializableLocation;
import de.codingair.codingapi.serializable.SerializableLocationHelper;
import de.codingair.warpsystem.spigot.features.warps.hiddenwarps.utils.actions.Action;
import de.codingair.warpsystem.spigot.features.warps.importfilter.WarpData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HiddenWarp {
    private String name;
    private String permission;
    private List<Action> actionList;
    private SerializableLocation location;

    private Date created;
    private Date lastChange;
    private String lastChanger;
    private int teleports;

    private double costs;

    public HiddenWarp(String s) throws ParseException {
        apply(s);
    }

    public HiddenWarp(WarpData data) throws IllegalStateException {
        this.name = data.getName();
        this.permission = data.getPermission();
        this.location = SerializableLocationHelper.buildSerializableLocation(data.getWorld(), data.getX(), data.getY(), data.getZ(), data.getYaw(), data.getPitch());

        this.actionList = new ArrayList<>();
        this.created = new Date();
        this.lastChange = new Date();
        this.lastChanger = "System";
        this.costs = 0;
        this.teleports = 0;
    }

    public HiddenWarp(Player player, String name, String permission) {
        this.name = name;
        this.permission = permission;
        this.location = new SerializableLocation(player.getLocation());

        this.created = new Date();
        this.lastChange = new Date();
        this.lastChanger = player.getName();
        this.teleports = 0;
        this.actionList = new ArrayList<>();

        this.costs = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public List<Action> getActionList() {
        return actionList;
    }

    public Location getLocation() {
        return location.getLocation();
    }

    public void setLocation(Location location) {
        this.location = new SerializableLocation(location);
    }

    public Date getCreated() {
        return created;
    }

    public Date getLastChange() {
        return lastChange;
    }

    public void setLastChange(Date lastChange) {
        this.lastChange = lastChange;
    }

    public String getLastChanger() {
        return lastChanger;
    }

    public void setLastChanger(String lastChanger) {
        this.lastChanger = lastChanger;
    }

    public int getTeleports() {
        return teleports;
    }

    public void increaseTeleports() {
        if(this.teleports == Integer.MAX_VALUE) return;
        this.teleports++;
    }

    public double getCosts() {
        return costs;
    }

    public void setCosts(double costs) {
        this.costs = costs;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();

        json.put("Name", this.name);
        json.put("Permission", this.permission);
        json.put("Actions", JSONArray.toJSONString(this.actionList));
        json.put("Location", this.location.toString());
        json.put("Created", this.created.getTime());
        json.put("LastChange", this.lastChange.getTime());
        json.put("LastChanger", this.lastChanger);
        json.put("Teleports", this.teleports);
        json.put("Costs", this.costs);

        return json.toJSONString();
    }

    private void apply(String s) throws ParseException {
        JSONObject json = (JSONObject) new JSONParser().parse(s);

        this.name = (String) json.get("Name");
        this.permission = json.get("Permission") == null ? null : (String) json.get("Permission");

        JSONArray array = (JSONArray) new JSONParser().parse((String) json.get("Actions"));
        this.actionList = new ArrayList<>();
        for(Object o : array) {
            String data = (String) o;
            try {
                this.actionList.add(Action.read(data));
            } catch(ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                throw new IllegalStateException("Couldn't revive action of HiddenWarp '" + this.name + "'.");
            }
        }

        this.location = SerializableLocationHelper.buildSerializableLocation((String) json.get("Location"));
        this.created = new Date(Long.parseLong(json.get("Created") + ""));
        this.lastChange = new Date(Long.parseLong(json.get("LastChange") + ""));
        this.lastChanger = (String) json.get("LastChanger");
        this.teleports = Integer.parseInt(json.get("Teleports") + "");
        this.costs = Double.parseDouble(json.get("Costs") + "");
    }

    public void apply(HiddenWarp warp) {
        try {
            this.apply(warp.toString());
        } catch(ParseException e) {
            e.printStackTrace();
        }
    }

    public HiddenWarp clone() {
        try {
            return new HiddenWarp(toString());
        } catch(ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
