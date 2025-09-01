import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapLoader {
    private List<String> mapsFiles;
    public final int NUM_OF_LEVELS;

    /**
     * Loads level file paths from the given directory and sets the number of levels.
     * @param levelsPath the path to the directory containing level files
     */
    public MapLoader(String levelsPath) {
        loadLevelFiles(levelsPath);
        NUM_OF_LEVELS = mapsFiles.size();
    }

    /**
     * Loads and returns the map data for the specified level index as a list of strings.
     * @param levelIndex the index of the level to load
     * @return a list of strings representing the map data, or null if loading fails
     */
    public List<String> loadMap(int levelIndex) {
        List<String> levelData = null;
        if (levelIndex < mapsFiles.size()) {
            try {
                levelData = Files.readAllLines(Paths.get(mapsFiles.get(levelIndex)));
                return levelData;
            } catch (Exception e) {
                System.out.println("Program failed because of the level files at level " + levelIndex);
            }
        }
        return levelData;
    }


    /**
     * Loads all .txt level file paths from the specified directory into the levelFiles list.
     * @param levelsPath the path to the directory containing level files
     */
    private void loadLevelFiles(String levelsPath) {
        mapsFiles = new ArrayList<>();
        File dir = new File(levelsPath);
        try {
            if (!dir.exists() || !dir.isDirectory()) {
                throw new Exception("Invalid levels directory: " + levelsPath);
            }

            File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
            if (files == null || files.length == 0) {
                throw new Exception("No level files found in directory: " + levelsPath);
            }

            Arrays.sort(files);
            for (File file : files) {
                mapsFiles.add(file.getAbsolutePath());
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
