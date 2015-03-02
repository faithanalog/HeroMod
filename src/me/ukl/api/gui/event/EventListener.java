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
package me.ukl.api.gui.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EventListener implements Comparable<EventListener> {
    private Method method;
    private Object listener;
    private EventHandler handler;
    private Class<? extends Event> type;

    @SuppressWarnings ("unchecked")
    public EventListener(Object o, Method m) {
        this.listener = o;
        this.method = m;
        this.handler = m.getAnnotation(EventHandler.class);
        m.setAccessible(true);
        type = (Class<? extends Event>) m.getParameterTypes()[0];
    }

    public void onEvent(Event e) {
        if (type.isInstance(e)) {
            try {
                method.invoke(listener, e);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public Object getListener() {
        return listener;
    }

    public Method getMethod() {
        return method;
    }

    public EventHandler getHandler() {
        return this.handler;
    }

    public int getPriority() {
        return handler.priority();
    }

    @Override
    public int compareTo(EventListener o) {
        return Integer.signum(Integer.valueOf(getPriority()).compareTo(o.getPriority())) * -1;
    }

    public static List<Method> getHandlers(Object o) {
        Class<?> clazz = o.getClass();
        Method[] methods = clazz.getMethods();
        List<Method> handlers = new ArrayList<Method>();
        for (Method m : methods) {
            EventHandler h = m.getAnnotation(EventHandler.class);
            if (h != null && m.getParameterTypes().length == 1) {
                handlers.add(m);
            }
        }
        return handlers;
    }
}
