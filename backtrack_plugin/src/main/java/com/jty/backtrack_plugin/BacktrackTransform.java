package com.jty.backtrack_plugin;

import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.google.common.base.Joiner;
import com.jty.backtrack_plugin.asm.collector.MethodCollector;
import com.jty.backtrack_plugin.asm.injector.MethodInjector;
import com.jty.backtrack_plugin.asm.mapping.MappingPrinter;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * @author jty
 * @date 2021/9/4
 */
class BacktrackTransform extends Transform {
    private Project project;

    public BacktrackTransform(Project project) {
        this.project = project;
    }

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
        //1、收集需要插桩的类
        MethodCollector collector = new MethodCollector();
        collector.collect(inputs);

        //2、插桩
        MethodInjector injector = new MethodInjector(collector);
        injector.run(inputs, outputProvider);

        //3、mapping文件输出
        String buildDir = project.getBuildDir().getAbsolutePath();
        String dirName = transformInvocation.getContext().getVariantName();
        String mappingOutDir = buildDir + File.separatorChar
                + "outputs" + File.separatorChar
                + "mapping" + File.separatorChar
                + dirName;

//        String mappingOut = Joiner.on(File.separatorChar).join(
//                buildDir,
//                "outputs",
//                "mapping",
//                dirName);

        MappingPrinter printer = new MappingPrinter(collector);
        printer.print(mappingOutDir);

        long cost = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("--------------- Backtrack Transform end --------------- ");
        System.out.println("Backtrack Transform cost " + cost + "s");
    }


}
