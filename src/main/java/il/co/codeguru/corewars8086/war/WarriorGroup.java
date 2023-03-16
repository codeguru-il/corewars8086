package il.co.codeguru.corewars8086.war;

import java.util.ArrayList;
import java.util.List;

public class WarriorGroup {
    private String name;
    private ArrayList<WarriorData> warriorData;
    private List<Float> scores;
    private float groupScore;
    private WarriorGroup myZomboxGroup;

    public WarriorGroup(String name) {
        this.name = name;
        warriorData = new ArrayList<>();
        scores = new ArrayList<>();
    }

    public void addWarrior(WarriorData data) {
        warriorData.add(data);
        scores.add(0f);
    }
    public WarriorGroup getMyZomboxGroup(){ return myZomboxGroup;};
    public void setMyZomboxGroup(WarriorGroup group){ myZomboxGroup = group;};
    public List<WarriorData> getWarriors() {
        return warriorData;
    }

    public List<Float> getScores() {
        return scores;
    }

    public String getName() {
        return name;
    }

    public float getGroupScore() {
        return groupScore;
    }

    public int addScoreToWarrior(String name, float value) {
        // find this warrior
        int i;
        for (i = 0; i < warriorData.size(); i++) {
            if (warriorData.get(i).getName().equals(name)) {
                scores.set(i, scores.get(i) + value);
                break;
            }
        }
        groupScore += value;
        return i;
    }
}