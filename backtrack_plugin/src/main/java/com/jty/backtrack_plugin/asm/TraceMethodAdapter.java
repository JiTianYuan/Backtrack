package com.jty.backtrack_plugin.asm;

import org.gradle.api.logging.Logging;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import groovy.util.logging.Log;

/**
 * @author jty
 * @date 2021/9/14
 */
public class TraceMethodAdapter extends AdviceAdapter {
    private String className;

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
    protected TraceMethodAdapter(int api, MethodVisitor methodVisitor, int access, String className, String methodName, String descriptor) {
        super(api, methodVisitor, access, methodName, descriptor);
        this.className = className;
    }

    @Override
    protected void onMethodEnter() {
        System.out.println("onMethodEnter --->>> " + className + " $ " + getName());
        mv.visitLdcInsn(getName());
        mv.visitMethodInsn(INVOKESTATIC, ASMConst.METHOD_TRACE_CLASS, ASMConst.METHOD_TRACE_IN, "(Ljava/lang/String;)V", false);
    }

    @Override
    protected void onMethodExit(int opcode) {
        System.out.println("onMethodExit --->>> " + className + " $ " + getName());
        mv.visitLdcInsn(getName());
        mv.visitMethodInsn(INVOKESTATIC, ASMConst.METHOD_TRACE_CLASS, ASMConst.METHOD_TRACE_OUT, "(Ljava/lang/String;)V", false);
    }

}
