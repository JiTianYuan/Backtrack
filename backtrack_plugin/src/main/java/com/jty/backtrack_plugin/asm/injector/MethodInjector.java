package com.jty.backtrack_plugin.asm.injector;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.utils.FileUtils;
import com.jty.backtrack_plugin.asm.ASMConfig;
import com.jty.backtrack_plugin.asm.Utils;
import com.jty.backtrack_plugin.asm.collector.MethodCollector;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author jty
 * @date 2021/10/26
 * 第二次遍历，根据MethodCollector的结果，对需要插桩的方法插桩
 */
public class MethodInjector {
    private final MethodCollector mMethodCollector;

    public MethodInjector(MethodCollector mMethodCollector) {
        this.mMethodCollector = mMethodCollector;
    }

    public void run(Collection<TransformInput> inputs, TransformOutputProvider outputProvider) throws IOException {
        //遍历inputs
        for (TransformInput input : inputs) {
            //遍历directoryInputs
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                handleDirectoryInput(directoryInput, outputProvider);
            }
            //遍历jarInputs
            for (JarInput jarInput : input.getJarInputs()) {
                handleJarInputs(jarInput, outputProvider);
            }
        }
    }

    /**
     * 处理文件目录下的class文件
     */
    private void handleDirectoryInput(DirectoryInput directoryInput, TransformOutputProvider outputProvider) throws IOException {
        //是否是目录
        if (directoryInput.getFile().isDirectory()) {
            //列出目录所有文件（包含子文件夹，子文件夹内文件
            List<File> files = Utils.eachFile(directoryInput.getFile());
            for (File file : files) {
                String name = file.getName();
                if (ASMConfig.isNeedTraceFile(name)) {
                    System.out.println("----------- deal with class file <" + name + "> -----------");
                    FileInputStream fis = new FileInputStream(file);
                    byte[] code = handleClass(fis);
                    FileOutputStream fos = new FileOutputStream(
                            file.getParentFile().getAbsolutePath() + File.separator + name);
                    fos.write(code);
                    fos.close();
                    fis.close();
                }
            }
        }
        //处理完输入文件之后，要把输出给下一个任务
        File dest = outputProvider.getContentLocation(directoryInput.getName(),
                directoryInput.getContentTypes(), directoryInput.getScopes(),
                Format.DIRECTORY);
        FileUtils.copyDirectory(directoryInput.getFile(), dest);
    }

    /**
     * 处理Jar中的class文件
     */
    private void handleJarInputs(JarInput jarInput, TransformOutputProvider outputProvider) throws IOException {
        if (jarInput.getFile().getAbsolutePath().endsWith(".jar")) {
            //重名名输出文件,因为可能同名,会覆盖
            String jarName = jarInput.getName();
            String md5Name = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath());
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4);
            }
            JarFile jarFile = new JarFile(jarInput.getFile());
            Enumeration enumeration = jarFile.entries();
            File tmpFile = new File(jarInput.getFile().getParent() + File.separator + "classes_temp.jar");
            //避免上次的缓存被重复插入
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile));
            //用于保存
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                String entryName = jarEntry.getName();
                ZipEntry zipEntry = new ZipEntry(entryName);
                InputStream inputStream = jarFile.getInputStream(jarEntry);
                //插桩class
                if (ASMConfig.isNeedTraceFile(entryName)) {
                    //class文件处理
                    System.out.println("----------- deal with jar class file <" + entryName + "> -----------");
                    jarOutputStream.putNextEntry(zipEntry);
                    byte[] code = handleClass(inputStream);
                    jarOutputStream.write(code);
                } else {
                    jarOutputStream.putNextEntry(zipEntry);
                    jarOutputStream.write(IOUtils.toByteArray(inputStream));
                }
                jarOutputStream.closeEntry();
            }
            //结束
            jarOutputStream.close();
            jarFile.close();
            File dest = outputProvider.getContentLocation(jarName + md5Name,
                    jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
            FileUtils.copyFile(tmpFile, dest);
            tmpFile.delete();
        }
    }

    private byte[] handleClass(InputStream inputStream) throws IOException {
        ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream));
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new InjectClassVisitor(classWriter, mMethodCollector);
        classReader.accept(cv, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }
}
