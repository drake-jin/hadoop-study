-- Pls change the location of input according to your environment
records = LOAD '/root/data/2M.SRCID.DSTID' AS (srcid:long, dstid:long);
grouped_records = GROUP records by dstid;
count_records = FOREACH grouped_records GENERATE group, COUNT(records) AS sum;
filtered_records = FILTER count_records  BY sum > 1;
ordered_filtered_records = ORDER filtered_records BY sum DESC;
-- Pls change the location of output according to your sitution
STORE ordered_filtered_records INTO '/root/output/pig_citation';
