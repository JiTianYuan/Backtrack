package com.jty.backtrack_plugin.asm.injector;

import com.jty.backtrack_plugin.asm.ASMConfig;
import com.jty.backtrack_plugin.asm.MethodItem;
import com.jty.backtrack_plugin.asm.collector.MethodCollector;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * @author jty
 * @date 2021/9/14
 * <p>
 * 插桩用的MethodVisitor
 */
class InjectMethodVisitor extends AdviceAdapter {
    private String className;
    private final MethodItem mMethodItem;

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
        this.mMethodItem = methodItem;
    }

    @Override
    protected void onMethodEnter() {
        if (mMethodItem != null) {
            System.out.println("onMethodEnter --->>> " + className + " $ " + getName());
            mv.visitLdcInsn(mMethodItem.id);
            mv.visitMethodInsn(INVOKESTATIC, ASMConfig.METHOD_TRACE_CLASS, ASMConfig.METHOD_TRACE_IN, "(I)V", false);
            //mv.visitLdcInsn(getName());
            //mv.visitMethodInsn(INVOKESTATIC, ASMConfig.METHOD_TRACE_CLASS, ASMConfig.METHOD_TRACE_IN, "(Ljava/lang/String;)V", false);
        }
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (mMethodItem != null) {
            System.out.println("onMethodExit --->>> " + className + " $ " + getName());
            mv.visitLdcInsn(mMethodItem.id);
            mv.visitMethodInsn(INVOKESTATIC, ASMConfig.METHOD_TRACE_CLASS, ASMConfig.METHOD_TRACE_OUT, "(I)V", false);
            //mv.visitLdcInsn(getName());
            //mv.visitMethodInsn(INVOKESTATIC, ASMConfig.METHOD_TRACE_CLASS, ASMConfig.METHOD_TRACE_OUT, "(Ljava/lang/String;)V", false);
        }
    }

}
