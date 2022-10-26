package org.example;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.List;
import org.apache.commons.io.FileUtils;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        task1("./test.txt");

        File fromFile = new File("test.txt").getAbsoluteFile();
        File toFile = new File("./toFile.exe").getAbsoluteFile();
        Path fileToDeletePath = toFile.toPath();

        sum(fromFile);
        System.out.println();

        Runtime runtime = Runtime.getRuntime();
        long startMemory = runtime.totalMemory();
        long start = System.currentTimeMillis();
        method1(fromFile,toFile);
        long finish = System.currentTimeMillis();
        long finishMemory = runtime.freeMemory();
        System.out.println("Через FileStream:");
        System.out.println("Затраты памяти: " + ((startMemory - finishMemory) / 1024) + " kb");
        System.out.println("Затраты времени: " + (finish - start) + " ms\n");
        Files.delete(fileToDeletePath);

        startMemory = runtime.totalMemory();
        start = System.currentTimeMillis();
        method2(fromFile,toFile);
        finish = System.currentTimeMillis();
        finishMemory = runtime.freeMemory();
        System.out.println("Через FileChannel:");
        System.out.println("Затраты памяти: " + ((startMemory - finishMemory) / 1024) + " kb");
        System.out.println("Затраты времени: " + (finish - start) + " ms\n");
        Files.delete(fileToDeletePath);

        startMemory = runtime.totalMemory();
        start = System.currentTimeMillis();
        method3(fromFile,toFile);
        finish = System.currentTimeMillis();
        finishMemory = runtime.freeMemory();
        System.out.println("С помощью ApacheCommonsIO:");
        System.out.println("Затраты памяти: " + ((startMemory - finishMemory) / 1024) + " kb");
        System.out.println("Затраты времени: " + (finish - start) + " ms\n");
        Files.delete(fileToDeletePath);

        startMemory = runtime.totalMemory();
        start = System.currentTimeMillis();
        method4(fromFile,toFile);
        finish = System.currentTimeMillis();
        finishMemory = runtime.freeMemory();
        System.out.println("Через класс Files:");
        System.out.println("Затраты памяти: " + ((startMemory - finishMemory) / 1024) + " kb");
        System.out.println("Затраты времени: " + (finish - start) + " ms\n");
        Files.delete(fileToDeletePath);

        task4();
    }
    private static void task1(String s) throws IOException {
        Path path = Paths.get(new File(s).getAbsolutePath());
        List<String> list = Files.readAllLines(path);
        for (String str : list)
            System.out.println(str);
    }
    private static void method1(File fromFile, File toFile) throws IOException {
        InputStream in = new FileInputStream(fromFile);
        OutputStream out = new FileOutputStream(toFile);
        byte[] buffer = new byte[128000000];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer,0, length);
        }
        in.close();
        out.close();
    }
    private static void method2(File fromFile, File toFile) throws IOException {
        FileChannel fc1 = new FileInputStream(fromFile).getChannel();
        FileChannel fc2 = new FileOutputStream(toFile).getChannel();
        fc2.transferFrom(fc1, 0, fc1.size());
        fc1.close();
        fc2.close();
    }
    private static void method3(File fromFile, File toFile) throws IOException {
        FileUtils.copyFile(fromFile, toFile);
    }
    private static void method4(File fromFile, File toFile) throws IOException {
        Files.copy(fromFile.toPath(), toFile.toPath());
    }
    private static int sum(ByteBuffer bb) {
        int sum = 0;
        while (bb.hasRemaining()) {
            if ((sum & 1) != 0)
                sum = (sum >> 1) + 0x8000;
            else
                sum >>= 1;
            sum += bb.get() & 0xff;
            sum &= 0xffff;
        }
        return sum;
    }

    private static void sum(File file) throws IOException {
        // Получение канала из потока
        try (FileInputStream fis = new FileInputStream(file);
                FileChannel fc = fis.getChannel()) {

            int size = (int) fc.size();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, size);

            System.out.println(file);
            System.out.println("Контрольная сумма: " +sum(bb));

        }
    }

    private static void task4() throws IOException, InterruptedException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path path = new File("").getAbsoluteFile().toPath();
        path.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        WatchKey key;
        while ((key = watchService.take()) != null) {
            for (WatchEvent event : key.pollEvents()) {
                System.out.println(event);
                if ("ENTRY_CREATE".equals(event.kind().toString())) {
                    System.out.println("======== ENTRY_CREATE ========");
                    System.out.println(event.context());
                } else if ("ENTRY_MODIFY".equals(event.kind().toString()) && !event.context().toString().contains("git")) {
                    System.out.println("======== ENTRY_MODIFY ========" + event.context());
                    ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "git diff");
                    builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                    builder.directory(new File("./").getAbsoluteFile());
                    builder.start();
                    builder = new ProcessBuilder("cmd.exe", "/c", "git add .");
                    builder.directory(new File("./").getAbsoluteFile());
                    builder.start();
                } else if ("ENTRY_DELETE".equals(event.kind().toString()) && !event.context().toString().contains("git")){
                    System.out.println("======== ENTRY_DELETE ========");
                    //sum(new File("C:\\Users\\Harlequin\\IdeaProjects\\PR2\\untitled\\\pr4" + event.context()));
                }
            }
            key.reset();
        }
    }
}