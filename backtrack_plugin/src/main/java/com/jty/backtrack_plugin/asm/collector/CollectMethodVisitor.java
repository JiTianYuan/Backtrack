package com.jty.backtrack_plugin.asm.collector;

import com.jty.backtrack_plugin.asm.ASMConfig;
import com.jty.backtrack_plugin.asm.MethodItem;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;


/**
 * @author jty
 * @date 2021/10/27
 */
class CollectMethodVisitor extends MethodNode {

    private String className;
    private boolean isConstructor;
    private final MethodCollector mMethodCollector;


    CollectMethodVisitor(String className, int access, String name, String desc,
                         String signature, String[] exceptions, MethodCollector methodCollector) {
        super(Opcodes.ASM5, access, name, desc, signature, exceptions);
        this.className = className;
        mMethodCollector = methodCollector;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (descriptor.contains(ASMConfig.ANNOTATION_BOOT_END_TAG)) {
            //标记 有BootEndTag
            ASMConfig.sHasBootEndTag = true;
        }
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        MethodItem methodItem = MethodItem.create(0, access, className, name, desc);

        if ("<init>".equals(name)) {
            isConstructor = true;
        }

        boolean isNeedTrace = mMethodCollector.isNeedTraceMethod(className, name);
        if (isNeedTrace) {
            if (isEmptyMethod() || isGetSetMethod() || isSingleMethod()) {
                //不需要插桩
                mMethodCollector.putCollectedIgnoreMethod(methodItem);
            } else if (!mMethodCollector.hasCollectedMethod(methodItem.getMethodName())) {
                methodItem.id = mMethodCollector.generateId();
                mMethodCollector.putCollectedMethod(methodItem);
            }
        } else {
            //不需要插桩
            mMethodCollector.putCollectedIgnoreMethod(methodItem);
        }
    }

    private boolean isGetSetMethod() {
        int ignoreCount = 0;
        ListIterator<AbstractInsnNode> iterator = instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode insnNode = iterator.next();
            int opcode = insnNode.getOpcode();
            if (-1 == opcode) {
                continue;
            }
            if (opcode != Opcodes.GETFIELD
                    && opcode != Opcodes.GETSTATIC
                    && opcode != Opcodes.H_GETFIELD
                    && opcode != Opcodes.H_GETSTATIC

                    && opcode != Opcodes.RETURN
                    && opcode != Opcodes.ARETURN
                    && opcode != Opcodes.DRETURN
                    && opcode != Opcodes.FRETURN
                    && opcode != Opcodes.LRETURN
                    && opcode != Opcodes.IRETURN

                    && opcode != Opcodes.PUTFIELD
                    && opcode != Opcodes.PUTSTATIC
                    && opcode != Opcodes.H_PUTFIELD
                    && opcode != Opcodes.H_PUTSTATIC
                    && opcode > Opcodes.SALOAD) {
                if (isConstructor && opcode == Opcodes.INVOKESPECIAL) {
                    ignoreCount++;
                    if (ignoreCount > 1) {
                        return false;
                    }
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    private boolean isSingleMethod() {
        ListIterator<AbstractInsnNode> iterator = instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode insnNode = iterator.next();
            int opcode = insnNode.getOpcode();
            if (-1 == opcode) {
                continue;
            } else if (Opcodes.INVOKEVIRTUAL <= opcode && opcode <= Opcodes.INVOKEDYNAMIC) {
                return false;
            }
        }
        return true;
    }


    private boolean isEmptyMethod() {
        ListIterator<AbstractInsnNode> iterator = instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode insnNode = iterator.next();
            int opcode = insnNode.getOpcode();
            if (-1 == opcode) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
}
