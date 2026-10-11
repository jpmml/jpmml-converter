/*
 * Copyright (c) 2026 Villu Ruusmann
 *
 * This file is part of JPMML-Converter
 *
 * JPMML-Converter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-Converter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-Converter.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.converter;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ApplicationTest {

	@Test
	public void loadManifest(){
		// The test application is not packaged into a JAR file at the time when unit tests are running.
		assertNull(Application.getManifestURL(TestApplication.class));
		assertNotNull(Application.getDefaultManifestURL(TestApplication.class));

		Application application = new TestApplication();

		Manifest applicationManifest = application.getManifest();

		Attributes applicationAttributes = applicationManifest.getMainAttributes();
		assertFalse(applicationAttributes.isEmpty());

		Manifest iterablesManifest = Application.loadManifest(Iterables.class);

		Attributes iterablesAttributes = iterablesManifest.getMainAttributes();
		assertFalse(iterablesAttributes.isEmpty());
		assertEquals("com.google.guava", iterablesAttributes.getValue("Bundle-SymbolicName"));

		assertNotEquals(applicationManifest, iterablesManifest);
	}

	static
	private class TestApplication extends Application {
	}
}