package upc.edu.gessi.repo.controller;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import upc.edu.gessi.repo.domain.App;
import upc.edu.gessi.repo.service.AppFinder;
import upc.edu.gessi.repo.service.GraphDBService;

public class RepoController {

    @Autowired
    private AppFinder appFinder;

    @Autowired
    private GraphDBService dbConnection;

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
