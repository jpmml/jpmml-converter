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

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingUtil {

	private LoggingUtil(){
	}

	static
	public void configureConsole(){
		Logger rootLogger = Logger.getLogger("");

		configureConsole(rootLogger, Level.INFO);
	}

	static
	public void configureConsole(Logger logger, Level level){
		Level loggerLevel = logger.getLevel();
		if((loggerLevel == null) || (loggerLevel.intValue() > level.intValue())){
			logger.setLevel(level);
		}

		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(level);
		consoleHandler.setFormatter(new ConsoleFormatter());
		logger.addHandler(consoleHandler);
	}
}