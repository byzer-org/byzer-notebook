--liquibase formatted sql

--changeset tianqing.guo:11
--validCheckSum: 8:5174c263b71b7b2e8ea03119d0f79d11

--labels: add table
--tag: 1.0.0

CREATE TABLE IF NOT EXISTS register_et (
    id int(32) UNSIGNED NOT NULL AUTO_INCREMENT,
    name varchar(50) NOT NULL UNIQUE,
    caption varchar(255) NULL,
    category varchar(255) NULL,
    description varchar(255) NULL,
    et_usage varchar(255) NOT NULL,
    enable tinyint(1) default 1,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS et_params_def (
    id int(32) UNSIGNED NOT NULL AUTO_INCREMENT,
    et_id int(32) NULL,
    name varchar(50) NOT NULL,
    description varchar(255) NULL,
    optional tinyint(1) default 1,
    type varchar(50) NOT NULL,
    value_type varchar(50) NOT NULL,
    default_value varchar(255) NULL,
    enum_values text NULL,
    required tinyint(1) default 1,
    depends text NULL,
    label varchar(255) NULL,
    `constraint` text NULL ,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS usage_template (
    id int(32) NOT NULL AUTO_INCREMENT,
    `usage` varchar(255) NOT NULL,
    template varchar(255) NOT NULL,
    PRIMARY KEY (`id`)
);

# ets
INSERT IGNORE INTO register_et VALUES(1, 'ALSInPlace', '', 'Algorithm', 'Alternating Least Square (ALS), a collaborative recommendation algorithm.', 'train', true);
INSERT IGNORE INTO register_et VALUES(2, 'TfIdfInPlace', '', 'Algorithm', 'Term Frequency–Inverse Document Frequency.', 'train', true);
INSERT IGNORE INTO register_et VALUES(3, 'KMeans', '', 'Algorithm', 'K-means clustering algorithm.', 'train', true);
INSERT IGNORE INTO register_et VALUES(4, 'LogisticRegression', '', 'Algorithm', 'Logistic regression is a generalized linear regression analysis model, which is often used in binary classification or multi-classification scenarios.', 'train', true);
INSERT IGNORE INTO register_et VALUES(5, 'NaiveBayes', '', 'Algorithm', 'Naive Bayes Classifier algorithm，which is an algorithm for supervised learning.', 'train', true);
INSERT IGNORE INTO register_et VALUES(6, 'RandomForest', '', 'Algorithm', 'Random forest is an algorithm that uses multiple decision trees to train, classify and predict samples.', 'train', true);
INSERT IGNORE INTO register_et VALUES(8, 'LinearRegression', '', 'Algorithm', 'Linear regression is a statistical analysis method that uses regression analysis to determine the interdependent quantitative relationship between two or more variables.', 'train', true);
INSERT IGNORE INTO register_et VALUES(9, 'Discretizer', '', 'Feature Engineering', 'Discretizer is used to transfer continuous features into discrete features.', 'train-fe', true);
INSERT IGNORE INTO register_et VALUES(10, 'NormalizeInPlace', '', 'Feature Engineering', 'Normalization allows each feature to be represented in the appropriate way at the same time.', 'train-fe', true);
INSERT IGNORE INTO register_et VALUES(11, 'ScalerInPlace', '', 'Feature Engineering', 'The Scaler component can smooth the data containing exceptions in the input feature to a certain range.', 'train-fe', true);
INSERT IGNORE INTO register_et VALUES(12, 'VecMapInPlace', '', 'Feature Engineering', 'VecMapInPlace can convert a Map[String,Double] to a vector.', 'train-fe', false);
INSERT IGNORE INTO register_et VALUES(13, 'Word2VecInPlace', '', 'Feature Engineering', 'Word2Vec can convert the text content into a vector.', 'train-fe', true);
INSERT IGNORE INTO register_et VALUES(14, 'JsonExpandExt', '', 'Data Processing', 'JsonExpand can easily expand a JSON field into multiple fields.', 'run', true);
INSERT IGNORE INTO register_et VALUES(15, 'RateSampler', '', 'Data Processing', 'RateSampler supports data sets sharding.', 'dp-train', true);
INSERT IGNORE INTO register_et VALUES(16, 'TableRepartition', 'TableRepartition supports re-sharding of data sets.', 'Data Processing', '', 'run', true);
INSERT IGNORE INTO register_et VALUES(17, 'PythonCommand', '', 'Tool', '', 'python', false);
INSERT IGNORE INTO register_et VALUES(18, 'Ray', '', 'Tool', '', 'ray', false);
INSERT IGNORE INTO register_et VALUES(20, 'RunScript', '', 'Tool', '', 'runScript', false);
INSERT IGNORE INTO register_et VALUES(21, 'SendMessage', '', 'Tool', ' SendMessage can be used to send email.', 'run-sm', false);
INSERT IGNORE INTO register_et VALUES(22, 'SyntaxAnalyzeExt', '', 'Tool', ' SyntaxAnalyzeExt can be used to complete table extraction, parsing all tables in SQL.', 'run-sa', true);
INSERT IGNORE INTO register_et VALUES(23, 'TreeBuildExt', '', 'Tool', ' TreeBuildExt is used to process tree structure analysis.', 'run', true);

# templates
INSERT IGNORE INTO usage_template VALUES(-1, 'train', 'train $INPUT_DATA as $ET_NAME.`$MODEL_PATH/$MODEL_NAME` where {{parameters}};');
INSERT IGNORE INTO usage_template VALUES(-2, 'train-fe', 'train $INPUT_DATA as $ET_NAME.`$MODEL_PATH/$MODEL_NAME` where {{parameters}};');
INSERT IGNORE INTO usage_template VALUES(-3, 'dp-train', 'train $INPUT_DATA as $ET_NAME.`` where {{parameters}} as $OUTPUT_DATA;');
INSERT IGNORE INTO usage_template VALUES(-4, 'run', 'run $INPUT_DATA as $ET_NAME.`` where {{parameters}} as $OUTPUT_DATA;');
INSERT IGNORE INTO usage_template VALUES(-5, 'runScript', "!runScript '''$parameters''' named $OUTPUT_DATA;");
INSERT IGNORE INTO usage_template VALUES(-6, 'run-sa', 'run command as $ET_NAME.`` where {{parameters}} as $OUTPUT_DATA;');
INSERT IGNORE INTO usage_template VALUES(-7, 'run-sm', 'run data as $ET_NAME.`` where {{parameters}};');

# key parameters
INSERT IGNORE INTO et_params_def VALUES(501, -1, 'INPUT_DATA', '', true, 'Key', 'INPUT/TABLE', '', '', true, '', 'Select the Table to Be Trained', null);
INSERT IGNORE INTO et_params_def VALUES(502, -1, 'MODEL_PATH', '', true, 'Key', 'OUTPUT/MODEL_PATH', '/tmp/model/algorithm', '', true, '', 'Save Path', null);
INSERT IGNORE INTO et_params_def VALUES(503, -1, 'MODEL_NAME', '', true, 'Key', 'OUTPUT/MODEL_NAME', '', '', true, '', 'Model Name', null);
INSERT IGNORE INTO et_params_def VALUES(504, -2, 'INPUT_DATA', '', true, 'Key', 'INPUT/TABLE', '', '', true, '', 'Select the Input Table', null);
INSERT IGNORE INTO et_params_def VALUES(505, -2, 'MODEL_PATH', '', true, 'Key', 'OUTPUT/MODEL_PATH', '/tmp/model/data', '', true, '', 'Save Path', null);
INSERT IGNORE INTO et_params_def VALUES(506, -2, 'MODEL_NAME', '', true, 'Key', 'OUTPUT/MODEL_NAME', '', '', true, '', 'Output Name', null);
INSERT IGNORE INTO et_params_def VALUES(507, -3, 'INPUT_DATA', '', true, 'Key', 'INPUT/TABLE', '', '', true, '', 'Select the Input Table', null);
INSERT IGNORE INTO et_params_def VALUES(508, -3, 'OUTPUT_DATA', '', true, 'Key', 'OUTPUT/TABLE', '', '', true, '', 'Output Name', null);
INSERT IGNORE INTO et_params_def VALUES(510, -4, 'INPUT_DATA', '', true, 'Key', 'INPUT/TABLE', '', '', true, '', 'Select the Input Table', null);
INSERT IGNORE INTO et_params_def VALUES(511, -4, 'OUTPUT_DATA', '', true, 'Key', 'OUTPUT/TABLE', '', '', true, '', 'Output Name', null);
INSERT IGNORE INTO et_params_def VALUES(512, -5, 'OUTPUT_DATA', '', true, 'Key', 'OUTPUT/TABLE', '', '', true, '', 'Output Name', null);
INSERT IGNORE INTO et_params_def VALUES(513, -6, 'OUTPUT_DATA', '', true, 'Key', 'OUTPUT/TABLE', '', '', true, '', 'Output Name', null);

# AlSInPlace
INSERT IGNORE INTO et_params_def VALUES(1001, 1, 'maxIter', '', true, 'Group/B', 'INT', '10', '', false, '', '', '{min:0}');
INSERT IGNORE INTO et_params_def VALUES(1002, 1, 'regParam', '', true, 'Group/B', 'FLOAT', '0.1', '', false, '', '', '{min:0}');
INSERT IGNORE INTO et_params_def VALUES(1003, 1, 'userCol', '', true, 'Group/A', 'STRING', 'user', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1004, 1, 'itemCol', '', true, 'Group/A', 'STRING', 'item', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1005, 1, 'ratingCol', '', true, 'Group/A', 'STRING', 'rating', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1006, 1, 'numItemBlocks', '', true, 'Group/B', 'INT', '10', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1007, 1, 'numUserBlocks', '', true, 'Group/B', 'INT', '10', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1008, 1, 'itemRec', '', true, 'Normal', 'INT', '10', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1009, 1, 'userRec', '', true, 'Normal', 'INT', '10', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1010, 1, 'alpha', '', false, '', '', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1011, 1, 'blockSize', '', false, '', '', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1012, 1, 'checkpointInterval', '', false, '', '', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1013, 1, 'finalStorageLevel', '', false, '', '', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1014, 1, 'implicitPrefs', '', false, '', '', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1015, 1, 'intermediateStorageLevel', '', false, '', '', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1016, 1, 'rank', '', false, '', '', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1017, 1, 'seed', '', false, '', '', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1018, 1, 'nonnegative', '', false, '', '', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1019, 1, 'coldStartStrategy', '', true, 'Group/A', 'ENUM', 'nan', 'nan,drop', false, '', '', null);

# TfIdfInPlace
INSERT IGNORE INTO et_params_def VALUES(1051, 2, 'inputCol', '', true, 'Normal', 'STRING', '', '', true, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1052, 2, 'priorityDicPath', '', true, 'Normal', 'INPUT/HDFS', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1053, 2, 'stopWordPath', '', true, 'Normal', 'INPUT/HDFS', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1054, 2, 'dicPaths', '', true, 'Normal', 'INPUT/HDFS', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1055, 2, 'nGrams', '', true, 'Normal', 'MULTI_ENUM', '2,3', '2,3', false, '', '', null);

# KMeans
INSERT IGNORE INTO et_params_def VALUES(1101, 3, 'distanceMeasure', '', true, 'Group/A', 'ENUM', 'euclidean', 'euclidean,cosine', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1102, 3, 'featuresCol', '', true, 'Group/A', 'STRING', 'features', '', true, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1103, 3, 'k', '', true, 'Group/A', 'INT', '4', '', true, '', '', '{min:2}');
INSERT IGNORE INTO et_params_def VALUES(1104, 3, 'maxIter', '', true, 'Group/B', 'INT', '20', '', false, '', '', '{min:0}');
INSERT IGNORE INTO et_params_def VALUES(1105, 3, 'minDivisibleClusterSize', '', true, 'Group/A', 'FLOAT', '1.0', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1106, 3, 'predictionCol', '', true, 'Group/A', 'STRING', 'prediction', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1107, 3, 'seed', '', true, 'Group/A', 'INT', '566573821', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1108, 3, 'weightCol', '', true, 'Group/A', 'STRING', '', '', false, '', '', null);

# NaiveBayes
INSERT IGNORE INTO et_params_def VALUES(1151, 5, 'featuresCol', '', true, 'Group/A', 'STRING', 'features', '', true, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1152, 5, 'labelCol', '', true, 'Group/A', 'STRING', 'label', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1153, 5, 'modelType', '', true, 'Group/A', 'ENUM', 'multinomial', 'multinomial,bernoulli', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1154, 5, 'predictionCol', '', true, 'Group/A', 'STRING', 'prediction', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1155, 5, 'probabilityCol', '', true, 'Group/A', 'STRING', 'probability', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1156, 5, 'rawPredictionCol', '', true, 'Group/A', 'STRING', 'rawPrediction', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1157, 5, 'smoothing', '', true, 'Group/B', 'FLOAT', '1.0', '', false, '', '', '{min:0}');
INSERT IGNORE INTO et_params_def VALUES(1158, 5, 'thresholds', '', true, 'Group/B', 'STRING', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1159, 5, 'weightCol', '', true, 'Group/A', 'STRING', '', '', false, '', '', null);

# RandomForest
INSERT IGNORE INTO et_params_def VALUES(1201, 6, 'bootstrap', '', true, 'Group/A', 'ENUM', 'false', 'true,false', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1202, 6, 'cacheNodeIds', '', true, 'Group/A', 'ENUM', 'true', 'true,false', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1203, 6, 'checkpointInterval', '', false, 'Group/A', 'INT', '10', '', false, '', '', '{min:1}');
INSERT IGNORE INTO et_params_def VALUES(1204, 6, 'featureSubsetStrategy', '', true, 'Group/A', 'ENUM', 'auto', 'auto,all,onethird,sqrt,log2', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1205, 6, 'featuresCol', '', true, 'Group/A', 'STRING', 'features', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1206, 6, 'impurity', '', true, 'Group/A', 'ENUM', 'gini', 'entropy,gini', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1207, 6, 'labelCol', '', true, 'Group/A', 'STRING', 'label', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1208, 6, 'maxBins', '', true, 'Group/B', 'INT', '32', '', false, '', '', '{min:2}');
INSERT IGNORE INTO et_params_def VALUES(1209, 6, 'maxDepth', '', true, 'Group/B', 'INT', '5', '', false, '', '', '{min:0}');
INSERT IGNORE INTO et_params_def VALUES(1210, 6, 'maxMemoryInMB', '', false, 'Group/A', 'INT', '256', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1211, 6, 'minInfoGain', '', false, 'Group/A', 'INT', '0.0', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1212, 6, 'minInstancesPerNode', '', false, 'Group/A', 'FLOAT', '0.0', '', false, '', '', '{min:0.0,max:0.5}');
INSERT IGNORE INTO et_params_def VALUES(1213, 6, 'numTrees', '', true, 'Group/A', 'INT', '20', '', false, '', '', '{min:1}');
INSERT IGNORE INTO et_params_def VALUES(1214, 6, 'predictionCol', '', true, 'Group/A', 'STRING', 'prediction', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1215, 6, 'probabilityCol', '', true, 'Group/A', 'STRING', 'probability', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1216, 6, 'rawPredictionCol', '', false, 'Group/A', 'STRING', 'rawPredictionCol', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1217, 6, 'seed', '', true, 'Group/A', 'INT', '207336481', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1218, 6, 'subsamplingRate', '', true, 'Group/A','FLOAT', '1.0', '', false, '', '', '{min:0,max:1}');
INSERT IGNORE INTO et_params_def VALUES(1219, 6, 'thresholds', '', true, 'Group/B','STRING', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1220, 6, 'weightCol', '', false, 'Group/A','STRING', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1221, 6, 'leafCol', '', false, '','', '', '', false, '', '', null);

# LogisticRegression
INSERT IGNORE INTO et_params_def VALUES(1251, 4, 'elasticNetParam', '', true, 'Group/A','FLOAT', '0.0', '', false, '', '', '{min:0,max:1}');
INSERT IGNORE INTO et_params_def VALUES(1252, 4, 'epsilon', '', true, 'Group/A','FLOAT', '1.35', '', false, '', '', '{min:1.0}');
INSERT IGNORE INTO et_params_def VALUES(1253, 4, 'featuresCol', '', true, 'Group/A','STRING', 'features', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1254, 4, 'fitIntercept', '', true, 'Group/A','ENUM', 'true', 'true,false', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1255, 4, 'labelCol', '', true, 'Group/A','STRING', 'label', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1256, 4, 'loss', '', true, 'Group/A','ENUM', 'squaredError', 'squaredError,huber', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1257, 4, 'maxIter', '', true, 'Group/A','INT', '100', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1258, 4, 'predictionCol', '', true, 'Group/A', 'STRING', 'prediction', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1259, 4, 'solver', '', true, 'Group/A', 'ENUM', 'auto', 'auto,normal,l-bfgs', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1260, 4, 'standardization', '', true, 'Group/A', 'ENUM', 'true', 'true,false', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1261, 4, 'aggregationDepth', '', false, 'Group/A','INT', '2', '', false, '', '', '{min:2}');
INSERT IGNORE INTO et_params_def VALUES(1262, 4, 'tol', '', false, 'Group/A', 'INT', '', '', false, '', '', '{min:0}');
INSERT IGNORE INTO et_params_def VALUES(1263, 4, 'lowerBoundsOnCoefficients', '', false, 'Group/A', 'STRING', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1264, 4, 'lowerBoundsOnIntercepts', '', false, 'Group/A', 'STRING', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1265, 4, 'maxBlockSizeInMB', '', false, 'Group/A','INT', '0.0', '', false, '', '', '{min:0}');
INSERT IGNORE INTO et_params_def VALUES(1266, 4, 'rawPredictionCol', '', false, 'Group/A', 'STRING', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1267, 4, 'regParam', '', false, 'Group/A', 'FLOAT', '0.0', '', false, '', '', '{min:0}');
INSERT IGNORE INTO et_params_def VALUES(1268, 4, 'upperBoundsOnCoefficients', '', false, 'Group/A', 'STRING', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1269, 4, 'upperBoundsOnIntercepts', '', false, 'Group/A', 'STRING', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1270, 4, 'weightCol', '', false, 'Group/A', 'STRING', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1271, 4, 'threshold', '', true, 'Group/B','FLOAT', '0.5', '', false, '', '', '{min:0,max:1}');
INSERT IGNORE INTO et_params_def VALUES(1272, 4, 'thresholds', '', true, 'Group/B','STRING', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1273, 4, 'family', '', true, 'Group/A','ENUM', 'auto', 'auto,binomial,multinomial', false, '', '', null);

# LinearRegression
INSERT IGNORE INTO et_params_def VALUES(1301, 8, 'elasticNetParam', '', true, 'Group/A','FLOAT', '0.0', '', false, '', '', '{min:0,max:1}');
INSERT IGNORE INTO et_params_def VALUES(1302, 8, 'featuresCol', '', true, 'Group/A', 'STRING', 'features', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1303, 8, 'fitIntercept', '', true, 'Group/A','ENUM', 'true', 'true,false', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1304, 8, 'labelCol', '', true, 'Group/A', 'STRING', 'label', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1305, 8, 'loss', '', true, 'Group/A','ENUM', 'squaredError', 'squaredError,huber', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1306, 8, 'maxIter', '', true, 'Group/B', 'INT', '10', '', false, '', '', '{min:0}');
INSERT IGNORE INTO et_params_def VALUES(1307, 8, 'predictionCol', '', true, 'Group/A', 'STRING', 'prediction', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1308, 8, 'regParam', '', false, 'Group/B', 'FLOAT', '0.1', '', false, '', '', '{min:0}');
INSERT IGNORE INTO et_params_def VALUES(1309, 8, 'solver', '', true, 'Group/A', 'ENUM', 'auto', 'auto,normal,l-bfgs', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1310, 8, 'standardization', '', true, 'Group/A', 'ENUM', 'true', 'true,false', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1311, 8, 'tol', '', false, 'Group/A', 'INT', '', '', false, '', '', '{min:0}');
INSERT IGNORE INTO et_params_def VALUES(1312, 8, 'weightCol', '', false, 'Group/A', 'STRING', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1313, 8, 'aggregationDepth', '', false, 'Group/A', 'INT', '', '', false, '', '', '{min:2}');
INSERT IGNORE INTO et_params_def VALUES(1314, 8, 'epsilon', '', false, 'Group/A', 'FLOAT', '', '', false, '', '', '{min:1.0}');
INSERT IGNORE INTO et_params_def VALUES(1315, 8, 'maxBlockSizeInMB', '', false, 'Group/A', 'INT', '', '', false, '', '', '{min:0}');

# RateSampler
INSERT IGNORE INTO et_params_def VALUES(1351, 15, 'labelCol', '', true, 'Normal', 'STRING', 'label', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1352, 15, 'sampleRate', '', true, 'Normal', 'STRING', '0.9,0.1', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1353, 15, 'isSplitWithSubLabel', '', true, 'Normal', 'ENUM', 'true', 'true,false', false, '', '', null);

# SyntaxAnalyzeExt
INSERT IGNORE INTO et_params_def VALUES(1401, 22, 'sql', '', true, 'Normal', 'TEXT', '', '', false, '', '', null);

# RunScript
INSERT IGNORE INTO et_params_def VALUES(1451, 20, 'parameters', '', true, 'Key', 'TEXT', '', '', false, '', 'Script', null);

# Discretizer
INSERT IGNORE INTO et_params_def VALUES(1501, 9, 'handleInvalid', '', true, 'Normal', 'ENUM', 'error', 'skip,error,keep', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1502, 9, 'splitsArray', '', true, 'Normal', 'STRING', 'e.g. [-inf,-0.3,0,0.3,inf],[-inf,-0.5,0, 0.5,inf]', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1503, 9, 'splits', '', true, 'Normal', 'STRING', 'e.g. [-inf,-0.5,0,0.5,inf]', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1504, 9, 'inputCols', '', false, 'Normal', 'STRING', '', '', true, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1505, 9, 'outputCols', '', false, 'Normal', 'STRING', '', '', true, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1506, 9, 'numBucketsArray', '', false, 'Normal', 'STRING', '', '', true, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1507, 9, 'splitArray', '', false, 'Normal', 'STRING', '', '', true, '', '', null);

# Word2VecInPlace
INSERT IGNORE INTO et_params_def VALUES(1551, 13, 'wordvecPaths', '', true, 'Normal', 'INPUT/HDFS', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1552, 13, 'stopWordPath', '', true, 'Normal', 'INPUT/HDFS', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1553, 13, 'dicPaths', '', true, 'Normal', 'INPUT/HDFS', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1554, 13, 'resultFeature', '', true, 'Normal', 'ENUM', '', 'flat,merge,index', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1555, 13, 'ignoreNature', '', true, 'Normal', 'ENUM', 'true', 'true,false', false, '', '', null);

# TableRepartition
INSERT IGNORE INTO et_params_def VALUES(1601, 16, 'partitionNum', '', true, 'Normal', 'INT', '', '', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1602, 16, 'partitionType', '', true, 'Normal', 'ENUM', 'hash', 'hash,range', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1603, 16, 'shuffle', '', true, 'Normal', 'ENUM', 'true', 'true,false', false, '', '', null);

# TreeBuildExt
INSERT IGNORE INTO et_params_def VALUES(1651, 23, 'treeType', '', true, 'Normal', 'ENUM', '', 'nodeTreePerRow,treePerRow', false, '', '', null);
INSERT IGNORE INTO et_params_def VALUES(1652, 23, 'recurringDependencyBreakTimes', '', true, 'Normal', 'INT', '1000', '', false, '', '', null);

--comment: table & records for et
