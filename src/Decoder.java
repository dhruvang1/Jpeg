import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Decoder {
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
    private static int maxX=0;
    private static int maxY=0;
    private static int[][] colorY;
    private static int[][] colorCb;
    private static int[][] colorCr;

    public static void getInput(String filename, StringBuilder input) throws IOException {
        Path path = Paths.get(filename);
        byte[] data = Files.readAllBytes(path);
        maxX = (data[0] > 0 ? data[0] : data[0] + 256);
        maxY = (data[1] > 0 ? data[1] : data[1] + 256);
        colorY = new int[8*maxY][8*maxX];

        maxX = (data[2] > 0 ? data[2] : data[2] + 256);
        maxY = (data[3] > 0 ? data[3] : data[3] + 256);
        colorCb = new int[8*maxY][8*maxX];

        maxX = (data[4] > 0 ? data[4] : data[4] + 256);
        maxY = (data[5] > 0 ? data[5] : data[5] + 256);
        colorCr = new int[8*maxY][8*maxX];

        for(int i=6;i<data.length;i++){
            int current = (data[i] > 0 ? data[i] : data[i] + 256);
            StringBuilder temp = new StringBuilder();
            for(int k=7;k>=0;k--){
                if(current % 2 == 1){
                    temp.append('1');
                }else{
                    temp.append('0');
                }
                current /= 2;
            }
            temp.reverse();
            input.append(temp);
        }
    }

    public static int[] getACvalue(StringBuilder input, int pos, int n, int p)
    {
        int isMatch,len,i,j,k;
        int [] ans = new int[3];
        int [][] codeLen = {
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
                {"1010",  "00",  "01",  "100",  "1011",  "11010",  "111000",  "1111000",  "1111110110",  "1111111110000010",  "1111111110000011"},
                {"","1100","111001","1111001","111110110","11111110110","1111111110000100","1111111110000101","1111111110000110","1111111110000111","1111111110001000"},
                {"","11011","11111000","1111110111","1111111110001001","1111111110001010","1111111110001011","1111111110001100","1111111110001101","1111111110001110","1111111110001111"},
                {"","111010","111110111","11111110111","1111111110010000","1111111110010001","1111111110010010","1111111110010011","1111111110010100","1111111110010101","1111111110010110"},
                {"","111011","1111111000","1111111110010111","1111111110011000","1111111110011001","1111111110011010","1111111110011011","1111111110011100","1111111110011101","1111111110011110"},
                {"","1111010","1111111001","1111111110011111","1111111110100000","1111111110100001","1111111110100010","1111111110100011","1111111110100100","1111111110100101","1111111110100110"},
                {"","1111011","11111111000","1111111110100111","1111111110101000","1111111110101001","1111111110101010","1111111110101011","1111111110101100","1111111110101101","1111111110101110"},
                {"","11111001","11111111001","1111111110101111","1111111110110000","1111111110110001","1111111110110010","1111111110110011","1111111110110100","1111111110110101","1111111110110110"},
                {"","11111010","111111111000000","1111111110110111","1111111110111000","1111111110111001","1111111110111010","1111111110111011","1111111110111100","1111111110111101","1111111110111110"},
                {"","111111000","1111111110111111","1111111111000000","1111111111000001","1111111111000010","1111111111000011","1111111111000100","1111111111000101","1111111111000110","1111111111000111"},
                {"","111111001","1111111111001000","1111111111001001","1111111111001010","1111111111001011","1111111111001100","1111111111001101","1111111111001110","1111111111001111","1111111111010000"},
                {"","111111010","1111111111010001","1111111111010010","1111111111010011","1111111111010100","1111111111010101","1111111111010110","1111111111010111","1111111111011000","1111111111011001"},
                {"","1111111010","1111111111011010","1111111111011011","1111111111011100","1111111111011101","1111111111011110","1111111111011111","1111111111100000","1111111111100001","1111111111100010"},
                {"","11111111010","1111111111100011","1111111111100100","1111111111100101","1111111111100110","1111111111100111","1111111111101000", "1111111111101001","1111111111101010","1111111111101011"},
                {"","111111110110","1111111111101100","1111111111101101","1111111111101110","1111111111101111","1111111111110000","1111111111110001","1111111111110010","1111111111110011","1111111111110100"},
                {"111111110111","1111111111110101","1111111111110110","1111111111110111","1111111111111000","1111111111111001","1111111111111010","1111111111111011","1111111111111100","1111111111111101","1111111111111110"}
        };

        for(k=0;k<16;k++){
            for(i=0;i<11;i++){
                isMatch = 1;
                len = code[k][i].length();
                for(j=0;j<len;j++){
                    if(code[k][i].charAt(j)!=input.charAt(pos+j))
                        isMatch = 0;
                }
                if ((isMatch==1)&&(!(k!=0 && k!=15 && i==0)))
                {
                    pos+=len;
                    len = codeLen[k][i]-len;
                    if(len==0)
                    {
                        n=k;
                        p=0;
                        ans[0] = pos; ans[1] = n; ans[2] = p;
                        return ans;
                    }
                    n = k;
                    p=0;
                    for(j=0;j<len;j++)
                    {
                        p*=2;
                        if(input.charAt(pos+j)=='1')
                            p++;
                    }
                    if(input.charAt(pos)=='0')
                        p = p + 1 - (int)Math.pow(2,len);
                    pos+=len;
                    ans[0] = pos; ans[1] = n; ans[2] = p;
                    return ans;
                }
            }
        }
        ans[0] = pos; ans[1] = n; ans[2] = p;
        return ans;
    }

    public static int[] getDCvalue(StringBuilder input, int pos)
    {
        int [] ans = new int [2];
        int isMatch,len,i,j,p;
        int [] codeLen = {3,4,5,5,7,8,10,12,14,16,18,20};
        String [] code = {"010","011","100","00","101","110","1110",
            "11110","111110","1111110","11111110","111111110"};
        for(i=0;i<12;i++)
        {
            isMatch = 1;
            len = code[i].length();
            for(j=0;j<len;j++){
                if(code[i].charAt(j)!= input.charAt(pos+j)){
                    isMatch = 0;
                }
            }
            if (isMatch==1)
            {
                pos+=len;
                len = codeLen[i]-len;
                if(len==0){
                    ans[0] = 0;
                    ans[1] = pos;
                    return ans;
                }
                p=0;
                for(j=0;j<len;j++)
                {
                    p *= 2;
                    if(input.charAt(pos+j)=='1')
                        p++;
                }
                if(input.charAt(pos)=='0'){
                    p = p + 1 - (int)Math.pow(2,len);
                }
                pos+=len;
                ans[0]=p;
                ans[1]=pos;
                
                return ans;
            }
        }
        ans[0] = 0;
        ans[1] = pos;
        return ans;
    }

    public static void RLED(int[] ZZ,int[] RL)
    {
        int rl=1;
        int i=1;
        int k = 0;
        ZZ[0] = RL[0];
        while(i<64)
        {
            if(RL[rl]==0 && RL[rl+1]==0)
            {
                for(k=i;k<64;k++)
                    ZZ[k] = 0;
                return;
            }
            for(k=0;k<RL[rl];k++)
                ZZ[i++] = 0;
            ZZ[i++] = RL[rl+1];
            rl+=2;
        }
    }

    public static void ZigZagD(int[][] QF,int[] ZZ)
    {
        int i=0,j=0,k=0,d=0;
        while(k<36)
        {
            QF[i][j] = ZZ[k++];
            if((i==0)&&(j%2==0))
            {
                j++;
                d=1;
            }
            else if((j==0)&&(i%2==1))
            {
                i++;
                d=0;
            }
            else if(d==0)
            {
                i--;
                j++;
            }
            else
            {
                i++;
                j--;
            }
        }
        i = 7;
        j = 1;
        while(k<64)
        {
            QF[i][j] = ZZ[k++];
            if((i==7)&&(j%2==0))
            {
                j++;
                d=0;
            }
            else if((j==7)&&(i%2==1))
            {
                i++;
                d=1;
            }
            else if(d==0)
            {
                i--;
                j++;
            }
            else
            {
                i++;
                j--;
            }
        }
    }

    public static void QuantizeD(int[][] F, int[][] QF)
    {
        int[][] q = {
                {16,11,10,16,24,40,51,61},
                {12,12,14,19,26,58,60,55},
                {14,13,16,24,40,57,69,56},
                {14,17,22,29,51,87,80,62},
                {18,22,37,56,68,109,103,77},
                {24,35,55,64,81,104,113,92},
                {49,64,78,87,103,121,120,101},
                {72,92,95,98,112,100,103,99}
                };
        int i,j;
        for(i=0;i<8;i++)
            for(j=0;j<8;j++)
                F[i][j] = QF[i][j]*q[i][j];
    }

    public static double C(int u)
    {
        if(u==0)
            return (1.0/Math.sqrt(8.0));
        else
            return (1.0/2.0);
    }


    public static void DCTD(int[][] f,int[][] F)
    {
        double a;
        for(int x=0;x<8;x++)
            for(int y=0;y<8;y++)
            {
                a = 0.0;
                for(int u=0;u<8;u++)
                    for(int v=0;v<8;v++)
                        a += C(u)*C(v)*F[u][v]*Math.cos((2.0*x+1.0)*(u)*3.14/16.0)*Math.cos((2.0*y+1.0)*v*3.14/16.0);
                f[x][y] = (int)a;
            }
    }

    public static void decompressImage(String filename) throws IOException{
        StringBuilder input = new StringBuilder();
        getInput(filename,input);
//        maxX *= 8;
//        maxY *= 8;
//        image = new int[maxY][maxX];
//        System.out.println("Image size: " + maxY + "*" + maxX);

        int pos = 0;
        int [][] image;
        for(int g=0;g<3;g++){
            if(g==0){
                image = colorY;
            }else if(g==1){
                image = colorCb;
            }else{
                image = colorCr;
            }

            maxY = image.length;
            maxX = image[0].length;

            int DCval = 0;
            int end = 0;
            int n,p=0,i,j;
            int ypos = 0;
            int xpos = 0;
            int[] RL = new int[128];
            int[] ZZ = new int[64];
            int[][] QF = new int[8][8];
            int[][] F = new int[8][8];
            int[][] f = new int[8][8];
            int rl = 0;
            while(end==0)
            {
//                System.out.println("Processing Block : "+ypos+"*"+xpos);
                rl = 0;
                int[] temp;
                temp = getDCvalue(input,pos);
                DCval += temp[0];
                pos = temp[1];

                n=1;
                RL[rl++] = DCval;
                while(!(n==0 && p==0))
                {
                    int temp2[];
                    temp2 = getACvalue(input,pos,n,p);
                    pos = temp2[0]; n = temp2[1]; p = temp2[2];
                    RL[rl++] = n;
                    RL[rl++] = p;
                }
                end = 1;

//            printrle(RL,rl);

                RLED(ZZ,RL);
                ZigZagD(QF,ZZ);

//            printqt(QF);

                QuantizeD(F,QF);

//            printdct(F);

                DCTD(f,F);
                for(i=0;i<8;i++)
                    for(j=0;j<8;j++)
                        image[ypos*8+i][xpos*8+j]=f[i][j]+128;
                xpos++;
                if(xpos==(maxX/8))
                {
                    System.out.println("Processing Row : "+ypos);
                    xpos = 0;
                    ypos++;
                }
                for(i=0;i<9;i++)
                {
                    if(input.charAt(pos+i)=='0')
                        end=0;
                }
                if(end==1){
                    pos += 9;
                }
            }
        }

    }

    public static void printdct(int[][] dctOut) throws  IOException{
        BufferedWriter tempwriter = new BufferedWriter(new FileWriter("dctout.txt",true));
        for(int i=0;i<8;i++){
            for(int j=0;j<8 ;j++)
                tempwriter.append(""+dctOut[i][j] + " ");
        }
        tempwriter.append('\n');
        tempwriter.close();
    }

    public static void printrle(int[] rleOut,int rl) throws  IOException{
        BufferedWriter tempwriter = new BufferedWriter(new FileWriter("rleout.txt",true));
        for(int i=0;i<rl;i++){
            tempwriter.append(""+rleOut[i] + " ");
        }
        tempwriter.append('\n');
        tempwriter.close();
    }

    public static void printqt(int[][] qtOut) throws  IOException{
        BufferedWriter tempwriter = new BufferedWriter(new FileWriter("qtout.txt",true));
        for(int i=0;i<8;i++){
            for(int j=0;j<8 ;j++)
                tempwriter.append(""+(int)qtOut[i][j] + " ");
        }
        tempwriter.append('\n');
        tempwriter.close();
    }

    public static void main(String[] args) throws  IOException{
        String filename = "encoded";
        decompressImage(filename);
        Mat imgmat = new Mat(colorY.length,colorY[0].length,CvType.CV_8UC3);
        double r,g,b;
        int y,cb,cr;
        for(int i=0;i<imgmat.rows();i++){
            for(int j=0;j<imgmat.cols();j++){
                y = colorY[i][j];
                cb = colorCb[i/2][j/2];
                cr = colorCr[i/2][j/2];
                r = 1.0*y + 1.4*(cr - 128);
                g = 1.0*y - 0.343*(cb - 128) -0.711* (cr - 128);
                b = 1.0*y + 1.765*(cb - 128);

                double[] temp = {b,g,r};
                imgmat.put(i,j,temp);
            }
        }
        Imgcodecs.imwrite("decoded.png",imgmat);
    }
}
