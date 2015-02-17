library("caret")
library("gbm")

source("util.R")

auto = loadAutoCsv("csv/AutoNA.csv")

auto_x = auto[, -ncol(auto)]
auto_y = auto[, ncol(auto)]

generateGBMFormulaAutoNA = function(){
	auto.formula = gbm(mpg ~ ., data = auto, interaction.depth = 3, shrinkage = 0.1, n.trees = 100)
	print(auto.formula)

	mpg = predict(auto.formula, newdata = auto, n.trees = 100)

	storeProtoBuf(auto.formula, "pb/GBMFormulaAutoNA.pb")
	storeCsv(data.frame("mpg" = mpg), "csv/GBMFormulaAutoNA.csv")
}

generateGBMFitAutoNA = function(){
	auto.fit = gbm.fit(x = auto_x, y = auto_y, distribution = "gaussian", interaction.depth = 3, shrinkage = 0.1, n.trees = 100)
	print(auto.fit)

	mpg = predict(auto.fit, newdata = auto_x, n.trees = 100)

	storeProtoBuf(auto.fit, "pb/GBMFitAutoNA.pb")
	storeCsv(data.frame("y" = mpg), "csv/GBMFitAutoNA.csv")
}

set.seed(42)

generateGBMFormulaAutoNA()
generateGBMFitAutoNA()

auto.caret = auto
auto.caret$origin = as.integer(auto.caret$origin)

generateCaretGBMFormulaAutoNA = function(){
	auto.formula = train(mpg ~ ., data = auto.caret, method = "gbm")
	print(auto.formula)

	mpg = predict(auto.formula, newdata = auto.caret, na.action = na.pass)

	storeProtoBuf(auto.formula, "pb/CaretGBMFormulaAutoNA.pb")
	storeCsv(data.frame("y" = mpg), "csv/CaretGBMFormulaAutoNA.csv")
}

generateCaretGBMFitAutoNA = function(){
	auto.fit = train(x = auto_x, y = auto_y, method = "gbm")
	print(auto.fit)

	mpg = predict(auto.fit, newdata = auto_x)

	storeProtoBuf(auto.fit, "pb/CaretGBMFitAutoNA.pb")
	storeCsv(data.frame("y" = mpg), "csv/CaretGBMFitAutoNA.csv")
}

set.seed(42)

generateCaretGBMFormulaAutoNA()
generateCaretGBMFitAutoNA()