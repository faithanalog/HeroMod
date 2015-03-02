package me.ukl.hmod.softgfx;

import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * Basic bitmap class for software rendering
 * @author unknownloner
 *
 */
public class Bitmap {
    
    public final int[] data;
    public final int width, height;
    public boolean hasAlpha = false;
    
    public Bitmap(int width, int height) {
        this.width = width;
        this.height = height;
        this.data = new int[width * height];
    }
    
    /**
     * Fills the entire image with the color
     * @param color
     */
    public void fill(int color) {
        for (int i = 0; i < data.length; i++) {
            data[i] = color;
        }
    }
    
    /**
     * Fills a rectangle with the color
     * @param color
     * @param x Top left X
     * @param y Top left Y
     * @param w Width of rectangle
     * @param h Height of rectangle
     */
    public void fillRect(int color, int x, int y, int w, int h) {
        if (!hasAlpha) {
            color |= 0xFF000000;
        }
        int mx = Math.min(x + w, width);
        int my = Math.min(y + h, height);
        x = Math.max(0, x);
        y = Math.max(0, y);
        int ptr = y * width + x;
        for (; y < my; y++) {
            int dst = ptr;
            for (int xx = x; xx < mx; xx++) {
                data[dst] = color;
                dst++;
            }
            ptr += width;
        }
    }
    
    /**
     * Draws a bitmap at (x, y)
     * @param bmp
     * @param x
     * @param y
     */
    public void drawBitmap(Bitmap bmp, int x, int y) {
        if (x >= width || y >= height) {
            return;
        }
        int maxX = Math.min(width, x + bmp.width);
        int maxY = Math.min(height, y + bmp.height);
        if (maxX <= 0 || maxY <= 0) {
            return;
        }
        int srcRow = 0;
        if (x < 0) {
            srcRow -= x;
            x = 0;
        }
        if (y < 0) {
            srcRow -= y * bmp.width;
            y = 0;
        }
        int dstRow = x + y * width;
        for (; y < maxY; y++) {
            int src = srcRow;
            int dst = dstRow;
            srcRow += bmp.width;
            dstRow += width;
            for (int xx = x; xx < maxX; xx++) {
                int color = bmp.data[src];
                if (!bmp.hasAlpha || (color & 0xFF000000) != 0) {
                    data[dst] = color;
                }
                dst++;
                src++;
            }
        }
    }
    
    /**
     * Draws a bitmap at (x, y) and scales it.
     * @param bmp
     * @param x
     * @param y
     * @param w Final width of the bitmap
     * @param h Final height of the bitmap
     */
    public void drawBitmap(Bitmap bmp, int x, int y, int w, int h) {
        int maxx = Math.min(width, x + w);
        int maxy = Math.min(height, y + h);
        int srcIncX = (bmp.width << 16) / w;
        int srcIncY = (bmp.height << 16) / h;
        int srcY = 0, srcXStart = 0;
        if (x < 0) {
            srcXStart -= x * srcIncX;
            x = 0;
        }
        if (y < 0) {
            srcY -= y * srcIncY;
            y = 0;
        }
        int dstRow = x + y * width;
        for (; y < maxy; y++) {
            int dst = dstRow;
            int src = (srcY >> 16) * bmp.width;
            dstRow += width;
            srcY += srcIncY;
            int srcX = srcXStart;
            for (int xx = x; xx < maxx; xx++) {
                int color = bmp.data[src + (srcX >> 16)];
                if (!bmp.hasAlpha || (color & 0xFF000000) != 0) {
                    data[dst] = color;
                }
                dst++;
                srcX += srcIncX;
            }
        }
    }
    
    /**
     * Draws a section of a bitmap at (x, y). Scales it to fit the given dimensions
     * @param bmp
     * @param x
     * @param y
     * @param w Width of image data
     * @param h Height of image data
     * @param tx Starting X of the subsection of the source bitmap
     * @param ty Starting Y of the subsection of the source bitmap
     * @param twidth Width of the subsection of the source bitmap
     * @param theight Height of the subsection of the source bitmap
     */
    public void drawBitmap(Bitmap bmp, int x, int y, int w, int h, int tx, int ty, int twidth, int theight) {
        int maxx = Math.min(width, x + w);
        int maxy = Math.min(height, y + h);

        int srcIncX = (twidth << 16) / w;
        int srcIncY = (theight << 16) / h;
        int srcY = Math.max(0, ty) << 16, srcXStart = Math.max(0, tx) << 16;
        if (x < 0) {
            srcXStart -= x * srcIncX;
            x = 0;
        }
        if (y < 0) {
            srcY -= y * srcIncY;
            y = 0;
        }
        int dstRow = x + y * width;
        for (; y < maxy; y++) {
            int dst = dstRow;
            int src = (srcY >> 16) * bmp.width;
            dstRow += width;
            srcY += srcIncY;
            int srcX = srcXStart;
            for (int xx = x; xx < maxx; xx++) {
                int color = bmp.data[src + (srcX >> 16)];
                if (!bmp.hasAlpha || (color & 0xFF000000) != 0) {
                    data[dst] = color;
                }
                dst++;
                srcX += srcIncX;
            }
        }
    }
    
    public void applyCircleFilter() {
    	int pix = 0;
    	int r = Math.min(width, height) >> 1;
        r = r * r;
        
        int cx = width >> 1;
        int cy = height >> 1;
    	for (int y = 0; y < height; y++) {
    		for (int x = 0; x < width; x++) {
    			int px = x - cx;
    			int py = y - cy;
    			int dist = px * px + py * py;
    			if (dist >= r) {
    				data[pix] = 0;
    			} else {
    				data[pix] |= 0xFF000000;
    			}
    			pix++;
    		}
    	}
    }
    
    public static Bitmap load(BufferedImage img) {
        Bitmap bmp = new Bitmap(img.getWidth(), img.getHeight());
        bmp.hasAlpha = img.getTransparency() != Transparency.OPAQUE;
        img.getRGB(0, 0, bmp.width, bmp.height, bmp.data, 0, bmp.width);
        return bmp;
    }
    
    public static Bitmap load(InputStream in) throws IOException {
        BufferedImage img = ImageIO.read(in);
        return load(img);
    }
    
    public static Bitmap load(String src) throws IOException {
        InputStream in = Bitmap.class.getResourceAsStream(src);
        Bitmap bmp = load(in);
        in.close();
        return bmp;
    }

}
