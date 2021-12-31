package com.jty.backtrack.data_parser.worker.correct;

import com.jty.backtrack.data_parser.worker.TraceRecordItem;
import com.jty.backtrack.data_parser.worker.correct.BaseCorrectTask;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jty
 * @date 2021/12/21
 * 用于处理因异常导致的 did not finished
 */
public class DealExceptionTask extends BaseCorrectTask {

    @Override
    protected void run(List<TraceRecordItem> data) {

        //查找是否有T类型，没有说明无异常，不处理
        //如果有T类型，就T类型往前找，找B类型的item。碰到E类型或者是B类型但是id == T的id，就停止找
        //把找到的内容记录下来，这些就是要补的，等遍历结束后，一起补
        for (int i = 0; i < data.size(); i++) {
            TraceRecordItem cur = data.get(i);
            if (cur.status.equals("T")) {
                //发现异常，向前查找需要补全的堆栈的起点
                int startPos = i - 1;
                while (startPos > 0) {
                    TraceRecordItem lastItem = data.get(startPos);
                    if (lastItem.methodId == cur.methodId) {
                        break;
                    }
                    startPos--;
                }
                if (startPos == i - 1){
                    continue;
                }
                //需要补全的区间确定：（startPos，i]
                //还原栈结构
                System.out.println("还原栈结构:(" + startPos + "," + i + "]");
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

                //补数据
                System.out.println("补数据");
                List<TraceRecordItem> lostItem = getLostItem(stack, cur.timeMicroseconds);
                data.addAll(i, lostItem);
                //删除T类型数据
                i += lostItem.size();
                //test:打印栈结构
                printStack(stack);
            }
        }
    }

    /**
     * 计算丢失的条目，这个条目会被插到 T类型的位置
     */
    private List<TraceRecordItem> getLostItem(List<List<TraceRecordItem[]>> stack, long timeMicroseconds) {
        List<TraceRecordItem> lostItem = new ArrayList<>();
        for (int i = stack.size() - 1; i >= 0; i--) {
            List<TraceRecordItem[]> line = stack.get(i);
            if (line != null && line.size() > 0) {
                for (TraceRecordItem[] method : line) {
                    if (method[1] == null) {
                        //补全
                        method[1] = new TraceRecordItem(method[0].methodId, timeMicroseconds, "E");
                        method[0].stackStatus = TraceRecordItem.STACK_STATUS_CATCH;
                        method[1].stackStatus = TraceRecordItem.STACK_STATUS_CATCH;
                        lostItem.add(method[1]);
                    }
                }
            }
        }
        return lostItem;
    }


}
