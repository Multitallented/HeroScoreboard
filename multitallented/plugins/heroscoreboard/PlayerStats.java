package multitallented.plugins.heroscoreboard;

/**
 *
 * @author Multitallented
 */
public class PlayerStats {
    private int kills=0;
    private int deaths=0;
    private String weapon;
    private String skill;
    //TODO add stuff for storage here.
    
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
    public String getWeapon() {
        return weapon;
    }
    public void setWeapon(String weapon) {
        this.weapon =  weapon;
    }
    public String getSkill() {
        return skill;
    }
    public void setSkill(String skill) {
        this.skill = skill;
    }
}
