import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class Error {
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String [] args){
        String original = "C:\\Users\\Dhruvang\\Desktop\\siv project\\images 2\\img1.png";
        String duplicate = "C:\\Users\\Dhruvang\\Desktop\\siv project\\images 2\\compress\\img1o.jpg";
        Mat a = Imgcodecs.imread(original);
        Mat b = Imgcodecs.imread(duplicate);
        int rows = a.rows();
        int cols = a.cols();
        double er =0.0, eb =0.0, eg=0.0;
        for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                eb += (a.get(i,j)[0] - b.get(i,j)[0])*(a.get(i,j)[0] - b.get(i,j)[0]);
                eg += (a.get(i,j)[1] - b.get(i,j)[1])*(a.get(i,j)[1] - b.get(i,j)[1]);
                er += (a.get(i,j)[2] - b.get(i,j)[2])*(a.get(i,j)[2] - b.get(i,j)[2]);
            }
        }
        eb = Math.sqrt(eb);
        eg = Math.sqrt(eg);
        er = Math.sqrt(er);
        eb /= rows*cols;
        er /= rows*cols;
        eg /= rows*cols;
        System.out.println(""+er+" "+eg+" "+eb);
    }

}
