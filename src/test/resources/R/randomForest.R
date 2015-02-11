library("randomForest")

source("util.R")

if(!exists("loadWineQuality", mode = "function")){
	source("winequality.R")
}

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
