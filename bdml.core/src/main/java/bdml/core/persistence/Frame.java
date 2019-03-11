package bdml.core.persistence;

import java.util.List;

public class Frame {
    private List<AccessToken> access = null;
    private Document doc = null;

    //For Kryo
    private Frame(){}

    public Frame(List<AccessToken> access, Document doc) {
        this.access = access;
        this.doc = doc;
        for(AccessToken a: access) {
            if(doc.getVersion() != a.getVersion()){
                throw new IllegalArgumentException("temp");
            }
        }

    }

    public int getVersion() {
        return doc.getVersion();
    }

    public List<AccessToken> getAccess() {
        return access;
    }

    public Document getDocument() {
        return doc;
    }
}
