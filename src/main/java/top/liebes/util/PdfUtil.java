package top.liebes.util;

import ch.qos.logback.classic.Logger;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.slf4j.LoggerFactory;
import top.liebes.env.Env;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfUtil {
    private static Logger logger = (Logger) LoggerFactory.getLogger(PdfUtil.class);
    static {
        logger.setLevel(Env.LOG_LEVEL);
    }
    public static void generatePdfFile(String path, String source){
        // 生成File所在文件夹
        File file;
        try{
            file = new File(path);
            if(file.exists()){
                file.delete();
            }
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        // 生成PDF文档
        Document document = new Document();
        try{
            BaseFont bfChinese;
            bfChinese = BaseFont.createFont("STSongStd-Light","UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);

            Font topfont = new Font(bfChinese,14, Font.BOLD);
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();
            Paragraph paragraph = new Paragraph(source);
            paragraph.setFont(topfont);
            document.add(paragraph);
        }
        catch (DocumentException | IOException e){
            e.printStackTrace();
        }
        finally {
            document.close();
        }
    }
//    public static final String SRC = "resources/pdfs/hello.pdf";
//    public static final String DEST = "results/stamper/hello_highlighted.pdf";
//    public static void main(String[] args) throws IOException, DocumentException {
//        File file = new File(DEST);
//        file.getParentFile().mkdirs();
//        manipulatePdf(SRC, DEST);
//    }
//
//    public static void manipulatePdf(String src, String dest) throws IOException, DocumentException {
//        PdfReader reader = new PdfReader(src);
//        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
//        PdfContentByte canvas = stamper.getUnderContent(1);
//        canvas.saveState();
//        canvas.setColorFill(BaseColor.YELLOW);
//        canvas.rectangle(36, 786, 66, 16);
//        canvas.fill();
//        canvas.restoreState();
//        stamper.close();
//        reader.close();
//    }
}
