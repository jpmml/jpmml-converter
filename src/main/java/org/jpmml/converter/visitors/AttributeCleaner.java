/*
 * Copyright (c) 2019 Villu Ruusmann
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;

import org.dmg.pmml.PMMLObject;
import org.dmg.pmml.VisitorAction;
import org.jpmml.model.ReflectionUtil;
import org.jpmml.model.visitors.AbstractVisitor;

/**
 * <p>
 * A Visitor that sets redundant attribute values to <code>null</code>.
 * </p>
 */
public class AttributeCleaner extends AbstractVisitor {

	@Override
	public VisitorAction visit(PMMLObject object){
		Map<Field, Method> getterMethods = ReflectionUtil.getGetterMethods(object.getClass());

		Collection<Map.Entry<Field, Method>> entries = getterMethods.entrySet();
		for(Map.Entry<Field, Method> entry : entries){
			Field field = entry.getKey();
			Method getterMethod = entry.getValue();

			XmlAttribute attribute = field.getAnnotation(XmlAttribute.class);
			if(attribute == null || attribute.required()){
				continue;
			}

			Object fieldValue = ReflectionUtil.getFieldValue(field, object);
			if(fieldValue != null){
				Object getterMethodValue = ReflectionUtil.getGetterMethodValue(getterMethod, object);

				if(Objects.equals(fieldValue, getterMethodValue)){
					ReflectionUtil.setFieldValue(field, object, null);

					Object defaultGetterMethodValue = ReflectionUtil.getGetterMethodValue(getterMethod, object);
					if(defaultGetterMethodValue == null || !Objects.equals(fieldValue, defaultGetterMethodValue)){
						ReflectionUtil.setFieldValue(field, object, fieldValue);
					}
				}
			}
		}

		return super.visit(object);
	}
}