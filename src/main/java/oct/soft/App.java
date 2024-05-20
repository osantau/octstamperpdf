package oct.soft;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.util.Matrix;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {       
                final String dirPath=args[0];
		Set<String> files = new HashSet<>();
		try (Stream<Path> stream = Files.list(Paths.get(dirPath))) {
			/*
			 * stream.filter(file -> !Files.isDirectory(file) &&
			 * file.getFileName().toFile().getName().toLowerCase().endsWith("pdf") &&
			 * !file.getFileName().toFile().getName().toLowerCase().contains("atas"))
			 * .map(Path::getFileName) .map(Path::toString) .collect(Collectors.toSet());
			 */
			stream.forEach(inPdf -> {
				if (!Files.isDirectory(inPdf)) {

					if (inPdf.getFileName().toFile().getName().toLowerCase().endsWith("pdf")
							&& !inPdf.getFileName().toFile().getName().toLowerCase().contains("atas")) {
						files.add(FilenameUtils.separatorsToUnix(inPdf.toFile().getAbsolutePath()));
					}
				}
			});

		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
		AtomicInteger ai = new AtomicInteger(1);
                Map<String,String> mapPdfs = new HashMap<>();
		files.forEach(inPdf -> {
			                       
			try(PDDocument doc = Loader.loadPDF(new File(inPdf)))
			{
				for (final PDPage page: doc.getPages())
				{
					final PDType1Font font = new PDType1Font(FontName.HELVETICA);
					addWatermarkText(doc, page, font, "A se utiliza in PLASTOR !");
				}			
                                 String outPdf = FilenameUtils.separatorsToUnix(dirPath)+"/"+ai.getAndIncrement()+".pdf";
                                 doc.save(outPdf);
				 doc.close();	
                                 mapPdfs.put(outPdf, inPdf);
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		});
                for(String key:mapPdfs.keySet())
                {                    
                    try {
                        FileUtils.copyFile(new File(key), new File(mapPdfs.get(key)));
                        FileUtils.forceDelete(new File(key));
                    } catch (IOException ex) {
                        Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
	}
	
    private static void addWatermarkText(final PDDocument doc, final PDPage page, final PDFont font, final String text)
            throws IOException {
        try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true,
                true)) {
            final float fontHeight =72; // arbitrary for short text
            final float width = page.getMediaBox().getWidth()-50;
            final float height = page.getMediaBox().getHeight()-50;
            final float stringWidth = font.getStringWidth(text) / 1000 * fontHeight;
            final float diagonalLength = (float) Math.sqrt(width * width + height * height);
            final float angle = (float) Math.atan2(height, width);
            final float x = (diagonalLength - stringWidth) / 2; // "horizontal" position in rotated world
            final float y = -fontHeight / 4; // 4 is a trial-and-error thing, this lowers the text a bit
            cs.transform(Matrix.getRotateInstance(angle, 0, 0));
            cs.setFont(font, fontHeight);
            cs.setRenderingMode(RenderingMode.STROKE); // for "hollow" effect
 
            final PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
            gs.setNonStrokingAlphaConstant(0.1f);
            gs.setStrokingAlphaConstant(0.1f);
            gs.setBlendMode(BlendMode.MULTIPLY);
            gs.setLineWidth(2f);
            cs.setGraphicsStateParameters(gs);
 
            // Set color
            cs.setNonStrokingColor(Color.red);
            cs.setStrokingColor(Color.red);
 
            cs.beginText();
            cs.newLineAtOffset(x, y);
            cs.showText(text);
            cs.endText();
        }
    }
}
