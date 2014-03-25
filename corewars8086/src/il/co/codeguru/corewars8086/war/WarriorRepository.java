package il.co.codeguru.corewars8086.war;

import il.co.codeguru.corewars8086.utils.EventMulticaster;

import java.io.*;
import java.util.*;

import javax.swing.JOptionPane;


public class WarriorRepository {

    /** Maximum initial code size of a single warrior */	
    private final static int MAX_WARRIOR_SIZE = 512;
    private static final String WARRIOR_DIRECTORY= "survivors";
    private static final String ZOMBIE_DIRECTORY= "zombies";

    private List<WarriorGroup> warriorGroups;
    private WarriorGroup zombieGroup;
    private Map<String,Integer> warriorNameToGroup;

    private EventMulticaster scoreEventsCaster;
    private ScoreEventListener scoreListener;

    public WarriorRepository() throws IOException {
        warriorNameToGroup = new HashMap<String,Integer>();
        warriorGroups = new ArrayList<WarriorGroup>();
        readWarriorFiles();

        scoreEventsCaster = new EventMulticaster(ScoreEventListener.class);
        scoreListener = (ScoreEventListener) scoreEventsCaster.getProxy();
    }

    public void addScoreEventListener(ScoreEventListener lis) {
        scoreEventsCaster.add(lis);
    }

    public void addScore(String name, float value) {
        Integer groupIndex = warriorNameToGroup.get(name);
        if (groupIndex == null) {// zombies
            return;
        }
        WarriorGroup group = warriorGroups.get(groupIndex);
        int subIndex = group.addScoreToWarrior(name, value);
        scoreListener.scoreChanged(name, value, groupIndex, subIndex);
    }

    public int getNumberOfGroups() {
        return warriorGroups.size();
    }

    public String[] getGroupNames() {
        List<String> names = new ArrayList<String>();
        for (WarriorGroup group : warriorGroups) {
            names.add(group.getName());
        }
        return names.toArray(new String[0]);
    }

    /**
     * Reads all warrior data files from the warriors' directory.
     * @throws IOException 
     */
    private void readWarriorFiles() throws IOException  {		
        File warriorsDirectory = new File(WARRIOR_DIRECTORY);
        File[] warriorFiles = warriorsDirectory.listFiles();
        if (warriorFiles == null) {
            JOptionPane.showMessageDialog(null,
                "Error - survivors directory (\"" +
                WARRIOR_DIRECTORY + "\") not found");
            System.exit(1);
        }

        WarriorGroup currentGroup = null;
        // sort by filename
        Arrays.sort(warriorFiles, new Comparator<File>() {
            public int compare(File o1, File o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
            }});

        for (File file : warriorFiles) {
            if (file.isDirectory()) {
                continue;
            }

            String name = file.getName();
            	
            WarriorData data = readWarriorFile(file);
            if (name.endsWith("1")) {
                // start a new group!
                currentGroup = new WarriorGroup(name.substring(0, name.length()-1));
                currentGroup.addWarrior(data);
                warriorNameToGroup.put(name, warriorGroups.size());
            } else if (name.endsWith("2")) {
                currentGroup.addWarrior(data);
                warriorNameToGroup.put(name, warriorGroups.size());
                warriorGroups.add(currentGroup);
                currentGroup = null;
            } else {
                currentGroup = new WarriorGroup(name);
                currentGroup.addWarrior(data);
                warriorNameToGroup.put(name, warriorGroups.size());
                warriorGroups.add(currentGroup);
                currentGroup = null;
            }
        }
        readZombies();
    }

    private void readZombies() throws IOException {
        File zombieDirectory = new File(ZOMBIE_DIRECTORY);
        File[] zombieFiles = zombieDirectory.listFiles();
        if (zombieFiles == null) {
            // no zombies!
            return;
        }
        zombieGroup = new WarriorGroup("ZoMbIeS");
        for (File file : zombieFiles) {
            if (file.isDirectory()) {
                continue;
            }

            WarriorData data = readWarriorFile(file);
            zombieGroup.addWarrior(data);
        }
    }

    private static WarriorData readWarriorFile(File filename) throws IOException {
        String warriorName = filename.getName();		

        int warriorSize = (int)filename.length();
        if (warriorSize > MAX_WARRIOR_SIZE) {
            warriorSize = MAX_WARRIOR_SIZE;
        }

        byte[] warriorData = new byte[warriorSize];		
        FileInputStream fis = new FileInputStream(filename);		
        int size = fis.read(warriorData);
        fis.close();

        if (size != warriorSize) {
            throw new IOException();
        }

        return new WarriorData(warriorName, warriorData);
    }

    /**
     * @return the warrior groups corresponding to a given list of indices, and
     * the zombies group.
     * 
     * @param groupIndices  Required warrior groups indices.
     */
    public WarriorGroup[] createGroupList(int[] groupIndices) {
        ArrayList<WarriorGroup> groupsList = new ArrayList<WarriorGroup>();

        // add requested warrior groups
        for (int i = 0; i < groupIndices.length; ++i) {
            groupsList.add(warriorGroups.get(groupIndices[i]));
        }

        // add zombies (if exist)
        if (zombieGroup != null) {
            groupsList.add(zombieGroup);
        }

        WarriorGroup[] groups = new WarriorGroup[groupsList.size()];
        groupsList.toArray(groups);
        return groups;
    }

    public void saveScoresToFile(String filename)
    {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            PrintStream ps = new PrintStream(fos);
            ps.print("Groups:\n");
            for (WarriorGroup group: warriorGroups) {
                ps.print(group.getName()+ "," + group.getGroupScore()+"\n");
            }
            ps.print("\nWarriors:\n");
            for (WarriorGroup group: warriorGroups) {
                List<Float> scores  = group.getScores();
                List<WarriorData> data = group.getWarriors();
                for (int i = 0; i < scores.size(); i++) {
                    ps.print(data.get(i).getName() + "," + scores.get(i)+"\n");
                }
            }
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getScores()
    {
    	String str = "";
    	for (WarriorGroup group: warriorGroups) {
            List<Float> scores  = group.getScores();
            List<WarriorData> data = group.getWarriors();
            for (int i = 0; i < scores.size(); i++) {
                str += data.get(i).getName() + "," + scores.get(i)+"\n";
            }
        }
    	return str;
    }
}