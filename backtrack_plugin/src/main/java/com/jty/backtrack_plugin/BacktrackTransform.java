package com.jty.backtrack_plugin;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;
import com.jty.backtrack_plugin.asm.BacktrackClassVisitor;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * @author jty
 * @date 2021/9/4
 */
class BacktrackTransform extends Transform {
    @Override
    public String getName() {
        return "BacktrackTransform";
    }

    /**
     * 输入类型
     */
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    /**
     * 处理范围
     *
     * @return
     */
    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        /*
         * PROJECT	只处理当前的项目
         * SUB_PROJECTS	只处理子项目
         * EXTERNAL_LIBRARIES	只处理外部的依赖库
         * TESTED_CODE	只处理测试代码
         * PROVIDED_ONLY	只处理provided-only的依赖库
         * PROJECT_LOCAL_DEPS	只处理当前项目的本地依赖,例如jar, aar（过期，被EXTERNAL_LIBRARIES替代）
         * SUB_PROJECTS_LOCAL_DEPS	只处理子项目的本地依赖,例如jar, aar（过期，被EXTERNAL_LIBRARIES替代）
         */
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    /**
     * 是否支持增量编译
     */
    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        System.out.println("--------------- Backtrack Transform start --------------- ");
        long startTime = System.currentTimeMillis();
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        //删除之前的输出
        if (outputProvider != null) {
            outputProvider.deleteAll();
        }
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
        long cost = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("--------------- Backtrack Transform end --------------- ");
        System.out.println("Backtrack Transform cost " + cost + "s");
    }


    /**
     * 广度优先遍历
     *
     * @param file
     * @return
     */
    private List<File> eachFile(File file) {
        List<File> result = new ArrayList<>();
        if (!file.exists()) {
            return result;
        }
        Queue<File> foreachQueue = new LinkedList<>();
        foreachQueue.add(file);
        while (foreachQueue.size() > 0) {
            File poll = foreachQueue.poll();
            if (poll != null) {
                if (poll.isDirectory()) {
                    foreachQueue.addAll(Arrays.asList(poll.listFiles()));
                } else {
                    result.add(poll);
                }
            }
        }
        return result;
    }

    /**
     * 处理文件目录下的class文件
     */
    private void handleDirectoryInput(DirectoryInput directoryInput, TransformOutputProvider outputProvider) throws IOException {
        //是否是目录
        if (directoryInput.getFile().isDirectory()) {
            //列出目录所有文件（包含子文件夹，子文件夹内文件
            List<File> files = eachFile(directoryInput.getFile());
            for (File file : files) {
                String name = file.getName();
                //todo:白名单
                if (name.endsWith(".class") && !"R.class".equals(name)) {
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
                //todo:白名单
                if (entryName.endsWith(".class") && !"R.class".equals(entryName)) {
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
        ClassVisitor cv = new BacktrackClassVisitor(classWriter);
        classReader.accept(cv, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }
}
