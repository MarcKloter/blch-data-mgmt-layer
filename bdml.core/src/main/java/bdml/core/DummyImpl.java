package bdml.core;

import bdml.cache.CacheImpl;
import bdml.services.Cache;
import bdml.services.api.Core;

public class DummyImpl {

	public static void main(String[] args) {
		//Cache cache = new CacheImpl();
		Core core = new CoreImpl();
		core.createAccount("myPassword");
	}

}
