package com.jty.backtrack_plugin.asm.injector;

import com.jty.backtrack_plugin.asm.ASMConfig;
import com.jty.backtrack_plugin.asm.MethodItem;
import com.jty.backtrack_plugin.asm.collector.MethodCollector;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.HashSet;

/**
 * @author jty
 * @date 2021/9/14
 * <p>
 * 插桩用的MethodVisitor
 */
class InjectMethodVisitor extends AdviceAdapter {
    private String className;
    private String methodName;
    private final MethodItem mMethodItem;
    private HashSet<Label> mTryCatchLabels = new HashSet<>();

    /**
     * Constructs a new {@link AdviceAdapter}.
     *
     * @param api           the ASM API version implemented by this visitor. Must be one of {@link
     *                      Opcodes#ASM4}, {@link Opcodes#ASM5}, {@link Opcodes#ASM6} or {@link Opcodes#ASM7}.
     * @param methodVisitor the method visitor to which this adapter delegates calls.
     * @param access        the method's access flags (see {@link Opcodes}).
     * @param methodName    the method's name.
     * @param descriptor    the method's descriptor
     */
    protected InjectMethodVisitor(int api, MethodVisitor methodVisitor, int access, String className,
                                  String methodName, String descriptor, MethodItem methodItem) {
        super(api, methodVisitor, access, methodName, descriptor);
        this.className = className;
        this.methodName = methodName;
        this.mMethodItem = methodItem;
    }

    @Override
    protected void onMethodEnter() {
        //方法入口插桩
        if (mMethodItem != null) {
            mv.visitLdcInsn(mMethodItem.id);
            mv.visitMethodInsn(INVOKESTATIC, ASMConfig.METHOD_TRACE_CLASS, ASMConfig.METHOD_TRACE_IN, "(I)V", false);
        }
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (className.equals(ASMConfig.CLASS_RUNTIME_CONFIG) && methodName.equals("<clinit>")) {
            injectRuntimeConfig();
            return;
        }
        //方法出口插桩
        if (mMethodItem != null) {
            mv.visitLdcInsn(mMethodItem.id);
            mv.visitMethodInsn(INVOKESTATIC, ASMConfig.METHOD_TRACE_CLASS, ASMConfig.METHOD_TRACE_OUT, "(I)V", false);
        }
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        mTryCatchLabels.add(handler);
        super.visitTryCatchBlock(start, end, handler, type);
    }


    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
        if (mTryCatchLabels.contains(label)) {
            //try-catch代码块插桩
            mv.visitLdcInsn(mMethodItem.id);
            mv.visitMethodInsn(INVOKESTATIC, ASMConfig.METHOD_TRACE_CLASS, ASMConfig.METHOD_TRACE_CATCH, "(I)V", false);
        }
    }

    private void injectRuntimeConfig() {
//        mv.visitLdcInsn(ASMConfig.sHasBootEndTag);
//        mv.visitMethodInsn(INVOKESTATIC, ASMConfig.CLASS_RUNTIME_CONFIG, "setHasBootEndTag", "(Z)V", false);
//        System.out.println("[Backtrack]injectRuntimeConfig：sHasBootEndTag = " + ASMConfig.sHasBootEndTag);
    }

}
