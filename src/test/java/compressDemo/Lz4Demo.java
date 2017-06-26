package compressDemo;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by mercop on 2017/6/26.
 */
public class Lz4Demo {

    public static void encode(File file, File outputFile,LZ4Compressor lz4Compressor,LZ4FastDecompressor lz4FastDecompressor)
    {
        try
        {
            FileChannel fileChannel = new RandomAccessFile(file,"r").getChannel();
            int bufferSize = (int)fileChannel.size();
            ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
            byte[] data = new byte[bufferSize];
            fileChannel.read(byteBuffer);
            byteBuffer.flip();
            byteBuffer.get(data);

            System.out.println(lz4Compressor.getClass().getSimpleName() +" "+ lz4FastDecompressor.getClass().getSimpleName());
            FileOutputStream fos= new FileOutputStream(outputFile);

            long start = System.currentTimeMillis();

            byte[] result= lz4Compressor.compress(data);
            System.out.print("comparess used time: " + (System.currentTimeMillis() - start) + " compress rate: " + result.length * 1.0 / data.length);
            fos.write(result);
            fos.close();

            start = System.currentTimeMillis();
            byte[] dresult = new byte[bufferSize];
            lz4FastDecompressor.decompress(result,dresult);
            System.out.println(" decompress used time: " + (System.currentTimeMillis() - start) + "success: " + (dresult.length == data.length));


        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void testEncode(){
        //used time: para: 9 17177 compress rate: 0.33581806117433816
        //used time: para: 1 2347 compress rate: 0.408804398681972
        File input = new File("C:\\home\\admin\\sync_results\\78517gs8re\\Result1.rs");
        File output = new File("D:\\test");

        LZ4Factory factory= LZ4Factory.safeInstance();

        encode(input,output,factory.highCompressor(1),factory.fastDecompressor());

        encode(input,output,factory.fastCompressor(),factory.fastDecompressor());

    }

}
