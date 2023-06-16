package src;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;

public class Graphic {
    public static List<Node> getCoordinates(List<Direction> conformation, List<Type> sequence) {
        List<Node> coordinates = new ArrayList<Node>();
        for (int i = 0; i < sequence.size(); i++) {
           coordinates.add(new Node());
        }

        double angle = 0;
        for(int i = 0; i < conformation.size() - 1; i++) {
            Direction dir = conformation.get(i);
            short currentX = coordinates.get(i).x;
            short currentY = coordinates.get(i).y;
            short nextX = 0;
            short nextY = 0;
            short x = 0;
            short y = 0;

            // Set up coordination
            if (dir != Direction.STOP) {
                if (dir == Direction.LEFT) {
                    x = -1;
                    y = 0;
                    nextX = (short) Math.round(currentX + (x * Math.cos(Math.toRadians(angle)) + y * Math.sin(Math.toRadians(angle))));
                    nextY = (short) Math.round(currentY + (-x * Math.sin(Math.toRadians(angle)) + y * Math.cos(Math.toRadians(angle))));
                    angle -= 90;
                }
                else if (dir == Direction.RIGHT) {
                    x = 1;
                    y = 0;
                    nextX = (short) Math.round(currentX + (x * Math.cos(Math.toRadians(angle)) + y * Math.sin(Math.toRadians(angle))));
                    nextY = (short) Math.round(currentY + -x * Math.sin(Math.toRadians(angle)) + y * Math.cos(Math.toRadians(angle)));
                    angle += 90;
                }
                else {
                    x = 0;
                    y = 1;
                    nextX = (short) Math.round(currentX + (x * Math.cos(Math.toRadians(angle)) + y * Math.sin(Math.toRadians(angle))));
                    nextY = (short) Math.round(currentY + -x * Math.sin(Math.toRadians(angle)) + y * Math.cos(Math.toRadians(angle)));
                }

                coordinates.get(i+1).x = nextX;
                coordinates.get(i+1).y = nextY;
            }
        }
        return coordinates;
    }

    public static BufferedImage resize(BufferedImage img) {
        int left = 0, right = img.getWidth() - 1;
        int top = 0, bottom = img.getHeight() - 1;

        // Find the left boundary of the white column
        while (left < right) {
            boolean isWhite = true;
            for (int y = 0; y < img.getHeight(); y++) {
                int pixel = img.getRGB(left, y);

                if (pixel != Color.WHITE.getRGB()) {
                    isWhite = false;
                    break;
                }
            }

            if (isWhite) {
                left++;
            } else {
                break;
            }
        }

        // Find the right boundary of the white column
        while (right > left) {
            boolean isWhite = true;
            for (int y = 0; y < img.getHeight(); y++) {
                int pixel = img.getRGB(right, y);
                if (pixel != Color.WHITE.getRGB()) {
                    isWhite = false;
                    break;
                }
            }
            if (isWhite) {
                right--;
            } else {
                break;
            }
        }

        // Find the top boundary of the white row
        while (top < bottom) {
            boolean isWhite = true;
            for (int x = 0; x < img.getWidth(); x++) {
                int pixel = img.getRGB(x, top);
                if (pixel != Color.WHITE.getRGB()) {
                    isWhite = false;
                    break;
                }
            }
            if (isWhite) {
                top++;
            } else {
                break;
            }
        }
        
        // Find the bottom boundary of the white row
        while (bottom > top) {
            boolean isWhite = true;
            for (int x = 0; x < img.getWidth(); x++) {
                int pixel = img.getRGB(x, bottom);
                if (pixel != Color.WHITE.getRGB()) {
                    isWhite = false;
                    break;
                }
            }
            if (isWhite) {
                bottom--;
            } else {
                break;
            }
        }
            
        // Cut the rectangle using the clip() method
        if (left > 0 || right < img.getWidth() - 1 || top > 0 || bottom < img.getHeight() - 1) {
            top = top - 150;
            return img.getSubimage(left, top, right - left + 1, bottom - top + 1);
        }
        else 
            return img;
    }

