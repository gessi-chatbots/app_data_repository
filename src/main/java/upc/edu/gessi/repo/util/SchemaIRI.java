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

    // Types
    private final IRI typeIRI = factory.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    private final IRI appIRI = factory.createIRI("https://schema.org/MobileApplication");
    private final IRI reviewIRI = factory.createIRI("https://schema.org/Review");
    private final IRI textIRI = factory.createIRI("https://schema.org/Text");
    private final IRI definedTermIRI = factory.createIRI("https://schema.org/DefinedTerm");
    private final IRI digitalDocumentIRI = factory.createIRI("https://schema.org/DigitalDocument");
    private final IRI developerIRI = factory.createIRI("https://schema.org/Organization");
    private final IRI creativeWorkIRI = factory.createIRI("https://schema.org/CreativeWork");
    private final IRI reactActionIRI = factory.createIRI("https://schema.org/ReactAction");

    //Mobile App properties
    private final IRI identifierIRI = factory.createIRI("https://schema.org/identifier");
    private final IRI categoryIRI = factory.createIRI("https://schema.org/applicationCategory");
    private final IRI descriptionIRI = factory.createIRI("https://schema.org/description");
    private final IRI disambiguatingDescriptionIRI = factory.createIRI("https://schema.org/disambiguatingDescription");
    private final IRI additionalPropertyIRI = factory.createIRI("https://schema.org/additionalProperty");

    private final IRI textPropertyIRI = factory.createIRI("https://schema.org/text");
    private final IRI summaryIRI = factory.createIRI("https://schema.org/abstract");
    private final IRI keywordIRI = factory.createIRI("https://schema.org/keywords");

    private final IRI changelogIRI = factory.createIRI("https://schema.org/releaseNotes");
    private final IRI reviewsIRI = factory.createIRI("https://schema.org/review");
    private final IRI reviewDocumentIRI = factory.createIRI("https://schema.org/featureList");
    private final IRI sameAsIRI = factory.createIRI("https://schema.org/sameAs");
    private final IRI softwareVersionIRI = factory.createIRI("https://schema.org/softwareVersion");
    private final IRI dateModifiedIRI = factory.createIRI("https://schema.org/dateModified");

    //Software App properties
    private final IRI softwareApplicationIRI = factory.createIRI("https://schema.org/softwareApplication");

    //Review properties
    private final IRI reviewBodyIRI = factory.createIRI("https://schema.org/reviewBody");
    private final IRI datePublishedIRI = factory.createIRI("https://schema.org/datePublished");
    private final IRI authorIRI = factory.createIRI("https://schema.org/author");
    private final IRI reviewRatingIRI = factory.createIRI("https://schema.org/reviewRating");
    private final IRI hasPartIRI = factory.createIRI("https://schema.org/hasPart");
    private final IRI potentialActionIRI = factory.createIRI("https://schema.org/potentialAction");
    //Person objects
    private final IRI nameIRI = factory.createIRI("https://schema.org/name");

    //Feature object
    private IRI synonymIRI = factory.createIRI("https://schema.org/sameAs");
}
