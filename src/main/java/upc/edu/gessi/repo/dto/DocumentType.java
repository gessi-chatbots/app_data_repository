package upc.edu.gessi.repo.dto;

public enum DocumentType {

    SUMMARY("summary"),
    DESCRIPTION("description"),
    CHANGELOG("changelog"),
    REVIEWS("reviews"),
    USER_ANNOTATED("user-annotated"),
    ALL("all");

    private final String name;

    DocumentType(String document) {
        this.name = document;
    }

    public String getName() {
        return name;
    }

    public static DocumentType getDocumentType(String name) {
        for(var documentType : DocumentType.values()) {
            if(documentType.getName().equals(name)) {
                return documentType;
            }
        }
        return null;
    }

}
