library("randomForest")

source("util.R")

audit = loadCsv("../csv/Audit.csv")
audit$Adjusted = as.factor(audit$Adjusted)

generateRandomForestFormulaAudit = function(){
	audit.formula = randomForest(Adjusted ~ ., data = audit, ntree = 7)
	print(audit.formula)

	adjusted = predict(audit.formula, newdata = audit)
	result = data.frame("Adjusted" = adjusted)

	storeProtoBuf(audit.formula, "../pb/RandomForestFormulaAudit.pb")
	storeCsv(result, "../csv/RandomForestFormulaAudit.csv")
}

generateRandomForestAudit = function(){
	audit.matrix = randomForest(x = audit[, -ncol(audit)], y = audit[, ncol(audit)], ntree = 7)
	print(audit.matrix)

	adjusted = predict(audit.matrix, newdata = audit)
	result = data.frame("_target" = adjusted)

	storeProtoBuf(audit.matrix, "../pb/RandomForestAudit.pb")
	storeCsv(result, "../csv/RandomForestAudit.csv")
}

set.seed(42)

generateRandomForestFormulaAudit()
generateRandomForestAudit()

wine_quality = loadCsv("../csv/WineQuality.csv")

wine_quality_x = wine_quality[, -ncol(wine_quality)]
wine_quality_y = wine_quality[, ncol(wine_quality)]

generateRandomForestFormulaWineQuality = function(){
	wine_quality.formula = randomForest(quality ~ ., data = wine_quality, ntree = 7)
	print(wine_quality.formula)

	quality = predict(wine_quality.formula, newdata = wine_quality)
	result = data.frame("quality" = quality)

	storeProtoBuf(wine_quality.formula, "../pb/RandomForestFormulaWineQuality.pb")
	storeCsv(result, "../csv/RandomForestFormulaWineQuality.csv")
}

generateRandomForestWineQuality = function(){
	wine_quality.matrix = randomForest(x = wine_quality_x, y = wine_quality_y, ntree = 7)
	print(wine_quality.matrix)

	quality = predict(wine_quality.matrix, newdata = wine_quality)
	result = data.frame("_target" = quality)

	storeProtoBuf(wine_quality.matrix, "../pb/RandomForestWineQuality.pb")
	storeCsv(result, "../csv/RandomForestWineQuality.csv")
}

set.seed(42)

generateRandomForestFormulaWineQuality()
generateRandomForestWineQuality()

wine_color = loadCsv("../csv/WineColor.csv")
wine_color$color = as.factor(wine_color$color)

wine_color_x = wine_color[, -ncol(wine_color)]
wine_color_y = wine_color[, ncol(wine_color)]

generateRandomForestFormulaWineColor = function(){
	wine_color.formula = randomForest(color ~ ., data = wine_color, ntree = 7)
	print(wine_color.formula)

	color = predict(wine_color.formula, newdata = wine_color)
	result = data.frame("color" = color)

	storeProtoBuf(wine_color.formula, "../pb/RandomForestFormulaWineColor.pb")
	storeCsv(result, "../csv/RandomForestFormulaWineColor.csv")
}

generateRandomForestWineColor = function(){
	wine_color.matrix = randomForest(x = wine_color_x, y = wine_color_y, ntree = 7)
	print(wine_color.matrix)

	color = predict(wine_color.matrix, newdata = wine_color)
	result = data.frame("_target" = color)

	storeProtoBuf(wine_color.matrix, "../pb/RandomForestWineColor.pb")
	storeCsv(result, "../csv/RandomForestWineColor.csv")
}

set.seed(42)

generateRandomForestFormulaWineColor()
generateRandomForestWineColor()