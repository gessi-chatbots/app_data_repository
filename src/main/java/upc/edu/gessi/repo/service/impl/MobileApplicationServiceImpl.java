package upc.edu.gessi.repo.service.impl;


import org.apache.poi.xdgf.util.Util;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationBasicDataDTO;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationFullDataDTO;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.repository.impl.MobileApplicationRepository;
import upc.edu.gessi.repo.service.MobileApplicationService;
import upc.edu.gessi.repo.util.Utils;

import java.util.ArrayList;
import java.util.List;


@Service
@Lazy
public class MobileApplicationServiceImpl implements MobileApplicationService {

    private final MobileApplicationRepository mobileApplicationRepository;

    @Autowired
    public MobileApplicationServiceImpl(final MobileApplicationRepository appRepository) {
        mobileApplicationRepository = appRepository;
    }

    @Override
    public List<MobileApplicationBasicDataDTO> getAllBasicData() throws ApplicationNotFoundException {
        return mobileApplicationRepository.findAllApplicationsBasicData();
    }

    @Override
    public List<GraphApp> getAllApps() {
        return mobileApplicationRepository.getAllApps();
    }

    @Override
    public void addFeatures(final MobileApplicationFullDataDTO mobileApplicationFullDataDTO,
                            final IRI sub,
                            final List<Statement> statements) {
        mobileApplicationRepository.addFeature(mobileApplicationFullDataDTO, sub, statements);
    }

    @Override
    public List<MobileApplicationFullDataDTO> create(List<MobileApplicationFullDataDTO> dtos) {
        List<MobileApplicationFullDataDTO> insertedApps = new ArrayList<>();
        for (MobileApplicationFullDataDTO mobileApplicationFullDataDTO : dtos) {
            // Insert App
            insertedApps.add(mobileApplicationRepository.insertApp(mobileApplicationFullDataDTO));
            // Insert App reviews
            // TODO

            // Insert app Features
            // TODO

            // Insert App digital Documents
            // TODO
        }
        return insertedApps;
    }

    @Override
    public MobileApplicationFullDataDTO get(String id) throws ObjectNotFoundException {
        return mobileApplicationRepository.findByName(Utils.sanitizeString(id));
    }

    @Override
    public List<MobileApplicationFullDataDTO> getListed(List<String> ids) throws ObjectNotFoundException {
        List<MobileApplicationFullDataDTO> mobileApplicationFullDataDTOS = new ArrayList<>();
        for (String id : ids) {
            try {
                mobileApplicationFullDataDTOS.add(mobileApplicationRepository.findByName(id));
            } catch (ObjectNotFoundException ignored) {
                // do nothing, refactor in future to error tracking
            }

        }
        return mobileApplicationFullDataDTOS;
    }

    @Override
    public List<MobileApplicationFullDataDTO> getAllPaginated(Integer page, Integer size) throws ObjectNotFoundException, ClassNotFoundException, IllegalAccessException {
        return mobileApplicationRepository.findAllPaginated(page, size);
    }

    @Override
    public List<MobileApplicationBasicDataDTO> getAllBasicDataPaginated(Integer page, Integer size) throws ObjectNotFoundException, ClassNotFoundException, IllegalAccessException {
        return mobileApplicationRepository.findAllBasicDataPaginated(page, size);
    }
    @Override
    public List<MobileApplicationFullDataDTO> getAll() {
        try {
            return mobileApplicationRepository.findAll();
        } catch (ApplicationNotFoundException applicationNotFoundException) {
            return new ArrayList<>();
        }

    }

    @Override
    public MobileApplicationFullDataDTO update(MobileApplicationFullDataDTO entity) {
        return null;
    }

    @Override
    public Void delete() {
        return null;
    }

    /*
        public List<String> getResultsContaining(String text) {
            RepositoryConnection repoConnection = repository.getConnection();
            String query = "PREFIX gessi: <http://gessi.upc.edu/app/> SELECT ?x ?y ?z " +
                                                                        "WHERE {?x ?y ?z .FILTER regex(str(?z), \""+text+"\")}" ;
            TupleQuery tupleQuery = repoConnection.prepareTupleQuery(query);
            List<String> resultList = new ArrayList<>();
            TupleQueryResult result = tupleQuery.evaluate();
            while (result.hasNext()) {  // iterate over the result
                BindingSet bindingSet = result.next();
                Value valueOfX = bindingSet.getValue("x");
                Value valueOfY = bindingSet.getValue("y");
                Value valueOfZ = bindingSet.getValue("z");
                resultList.add(valueOfZ.stringValue());
            }
            return resultList;
        }
    */
}
