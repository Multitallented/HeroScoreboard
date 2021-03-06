package multitallented.plugins.heroscoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Multitallented
 */
public class PlayerStats {
    private int kills=0;
    private int deaths=0;
    private Map<String, Integer> nemeses = new HashMap<String, Integer>();
    private Map<String, Integer> weapons = new HashMap<String, Integer>();
    private double points = 0;
    private int killstreak = 0;
    private int highestKillstreak = 0;
    private int karma = 0;

    public void setKarma(int karma) {
        this.karma = karma;
    }
    public int getKarma() {
        return this.karma;
    }

    public void setHighestKillstreak(int highestKillstreak) {
        this.highestKillstreak = highestKillstreak;
    }
    
    public int getHighestKillstreak() {
        return highestKillstreak;
    }
    
    public int getKills() {
        return kills;
    }
    public void setKills(int kills) {
        this.kills = kills;
    }
    public int getDeaths() {
        return deaths;
    }
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
    public double getPoints() {
        return points;
    }
    public void setPoints(double pts) {
        this.points = pts;
    }
    public int getKillstreak() {
        return killstreak;
    }
    public void setKillstreak(int killstreak) {
        this.killstreak = killstreak;
    }
    
    public String getWeapon() {
        String bestWeapon = "None";
        int i = 0;
        for (String s : weapons.keySet()) {
            if (i < Math.max(i,weapons.get(s))) {
                i = weapons.get(s);
                bestWeapon = s;
            }
        }
        return bestWeapon;
    }
    public void addWeapon(String weapon) {
        if (weapons.containsKey(weapon)) {
            weapons.put(weapon, weapons.get(weapon) + 1);
        } else {
            weapons.put(weapon, 1);
        }
    }
    public void setWeapon(HashMap<String, Integer> weapons) {
        this.weapons = weapons;
    }
    public String getNemesis() {
        String bestNemesis = "None";
        int i = 0;
        for (String s : nemeses.keySet()) {
            if (i < Math.max(i,nemeses.get(s))) {
                i = nemeses.get(s);
                bestNemesis = s;
            }
        }
        return bestNemesis;
    }
    public ArrayList<String> getNemeses() {
        ArrayList<String> tempArray = new ArrayList<String>();
        for (String s : nemeses.keySet()) {
            tempArray.add(s + "," + nemeses.get(s));
        }
        return tempArray;
    }
    public ArrayList<String> getWeapons() {
        ArrayList<String> tempArray = new ArrayList<String>();
        for (String s : weapons.keySet()) {
            tempArray.add(s + "," + weapons.get(s));
        }
        return tempArray;
    }
    public void addNemesis(String nemesis) {
        if (nemeses.containsKey(nemesis)) {
            nemeses.put(nemesis, nemeses.get(nemesis) + 1);
        } else {
            nemeses.put(nemesis, 1);
        }
    }
    public void setNemesis(HashMap<String, Integer> nemeses) {
        this.nemeses = nemeses;
    }
}
