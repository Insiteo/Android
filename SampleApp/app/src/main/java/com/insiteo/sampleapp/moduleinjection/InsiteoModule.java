package com.insiteo.sampleapp.moduleinjection;

import com.insiteo.lbs.Insiteo;
import com.insiteo.lbs.common.auth.entities.ISSite;
import com.insiteo.lbs.common.auth.entities.ISUser;
import com.insiteo.sampleapp.InsiteoController;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
		injects = InsiteoController.class
)
public class InsiteoModule {
	@Provides
	@Singleton
	Insiteo provideInsiteo() {
		return Insiteo.getInstance();
	}

	@Provides
	@Singleton
	ISUser provideISUser() {
		return Insiteo.getCurrentUser();
	}

	@Provides
	@Singleton
	ISSite provideISSite() {
		return Insiteo.getCurrentSite();
	}
}