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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.dmg.pmml.PMMLObject;

abstract
public class PMMLObjectTransformerBuilder<E extends PMMLObject> {

	private Map<String, Object> options = new LinkedHashMap<>();


	abstract
	public PMMLObjectTransformer<E> build();

	public boolean hasOption(String name){
		return this.options.containsKey(name);
	}

	public Object getOption(String name){

		if(this.options.containsKey(name)){
			return this.options.get(name);
		}

		Map<String, ?> defaults = getDefaults();

		return defaults.get(name);
	}

	public PMMLObjectTransformerBuilder<E> putOption(String name, Object value){
		this.options.put(name, value);

		return this;
	}

	public PMMLObjectTransformerBuilder<E> putOptions(Map<String, ?> options){
		this.options.putAll(options);

		return this;
	}

	/**
	 * Tests whether the resolved option value is set and not false.
	 */
	protected boolean isEnabled(String name){
		Object value = getOption(name);

		return (value != null) && !Objects.equals(Boolean.FALSE, value);
	}

	/**
	 * Tests whether the explicitly set option value is set and not false. Ignores defaults and {@link #getOption(String)} overrides.
	 */
	protected boolean isExplicitlyEnabled(String name){
		Object value = this.options.get(name);

		return (value != null) && !Objects.equals(Boolean.FALSE, value);
	}

	protected Map<String, ?> getDefaults(){
		return Collections.emptyMap();
	}
}