    public static void generateImage(List<Direction> conformation, List<Type> sequence, String imageName) throws Exception {
		int cellSize = 40;
        int indexSize = 20;
        int coordinateScale = 100;

        // Calculate relevant data (fitness, overlapped, h-h pairs and maxdistance)
        Calculation calculator = new Calculation();
        Map<String, Double> data = calculator.getFitness(conformation, sequence);
        double fitness = data.get("fitness");
        double numOfOverlapped = data.get("overlapped");
        double numOfHHpairs = data.get("hh pairs");

        // Set the main board size 
        int height = (conformation.size() + 5) * coordinateScale;
		int width = (conformation.size() + 5) * coordinateScale;
        int offsetX = width / 2;
        int offsetY = height / 2;
		
        // Draw main board
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setColor(Color.white);
		g2.fillRect(0, 0, width, height);

        // Draw the given conformation
        List<Node> coordinates = getCoordinates(conformation, sequence);
        for (int i = 0; i < coordinates.size(); i++) {
            int currentX = coordinates.get(i).x * coordinateScale + offsetX;
            int currentY = coordinates.get(i).y * coordinateScale + offsetY;

            // Draw line
            if (i != (coordinates.size() - 1)) {
                int nextX = coordinates.get(i + 1).x * coordinateScale + offsetX;
                int nextY = coordinates.get(i + 1).y * coordinateScale + offsetY;
		        g2.setColor(Color.BLACK);
		        g2.drawLine(currentX + cellSize / 2, currentY + cellSize / 2, nextX + cellSize / 2, nextY + cellSize/2);
            }

            // Draw box
            if (sequence.get(i) == Type.HYDROPHOB) {
		        g2.setColor(Color.BLACK);
            }
            else {
		        g2.setColor(Color.RED);
            }
		    g2.fillRect(currentX, currentY, cellSize, cellSize);		

            // Draw index
		    g2.setColor(new Color(255, 255, 255));
            String label = Integer.toString(i);
            Font font = new Font("Serif", Font.PLAIN, indexSize);
            g2.setFont(font);
            FontMetrics metrics = g2.getFontMetrics();
            int ascent = metrics.getAscent();
            int labelWidth = metrics.stringWidth(label);

            g2.drawString(label, currentX + cellSize/2 - labelWidth/2 , currentY + cellSize/2 + ascent/2);
        }

		image = resize(image);
		g2 = image.createGraphics();

        // Draw fitness line 
        g2.setColor(Color.BLACK);
        String label = "Fitness: " + Double.toString(fitness);
        Font font = new Font("Serif", Font.PLAIN, indexSize);
        g2.setFont(font);
        g2.drawString(label, 0, cellSize/2);

        // Draw number of overlapped line 
        g2.setColor(Color.BLACK);
        label = "Overlapped: " + Double.toString(numOfOverlapped);
        g2.setFont(font);
        g2.drawString(label, 0, (cellSize/2) * 2);
        
        // Draw number of HH-Pairs line
        g2.setColor(Color.BLACK);
        label = "H-H pairs: " + Double.toString(numOfHHpairs);
        g2.setFont(font);
        g2.drawString(label, 0, (cellSize/2) * 3);

        // Draw legends
        g2.setColor(Color.BLACK);
        label = "Hydrophob: black";
        g2.setFont(font);
        g2.drawString(label, 0, (cellSize/2) * 5);
        g2.setColor(Color.BLACK);
        label = "Hydrophil: red";
        g2.setFont(font);
        g2.drawString(label, 0, (cellSize/2) * 6);

		String folder = "./images/";
		String filename = imageName;
		if (new File(folder).exists() == false) new File(folder).mkdirs();
		
		try {
			ImageIO.write(image, "png", new File(folder + File.separator + filename));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
    }
}
