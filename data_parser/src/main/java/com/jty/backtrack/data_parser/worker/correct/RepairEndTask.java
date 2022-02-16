package com.jty.backtrack.data_parser.worker.correct;

import com.jty.backtrack.data_parser.worker.TraceRecordItem;
import com.jty.backtrack.data_parser.worker.correct.BaseCorrectTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author jty
 * @date 2021/12/21
 * 补充End事件，用于解决 异常处理完还是存在没有End 的情况
 * 比如 try-catch里套try-catch的情况。。。
 */
class RepairEndTask extends BaseCorrectTask {

    @Override
    public void run(List<TraceRecordItem> data) {
        //System.out.println("处理前");
        //printDataAsStack(data);
        //System.out.println("=========================================");
        Stack<TraceRecordItem> stack = new Stack<>();
        for (int i = 0; i < data.size(); i++) {
            TraceRecordItem item = data.get(i);
            if (item.status.equals("B")) {
                //入栈
                stack.push(item);
            } else if (item.status.equals("E")) {
                //出栈
                TraceRecordItem peek = null;
                if (!stack.empty()) {
                    peek = stack.peek();
                } else {
                    System.out.println("[Error!!!] item还没有入栈，缺少B事件，item = " + item);
                }
                if (peek != null) {
                    if (peek.methodId != item.methodId) {
                        //当前栈顶缺失end事件
                        while (stack.peek().methodId != item.methodId) {
                            //构建需要补上的item
                            TraceRecordItem loseEndItem = stack.peek();
                            TraceRecordItem addItem = new TraceRecordItem(loseEndItem.methodId, item.timeMicroseconds, item.status);
                            loseEndItem.stackStatus = TraceRecordItem.STACK_STATUS_UNKNOWN_EXCEPTION;
                            addItem.stackStatus = TraceRecordItem.STACK_STATUS_UNKNOWN_EXCEPTION;
                            data.add(i + 1, addItem);
                            i++;
                            stack.pop();
                        }
                    }
                    stack.pop();
                }
            } else if (item.status.equals("F")) {
                //遇到了 "强制结束" 的标记
                while (!stack.empty()) {
                    //构建需要补上的item
                    TraceRecordItem loseEndItem = stack.pop();
                    TraceRecordItem addItem = new TraceRecordItem(loseEndItem.methodId, item.timeMicroseconds, "E");
                    loseEndItem.stackStatus = TraceRecordItem.STACK_STATUS_FORCE_DUMP;
                    addItem.stackStatus = TraceRecordItem.STACK_STATUS_FORCE_DUMP;
                    data.add(i + 1, addItem);
                    i++;
                }
            }
        }

        //System.out.println("处理后");
        //printDataAsStack(data);
    }

    private void printDataAsStack(List<TraceRecordItem> data) {
        List<List<TraceRecordItem[]>> stack = new ArrayList();
        int stackFrame = 0;
        for (int i = 0; i < data.size(); i++) {
            TraceRecordItem item = data.get(i);

            if (item.status.equals("B")) {
                //如果上一个也是B，栈帧++
                if ((i > 0)
                        && data.get(i - 1).status.equals("B")) {
                    stackFrame++;
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
                }
            }
        }
        printStack(stack);
    }


}
