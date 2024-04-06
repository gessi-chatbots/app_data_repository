package upc.edu.gessi.repo.dto;

import java.io.Serializable;

public class Document implements Serializable {

    private String propertyOf;
    private DocumentType documentType;
    private String text;

    public Document(String propertyOf, DocumentType documentType, String text) {
        this.propertyOf = propertyOf;
        this.documentType = documentType;
        this.text = text;
    }

    public String getPropertyOf() {
        return propertyOf;
    }

    public void setPropertyOf(String propertyOf) {
        this.propertyOf = propertyOf;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
