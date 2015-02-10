JPMML-Converter
===============

Fast conversion of R models to PMML

# Installation #

Enter the project root directory and build using [Apache Maven] (http://maven.apache.org/):
```
mvn clean install
```

The build produces an executable uber-JAR file `target/converter-executable-1.0-SNAPSHOT.jar`.

# Usage #

A typical fast conversion workflow has the following steps:

1. Use R to train a model.
2. Serialize this model in [ProtoBuf data format] (https://code.google.com/p/protobuf/) to a file in local filesystem.
3. Use the JPMML-Converter application to turn this ProtoBuf file to a PMML file.

### The R side of operations

The serialization is handled by the [`RProtoBuf` library] (http://cran.r-project.org/web/packages/RProtoBuf/).

The following R script trains a Random Forest (RF) model and saves it in ProtoBuf data format to a file `rf.pb`:
```R
library("randomForest")
library("RProtoBuf")

data(mydata)

rf = randomForest(target ~ ., data = mydata)

con = file("rf.pb", open = "wb")
serialize_pb(rf, con)
close(con)
```

### The JPMML-Converter side of operations

Converting the ProtoBuf file `rf.pb` to a PMML file `rf.pmml`:
```
java -Xms2048M -Xmx2048M -jar target/converter-executable-1.0-SNAPSHOT.jar --pb-input rf.pb --pmml-output rf.pmml
```

# License #

JPMML-Converter is dual-licensed under the [GNU Affero General Public License (AGPL) version 3.0] (http://www.gnu.org/licenses/agpl-3.0.html) and a commercial license.

# Additional information #

Please contact [info@openscoring.io] (mailto:info@openscoring.io)