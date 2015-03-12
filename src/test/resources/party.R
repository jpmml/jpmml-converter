library("party")

source("util.R")

predictClassification = function(ctree, data, targetName){
	target = data[, targetName]

	classes = ctree@predict_response(newdata = data, type = "response")
	probabilities = ctree@predict_response(newdata = data, type = "prob")
	nodes = ctree@predict_response(newdata = data, type = "node")

	# Convert from list of lists to data.frame
	probabilities = data.frame(matrix(unlist(probabilities), nrow = nrow(data), byrow = TRUE))
	names(probabilities) = lapply(levels(target), function(value){ return (paste("probability", value, sep = "_")) })

	result = data.frame("y" = classes, probabilities, "nodeId" = nodes)
	names(result) = gsub("^y$", targetName, names(result))

	return (result)
}

audit = loadAuditCsv("csv/Audit.csv")

generateBinaryTreeAudit = function(){
	audit.ctree = ctree(Adjusted ~ ., data = audit)
	print(audit.ctree)

	storeProtoBuf(audit.ctree, "pb/BinaryTreeAudit.pb")

	result = predictClassification(audit.ctree, audit, "Adjusted")
	storeCsv(result, "csv/BinaryTreeAudit.csv")
}

set.seed(42)

generateBinaryTreeAudit()

auto = loadAutoCsv("csv/Auto.csv")

generateBinaryTreeAuto = function(){
	auto.ctree = ctree(mpg ~ ., data = auto)
	print(auto.ctree)

	storeProtoBuf(auto.ctree, "pb/BinaryTreeAuto.pb")

	mpg = auto.ctree@predict_response(newdata = auto, type = "response")

	storeCsv(data.frame("mpg" = mpg), "csv/BinaryTreeAuto.csv")
}

set.seed(42)

generateBinaryTreeAuto()

iris = loadIrisCsv("csv/Iris.csv")

generateBinaryTreeIris = function(){
	iris.ctree = ctree(Species ~ ., data = iris)
	print(iris.ctree)

	storeProtoBuf(iris.ctree, "pb/BinaryTreeIris.pb")

	result = predictClassification(iris.ctree, iris, "Species")
	storeCsv(result, "csv/BinaryTreeIris.csv")
}

set.seed(42)

generateBinaryTreeIris()