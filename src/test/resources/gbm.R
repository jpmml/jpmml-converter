library("caret")
library("gbm")

source("util.R")

auto = loadCsv("csv/AutoNA.csv")

auto_x = auto[, -ncol(auto)]
auto_y = auto[, ncol(auto)]

generateGBMFormulaAutoNA = function(){
	auto.formula = gbm(mpg ~ ., data = auto, interaction.depth = 3, shrinkage = 0.1, n.trees = 100)
	print(auto.formula)

	mpg = predict(auto.formula, newdata = auto, n.trees = 100)
	result = data.frame("mpg" = mpg)

	storeProtoBuf(auto.formula, "pb/GBMFormulaAutoNA.pb")
	storeCsv(result, "csv/GBMFormulaAutoNA.csv")
}

generateGBMFitAutoNA = function(){
	auto.fit = gbm.fit(x = auto_x, y = auto_y, distribution = "gaussian", interaction.depth = 3, shrinkage = 0.1, n.trees = 100)
	print(auto.fit)

	mpg = predict(auto.fit, newdata = auto, n.trees = 100)
	result = data.frame("y" = mpg)

	storeProtoBuf(auto.fit, "pb/GBMFitAutoNA.pb")
	storeCsv(result, "csv/GBMFitAutoNA.csv")
}

set.seed(42)

generateGBMFormulaAutoNA()
generateGBMFitAutoNA()

?predict.gbm
?predict.train

generateCaretGBMFormulaAutoNA = function(){
	auto.formula = train(mpg ~ ., data = auto, method = "gbm")
	print(auto.formula)

	mpg = predict(auto.formula, newdata = auto, na.action = na.pass)
	result = data.frame("y" = mpg)

	storeProtoBuf(auto.formula, "pb/CaretGBMFormulaAutoNA.pb")
	storeCsv(result, "csv/CaretGBMFormulaAutoNA.csv")
}

generateCaretGBMFitAutoNA = function(){
	auto.fit = train(x = auto_x, y = auto_y, method = "gbm")
	print(auto.fit)

	mpg = predict(auto.fit, newdata = auto)
	result = data.frame("y" = mpg)

	storeProtoBuf(auto.fit, "pb/CaretGBMFitAutoNA.pb")
	storeCsv(result, "csv/CaretGBMFitAutoNA.csv")
}

set.seed(42)

generateCaretGBMFormulaAutoNA()
generateCaretGBMFitAutoNA()