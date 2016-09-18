

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

public class Main {
	
	private static String fileFrom, fileTo;
	private static final String ext = "jpg";
	private static int TYPE = BufferedImage.TYPE_INT_RGB;

	public static void main(String[] args) {
		Date date = new Date();
		
		JSONObject sizes = null;
		if (args.length < 3) {
			System.err.println("Give me at least 3 arguments:\n"
					+ "\timg_origin_path+name+extension -> 'C:\\images\\test.jpg'"
					+ "\n\timg_destination_path+name -> 'C:\\images\\newImage'"
					+ "\n\t{json_crop_configuration} -> '{x:0, y:0, w:100, h: 150}'"
					+ "\n\t{json_resizes_configurations}(optional) -> '{size20Pixels: 20, halfSize: 0.5}'");
			return;
		}
		System.out.println(String.format("[img-jar]\nfrom:\n\t\"%s\"\nto:\n\t\"%s\"\nwith:\n\t%s...", args[0], args[1], args[2]));
		fileFrom = args[0];
		fileTo = args[1];
	
		try {
			JSONObject json = new JSONObject(args[2]);
			if(args.length == 4){
				sizes = new JSONObject(args[3]);
			}
			BufferedImage img = ImageIO.read(new File(fileFrom));
			BufferedImage nImg = cropImage(img,
					new Rectangle(json.getInt("x"), json.getInt("y"), json.getInt("w"), json.getInt("h")));

			saveImage(nImg, "original");
			if(sizes != null){
				for(String key : sizes.keySet()){
					Object obj = sizes.get(key);
					if(obj instanceof String){
						String s = sizes.getString(key);
						if(s.contains("."))
							saveImage(resizeImage(nImg, Double.valueOf(s)), key);
						else
							saveImage(resizeImage(nImg, Integer.valueOf(s)), key);
					}
					if(obj instanceof Integer){
						saveImage(resizeImage(nImg, sizes.getInt(key)), key);
					}
					if(obj instanceof Double){
						saveImage(resizeImage(nImg, sizes.getDouble(key)), key);
					}
				}
			}
			long timestamp = new Date().getTime() - date.getTime();
			System.out.println("Done in " + timestamp + "ms");
		} catch (JSONException e) {
			System.err.println("Error while parsing the JSON:\n" + e.getMessage());
		} catch (IOException ioe){
			System.err.println("Error while opening the image file:\n" + ioe.getMessage());
		}
	}
	
	private static void saveImage(BufferedImage nImg, String caption){
		try {
			String novo = fileTo + "-" + caption + "." + ext;
			ImageIO.write(nImg, ext, new File(novo));
			System.out.println(novo + " saved!");
		} catch (IOException e) {
			System.err.println("Error while saving the file:\n" + e.getMessage());
		}
	}

	private static BufferedImage cropImage(BufferedImage src, Rectangle rect) {
		BufferedImage dest = src.getSubimage(rect.x, rect.y, rect.width, rect.height);
		BufferedImage copyOfImage = new BufferedImage(dest.getWidth(), dest.getHeight(), TYPE);
		Graphics g = copyOfImage.createGraphics();
		g.drawImage(dest, 0, 0, null);
		g.dispose();
		return copyOfImage;
	}

	private static BufferedImage resizeImage(BufferedImage originalImage, double percent) {
		int IMG_WIDTH = (int)(originalImage.getWidth()*percent);
		int IMG_HEIGHT = (int)(originalImage.getHeight()*percent);
		BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, TYPE);
		Graphics g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
		g.dispose();

		return resizedImage;
	}
	
	
	private static BufferedImage resizeImage(BufferedImage originalImage, int bigSide) {
		double IMG_WIDTH = originalImage.getWidth();
		double IMG_HEIGHT = originalImage.getHeight();
		double ratio = IMG_WIDTH / IMG_HEIGHT;
		if(IMG_WIDTH > IMG_HEIGHT){
			IMG_WIDTH = bigSide;
			IMG_HEIGHT = IMG_WIDTH/ratio;
		}else{
			IMG_HEIGHT = bigSide;
			IMG_WIDTH = IMG_HEIGHT/ratio;
		}
		BufferedImage resizedImage = new BufferedImage((int)IMG_WIDTH, (int)IMG_HEIGHT, TYPE);
		Graphics g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, (int)IMG_WIDTH, (int)IMG_HEIGHT, null);
		g.dispose();

		return resizedImage;
	}

}
