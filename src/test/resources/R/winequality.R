source("util.R")

loadWineQuality = function(color){
	data = read.table(paste("http://archive.ics.uci.edu/ml/machine-learning-databases/wine-quality/winequality-", color, ".csv", sep = ""), sep = ";", header = TRUE)

	return (data)
}

red_data = loadWineQuality("red")
white_data = loadWineQuality("white")

wine_quality = rbind(red_data, white_data)

storeCsv(wine_quality, "../csv/WineQuality.csv")

wine_color = rbind(red_data, white_data)
wine_color$quality = NULL
wine_color$color = "white"
wine_color$color[1:nrow(red_data)] = "red"
wine_color$color = as.factor(wine_color$color)

storeCsv(wine_color, "../csv/WineColor.csv")

