/*
 * Copyright (c) 2016 Villu Ruusmann
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

import org.dmg.pmml.Visitable;
import org.jpmml.model.visitors.FieldResolver;

abstract
public class DeepFieldResolver extends FieldResolver {

	private FieldDependencyResolver fieldDependencyResolver = null;


	@Override
	public void reset(){
		super.reset();

		this.fieldDependencyResolver = null;
	}

	@Override
	public void applyTo(Visitable visitable){
		FieldDependencyResolver fieldDependencyResolver = new FieldDependencyResolver();
		fieldDependencyResolver.applyTo(visitable);

		setFieldDependencyResolver(fieldDependencyResolver);

		super.applyTo(visitable);
	}

	public FieldDependencyResolver getFieldDependencyResolver(){

		if(this.fieldDependencyResolver == null){
			throw new IllegalStateException();
		}

		return this.fieldDependencyResolver;
	}

	private void setFieldDependencyResolver(FieldDependencyResolver fieldDependencyResolver){

		if(this.fieldDependencyResolver != null){
			throw new IllegalStateException();
		}

		this.fieldDependencyResolver = fieldDependencyResolver;
	}
}