library("party")

source("util.R")

predictCsv = function(ctree, data, targetName){
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

	result = predictCsv(audit.ctree, audit, "Adjusted")
	storeCsv(result, "csv/BinaryTreeAudit.csv")
}

set.seed(42)

generateBinaryTreeAudit()

iris = loadIrisCsv("csv/Iris.csv")

generateBinaryTreeIris = function(){
	iris.ctree = ctree(Species ~ ., data = iris)
	print(iris.ctree)

	storeProtoBuf(iris.ctree, "pb/BinaryTreeIris.pb")

	result = predictCsv(iris.ctree, iris, "Species")
	storeCsv(result, "csv/BinaryTreeIris.csv")
}

set.seed(42)

generateBinaryTreeIris()