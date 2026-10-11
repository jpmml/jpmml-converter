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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.jar.Manifest;

abstract
public class Application {

	private Manifest manifest = null;


	protected Application(){
	}

	public Manifest getManifest(){

		if(this.manifest == null){
			Class<?> clazz = getClass();

			this.manifest = loadManifest(clazz);
		}

		return this.manifest;
	}

	static
	protected Manifest loadManifest(Class<?> clazz){
		URL url = getManifestURL(clazz);

		if(url != null){

			try {
				return readManifest(url);
			} catch(IOException ioe){
				// Ignored
			}
		}

		url = getDefaultManifestURL(clazz);
		if(url != null){

			try {
				return readManifest(url);
			} catch(IOException ioe){
				throw new RuntimeException(ioe);
			}
		}

		return new Manifest();
	}

	static
	protected URL getManifestURL(Class<?> clazz){
		URL url = clazz.getResource("/" + (clazz.getName()).replace('.', '/') + ".class");

		if(url != null && Objects.equals("jar", url.getProtocol())){

			try {
				return new URL(url, "/" + Application.MANIFEST_NAME);
			} catch(MalformedURLException mue){
				throw new RuntimeException(mue);
			}
		}

		return null;
	}

	static
	protected URL getDefaultManifestURL(Class<?> clazz){
		return clazz.getResource("/" + Application.MANIFEST_NAME);
	}

	static
	private Manifest readManifest(URL url) throws IOException {

		try(InputStream is = url.openStream()){
			return new Manifest(is);
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

	private static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
}