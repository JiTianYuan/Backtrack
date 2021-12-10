package com.jty.backtrack_plugin.asm.collector;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.TransformInput;
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
import java.util.Collection;
import java.util.Enumeration;
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
    private final ConcurrentHashMap<String, MethodItem> mCollectedMethodMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MethodItem> mCollectedIgnoreMethodMap = new ConcurrentHashMap<>();

    private final AtomicInteger methodId = new AtomicInteger(0);

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

    protected int generateId(){
        return methodId.incrementAndGet();
    }

    /**
     * 需要插桩的方法，存入集合
     */
    protected void putCollectedMethod(MethodItem methodItem) {
        mCollectedMethodMap.put(methodItem.getMethodName(), methodItem);
    }

    protected boolean hasCollectedMethod(String methodName){
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
        //todo:白名单
        return true;
    }

    public boolean isNeedTraceClass(String className) {
        //todo:白名单
        return ASMConfig.isNeedTraceClass(className);
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
