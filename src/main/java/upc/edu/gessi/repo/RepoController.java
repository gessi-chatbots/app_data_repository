package upc.edu.gessi.repo;

import com.google.gson.Gson;
import upc.edu.gessi.repo.domain.App;

public class RepoController {

    private final AppFinder appFinder;
    private final DBConnection dbConnection;

    public RepoController(String url) {
        this.appFinder = new AppFinder(url);
        this.dbConnection = new DBConnection(url);

    }

    public String getAppInfo(String app) throws ClassNotFoundException, IllegalAccessException {
        App result = appFinder.retrieveAppByName(app);
        Gson g = new Gson();
        return g.toJson(result);
    }

    public void storeAppInfo(String appInfo, String appName) throws ClassNotFoundException {
        Gson g = new Gson();
        App app = g.fromJson(appInfo,App.class);
        dbConnection.insertApp(app,appName);
    }
}
