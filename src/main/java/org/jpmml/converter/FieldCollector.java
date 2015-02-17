/*
 * Copyright (c) 2015 Villu Ruusmann
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

import java.util.Set;

import com.beust.jcommander.internal.Sets;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.Visitable;
import org.jpmml.model.visitors.AbstractVisitor;

abstract
public class FieldCollector extends AbstractVisitor {

	private Set<FieldName> fields = Sets.newHashSet();


	@Override
	public void applyTo(Visitable visitable){
		this.fields.clear();

		super.applyTo(visitable);
	}

	public void addField(FieldName field){
		this.fields.add(field);
	}

	public Set<FieldName> getFields(){
		return this.fields;
	}
}