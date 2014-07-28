/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testing;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 *
 * @author Patti Spala <pd.spala@gmail.com>
 */
public class Pooling extends RecursiveTask<Long> {

     static final int SEQUENTIAL_THRESHOLD = 5000;

    int low;
    int high;
    int[] array;

    public Pooling(int[] arr, int lo, int hi) {
        array = arr;
        low   = lo;
        high  = hi;
    }

    @Override
    protected Long compute() {
         if(high - low <= SEQUENTIAL_THRESHOLD) {
            long sum = 0;
            for(int i=low; i < high; ++i) 
                sum += array[i];
            return sum;
         } else {
            int mid = low + (high - low) / 2;
            Pooling left  = new Pooling(array, low, mid);
            Pooling right = new Pooling(array, mid, high);
            left.fork();
            long rightAns = right.compute();
            long leftAns  = left.join();
            return leftAns + rightAns;
         }
    }

}

class PoolingExample {

    static ForkJoinPool pool = new ForkJoinPool();
    
    public long printExample(int[] array){
        return pool.invoke(new Pooling(array,0,array.length));
        
    }
}
