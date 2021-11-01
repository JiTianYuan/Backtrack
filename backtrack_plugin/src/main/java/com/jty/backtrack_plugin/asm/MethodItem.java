package com.jty.backtrack_plugin.asm;

import org.objectweb.asm.Opcodes;

/**
 * @author jty
 * @date 2021/10/26
 */
public class MethodItem {
    public int id;
    public int accessFlag;
    public String className;
    public String methodName;
    public String desc;

    public static MethodItem create(int id, int accessFlag, String className, String methodName, String desc) {
        MethodItem item = new MethodItem();
        item.id = id;
        item.accessFlag = accessFlag;
        item.className = className.replace("/", ".");
        item.methodName = methodName;
        item.desc = desc.replace("/", ".");
        return item;
    }

    public String getMethodName() {
        if (desc == null || isNativeMethod()) {
            return this.className + "." + this.methodName;
        } else {
            return this.className + "." + this.methodName + "." + desc;
        }
    }

    public boolean isNativeMethod() {
        return (accessFlag & Opcodes.ACC_NATIVE) != 0;
    }

    @Override
    public String toString() {
        if (desc == null || isNativeMethod()) {
            return id + "," + accessFlag + "," + className + " " + methodName;
        } else {
            return id + "," + accessFlag + "," + className + " " + methodName + " " + desc;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodItem) {
            MethodItem item = (MethodItem) obj;
            return item.getMethodName().equals(getMethodName());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
