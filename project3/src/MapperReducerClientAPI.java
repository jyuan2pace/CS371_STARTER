import java.util.Comparator;

public abstract class MapperReducerClientAPI {
	abstract void Map(Object inputSource);
	
	abstract void Reduce(Object key, int partition_number);

	long Partitioner(Object key, int num_partitions){
		//This is the hash for your wordcount case by default
		//The cast is there because keys are treated as strings
		//by default
		String k = (String) key;
		char [] ck = k.toCharArray();
		long hash = 5381;
		int c;
		int i=0;
		while (i<ck.length) {
			c = ck[i++];
			hash = hash * 33 + c;
		}
		long ret =  hash % num_partitions;
		ret = ret>= 0? ret:ret+num_partitions;
		return ret;
	}

}
