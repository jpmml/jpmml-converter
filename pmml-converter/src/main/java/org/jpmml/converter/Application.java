/*
 * Copyright (c) 2022 Villu Ruusmann
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

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

abstract
public class Application {

	private Manifest manifest = null;


	protected Application(){
	}

	public Manifest getManifest(){

		if(this.manifest == null){
			this.manifest = loadManifest();
		}

		return this.manifest;
	}

	protected Manifest loadManifest(){
		Class<?> clazz = getClass();

		ClassLoader clazzLoader = clazz.getClassLoader();

		try(InputStream is = clazzLoader.getResourceAsStream("META-INF/MANIFEST.MF")){
			return new Manifest(is);
		} catch(IOException ioe){
			throw new RuntimeException(ioe);
		}
	}

	static
	public Application getInstance(){
		return Application.application;
	}

	static
	public void setInstance(Application application){
		Application.application = application;
	}

	private static Application application = null;
}