library("rattle")

source("util.R")

createAudit = function(){
	data("audit")

	audit = na.omit(audit)

	audit$ID = NULL
	audit$IGNORE_Accounts = NULL
	audit$RISK_Adjustment = NULL

	audit$Deductions = as.logical(audit$Deductions > 0)

	names(audit)[ncol(audit)] = "Adjusted"

	storeCsv(audit, "csv/Audit.csv")

	audit.matrix = data.frame(model.matrix(formula("Adjusted ~ ."), audit))
	names(audit.matrix) = gsub("\\.", "-", names(audit.matrix))

	# Delete the leading "X-Intercept" column
	audit.matrix = audit.matrix[, 2:ncol(audit.matrix)]

	storeCsv(audit.matrix, "csv/AuditMatrix.csv")
}

loadWineQuality = function(color){
	data = read.table(paste("http://archive.ics.uci.edu/ml/machine-learning-databases/wine-quality/winequality-", color, ".csv", sep = ""), sep = ";", header = TRUE)

	return (data)
}

createWineQuality = function(){
	red_data = loadWineQuality("red")
	white_data = loadWineQuality("white")

	wine_quality = rbind(red_data, white_data)

	storeCsv(wine_quality, "csv/WineQuality.csv")

	wine_color = rbind(red_data, white_data)
	wine_color$quality = NULL
	wine_color$color = "white"
	wine_color$color[1:nrow(red_data)] = "red"

	storeCsv(wine_color, "csv/WineColor.csv")
}

createAudit()
createWineQuality()
