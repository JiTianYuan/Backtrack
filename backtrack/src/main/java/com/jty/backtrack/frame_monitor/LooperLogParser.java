package com.jty.backtrack.frame_monitor;

/**
 * @author jty
 * @date 2021/11/01
 * <p>
 * Looper printer 内容解析器
 */
public class LooperLogParser {

    /**
     * result[0] = target Handler className
     * result[1] = target Handler identityHashCode to HexString
     * result[2] = callback className
     * result[3] = callback identityHashCode to HexString
     * result[4] = msg.what
     */
    public static String[] parse(boolean isBegin, String log) {
        String[] result = new String[5];
        //>>>>> Dispatching to Handler (android.view.Choreographer$FrameHandler) {d02c679} android.view.Choreographer$FrameDisplayEventReceiver@ff6481f: 0
        String body = getBody(isBegin, log);
        int targetEnd = body.indexOf("}");
        if (targetEnd != -1) {
            String targetBody = body.substring(0, targetEnd);
            String[] targetSplit = targetBody.split("\\) \\{");
            if (targetSplit.length == 1) {
                result[0] = targetSplit[0];
            } else if (targetSplit.length == 2) {
                result[0] = targetSplit[0];
                result[1] = targetSplit[1];
            }
            int whatStart = isBegin ? body.lastIndexOf(": ") : -1;
            String callbackBody;
            if (whatStart != -1) {
                result[4] = body.substring(whatStart).trim();
                callbackBody = body.substring(targetEnd, whatStart);
            } else {
                callbackBody = body.substring(targetEnd).trim();
            }
            String[] callbackSplit = callbackBody.split("@");
            if (callbackSplit.length == 1) {
                result[2] = callbackSplit[0];
            } else if (callbackSplit.length == 2) {
                result[2] = callbackSplit[0];
                result[3] = callbackSplit[1];
            }
        }
        return result;
    }

    public static String getCallbackStr(boolean isBegin, String log) {
        int whatStart = isBegin ? log.lastIndexOf(": ") : -1;
        if (whatStart != -1) {
            log = log.substring(0, whatStart).trim();
        }
        int callbackStart = log.lastIndexOf(" ");
        if (callbackStart != -1) {
            return log.substring(callbackStart + 1);
        } else {
            return "";
        }

    }

    private static String getBody(boolean isBegin, String log) {
        int head = isBegin ? 30 : 27;   //截取到 ...Handler (
        return log.substring(head);
    }
}
