package upc.edu.gessi.repo.dto.graph;

import java.io.Serializable;

public class GraphApp  extends GraphNode implements Serializable {

    private String identifier;
    private String name;
    @Deprecated
    private String[] applicationCategory;

    @Deprecated
    public GraphApp(String nodeIdentifier, String identifier, String name, String[] applicationCategory) {
        super(nodeIdentifier);
        this.identifier = identifier;
        this.name = name;
        this.applicationCategory = applicationCategory;
    }

    public GraphApp(String nodeIdentifier, String identifier, String name) {
        super(nodeIdentifier);
        this.identifier = identifier;
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getApplicationCategory() {
        return applicationCategory;
    }

    public void setApplicationCategory(String[] applicationCategory) {
        this.applicationCategory = applicationCategory;
    }
}
