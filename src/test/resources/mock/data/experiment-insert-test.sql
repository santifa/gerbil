TRUNCATE TABLE  Experiments;
TRUNCATE TABLE ExperimentTasks;


INSERT INTO ExperimentTasks (annotatorName, datasetName, filterName, experimentType, matching) VALUES ('derGuteBernd', 'test_dataset', 'nofilter', 'QUATSCH', 'matsch');
INSERT INTO Experiments (id, taskId) VALUES ('id-123', '1');

