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
package org.jpmml.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.jpmml.model.ToStringHelper;
import org.jpmml.model.ValueUtil;

public class CategoricalLabel extends Label {

	private List<String> values = null;


	public CategoricalLabel(DataField dataField){
		this(dataField.getName(), dataField.getDataType(), formatValues(PMMLUtil.getValues(dataField)));
	}

	public CategoricalLabel(FieldName name, DataType dataType, List<String> values){
		super(name, dataType);

		setValues(values);
	}

	@Override
	public CategoricalLabel toRenamedLabel(FieldName name){
		return new CategoricalLabel(name, getDataType(), getValues());
	}

	@Override
	public CategoricalLabel toAnonymousLabel(){
		return (CategoricalLabel)super.toAnonymousLabel();
	}

	@Override
	public int hashCode(){
		return (31 * super.hashCode()) + Objects.hashCode(this.getValues());
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof CategoricalLabel){
			CategoricalLabel that = (CategoricalLabel)object;

			return super.equals(object) && Objects.equals(this.getValues(), that.getValues());
		}

		return false;
	}

	@Override
	protected ToStringHelper toStringHelper(){
		return super.toStringHelper()
			.add("values", getValues());
	}

	public int size(){
		List<String> values = getValues();

		return values.size();
	}

	public String getValue(int index){
		List<String> values = getValues();

		return values.get(index);
	}

	public List<String> getValues(){
		return this.values;
	}

	private void setValues(List<String> values){
		this.values = Objects.requireNonNull(values);
	}

	static
	public List<String> formatValues(List<?> values){

		if(values == null){
			return null;
		}

		return new ArrayList<>(Lists.transform(values, ValueUtil::toString));
	}
}