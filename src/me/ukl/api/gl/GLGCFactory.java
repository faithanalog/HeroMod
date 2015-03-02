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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GLGCFactory {
    private final static Queue<GLGCObject> glDeleteQueue = new ConcurrentLinkedQueue<GLGCObject>();
    
    public static void onTick() {
      GLGCObject toDelete;
      while ((toDelete = glDeleteQueue.poll()) != null) {
          toDelete.delete();
      }
    }

//    static {
//        TickRegistry.registerTickHandler(new ITickHandler() {
//            @Override
//            public void tickStart(EnumSet<TickType> type, Object... tickData) {
//            }
//
//            @Override
//            public void tickEnd(EnumSet<TickType> type, Object... tickData) {
//                GLGCObject toDelete;
//                while ((toDelete = glDeleteQueue.poll()) != null) {
//                    toDelete.delete();
//                }
//            }
//
//            @Override
//            public EnumSet<TickType> ticks() {
//                return EnumSet.of(TickType.CLIENT);
//            }
//
//            @Override
//            public String getLabel() {
//                return "Spoutcraft - GL Garbage Collection";
//            }
//        }, Side.CLIENT);
//    }

    public static void offer(GLGCObject toDelete) {
        glDeleteQueue.offer(toDelete);
    }
}
