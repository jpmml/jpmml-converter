/*
 * Copyright (c) 2018 Villu Ruusmann
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
package org.jpmml.converter.visitors;

import org.dmg.pmml.Extension;
import org.dmg.pmml.HasExtensions;
import org.dmg.pmml.PMMLObject;
import org.jpmml.converter.PMMLUtil;
import org.jpmml.model.visitors.AbstractVisitor;

abstract
public class AbstractExtender extends AbstractVisitor {

	private String name = null;


	public AbstractExtender(String name){
		setName(name);
	}

	public <E extends PMMLObject & HasExtensions<E>> void addExtension(E object, String value){
		String name = getName();

		Extension extension = PMMLUtil.createExtension(name, value);

		object.addExtensions(extension);
	}

	public String getName(){
		return this.name;
	}

	private void setName(String name){
		this.name = name;
	}
}