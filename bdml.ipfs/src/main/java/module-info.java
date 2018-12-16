module bdml.ipfs {
	requires bdml.services;
	
	exports bdml.ipfs;

	provides bdml.services.IPFS with bdml.ipfs.IPFSAdapter;
}