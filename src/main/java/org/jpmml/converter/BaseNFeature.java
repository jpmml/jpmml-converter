/*
 * Copyright (c) 2020 Villu Ruusmann
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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.SetMultimap;
import org.dmg.pmml.Apply;
import org.dmg.pmml.DataType;
import org.dmg.pmml.Expression;
import org.dmg.pmml.Field;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldRef;
import org.dmg.pmml.NormDiscrete;
import org.dmg.pmml.PMMLFunctions;
import org.jpmml.model.ToStringHelper;

public class BaseNFeature extends Feature implements HasDerivedName {

	private int base = -1;

	private int index = -1;

	private SetMultimap<Integer, ?> values = null;


	public BaseNFeature(PMMLEncoder encoder, Field<?> field, int base, int index, SetMultimap<Integer, ?> values){
		this(encoder, field.getName(), field.getDataType(), base, index, values);
	}

	public BaseNFeature(PMMLEncoder encoder, Feature feature, int base, int index, SetMultimap<Integer, ?> values){
		this(encoder, feature.getName(), feature.getDataType(), base, index, values);
	}

	public BaseNFeature(PMMLEncoder encoder, FieldName name, DataType dataType, int base, int index, SetMultimap<Integer, ?> values){
		super(encoder, name, dataType);

		setBase(base);
		setIndex(index);

		setValues(values);
	}

	@Override
	public FieldName getDerivedName(){
		return FieldName.create("base" + getBase() + "(" + (getName()).getValue() + ", " + getIndex() + ")");
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		FieldName name = getName();
		DataType dataType = getDataType();
		int base = getBase();
		SetMultimap<Integer, ?> values = getValues();

		Supplier<Expression> expressionSupplier = () -> {
			Map<Integer, ? extends Collection<?>> valueMap = values.asMap();

			if(base == 2){
				Collection<?> categories = valueMap.get(1);

				if(categories != null && categories.size() == 1){
					Object category = Iterables.getOnlyElement(categories);

					return new NormDiscrete(name, category);
				}
			}

			Apply apply = null;

			Apply prevIfApply = null;

			Collection<? extends Map.Entry<Integer, ? extends Collection<?>>> entries = valueMap.entrySet();

			entries = entries.stream()
				.sorted((left, right) -> Integer.compare(left.getKey(), right.getKey()))
				.filter(entry -> (entry.getKey() > 0))
				.collect(Collectors.toList());

			for(Map.Entry<Integer, ? extends Collection<?>> entry : entries){
				Integer baseValue = entry.getKey();
				Collection<?> categories = entry.getValue();

				Apply valueApply = PMMLUtil.createApply((categories.size() == 1 ? PMMLFunctions.EQUAL : PMMLFunctions.ISIN), new FieldRef(name));

				for(Object category : categories){
					valueApply.addExpressions(PMMLUtil.createConstant(category, dataType));
				}

				Apply ifApply = PMMLUtil.createApply(PMMLFunctions.IF, valueApply)
					.addExpressions(PMMLUtil.createConstant(baseValue));

				if(apply == null){
					apply = ifApply;
				} // End if

				if(prevIfApply != null){
					prevIfApply.addExpressions(ifApply);
				}

				prevIfApply = ifApply;
			}

			if(apply == null){
				return PMMLUtil.createConstant(0);
			} else

			{
				prevIfApply.addExpressions(PMMLUtil.createConstant(0));

				return apply;
			}
		};

		return toContinuousFeature(getDerivedName(), DataType.INTEGER, expressionSupplier);
	}

	public Set<?> getValues(Predicate<Integer> predicate){
		SetMultimap<Integer, ?> values = getValues();

		Map<Integer, ? extends Collection<?>> valueMap = values.asMap();

		Set<Object> result = new LinkedHashSet<>();

		Set<? extends Map.Entry<Integer, ? extends Collection<?>>> entries = valueMap.entrySet();

		entries.stream()
			.sorted((left, right) -> Integer.compare(left.getKey(), right.getKey()))
			.filter((entry) -> predicate.test(entry.getKey()))
			.map((entry) -> entry.getValue())
			.forEach(result::addAll);

		return result;
	}

	@Override
	public int hashCode(){
		int result = super.hashCode();

		result = (31 * result) + Objects.hash(this.getBase());
		result = (31 * result) + Objects.hash(this.getIndex());
		result = (31 * result) + Objects.hash(this.getValues());

		return result;
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof BaseNFeature){
			BaseNFeature that = (BaseNFeature)object;

			return super.equals(object) && Objects.equals(this.getBase(), that.getBase()) && Objects.equals(this.getIndex(), that.getIndex()) && Objects.equals(this.getValues(), that.getValues());
		}

		return false;
	}

	@Override
	protected ToStringHelper toStringHelper(){
		return super.toStringHelper()
			.add("base", getBase())
			.add("index", getIndex())
			.add("values", getValues());
	}

	public int getBase(){
		return this.base;
	}

	private void setBase(int base){
		this.base = base;
	}

	public int getIndex(){
		return this.index;
	}

	private void setIndex(int index){
		this.index = index;
	}

	public SetMultimap<Integer, ?> getValues(){
		return this.values;
	}

	private void setValues(SetMultimap<Integer, ?> values){
		this.values = Objects.requireNonNull(values);
	}
}