package upc.edu.gessi.repo.util;

import lombok.Getter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SchemaIRI {
    private final ValueFactory factory = SimpleValueFactory.getInstance();

    private final IRI typeIRI = factory.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    private final IRI appIRI = factory.createIRI("https://schema.org/MobileApplication");
    private final IRI reviewIRI = factory.createIRI("https://schema.org/Review");
    private final IRI definedTermIRI = factory.createIRI("https://schema.org/DefinedTerm");
    private final IRI digitalDocumentIRI = factory.createIRI("https://schema.org/DigitalDocument");
    private final IRI developerIRI = factory.createIRI("https://schema.org/Organization");

    //App objects
    private final IRI identifierIRI = factory.createIRI("https://schema.org/identifier");
    private final IRI categoryIRI = factory.createIRI("https://schema.org/applicationCategory");
    private final IRI descriptionIRI = factory.createIRI("https://schema.org/description");
    private final IRI disambiguatingDescriptionIRI = factory.createIRI("https://schema.org/disambiguatingDescription");
    private final IRI textIRI = factory.createIRI("https://schema.org/text");
    private final IRI summaryIRI = factory.createIRI("https://schema.org/abstract");
    private final IRI featuresIRI = factory.createIRI("https://schema.org/keywords");

    private final IRI changelogIRI = factory.createIRI("https://schema.org/releaseNotes");
    private final IRI reviewsIRI = factory.createIRI("https://schema.org/review");
    private final IRI reviewDocumentIRI = factory.createIRI("https://schema.org/featureList");
    private final IRI sameAsIRI = factory.createIRI("https://schema.org/sameAs");
    private final IRI softwareVersionIRI = factory.createIRI("https://schema.org/softwareVersion");
    private final IRI dateModifiedIRI = factory.createIRI("https://schema.org/dateModified");

    //Review objects
    private final IRI reviewBodyIRI = factory.createIRI("https://schema.org/reviewBody");
    private final IRI datePublishedIRI = factory.createIRI("https://schema.org/datePublished");
    private final IRI authorIRI = factory.createIRI("https://schema.org/author");
    private final IRI reviewRatingIRI = factory.createIRI("https://schema.org/reviewRating");

    //Person objects
    private final IRI nameIRI = factory.createIRI("https://schema.org/name");

    //Feature object
    private IRI synonymIRI = factory.createIRI("https://schema.org/sameAs");
}
