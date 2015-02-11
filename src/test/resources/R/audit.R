library("rattle")

source("util.R")

data("audit")

audit = na.omit(audit)

audit$ID = NULL
audit$IGNORE_Accounts = NULL
audit$RISK_Adjustment = NULL

audit$Deductions = as.logical(audit$Deductions > 0)

names(audit)[ncol(audit)] = "Adjusted"

audit$Adjusted = as.factor(audit$Adjusted)

storeCsv(audit, "../csv/Audit.csv")