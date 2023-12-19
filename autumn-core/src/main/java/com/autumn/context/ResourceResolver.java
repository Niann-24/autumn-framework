package com.autumn.context;


import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;


/**
 * @author niann
 * @description 扫描文件
 * @dateTime 2023/12/19 23:43
 */
public class ResourceResolver {

    String basePath;

    Logger log = Logger.getLogger("");

    public ResourceResolver(String basePath) {
        this.basePath = basePath;
    }

    public static void main(String[] args) {
        ResourceResolver rr = new ResourceResolver("com.autumn");
        List<Resource> scan = rr.scan(res -> res);
        for (Resource resource : scan) {
            System.out.println(resource);
        }
    }

    public <R> List<R> scan(Function<Resource, R> res) {
        String path = basePath.replace(".", "/");
        List<R> list = new ArrayList<>();
        scan0(path, list, res);
        return list;
    }

    private String formatURL(String url) {
        if (url.startsWith("/")) {
            url = url.substring(1);
        }
        return url;
    }

    private <R> void scan0(String path, List<R> list, Function<Resource, R> res) {
        Enumeration<URL> ens = null;
        try {
            ens = getClassLoad().getResources(path);
            while (ens.hasMoreElements()) {
                URL url = ens.nextElement();
                String uri = formatURL(url.getPath());
                if (url.toString().startsWith("jar:")) {
                    JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                    scanJar(uri, jar, list, res);
                } else {
                    scanFile(uri, list, res);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <R> void scanJar(String uri, JarFile jar, List<R> list, Function<Resource, R> res) {
        Enumeration<JarEntry> en = jar.entries();
        String subURL = basePath.replace(".", "/");
        uri = formatURL(uri.substring(5, uri.indexOf(subURL) - 1));
        while (en.hasMoreElements()) {
            JarEntry jarEntry = en.nextElement();
            String name = jarEntry.getName().replace("/", ".");
            if (name.startsWith(basePath) && !name.endsWith(".")) {
                Resource rs = new Resource(uri + jarEntry.getName(), jarEntry.getName());
                R r = res.apply(rs);
                if (r != null) {
                    list.add(r);
                }
            }
        }
    }

    private String getClassName(String url) {
        int i = url.lastIndexOf("classes\\");
        return url.substring(i + "classes\\".length());
    }

    private <R> void scanFile(String path, List<R> list, Function<Resource, R> res) {
        Path url = Paths.get(path);
        try (var steam = Files.walk(url)) {
            steam
                    .filter(file -> file.toFile().isFile())
                    .forEach(file -> {
                        String classPath = file.toFile().getPath();
                        if (!classPath.isEmpty()) {
                            Resource rs = new Resource(classPath, getClassName(classPath));
                            R r = res.apply(rs);
                            if (r != null) {
                                list.add(r);
                            }
                        }

                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private ClassLoader getClassLoad() {
        ClassLoader classLoader = null;
        classLoader = getClass().getClassLoader();
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        return classLoader;
    }


}
