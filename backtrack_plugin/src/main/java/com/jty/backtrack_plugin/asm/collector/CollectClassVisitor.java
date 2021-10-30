package com.jty.backtrack_plugin.asm.collector;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * @author jty
 * @date 2021/10/27
 * <p>
 * 收集用的ClassVisitor
 */
class CollectClassVisitor extends ClassVisitor {
    private String className;
    private boolean isABSClass = false;
    private boolean isNeedTrace = false;

    private final MethodCollector mMethodCollector;

    public CollectClassVisitor(int i, ClassVisitor classVisitor, MethodCollector collector) {
        super(i, classVisitor);
        mMethodCollector = collector;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        if ((access & Opcodes.ACC_ABSTRACT) > 0 || (access & Opcodes.ACC_INTERFACE) > 0) {
            this.isABSClass = true;
        }
        isNeedTrace = mMethodCollector.isNeedTraceClass(className) && !isABSClass;

    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (!isNeedTrace) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        } else {
            return new CollectMethodVisitor(className, access, name, desc, signature, exceptions,mMethodCollector);
        }
    }

}
