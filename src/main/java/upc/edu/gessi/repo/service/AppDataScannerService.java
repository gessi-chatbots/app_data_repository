package upc.edu.gessi.repo.service;

import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationFullDataDTO;
import upc.edu.gessi.repo.dto.graph.GraphApp;

public interface AppDataScannerService {
    MobileApplicationFullDataDTO scanApp(GraphApp app, int daysFromLastUpdate);
}
