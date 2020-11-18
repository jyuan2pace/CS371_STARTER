import Utils.BoundBuffer;
import java.util.HashMap;

import Utils.*;
public class PartitionTable {
	//TODO: your codde here
	//Notes:
	// (1) each partition works like an bounded buffer between
	// mappers and a reducer. (you can assume size = 10 or 50)
	// (2) if reducer_i wants to fetch a KV pair it can
	// only fetches from partition_i
	// (3) reducer_i also has to maintain a kv store which maps
	// a key to all the value associated to the key, for instace
	// if reducer_i has fetched {"foo", 1} {"bar",1} {"foo",1}
	// {"foo",1} and {"bar",1}
	// from partition_i, then reducer_i should have maintained
	// {"foo", {1,1,1}} and {"bar", {1,1}}. You can use a
	// hashmap or a tree to implement this KV store, but where
	// should this KV stored be? inside the reducer? inside
	// this partitionTable?

}
