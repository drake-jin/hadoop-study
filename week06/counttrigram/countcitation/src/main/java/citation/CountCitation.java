package citation;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
@What
 - 위키피디아의 문서들간의 연결관계를 알아보려한다.
 - 가장 많이 인용된 논문이 가장 유명하고 의미있다고 볼 수 있는데 그걸 위키피디아 문서에도 적용하려 한다.
 - 문서들의 상대적인 중요도 등을 더 고려하여 유명한 문서로부터 인용되게 되면 중요하다는 개념을 도입한 웹문서들의 관계에 적용한 구글 페이지 랭크라 볼 수 있다.
 
@Source 
 - 2M.SRCID.DSTID 를 KeyValueTextInputFormat 입력 포맷으로 읽어들인다.

@Purpose
 - 가장 많이 링크된 위키피디아 페이지들의 문서 ID를 찾는다. 
 - CountCitation이고 하는일은 WordCount와 동일
 - WordCount는 단어를 샛고 CountCitation은 문서의 ID를 카운트 한다.
 
@Mapper
 - 입력의 키는 소스 문서의 ID가 된다. |밸류는 이 소스 문서에서 링크를 받고 있는 타겟 문서의 ID
 - 출력의 키는 타겟 문서의 ID |밸류는 1

@Reducer
 - Mapper의 출력을 입력으로 받는다.
 - 밸류단의 리스트값들을 모두 더한다.[해당 문서 ID에 대한 인용횟수를 카운트]
 - WordCount와 동일하기 때문에 컴바이너 적용이 가능하다. 
 */


public class CountCitation {

	public static class Map extends Mapper<Text, Text, Text, IntWritable>{
		private final static IntWritable one = new IntWritable(1);

		@Override
		protected void map(Text key, Text value, Mapper<Text, Text, Text, IntWritable>.Context context)
				throws IOException, InterruptedException {
			context.write(value,one);
		}
	}
	
	public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable>{
		@Override
		protected void reduce(Text key, Iterable<IntWritable> value,
				Reducer<Text, IntWritable, Text, IntWritable>.Context context) throws IOException, InterruptedException {
			int sum = 0;
			for(IntWritable val : value){
				sum += val.get();
			}
			context.write(key, new IntWritable(sum));
		}
	}
	public static void main(String[] args) throws Exception{
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		Job job = new Job(conf, "countcitation");

		job.setJarByClass(CountCitation.class);
		// if mapper outputs are different, call setMapOutputKeyClass and
		// setMapOutputValueClass
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(Map.class);
		job.setCombinerClass(Reduce.class);
		job.setReducerClass(Reduce.class);

		// An InputFormat for plain text files. Files are broken into lines.
		// Either linefeed or carriage-return are used to signal end of line.
		// Keys are the position in the file, and values are the line of text..
		job.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setNumReduceTasks(10);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.waitForCompletion(true);
	}
	
}













