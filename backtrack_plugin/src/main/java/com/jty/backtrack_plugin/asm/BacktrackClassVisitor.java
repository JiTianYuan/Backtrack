package com.jty.backtrack_plugin.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author jty
 * @date 2021/9/14
 */
public class BacktrackClassVisitor extends ClassVisitor {
    private String className;
    private String superName;
    private boolean isABSClass = false;
    private boolean isNeedTrace;

    public BacktrackClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        this.superName = superName;
        isNeedTrace = isNeedTrace(name);
        //todo:白名单
        //this.isNeedTrace = MethodCollector.isNeedTrace(configuration, className, mappingCollector);
        if ((access & Opcodes.ACC_ABSTRACT) > 0 || (access & Opcodes.ACC_INTERFACE) > 0) {
            this.isABSClass = true;
        }

    }

    public static boolean isNeedTrace(String clsName) {
        boolean isNeed = true;
        clsName = clsName.replaceAll("/", ".");
        if (clsName.startsWith("com.jty.backtrack.")) {
            isNeed = false;
        }
        return isNeed;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        if (isABSClass || (!isNeedTrace)) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        } else {
            MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
            return new TraceMethodAdapter(api, methodVisitor, access,className, name, desc);
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
