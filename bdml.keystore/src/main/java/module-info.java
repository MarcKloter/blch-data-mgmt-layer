module bdml.keyserver {
	requires bdml.core;

	provides bdml.core.service.KeyServer with bdml.keyserver.KeyServerAdapter;
}