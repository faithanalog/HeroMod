/**
 * This file is part of SpoutcraftMod, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 SpoutcraftDev <http://spoutcraft.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.ukl.api.gl;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import me.ukl.api.util.RenderUtil;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public class ArrayBuffer extends GLObject {
    public ArrayBuffer() {
        super(glGenBuffers());
    }

    @Override
    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, getID());
    }

    @Override
    public void unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    @Override
    protected GLGCObject getGLGCObject() {
        return new BufferGLGCObject(getID());
    }

    public static void buffer(ByteBuffer data, BufferUsage usage) {
        glBufferData(GL_ARRAY_BUFFER, data, usage.getGLEnum());
    }

    public static void buffer(ShortBuffer data, BufferUsage usage) {
        glBufferData(GL_ARRAY_BUFFER, data, usage.getGLEnum());
    }

    public static void buffer(IntBuffer data, BufferUsage usage) {
        glBufferData(GL_ARRAY_BUFFER, data, usage.getGLEnum());
    }

    public static void buffer(FloatBuffer data, BufferUsage usage) {
        glBufferData(GL_ARRAY_BUFFER, data, usage.getGLEnum());
    }

    public static void buffer(DoubleBuffer data, BufferUsage usage) {
        glBufferData(GL_ARRAY_BUFFER, data, usage.getGLEnum());
    }

    public static void allocate(int length, BufferUsage usage) {
        glBufferData(GL_ARRAY_BUFFER, length, usage.getGLEnum());
    }

    public static void orphan(int length) {
        allocate(length, BufferUsage.STREAM_DRAW);
    }

    public static ByteBuffer map(int length, BufferAccess access, ByteBuffer prevBuffer) {
        return glMapBuffer(GL_ARRAY_BUFFER, access.getGLEnum(), length, prevBuffer);
    }

    public static ByteBuffer map(int length, BufferAccess access) {
        return map(length, access, null);
    }

    public static ByteBuffer mapUnsync(int length, BufferAccess access, ByteBuffer prevBuffer) {
        if (!RenderUtil.GL_30) {
            return map(length, access, prevBuffer);
        }
        return glMapBufferRange(GL_ARRAY_BUFFER, 0, length, access.getGL30Enum() | GL_MAP_UNSYNCHRONIZED_BIT, prevBuffer);
    }

    public static ByteBuffer mapUnsync(int length, BufferAccess access) {
        return mapUnsync(length, access, null);
    }

    public static void unmap() {
        glUnmapBuffer(GL_ARRAY_BUFFER);
    }
}
