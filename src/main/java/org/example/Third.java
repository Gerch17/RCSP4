package org.example;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import lombok.SneakyThrows;

public class Third {

    @SneakyThrows
    public static void main(String[] args) {
        File dir1 = new File("./").getAbsoluteFile();
        String name = "file";
        String ext = ".txt";

        int a = 0;
        int f =0;
        Queue<FutureTask<File>> que = new LinkedList<>();
        while (f < 5) {
            int finalA = a;
            Callable<File> task1 = () -> {
                File new_f = new File(dir1, name+ finalA +ext);
                Thread.sleep(100+(int) (Math.random()*900));
                return new_f;
            };


            FutureTask<File> future = new FutureTask<>(task1);
            new Thread(future).start();
            que.add(future);
            a++;
            f++;

            Thread.sleep((int) new File(name + a + ext).length() * 7L);

            System.out.println(que.poll().get().getName());
            future.cancel(true);
            f--;
        }
    }
}
