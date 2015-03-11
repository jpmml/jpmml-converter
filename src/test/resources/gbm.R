library("caret")
library("gbm")

source("util.R")

audit = loadAuditCsv("csv/AuditNA.csv")

# Error in gbm.fit: Deductions is not of type numeric, ordered, or factor
audit$Deductions = NULL

audit_x = audit[, -ncol(audit)]
audit_y = audit[, ncol(audit)]
audit_y = as.numeric(audit_y == "1")

generateGBMFitAdaBoostAuditNA = function(){
	audit.fit = gbm.fit(x = audit_x, y = audit_y, distribution = "adaboost", interaction.depth = 3, shrinkage = 0.1, n.trees = 100, response.name = "Adjusted")
	print(audit.fit)

	adjusted = predict(audit.fit, newdata = audit_x, n.trees = 100)

	probability_1 = predict(audit.fit, newdata = audit_x, type = "response", n.trees = 100)
	probability_0 = (1 - probability_1)

	storeProtoBuf(audit.fit, "pb/GBMFitAdaBoostAuditNA.pb")
	storeCsv(data.frame("Adjusted" = adjusted, "probability_0" = probability_0, "probability_1" = probability_1), "csv/GBMFitAdaBoostAuditNA.csv")
}

generateGBMFitBernoulliAuditNA = function(){
	audit.fit = gbm.fit(x = audit_x, y = audit_y, distribution = "bernoulli", interaction.depth = 3, shrinkage = 0.1, n.trees = 100, response.name = "Adjusted")
	print(audit.fit)

	adjusted = predict(audit.fit, newdata = audit_x, n.trees = 100)

	probability_1 = predict(audit.fit, newdata = audit_x, type = "response", n.trees = 100)
	probability_0 = (1 - probability_1)

	storeProtoBuf(audit.fit, "pb/GBMFitBernoulliAuditNA.pb")
	storeCsv(data.frame("Adjusted" = adjusted, "probability_0" = probability_0, "probability_1" = probability_1), "csv/GBMFitBernoulliAuditNA.csv")
}

set.seed(42)

generateGBMFitAdaBoostAuditNA()
generateGBMFitBernoulliAuditNA()

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
	auto.fit = gbm.fit(x = auto_x, y = auto_y, distribution = "gaussian", interaction.depth = 3, shrinkage = 0.1, n.trees = 100, response.name = "mpg")
	print(auto.fit)

	mpg = predict(auto.fit, newdata = auto_x, n.trees = 100)

	storeProtoBuf(auto.fit, "pb/GBMFitAutoNA.pb")
	storeCsv(data.frame("mpg" = mpg), "csv/GBMFitAutoNA.csv")
}

set.seed(42)

generateGBMFormulaAutoNA()
generateGBMFitAutoNA()

auto.caret = auto
auto.caret$origin = as.integer(auto.caret$origin)

generateCaretGBMFormulaAutoNA = function(){
	auto.formula = train(mpg ~ ., data = auto.caret, method = "gbm", response.name = "mpg")
	print(auto.formula)

	mpg = predict(auto.formula, newdata = auto.caret, na.action = na.pass)

	storeProtoBuf(auto.formula, "pb/CaretGBMFormulaAutoNA.pb")
	storeCsv(data.frame("mpg" = mpg), "csv/CaretGBMFormulaAutoNA.csv")
}

generateCaretGBMFitAutoNA = function(){
	auto.fit = train(x = auto_x, y = auto_y, method = "gbm", response.name = "mpg")
	print(auto.fit)

	mpg = predict(auto.fit, newdata = auto_x)

	storeProtoBuf(auto.fit, "pb/CaretGBMFitAutoNA.pb")
	storeCsv(data.frame("mpg" = mpg), "csv/CaretGBMFitAutoNA.csv")
}

set.seed(42)

generateCaretGBMFormulaAutoNA()
generateCaretGBMFitAutoNA()