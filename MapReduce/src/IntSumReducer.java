import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Iterator;

public class IntSumReducer extends Reducer<Text, IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public IntSumReducer() {
    }

    public void reduce(Text key,Iterable<IntWritable>values,Reducer<Text,IntWritable,Text,IntWritable>.Context context)throws IOException,InterruptedException{
        int sum=0;
        IntWritable val;
        for(Iterator i$ = values.iterator(); i$.hasNext();sum+=val.get()){
            val=(IntWritable)i$.next();
        }
        result.set(sum);
        context.write(key,result);
    }
}
