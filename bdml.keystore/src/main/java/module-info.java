module bdml.keyserver {
	requires bdml.services;

	provides bdml.services.KeyServer with bdml.keyserver.KeyServerAdapter;
}