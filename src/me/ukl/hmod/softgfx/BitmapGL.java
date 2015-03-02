package me.ukl.hmod.softgfx;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.ARBPixelBufferObject.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

import com.mumfrey.liteloader.gl.GL;

public class BitmapGL extends Bitmap {
    
    public int glTex;
    public int glPBO;
    
    public BitmapGL(int width, int height) {
        this(width, height, false);
    }
    
    public BitmapGL(int width, int height, boolean alpha) {
        super(width, height);
        this.hasAlpha = alpha;
        glPBO = glGenBuffersARB();
        glTex = glGenTextures();
        
        glBindBufferARB(GL_PIXEL_UNPACK_BUFFER_ARB, glPBO);
        glBufferDataARB(GL_PIXEL_UNPACK_BUFFER_ARB, width * height * 4, GL_STREAM_DRAW_ARB);
        glBindBufferARB(GL_PIXEL_UNPACK_BUFFER_ARB, 0);
        
        glBindTexture(GL_TEXTURE_2D, glTex);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, hasAlpha ? GL_RGBA8 : GL_RGB8, width, height, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, (ByteBuffer)null);
    }
    
    public void bind() {
    	GL.glBindTexture2D(glTex);
    }
    
    public void unbind() {
    	GL.glBindTexture2D(0);
    }
    
    public void update() {
        glBindBufferARB(GL_PIXEL_UNPACK_BUFFER_ARB, glPBO);
        IntBuffer glBuffer = glMapBufferARB(GL_PIXEL_UNPACK_BUFFER_ARB, GL_WRITE_ONLY_ARB, width * height * 4, null).asIntBuffer();
        glBuffer.put(data);
        glUnmapBufferARB(GL_PIXEL_UNPACK_BUFFER_ARB);
        
        glBindTexture(GL_TEXTURE_2D, glTex);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, 0);
        glBindBufferARB(GL_PIXEL_UNPACK_BUFFER_ARB, 0);
    }
    
    public void dispose() {
        glDeleteTextures(glTex);
        glDeleteBuffersARB(glPBO);
    }

}
