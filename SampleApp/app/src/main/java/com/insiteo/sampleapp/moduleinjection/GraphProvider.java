package com.insiteo.sampleapp.moduleinjection;

import dagger.ObjectGraph;

public class GraphProvider {

	private static ObjectGraph applicationGraph;

	public static void injectApplicationGraph(Object target) {
		getApplicationGraph().inject(target);
	}

	public static ObjectGraph getApplicationGraph() {
		if (applicationGraph == null) {
			applicationGraph = ObjectGraph.create(new InsiteoModule());
		}
		return applicationGraph;
	}
}
