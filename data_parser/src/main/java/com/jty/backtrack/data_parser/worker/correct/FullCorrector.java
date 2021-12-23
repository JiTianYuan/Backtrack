package com.jty.backtrack.data_parser.worker.correct;

import com.jty.backtrack.data_parser.worker.TraceRecordItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jty
 * @date 2021/12/21
 * Trace修正器，用于处理 did not finished
 * todo:
 */
class FullCorrector extends BaseCorrector {

    public void correct(List<TraceRecordItem> data) {
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
        //补全栈

        //展开栈
    }




}
