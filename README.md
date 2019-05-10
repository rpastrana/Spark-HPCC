# Spark-HPCC
Spark classes for working with HPCC clusters

There are two projects, DataAccess and Examples.

The DataAccess project contains the classes to support
reading data from a THOR cluster with a Spark RDD.  In
addition, te HPCC data is exposed as a Dataframe for
the convenience of the Spark developer.

The Examples project contains examples in Scala for
using HPCC THOR cluster based data in a Machine
Learning application.

## Spark-hpcc (sparkthor) roadmap
### 3Q19
- Support Spark 2.4.x 
- Provide Zeppelin usage guidelines
-- (Basic setup, Spark/pyspark Interpreter setup, Link to HPCC JARs via MVN, ESP Credential masking, Dep repo setup, Code repo setup)
- DataSource API 2 support
- Interface Improvement (including structured file filter support)
- Pyspark HPCC write support
### 4Q19
- Sparkthor docker container (full integration with HPCC container environment)
- Security improvements 
- Variable file format support
