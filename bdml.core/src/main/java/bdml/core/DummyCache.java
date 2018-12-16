package bdml.core;

import java.util.Iterator;
import java.util.ServiceLoader;

import bdml.services.Cache;

public class DummyCache {

	public static void main(String[] args) {
		// get Cache service instance
		System.out.println("Loading Cache implementations.");
		Iterator<Cache> it = ServiceLoader.load(Cache.class).iterator();
		if (it.hasNext())
			System.out.println("Cache implementation found.");
		else
			throw new RuntimeException("No bdml.core.services.Cache implementation available");
	}

}
