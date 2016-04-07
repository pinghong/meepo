package com.yun.hop.commons.classloader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by hongping on 2016/4/7.
 * description：
 */
public class ByteClassLoader extends URLClassLoader {

    private final Hashtable<String, Class> cache = new Hashtable();
    private static Logger LOG = LoggerFactory.getLogger(ByteClassLoader.class);


    public ByteClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    public synchronized Class loadClass(String name, byte[] clazz, boolean resolve)
            throws ClassNotFoundException {
        if (name.startsWith("java.")) {
            return findSystemClass(name);
        }
        Class c = (Class) cache.get(name);
        if (c == null) {

            c = defineClass(name, clazz, 0, clazz.length);
            cache.put(name, c);
        } else {
            LOG.info("loadClass: found " + name + " in cache.");
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }


    public synchronized Class loadClassFromJar(String className, byte[] jarFileBytes, boolean resolve)
            throws ClassNotFoundException {
        File tempFile = null;
        JarFile jarFile = null;
        String jarEntryName = className.replaceAll("\\.", "\\/") + ".class";
        try {
            tempFile = File.createTempFile("tempJarFile", "jar");
            FileUtils.writeByteArrayToFile(tempFile, jarFileBytes);
            jarFile = new JarFile(tempFile);
            Enumeration<JarEntry> jarEntrys = jarFile.entries();


            while (jarEntrys.hasMoreElements()) {
                JarEntry jarEntry = jarEntrys.nextElement();
                if (jarEntry.getName().equals(jarEntryName)) {
                    byte bytes[] = IOUtils.toByteArray(jarFile.getInputStream(jarEntry));
                    return loadClass(className, bytes, true);
                }
            }

        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            try {
                if (jarFile != null) {
                    jarFile.close();
                }
                if (tempFile != null) {
                    tempFile.delete();
                }

            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }

        }

        return null;

    }


    public void testUrlClassloader(File file) {
        try {
            URL[] urls = new URL[1];
            urls[0] = file.toURI().toURL();

            URLClassLoader urlClassLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
            Class.forName("com.yun.hop.apitest.Out", true, urlClassLoader);
            Class classz = Class.forName("com.yun.hop.apitest.OutA", true, urlClassLoader);
            Object object = classz.newInstance();
            Method method = classz.getMethod("out", String.class);
            method.invoke(object, "Hello test testUrlClassloader");

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

    }

    public void testClassLoaderFromJar() {
        String filePath = "F:/class/apitest-0.0.1-SNAPSHOT.jar";
        try {
            Class classz1 = loadClassFromJar("com.yun.hop.apitest.Out", //
                    IOUtils.toByteArray(FileUtils.openInputStream(new File(filePath))), //
                    true);
            Class classz = loadClassFromJar("com.yun.hop.apitest.OutA", //
                    IOUtils.toByteArray(FileUtils.openInputStream(new File(filePath))), //
                    true);

            Object object = classz.newInstance();
            Method method = classz.getMethod("out", String.class);
            method.invoke(object, "Hello test testClassLoaderFromJar");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }


    public void testClassLoader() {
        try {
            Class classz1 = loadClass("com.yun.hop.apitest.Out", //
                    IOUtils.toByteArray(FileUtils.openInputStream(new File("F:/class/Out.class"))),//
                    true//
            );
            Class classz = loadClass("com.yun.hop.apitest.OutA", //
                    IOUtils.toByteArray(FileUtils.openInputStream(new File("F:/class/OutA.class"))),//
                    true//
            );
            Object object = classz.newInstance();
            Method method = classz.getMethod("out", String.class);
            method.invoke(object, "Hello test classloader");

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }


    }

    public static void main(String args[]) {

        ByteClassLoader byteClassLoader = new ByteClassLoader(Thread.currentThread().getContextClassLoader());
        byteClassLoader.testUrlClassloader(new File("F:/class/apitest-0.0.1-SNAPSHOT.jar"));
        byteClassLoader.testClassLoader();
        byteClassLoader.testClassLoaderFromJar();
    }

}
