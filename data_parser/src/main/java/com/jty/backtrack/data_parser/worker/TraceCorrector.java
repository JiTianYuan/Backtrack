package com.jty.backtrack.data_parser.worker;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jty
 * @date 2021/12/21
 * Trace修正器，用于处理因异常导致的 did not finished
 */
class TraceCorrector {

    public static void main(String[] args) {
        //模拟数据
        TraceCorrector corrector = new TraceCorrector();
        List<TraceRecordItem> data = new ArrayList<>();
        data.add(new TraceRecordItem(1, 1, "B"));
        data.add(new TraceRecordItem(2, 2, "B"));
        data.add(new TraceRecordItem(3, 3, "B"));
        data.add(new TraceRecordItem(4, 4, "B"));
        data.add(new TraceRecordItem(4, 5, "E"));
        data.add(new TraceRecordItem(3, 6, "E"));
        data.add(new TraceRecordItem(2, 7, "E"));
        data.add(new TraceRecordItem(5, 8, "B"));
        data.add(new TraceRecordItem(6, 9, "B"));
        data.add(new TraceRecordItem(7, 10, "B"));
        data.add(new TraceRecordItem(8, 11, "B"));
        data.add(new TraceRecordItem(8, 12, "E"));
        data.add(new TraceRecordItem(7, 13, "E"));
        data.add(new TraceRecordItem(9, 14, "B"));
        data.add(new TraceRecordItem(5, 15, "T"));
        data.add(new TraceRecordItem(5, 16, "E"));
        data.add(new TraceRecordItem(1, 17, "E"));


        corrector.stackRecoverTest(data);
    }

    public void correct(List<TraceRecordItem> data) {
        List<TraceRecordItem> exceptionItems = new ArrayList<>();


        //查找是否有T类型，没有说明无异常，不处理
        //如果有T类型，就T类型往前找，找B类型的item。碰到E类型或者是B类型但是id == T的id，就停止找
        //把找到的内容记录下来，这些就是要补的，等遍历结束后，一起补
        for (int i = 0; i < data.size(); i++) {
            TraceRecordItem cur = data.get(i);
            if (cur.status.equals("T")) {
                //发现异常，向前查找需要补全的堆栈的起点
                exceptionItems.add(cur);
                int startPos = i - 1;
                while (startPos > 0) {
                    TraceRecordItem lastItem = data.get(startPos);
                    if (lastItem.methodId == cur.methodId) {
                        break;
                    }
                    startPos--;
                }
                //需要补全的区间确定：[i，startPos]
                //还原栈结构
                System.out.println("还原栈结构");
                List<List<TraceRecordItem[]>> stack = new ArrayList();
                int stackFrame = 0;
                for (int pos = startPos + 1; pos < i; pos++) {
                    TraceRecordItem item = data.get(pos);
                    System.out.println("处理：当前栈层级 = " + stackFrame + "，item = " + item);

                    if (item.status.equals("B")) {
                        //如果上一个也是B，栈帧++
                        if ((pos > (startPos + 1))
                                && data.get(pos - 1).status.equals("B")) {
                            stackFrame++;
                            System.out.println("上一个也是B，栈帧++");
                        }
                        //获取当前栈层级空间
                        if (stack.size() <= stackFrame) {
                            stack.add(new ArrayList<>());
                        }
                        List<TraceRecordItem[]> stackLineSpace = stack.get(stackFrame);
                        //创建方法，入栈
                        TraceRecordItem[] method = new TraceRecordItem[2];
                        method[0] = item;
                        stackLineSpace.add(method);
                    } else if (item.status.equals("E")) {
                        //方法退出，找到与id匹配的begin
                        SAVE:
                        for (int frame = stack.size() - 1; frame >= 0; frame--) {
                            //在当前栈结构中一层一层的找
                            List<TraceRecordItem[]> line = stack.get(frame);
                            if (line != null && line.size() > 0) {
                                for (TraceRecordItem[] method : line) {
                                    if (method[0].methodId == item.methodId && method[1] == null) {
                                        //id匹配。并且还没有存放end
                                        method[1] = item;
                                        break SAVE;
                                    }
                                }
                            }
                        }
                        //如果上一个也是E，栈帧--
                        if ((pos > (startPos + 1))
                                && data.get(pos - 1).status.equals("E")
                                && stackFrame > 0) {
                            stackFrame--;
                            System.out.println("上一个也是E，栈帧--");
                        }
                    }
                }
                //test:打印栈结构
                printStack(stack);
            }
        }

        //补数据


        //删除T类型数据

    }

    private void stackRecoverTest(List<TraceRecordItem> data) {
        System.out.println("还原栈结构");
        List<List<TraceRecordItem[]>> stack = new ArrayList();
        int stackFrame = 0;
        for (int i = 0; i < data.size(); i++) {
            TraceRecordItem item = data.get(i);
            System.out.println("处理：当前栈层级 = " + stackFrame + "，item = " + item);

            if (item.status.equals("B")) {
                //如果上一个也是B，栈帧++
                if ((i > 0)
                        && data.get(i - 1).status.equals("B")) {
                    stackFrame++;
                    System.out.println("上一个也是B，栈帧++");
                }
                //获取当前栈层级空间
                if (stack.size() <= stackFrame) {
                    stack.add(new ArrayList<>());
                }
                List<TraceRecordItem[]> stackLineSpace = stack.get(stackFrame);
                //创建方法，入栈
                TraceRecordItem[] method = new TraceRecordItem[2];
                method[0] = item;
                stackLineSpace.add(method);
            } else if (item.status.equals("E")) {
                //方法退出，找到与id匹配的begin
                SAVE:
                for (int frame = stack.size() - 1; frame >= 0; frame--) {
                    //在当前栈结构中一层一层的找
                    List<TraceRecordItem[]> line = stack.get(frame);
                    if (line != null && line.size() > 0) {
                        for (TraceRecordItem[] method : line) {
                            if (method[0].methodId == item.methodId && method[1] == null) {
                                //id匹配。并且还没有存放end
                                method[1] = item;
                                break SAVE;
                            }
                        }
                    }
                }
                //如果上一个也是E，栈帧--
                if ((i > 0)
                        && data.get(i - 1).status.equals("E")
                        && stackFrame > 0) {
                    stackFrame--;
                    System.out.println("上一个也是E，栈帧--");
                }
            }
        }
        printStack(stack);

    }

    private void printStack(List<List<TraceRecordItem[]>> stack) {
        System.out.println("-------------调用栈--------------");
        for (int i = 0; i < stack.size(); i++) {
            List<TraceRecordItem[]> line = stack.get(i);
            StringBuilder sb = new StringBuilder();
            for (TraceRecordItem[] method : line) {
                sb.append("|");
                sb.append(method[0].toString());
                sb.append("---");
                if (method[1] != null) {
                    sb.append(method[1].toString());
                } else {
                    sb.append("[空]");
                }

            }
            sb.append("|");
            System.out.println(sb.toString());
        }
        System.out.println("-------------——————--------------");
    }


}
