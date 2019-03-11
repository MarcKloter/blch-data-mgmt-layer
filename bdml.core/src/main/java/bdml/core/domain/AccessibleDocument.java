package bdml.core.domain;

import bdml.core.persistence.Document;
import bdml.core.persistence.Frame;


public class AccessibleDocument {
    private final Capability capability;
    private final Document doc;

    public AccessibleDocument(Document doc, Capability capability) {
        this.capability = capability;
        this.doc = doc;
    }


    public Capability getCapability() {
        return capability;
    }

    public DataIdentifier getIdentifier() {
        return capability.getIdentifier();
    }

    public Document getDocument() {
        return doc;
    }

}
