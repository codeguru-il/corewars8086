package il.co.codeguru.corewars8086.war;

import il.co.codeguru.corewars8086.cli.Options;
import il.co.codeguru.corewars8086.utils.EventMulticaster;

import java.io.*;
import java.util.*;

import javax.swing.JOptionPane;


public class WarriorRepository {

    /**
     * Maximum initial code size of a single warrior
     */
    private final static int MAX_WARRIOR_SIZE = 512;

    private List<WarriorGroup> warriorGroups;
    private WarriorGroup zombieGroup;
    private Map<String, Integer> warriorNameToGroup;

    private EventMulticaster scoreEventsCaster;
    private ScoreEventListener scoreListener;
    
    private final Options options;

    public WarriorRepository(Options options) throws IOException {
        this(true, options);
    }
    public WarriorRepository(boolean shouldReadWarriorsFile, Options options) throws IOException {
      this.options = options;
        warriorNameToGroup = new HashMap<>();
        warriorGroups = new ArrayList<>();
        if (shouldReadWarriorsFile)
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
        List<String> names = new ArrayList<>();
        for (WarriorGroup group : warriorGroups) {
            names.add(group.getName());
        }
        return names.toArray(new String[0]);
    }

    /**
     * Reads all warrior data files from the warriors' directory.
     *
     * @throws IOException
     */
    private void readWarriorFiles() throws IOException {
        readWarriorsFileFromPath(options.warriorsDir);
        readZombiesFiles();
    }

    private void readZombiesFiles() throws IOException {
        readZombiesFileFromPath(options.zombiesDir);
    }

    public void readZombiesFileFromPath(String path) throws IOException {
        File zombieDirectory = new File(path);
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

            WarriorData data = readWarriorFile(file, WarriorType.ZOMBIE);
            zombieGroup.addWarrior(data);
        }
    }

    public void readWarriorsFileFromPath(String path) throws IOException {
        File warriorsDirectory = new File(path);

        fixFiles(warriorsDirectory);

        File[] warriorFiles = warriorsDirectory.listFiles();
        if (warriorFiles == null) {
            JOptionPane.showMessageDialog(null,
                    "Error - survivors directory (\"" + path + "\") not found");
            System.exit(1);
        }

        WarriorGroup currentGroup = null;
        // sort by filename
        Arrays.sort(warriorFiles, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));

        for (File file : warriorFiles) {
            if (file.isDirectory()) {
                continue;
            }

            String name = file.getName();

            if (name.endsWith("1")) {
                // start a new group!
                currentGroup = new WarriorGroup(name.substring(0, name.length() - 1));
                currentGroup.addWarrior(readWarriorFile(file, WarriorType.SURVIVOR_1));
                warriorNameToGroup.put(name, warriorGroups.size());
            } else if (name.endsWith("2")) {
                currentGroup.addWarrior(readWarriorFile(file, WarriorType.SURVIVOR_2));
                warriorNameToGroup.put(name, warriorGroups.size());
                warriorGroups.add(currentGroup);
                currentGroup = null;
            } else {
                currentGroup = new WarriorGroup(name);
                currentGroup.addWarrior(readWarriorFile(file, WarriorType.SURVIVOR));
                warriorNameToGroup.put(name, warriorGroups.size());
                warriorGroups.add(currentGroup);
                currentGroup = null;
            }
        }
    }

    private void fixFiles(File warriorsDirectory) {
        if (!warriorsDirectory.exists()) {
            throw new RuntimeException("Missing directory " + warriorsDirectory.getAbsolutePath());
        }
        File[] files = warriorsDirectory.listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".bin")) {
                File renameTo = new File(file.getPath().replace(".bin", ""));
                renameTo.delete();
                file.renameTo(renameTo);
            }
        }

        files = warriorsDirectory.listFiles();
        for (File file : files) {
            if (file.getName().contains("."))
                file.delete();
        }
    }

    private static WarriorData readWarriorFile(File file, WarriorType type) throws IOException {
        String warriorName = file.getName();

        int warriorSize = (int) file.length();
        if (warriorSize > MAX_WARRIOR_SIZE) {
            warriorSize = MAX_WARRIOR_SIZE;
        }

        byte[] warriorData = new byte[warriorSize];
        FileInputStream fis = new FileInputStream(file);
        int size = fis.read(warriorData);
        fis.close();

        if (size != warriorSize) {
            throw new IOException();
        }

        // Zombie H - runs at double speed
        if (type == WarriorType.ZOMBIE && warriorName.toLowerCase().endsWith("h")) {
            type = WarriorType.ZOMBIE_H;
        }

        return new WarriorData(warriorName, warriorData, type);
    }

    /**
     * @param groupIndices Required warrior groups indices.
     * @return the warrior groups corresponding to a given list of indices, and
     * the zombies group.
     */
    public WarriorGroup[] createGroupList(int[] groupIndices) {
        ArrayList<WarriorGroup> groupsList = new ArrayList<WarriorGroup>();

        // add requested warrior groups
        for (int groupIndex : groupIndices) {
          groupsList.add(warriorGroups.get(groupIndex));
        }

        // add zombies (if exist)
        if (zombieGroup != null) {
            groupsList.add(zombieGroup);
        }

        WarriorGroup[] groups = new WarriorGroup[groupsList.size()];
        groupsList.toArray(groups);
        return groups;
    }

    public void saveScoresToFile(String filename) {
        System.out.printf("Writing scores to file %s%n", filename);
        
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            PrintStream ps = new PrintStream(fos);
            ps.print("Groups:\n");
            for (WarriorGroup group : warriorGroups) {
                ps.print(group.getName() + "," + group.getGroupScore() + "\n");
            }
            ps.print("\nWarriors:\n");
            for (WarriorGroup group : warriorGroups) {
                List<Float> scores = group.getScores();
                List<WarriorData> data = group.getWarriors();
                for (int i = 0; i < scores.size(); i++) {
                    ps.print(data.get(i).getName() + "," + scores.get(i) + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getScores() {
        String str = "";
        List<WarriorGroup> sorted = new ArrayList<>(warriorGroups);
        sorted.sort((o1, o2) -> Float.compare(o2.getGroupScore(), o1.getGroupScore()));
        for (WarriorGroup group : sorted) {
            List<Float> scores = group.getScores();
            List<WarriorData> data = group.getWarriors();
            str += group.getName() + "," + group.getGroupScore();
            for (int i = 0; i < scores.size(); i++) {
                str += "," + data.get(i).getName() + "," + scores.get(i);
            }
            str += "\n";
        }
        return str;
    }

    public List<WarriorGroup> getWarriorGroups() {
        return warriorGroups;
    }
}
