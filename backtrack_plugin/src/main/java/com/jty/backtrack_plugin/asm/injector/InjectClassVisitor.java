package com.jty.backtrack_plugin.asm.injector;

import com.jty.backtrack_plugin.asm.MethodItem;
import com.jty.backtrack_plugin.asm.collector.MethodCollector;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author jty
 * @date 2021/9/14
 * <p>
 * 插桩用的ClassVisitor
 */
public class InjectClassVisitor extends ClassVisitor {
    private String className;
    private String superName;
    private boolean isABSClass = false;
    private boolean isNeedTrace;
    private final MethodCollector mMethodCollector;

    public InjectClassVisitor(ClassVisitor classVisitor, MethodCollector methodCollector) {
        super(Opcodes.ASM7, classVisitor);
        mMethodCollector = methodCollector;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        this.superName = superName;
        if ((access & Opcodes.ACC_ABSTRACT) > 0 || (access & Opcodes.ACC_INTERFACE) > 0) {
            this.isABSClass = true;
        }
        isNeedTrace = mMethodCollector.isNeedTraceClass(className) && !isABSClass;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodItem tempItem = MethodItem.create(0, access, className, name, desc);
        MethodItem collectedMethod = mMethodCollector.getCollectedMethod(tempItem.getMethodName());
        if (collectedMethod == null || (!isNeedTrace)) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        } else {
            MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
            return new InjectMethodVisitor(api, methodVisitor, access, className, name, desc, collectedMethod);
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
