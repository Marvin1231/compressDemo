package compressDemo;

import org.junit.Test;
import org.xerial.snappy.Snappy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by mercop on 2017/6/26.
 */
public class SnappyDemo {

    public static void encode(File file, File outputFile) {
        try {
            FileChannel fileChannel = new RandomAccessFile(file, "r").getChannel();
            int bufferSize = (int) fileChannel.size();
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bufferSize);
            byte[] data = new byte[bufferSize];
            fileChannel.read(byteBuffer);
            byteBuffer.flip();
            byteBuffer.get(data);

            FileOutputStream fos = new FileOutputStream(outputFile);

            long start = System.currentTimeMillis();

            byte[] result = Snappy.compress(data);
            System.out.println("comparess used time: " + (System.currentTimeMillis() - start) + " compress rate: " + result.length * 1.0 / data.length);
            fos.write(result);
            fos.close();
            ByteBuffer byteBufferResult = ByteBuffer.allocateDirect(result.length);
            byteBufferResult.put(result);
            ByteBuffer byteBufferNew = ByteBuffer.allocateDirect(bufferSize);
            byte[] dresult = new byte[bufferSize];
            start = System.currentTimeMillis();
            //int i = Snappy.uncompress(byteBufferResult, byteBufferNew);
            Snappy.uncompress(result);
            System.out.println(" decompress used time: " + (System.currentTimeMillis() - start));


        } catch (Exception e) {

        }
    }
    @Test
    public void testEncode(){
        File input = new File("C:\\home\\admin\\sync_results\\78517gs8re\\Result1.rs");
        File output = new File("D:\\test");
        encode(input,output);
    }
}
