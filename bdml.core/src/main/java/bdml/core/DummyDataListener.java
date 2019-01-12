package bdml.core;

import bdml.services.api.helper.DataListener;

public class DummyDataListener implements DataListener {
    @Override
    public void update(String identifier) {
        System.out.println(identifier);
    }
}
