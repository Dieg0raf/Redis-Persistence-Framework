package com.ecs160;
import com.ecs160.persistence.Session;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.List;

public class MyApp {
    private final JsonParser jsonParser;
    private final ConfigManager configManager;
    private final Session curSession;
    private final ScannerManager scanManager;

    public MyApp(JsonParser jsonParser, ConfigManager configManager, Session curSession, ScannerManager scanManager) {
        this.jsonParser = jsonParser;
        this.configManager = configManager;
        this.curSession = curSession;
        this.scanManager = scanManager;
    }

    public static void main(String[] args) throws FileNotFoundException, URISyntaxException, NoSuchFieldException {
        JsonParser jsonParser = new JsonParser();
        ConfigManager configManager = new ConfigManager();
        Session curSession = new Session();
        ScannerManager scannManger = new ScannerManager(curSession);
        MyApp driver = new MyApp(jsonParser, configManager, curSession, scannManger);

        // run the whole program flow
        driver.run(args);
    }

    private void run(String[] args) {
        String filePath = getFilePath(args);
        List<Post> allPosts = parseJsonFile(filePath);

        // before starting program, clear db to restart
        this.curSession.clearDB();

        // add to session list
        for (Post post: allPosts) {
            curSession.add(post);
        }

        this.curSession.persistAll();
        this.scanManager.handleUserInput();
    }

    private String getFilePath(String[] args) {
        return this.configManager.getFilePathFromArgs(args);
    }

    private List<Post> parseJsonFile(String filePath) {
        return this.jsonParser.parseJson(filePath);
    }
}
