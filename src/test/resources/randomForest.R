library("caret")
library("randomForest")

source("util.R")

audit = loadCsv("csv/Audit.csv")
audit$Adjusted = as.factor(audit$Adjusted)

audit_x = audit[, -ncol(audit)]
audit_y = audit[, ncol(audit)]

generateRandomForestFormulaAudit = function(){
	audit.formula = randomForest(Adjusted ~ ., data = audit, ntree = 7)
	print(audit.formula)

	adjusted = predict(audit.formula, newdata = audit)

	storeProtoBuf(audit.formula, "pb/RandomForestFormulaAudit.pb")
	storeCsv(data.frame("Adjusted" = adjusted), "csv/RandomForestFormulaAudit.csv")
}

generateRandomForestAudit = function(){
	audit.matrix = randomForest(x = audit_x, y = audit_y, ntree = 7)
	print(audit.matrix)

	adjusted = predict(audit.matrix, newdata = audit_x)

	storeProtoBuf(audit.matrix, "pb/RandomForestAudit.pb")
	storeCsv(data.frame("_target" = adjusted), "csv/RandomForestAudit.csv")
}

set.seed(42)

generateRandomForestFormulaAudit()
generateRandomForestAudit()

generateCaretRandomForestFormulaAuditMatrix = function(){
	audit.formula = train(Adjusted ~ ., data = audit, method = "rf", ntree = 7)
	print(audit.formula)

	adjusted = predict(audit.formula, newdata = audit)

	storeProtoBuf(audit.formula, "pb/CaretRandomForestFormulaAuditMatrix.pb")
	storeCsv(data.frame("_target" = adjusted), "csv/CaretRandomForestFormulaAuditMatrix.csv")
}

generateCaretRandomForestAudit = function(){
	audit.matrix = train(x = audit_x, y = audit_y, method = "rf", ntree = 7)
	print(audit.matrix)

	adjusted = predict(audit.matrix, newdata = audit_x)

	storeProtoBuf(audit.matrix, "pb/CaretRandomForestAudit.pb")
	storeCsv(data.frame("_target" = adjusted), "csv/CaretRandomForestAudit.csv")
}

set.seed(42)

generateCaretRandomForestFormulaAuditMatrix()
generateCaretRandomForestAudit()

auto = loadCsv("csv/Auto.csv")

auto_x = auto[, -ncol(auto)]
auto_y = auto[, ncol(auto)]

generateRandomForestFormulaAuto = function(){
	auto.formula = randomForest(mpg ~ ., data = auto, ntree = 7)
	print(auto.formula)

	mpg = predict(auto.formula, newdata = auto)

	storeProtoBuf(auto.formula, "pb/RandomForestFormulaAuto.pb")
	storeCsv(data.frame("mpg" = mpg), "csv/RandomForestFormulaAuto.csv")
}

generateRandomForestAuto = function(){
	auto.matrix = randomForest(x = auto_x, y = auto_y, ntree = 7)
	print(auto.matrix)

	mpg = predict(auto.matrix, newdata = auto_x)

	storeProtoBuf(auto.matrix, "pb/RandomForestAuto.pb")
	storeCsv(data.frame("_target" = mpg), "csv/RandomForestAuto.csv")
}

set.seed(42)

generateRandomForestFormulaAuto()
generateRandomForestAuto()

generateCaretRandomForestFormulaAuto = function(){
	auto.formula = train(mpg ~ ., data = auto, method = "rf", ntree = 7)
	print(auto.formula)

	mpg = predict(auto.formula, newdata = auto)

	storeProtoBuf(auto.formula, "pb/CaretRandomForestFormulaAuto.pb")
	storeCsv(data.frame("_target" = mpg), "csv/CaretRandomForestFormulaAuto.csv")
}

generateCaretRandomForestAuto = function(){
	auto.matrix = train(x = auto_x, y = auto_y, method = "rf", ntree = 7)
	print(auto.matrix)

	mpg = predict(auto.matrix, newdata = auto_x)

	storeProtoBuf(auto.matrix, "pb/CaretRandomForestAuto.pb")
	storeCsv(data.frame("_target" = mpg), "csv/CaretRandomForestAuto.csv")
}

set.seed(42)

generateCaretRandomForestFormulaAuto()
generateCaretRandomForestAuto()

wine_quality = loadCsv("csv/WineQuality.csv")

wine_quality_x = wine_quality[, -ncol(wine_quality)]
wine_quality_y = wine_quality[, ncol(wine_quality)]

generateRandomForestFormulaWineQuality = function(){
	wine_quality.formula = randomForest(quality ~ ., data = wine_quality, ntree = 7)
	print(wine_quality.formula)

	quality = predict(wine_quality.formula, newdata = wine_quality)

	storeProtoBuf(wine_quality.formula, "pb/RandomForestFormulaWineQuality.pb")
	storeCsv(data.frame("quality" = quality), "csv/RandomForestFormulaWineQuality.csv")
}

generateRandomForestWineQuality = function(){
	wine_quality.matrix = randomForest(x = wine_quality_x, y = wine_quality_y, ntree = 7)
	print(wine_quality.matrix)

	quality = predict(wine_quality.matrix, newdata = wine_quality_x)

	storeProtoBuf(wine_quality.matrix, "pb/RandomForestWineQuality.pb")
	storeCsv(data.frame("_target" = quality), "csv/RandomForestWineQuality.csv")
}

set.seed(42)

generateRandomForestFormulaWineQuality()
generateRandomForestWineQuality()

wine_color = loadCsv("csv/WineColor.csv")
wine_color$color = as.factor(wine_color$color)

wine_color_x = wine_color[, -ncol(wine_color)]
wine_color_y = wine_color[, ncol(wine_color)]

generateRandomForestFormulaWineColor = function(){
	wine_color.formula = randomForest(color ~ ., data = wine_color, ntree = 7)
	print(wine_color.formula)

	color = predict(wine_color.formula, newdata = wine_color)

	storeProtoBuf(wine_color.formula, "pb/RandomForestFormulaWineColor.pb")
	storeCsv(data.frame("color" = color), "csv/RandomForestFormulaWineColor.csv")
}

generateRandomForestWineColor = function(){
	wine_color.matrix = randomForest(x = wine_color_x, y = wine_color_y, ntree = 7)
	print(wine_color.matrix)

	color = predict(wine_color.matrix, newdata = wine_color_x)

	storeProtoBuf(wine_color.matrix, "pb/RandomForestWineColor.pb")
	storeCsv(data.frame("_target" = color), "csv/RandomForestWineColor.csv")
}

set.seed(42)

generateRandomForestFormulaWineColor()
generateRandomForestWineColor()