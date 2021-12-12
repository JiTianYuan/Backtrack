package com.jty.backtrack_plugin;

import com.android.build.api.transform.Transform;
import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author jty
 * @date 2021/9/4
 */
public class BacktrackPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        System.out.println("BacktrackPlugin apply");
        BacktrackExtension extension = project.getExtensions().create("backtrack", BacktrackExtension.class);

        attachTransform(project, extension);
    }

    private void attachTransform(Project project, BacktrackExtension extension) {
        AppExtension android = project.getExtensions().getByType(AppExtension.class);

        System.out.println("添加 Transform : 当前的列表：");
        for (Transform transform : android.getTransforms()) {
            System.out.println("Transform : " + transform.getName());
        }

        android.registerTransform(new BacktrackTransform(project, extension));

        System.out.println("====================================");
        System.out.println("添加后的列表：");
        for (Transform transform : android.getTransforms()) {
            System.out.println("Transform : name = " + transform.getName() + "，class = " + transform.getClass());
        }
    }
}
