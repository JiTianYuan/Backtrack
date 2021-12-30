package com.jty.backtrack_plugin.asm.collector;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.TransformInput;
import com.jty.backtrack_plugin.BacktrackExtension;
import com.jty.backtrack_plugin.asm.ASMConfig;
import com.jty.backtrack_plugin.asm.MethodItem;
import com.jty.backtrack_plugin.asm.Utils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * @author jty
 * @date 2021/10/26
 * 第一次遍历，用于收集需要插桩的方法
 */
public class MethodCollector {
    private final BacktrackExtension extension;
    private final ConcurrentHashMap<String, MethodItem> mCollectedMethodMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MethodItem> mCollectedIgnoreMethodMap = new ConcurrentHashMap<>();

    private final HashSet<String> mWhitePackages = new HashSet<>();
    private final HashSet<String> mWhiteClasses = new HashSet<>();
    private final HashSet<String> mWhiteMethods = new HashSet<>();

    private final AtomicInteger methodId;

    public MethodCollector(BacktrackExtension extension, File whiteListFile) {
        this.extension = extension;

        parseWhiteList(whiteListFile);
        int methodIdStart = getStartMethodId();
        methodId = new AtomicInteger(methodIdStart);
        System.out.println("[Backtrack] methodId start = " + methodIdStart);
    }

    //解析白名单
    private void parseWhiteList(File whiteListFile) {
        mWhitePackages.addAll(Arrays.asList(ASMConfig.DEFAULT_WHITE_PACKAGE));

        if (whiteListFile != null) {
            System.out.println("解析白名单：" + whiteListFile);
            String str = Utils.readFileAsString(whiteListFile);
            String[] whiteListArray = str.trim().replace(".", "/").replace("\r", "").split("\n");
            if (whiteListArray != null) {
                for (String item : whiteListArray) {
                    if (item.length() == 0) {
                        continue;
                    }
                    if (item.startsWith("#")) {
                        continue;
                    }
                    if (item.startsWith("[")) {
                        continue;
                    }
                    if (item.startsWith("-keepClass ")) {
                        item = item.replace("-keepClass ", "");
                        mWhiteClasses.add(item);
                        System.out.println("class白名单：" + item);
                    } else if (item.startsWith("-keepPackage ")) {
                        item = item.replace("-keepPackage ", "");
                        item = item.replaceAll("\\*", "");
                        mWhitePackages.add(item);
                        System.out.println("package白名单：" + item);
                    } else if (item.startsWith("-keepMethod ")) {
                        //最后一个点后面的是方法名，前面的是类名
                        item = item.replace("-keepMethod ", "");
                        mWhiteMethods.add(item);
                        System.out.println("方法白名单：" + item);
                    }
                }
            }
        } else {
            System.out.println("无白名单文件");
        }

    }

    private int getStartMethodId() {
        int start = 0;
        String str = extension.methodIdStart.trim();
        if (!str.startsWith("0x")) {
            throw new IllegalArgumentException("[backtrack extension] methodIdStart must start with 0x !!");
        } else if (str.length() != 4) {
            throw new IllegalArgumentException("[backtrack extension] methodIdStart must be Two hexadecimal digits !!!");
        } else {
            //16进制转10进制
            str = str + "000000";
            start = Integer.parseInt(str.substring(2), 16);
        }
        return start;
    }

    /**
     * 仅仅是收集，不会对Inputs修改和输出
     */
    public void collect(Collection<TransformInput> inputs) throws IOException {
        //遍历inputs
        for (TransformInput input : inputs) {
            //遍历directoryInputs
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                handleDirectoryInput(directoryInput);
            }
            //遍历jarInputs
            for (JarInput jarInput : input.getJarInputs()) {
                handleJarInputs(jarInput);
            }
        }
    }

    protected int generateId() {
        return methodId.incrementAndGet();
    }

    /**
     * 需要插桩的方法，存入集合
     */
    protected void putCollectedMethod(MethodItem methodItem) {
        mCollectedMethodMap.put(methodItem.getMethodName(), methodItem);
    }

    protected boolean hasCollectedMethod(String methodName) {
        return mCollectedMethodMap.containsKey(methodName);
    }

    /**
     * 不需要插桩的方法
     */
    protected void putCollectedIgnoreMethod(MethodItem methodItem) {
        mCollectedIgnoreMethodMap.put(methodItem.getMethodName(), methodItem);
    }

    /**
     * 根据methodName获取MethodItem，如果这个Method未被收集，返回空
     */
    public MethodItem getCollectedMethod(String methodName) {
        return mCollectedMethodMap.get(methodName);
    }

    public boolean isNeedTraceMethod(String className, String methodName) {
        if (mWhiteMethods.contains(className + "/" + methodName)) {
            return false;
        }
        return true;
    }

    public boolean isNeedTraceClass(String className) {
        //白名单
        if (mWhiteClasses.contains(className)) {
            return false;
        }
        for (String whitePackage : mWhitePackages) {
            if (className.startsWith(whitePackage)) {
                return false;
            }
        }
        return true;
    }


    public ConcurrentHashMap<String, MethodItem> getCollectedMethodMap() {
        return mCollectedMethodMap;
    }

    public ConcurrentHashMap<String, MethodItem> getCollectedIgnoreMethodMap() {
        return mCollectedIgnoreMethodMap;
    }

    /**
     * 处理文件目录下的class文件
     */
    private void handleDirectoryInput(DirectoryInput directoryInput) throws IOException {
        //是否是目录
        if (directoryInput.getFile().isDirectory()) {
            //列出目录所有文件（包含子文件夹，子文件夹内文件
            List<File> files = Utils.eachFile(directoryInput.getFile());
            for (File file : files) {
                String name = file.getName();
                if (ASMConfig.isNeedTraceFile(name)) {
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(file);
                        handleClass(fis);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            fis.close();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理Jar中的class文件
     */
    private void handleJarInputs(JarInput jarInput) throws IOException {
        if (jarInput.getFile().getAbsolutePath().endsWith(".jar")) {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(jarInput.getFile());
                Enumeration<JarEntry> enumeration = jarFile.entries();
                while (enumeration.hasMoreElements()) {
                    ZipEntry zipEntry = enumeration.nextElement();
                    String zipEntryName = zipEntry.getName();
                    if (ASMConfig.isNeedTraceFile(zipEntryName)) {
                        InputStream inputStream = jarFile.getInputStream(zipEntry);
                        handleClass(inputStream);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    jarFile.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private void handleClass(InputStream inputStream) throws IOException {
        ClassReader classReader = new ClassReader(inputStream);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new CollectClassVisitor(classWriter, this);
        classReader.accept(cv, 0);
    }


}
