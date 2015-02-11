library("RProtoBuf")

storeProtoBuf = function(model, file){
	con = file(file, open = "wb")
	serialize_pb(model, con)
	close(con)
}

loadCsv = function(file){
	return (read.csv(file = file, header = TRUE))
}

storeCsv = function(data, file){
	write.table(data, file = file, sep = ",", quote = FALSE, row.names = FALSE, col.names = gsub("X_target", "_target", names(data)))
}
