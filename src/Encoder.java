import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Encoder {
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    private static int[][] colorY;
    private static int[][] colorCb;
    private static int[][] colorCr;
    //private static BufferedWriter writer;
    private static FileOutputStream fout;
    private static List<Byte> imgbyte = new ArrayList<>();
    private static StringBuilder writeCode = new StringBuilder();

    private static int [][] qY = new int[][] {
            {16,11,10,16,24,40,51,61},
            {12,12,14,19,26,58,60,55},
            {14,13,16,24,40,57,69,56},
            {14,17,22,29,51,87,80,62},
            {18,22,37,56,68,109,103,77},
            {24,35,55,64,81,104,113,92},
            {49,64,78,87,103,121,120,101},
            {72,92,95,98,112,100,103,99}
    };

    private int [][] qCbCr = new int[][] {

    };

    public static int compress(int [][] src, int oldDC, StringBuilder code) throws IOException{
        double [][] dctOut =  new double [8][8];
        dct(src,dctOut);

        //printdct(dctOut);

        int [][] quantOut = new int [8][8];
        quantization(dctOut, quantOut, qY);

        //printqt(quantOut);

        int newDC = quantOut[0][0];
        quantOut[0][0] -= oldDC;

        int [] zigzagOut = new int[64];
        zigZag(quantOut,zigzagOut);

        int [] runOut = new int [128];
        int rl = RLE(zigzagOut,runOut);

        //printrle(runOut,rl);
        Encode(runOut,rl,code);

        return newDC;
    }

    public static void printdct(double[][] dctOut) throws  IOException{
        BufferedWriter tempwriter = new BufferedWriter(new FileWriter("dctin.txt",true));
        for(int i=0;i<8;i++){
            for(int j=0;j<8 ;j++)
                tempwriter.append(""+(int)dctOut[i][j] + " ");
        }
        tempwriter.append('\n');
        tempwriter.close();
    }

    public static void printqt(int[][] qtOut) throws  IOException{
        BufferedWriter tempwriter = new BufferedWriter(new FileWriter("qtin.txt",true));
        for(int i=0;i<8;i++){
            for(int j=0;j<8 ;j++)
                tempwriter.append(""+(int)qtOut[i][j] + " ");
        }
        tempwriter.append('\n');
        tempwriter.close();
    }

    public static void printrle(int[] rleOut,int rl) throws  IOException{
        BufferedWriter tempwriter = new BufferedWriter(new FileWriter("rlein.txt",true));
        for(int i=0;i<rl;i++){
                tempwriter.append(""+(int)rleOut[i] + " ");
        }
        tempwriter.append('\n');
        tempwriter.close();
    }

    public static void dct(int [][] src, double[][] dest){
        for(int u = 0; u < 8; u++){
            for(int v = 0; v < 8; v++){
                double sum = 0.0;
                double coeff = 1.0/4.0;
                if(u == 0){
                    coeff /= Math.sqrt(2);
                }
                if(v == 0){
                    coeff /= Math.sqrt(2);
                }

                for(int x = 0; x < 8 ; x++){
                    for(int y = 0; y < 8; y++){
                        sum += src[x][y] * Math.cos((2*x + 1)*u*Math.PI/16) * Math.cos((2*y + 1)*v*Math.PI/16);
                    }
                }

                dest[u][v] = coeff * sum;
            }
        }
    }

    public static void quantization(double [][] src, int dest[][], int q [][]){
        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++)
                dest[i][j] =  (int)src[i][j]/q[i][j];
        }
    }

    public static void zigZag(int [][] src, int[] dest) {
        int size = 8;
        int i = 1;
        int j = 1;
        for (int element = 0; element < size * size; element++)
        {
//            data[i - 1][j - 1] = element;
            dest[element] = src[i-1][j-1];
            if ((i + j) % 2 == 0)
            {
                // Even stripes
                if (j < size)
                    j++;
                else
                    i+= 2;
                if (i > 1)
                    i--;
            }
            else
            {
                // Odd stripes
                if (i < size)
                    i++;
                else
                    j+= 2;
                if (j > 1)
                    j--;
            }
        }
    }

    public static int RLE(int [] ZZ, int [] RL){
        int rl = 1;
        int i = 1;
        int k = 0;
        RL[0] = ZZ[0];
        while(i<64)
        {
            k=0;
            while((i<64)&&(ZZ[i]==0)&&(k<15))
            {
                i++;
                k++;
            }
            if(i==64)
            {
                RL[rl++] = 0;
                RL[rl++] = 0;
            }
            else
            {
                RL[rl++] = k;
                RL[rl++] = ZZ[i++];
            }
        }
        if(!(RL[rl-1]==0 && RL[rl-2]==0))
        {
            RL[rl++] = 0;
            RL[rl++] = 0;
        }
        while(rl-4 > 0 && RL[rl-4] == 15 && RL[rl-3] == 0){
            rl-=2;
        }
        RL[rl-2] = 0;
//        if((RL[rl-4]==15)&&(RL[rl-3]==0))
//        {
//            RL[rl-4]=0;
//            rl-=2;
//        }
        return rl;
    }

    public static void Encode(int [] RL,int rl, StringBuilder output){
        StringBuilder b = new StringBuilder();
        int blen=0;
        blen = getDCcode(RL[0],b);
        output.setLength(0);
        output.append(b);
        int i;
        for(i=1;i<rl;i+=2)
        {
            blen = getACcode(RL[i],RL[i+1],b);
            output.append(b);
        }
    }

    public static int getDCcode(int a, StringBuilder b){
        int[] codeLen = {3,4,5,5,7,8,10,12,14,16,18,20};
        String[] code = {"010","011","100","00","101","110","1110","11110","111110","1111110","11111110","111111110"};
        int cat = getCat(a);
        int lenb = codeLen[cat];
        b.setLength(0);
        b.append(code[cat]);
        b.setLength(lenb);
        int j;
        int c = a;
        if(a<0)
            c+=(int)Math.pow(2,cat)-1;
        for(j=lenb-1;j>lenb-cat-1;j--)
        {
            if(c%2==1)
                b.setCharAt(j,'1');
            else
                b.setCharAt(j,'0');
            c/=2;
        }
//        b.setCharAt(lenb,'\0');
        return lenb;
    }

    public static int getACcode(int n,int a, StringBuilder b)
    {
        int[][] codeLen = {
                {4 ,3 ,4 ,6 ,8 ,10,12,14,18,25,26},
                {0 ,5 ,8 ,10,13,16,22,23,24,25,26},
                {0 ,6 ,10,13,20,21,22,23,24,25,26},
                {0 ,7 ,11,14,20,21,22,23,24,25,26},
                {0 ,7 ,12,19,20,21,22,23,24,25,26},
                {0 ,8 ,12,19,20,21,22,23,24,25,26},
                {0 ,8 ,13,19,20,21,22,23,24,25,26},
                {0 ,9 ,13,19,20,21,22,23,24,25,26},
                {0 ,9 ,17,19,20,21,22,23,24,25,26},
                {0 ,10,18,19,20,21,22,23,24,25,26},
                {0 ,10,18,19,20,21,22,23,24,25,26},
                {0 ,10,18,19,20,21,22,23,24,25,26},
                {0 ,11,18,19,20,21,22,23,24,25,26},
                {0 ,12,18,19,20,21,22,23,24,25,26},
                {0 ,13,18,19,20,21,22,23,24,25,26},
                {12,17,18,19,20,21,22,23,24,25,26}
    };
  String[][] code = {
          {"1010", "00", "01", "100", "1011", "11010", "111000", "1111000", "1111110110", "1111111110000010", "1111111110000011"},
          {"", "1100", "111001", "1111001", "111110110", "11111110110", "1111111110000100", "1111111110000101", "1111111110000110", "1111111110000111", "1111111110001000"},
          {"", "11011", "11111000", "1111110111", "1111111110001001", "1111111110001010", "1111111110001011", "1111111110001100", "1111111110001101", "1111111110001110", "1111111110001111"},
          {"", "111010", "111110111", "11111110111", "1111111110010000", "1111111110010001", "1111111110010010", "1111111110010011", "1111111110010100", "1111111110010101", "1111111110010110"},
          {"", "111011", "1111111000", "1111111110010111", "1111111110011000", "1111111110011001", "1111111110011010", "1111111110011011", "1111111110011100", "1111111110011101", "1111111110011110"},
          {"", "1111010", "1111111001", "1111111110011111", "1111111110100000", "1111111110100001", "1111111110100010", "1111111110100011", "1111111110100100", "1111111110100101", "1111111110100110"},
          {"", "1111011", "11111111000", "1111111110100111", "1111111110101000", "1111111110101001", "1111111110101010", "1111111110101011", "1111111110101100", "1111111110101101", "1111111110101110"},
          {"", "11111001", "11111111001", "1111111110101111", "1111111110110000", "1111111110110001", "1111111110110010", "1111111110110011", "1111111110110100", "1111111110110101", "1111111110110110"},
          {"", "11111010", "111111111000000", "1111111110110111", "1111111110111000", "1111111110111001", "1111111110111010", "1111111110111011", "1111111110111100", "1111111110111101", "1111111110111110"},
          {"", "111111000", "1111111110111111", "1111111111000000", "1111111111000001", "1111111111000010", "1111111111000011", "1111111111000100", "1111111111000101", "1111111111000110", "1111111111000111"},
          {"", "111111001", "1111111111001000", "1111111111001001", "1111111111001010", "1111111111001011", "1111111111001100", "1111111111001101", "1111111111001110", "1111111111001111", "1111111111010000"},
          {"", "111111010", "1111111111010001", "1111111111010010", "1111111111010011", "1111111111010100", "1111111111010101", "1111111111010110", "1111111111010111", "1111111111011000", "1111111111011001"},
          {"", "1111111010", "1111111111011010", "1111111111011011", "1111111111011100", "1111111111011101", "1111111111011110", "1111111111011111", "1111111111100000", "1111111111100001", "1111111111100010"},
          {"", "11111111010", "1111111111100011", "1111111111100100", "1111111111100101", "1111111111100110", "1111111111100111", "1111111111101000", "1111111111101001", "1111111111101010", "1111111111101011"},
          {"", "111111110110", "1111111111101100", "1111111111101101", "1111111111101110", "1111111111101111", "1111111111110000", "1111111111110001", "1111111111110010", "1111111111110011", "1111111111110100"},
          {"111111110111","1111111111110101","1111111111110110","1111111111110111","1111111111111000","1111111111111001","1111111111111010","1111111111111011","1111111111111100","1111111111111101","1111111111111110"}
    };

        int cat = getCat(a);
        int lenb = codeLen[n][cat];
        b.setLength(0);
        b.append(code[n][cat]);
        b.setLength(lenb);
        int c = a;
        if(a<0)
            c+=(int)Math.pow(2,cat)-1;
        for(int j=lenb-1;j>lenb-cat-1;j--)
        {
            if(c%2==1)
                b.setCharAt(j,'1');
            else
                b.setCharAt(j,'0');
            c/=2;
        }
//        b.setCharAt(lenb,'\0');
        return lenb;
    }

    public static int getCat(int a)
    {
        if(a==0)
            return 0;
        else if(Math.abs(a)<=1)
            return 1;
        else if(Math.abs(a)<=3)
            return 2;
        else if(Math.abs(a)<=7)
            return 3;
        else if(Math.abs(a)<=15)
            return 4;
        else if(Math.abs(a)<=31)
            return 5;
        else if(Math.abs(a)<=63)
            return 6;
        else if(Math.abs(a)<=127)
            return 7;
        else if(Math.abs(a)<=255)
            return 8;
        else if(Math.abs(a)<=511)
            return 9;
        else if(Math.abs(a)<=1023)
            return 10;
        else if(Math.abs(a)<=2047)
            return 11;
        else if(Math.abs(a)<=4095)
            return 12;
        else if(Math.abs(a)<=8191)
            return 13;
        else if(Math.abs(a)<=16383)
            return 14;
        else
            return 15;
    }

    public static void loadImage(String filename) throws IOException{
        Mat image = Imgcodecs.imread(filename);
        Imgcodecs.imwrite("inputGray.png", image);
        System.out.println(image.rows() + " " + image.cols());
        int[][] Y = new int[image.rows()][image.cols()];
        int[][] Cb = new int[image.rows()][image.cols()];
        int[][] Cr = new int[image.rows()][image.cols()];
        double b, g, r;
        for (int i = 0; i < image.rows(); i++) {
            for (int j = 0; j < image.cols(); j++) {
                b = image.get(i, j)[0];
                g = image.get(i, j)[1];
                r = image.get(i, j)[2];

                Y[i][j] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                Cb[i][j] = (int) (128 - 0.169* r - 0.331 * g + 0.5 * b);
                Cr[i][j] = (int) (128 + 0.5* r - 0.419 * g - 0.081 * b);
            }
        }

        //downsampling
        int rows = image.rows();
        int cols = image.cols();
        int downrows = (rows + 1)/2;
        int downcols = (cols + 1)/2;
        int[][] CbDown = new int[downrows][downcols];
        int[][] CrDown = new int[downrows][downcols];
        for(int i=0;i<downrows;i++){
            for(int j=0;j<downcols;j++){
                int n = 1;
                int tempCb = Cb[2*i][2*j],tempCr = Cr[2*i][2*j];
                if(2*i + 1 < rows && 2*j + 1 < cols){
                    tempCb += Cb[2*i + 1][2*j + 1];
                    tempCr += Cr[2*i + 1][2*j + 1];
                    n++;
                }
                if(2*i + 1 < rows){
                    tempCb += Cb[2*i + 1][2*j];
                    tempCr += Cr[2*i + 1][2*j];
                    n++;
                }
                if(2*j + 1 < cols){
                    tempCb += Cb[2*i][2*j + 1];
                    tempCr += Cr[2*i][2*j + 1];
                    n++;
                }
                CbDown[i][j] = tempCb/n;
                CrDown[i][j] = tempCr/n;
            }
        }

        colorY = Y;
        colorCb = CbDown;
        colorCr = CrDown;
        //writer = new BufferedWriter(new FileWriter("output",false));
        fout = new FileOutputStream("encoded", false);
    }

    public static void compressImage(int[][] img) throws IOException {
        int [][] block = new int [8][8];
        int DCcomp = 0;
        StringBuilder code = new StringBuilder();
        int maxX = img[0].length;
        int maxY = img.length;
        int numblockX = (maxX + 7)/8;
        int numblockY = (maxY + 7)/8;
       // writer.append((char)numblockX).append((char)numblockY);
        imgbyte.add((byte)numblockX);
        imgbyte.add((byte)numblockY);

        for(int i=0;i<numblockY;i++){
            System.out.println("Processing row: "+i);
            for(int j=0;j<numblockX;j++){
                for(int i1=0;i1<8;i1++){
                    for(int j1=0;j1<8;j1++){
                        if(i*8 + i1 < maxY && j*8 + j1<maxX){
                            block[i1][j1] = img[i*8+i1][j*8+j1] - 128;
                        }else{
                            block[i1][j1] = 0;
                        }
                    }
                }
//                System.out.println("Processing block: "+i+"*"+j);
                DCcomp = compress(block,DCcomp,code);
                writeCode.append(code);
            }
        }
        writeCode.append("111111111");
    }

    public static void writetoFile(StringBuilder a) throws IOException{
        int len = a.length();
        int b = 0;
        int c = (len+7)/8;
        int i,j;
        char d;
        for(i=0;i<c;i++)
        {
            b = 0;
            for(j=0;j<8;j++)
                if((i*8+j<len)&&(a.charAt(i*8+j)=='1'))
                    b = b | (int)Math.pow(2,7-j);
//            d = (char)b;
            //writer.append(d);
            imgbyte.add((byte)b);
        }

        byte[] imgbyte2 = new byte[imgbyte.size()];
        for(i=0;i<imgbyte.size();i++){
            imgbyte2[i] = imgbyte.get(i);
        }

        fout.write(imgbyte2);
        fout.close();
       // writer.close();
    }

    public static void main(String [] args) throws IOException {
        String imgname = "C:\\Users\\Dhruvang\\Desktop\\siv project\\images 2\\img3.png";
        loadImage(imgname);
        compressImage(colorY);
        compressImage(colorCb);
        compressImage(colorCr);
        writetoFile(writeCode);
    }
}
