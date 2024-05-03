package upc.edu.gessi.repo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import upc.edu.gessi.repo.service.*;
import upc.edu.gessi.repo.service.AnalysisService;
import upc.edu.gessi.repo.service.AppDataScannerService;
import upc.edu.gessi.repo.service.DocumentService;
import upc.edu.gessi.repo.service.FeatureService;
import upc.edu.gessi.repo.service.InductiveKnowledgeService;
import upc.edu.gessi.repo.service.NLFeatureService;
import upc.edu.gessi.repo.service.ReviewService;
import upc.edu.gessi.repo.service.SimilarityService;

@Component
public class ServiceFactoryImpl implements ServiceFactory {

    private final AnalysisService analysisService;
    private final AppDataScannerService appDataScannerService;
    private final DocumentService documentService;
    private final FeatureService featureService;
    private final GraphDBServiceImpl graphDBServiceImpl;
    private final InductiveKnowledgeService inductiveKnowledgeService;
    private final MobileApplicationService mobileApplicationService;
    private final NLFeatureService nlFeatureService;
    private final ReviewService reviewService;
    private final SimilarityService similarityService;

    @Autowired
    public ServiceFactoryImpl(AnalysisService analysisService,
                              AppDataScannerService appDataScannerService,
                              DocumentService documentService,
                              FeatureService featureService,
                              GraphDBServiceImpl graphDBServiceImpl,
                              InductiveKnowledgeService inductiveKnowledgeService,
                              MobileApplicationService mobileApplicationService,
                              NLFeatureService nlFeatureService,
                              ReviewService reviewService,
                              SimilarityService similarityService) {
        this.analysisService = analysisService;
        this.appDataScannerService = appDataScannerService;
        this.documentService = documentService;
        this.featureService = featureService;
        this.graphDBServiceImpl = graphDBServiceImpl;
        this.inductiveKnowledgeService = inductiveKnowledgeService;
        this.mobileApplicationService = mobileApplicationService;
        this.nlFeatureService = nlFeatureService;
        this.reviewService = reviewService;
        this.similarityService = similarityService;
    }

    @Override
    public Object createService(Class<?> clazz) {
        if (clazz == AnalysisService.class) {
            return analysisService;
        } else if (clazz == AppDataScannerService.class) {
            return appDataScannerService;
        } else if (clazz == DocumentService.class) {
            return documentService;
        } else if (clazz == FeatureService.class) {
            return featureService;
        } else if (clazz == GraphDBServiceImpl.class) {
            return graphDBServiceImpl;
        } else if (clazz == InductiveKnowledgeService.class) {
            return inductiveKnowledgeService;
        } else if (clazz == MobileApplicationService.class) {
            return mobileApplicationService;
        } else if (clazz == NLFeatureService.class) {
            return nlFeatureService;
        } else if (clazz == ReviewService.class) {
            return reviewService;
        } else if (clazz == SimilarityService.class) {
            return similarityService;
        }
        throw new IllegalArgumentException("Not valid class: " + clazz.getName());
    }
}
