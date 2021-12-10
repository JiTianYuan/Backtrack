package com.jty.backtrack.data_parser.worker;

import com.jty.backtrack.data_parser.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;

/**
 * @author jty
 * @date 2021/12/2
 * Trace文件转换器，.backtrace > .trace
 */
class TraceConverter {
    private static final boolean DEBUG = true;

    private static final String TRACE_HEAD = "# tracer: nop\n" +
            "#\n" +
            "# entries-in-buffer/entries-written: 30624/30624   #P:4\n" +
            "#\n" +
            "#                                  _-----=> irqs-off\n" +
            "#                                 / _----=> need-resched\n" +
            "#                                | / _---=> hardirq/softirq\n" +
            "#                                || / _--=> preempt-depth\n" +
            "#                                ||| /     delay\n" +
            "#       TASK-PID    TGID   CPU#  ||||    TIMESTAMP  FUNCTION\n" +
            "#          | |        |      |   ||||       |         |";

    private HashMap<Integer, String> mapping;

    public TraceConverter(HashMap<Integer, String> mapping) {
        this.mapping = mapping;
    }

    public Result convert2Trace(String traceDir, String outDir) {
        Result result = new Result();
        if (Util.isEmpty(traceDir)) {
            result.success = false;
            result.msg = "traceDir is null!";
            return result;
        }
        if (Util.isEmpty(outDir)) {
            result.success = false;
            result.msg = "outDir is null!";
            return result;
        }
        File traceDirPath = new File(traceDir);
        List<File> files = Util.eachFile(traceDirPath);
        if (files.size() == 0){
            result.success = false;
            result.msg = "没有发现 backtrace 文件";
            return result;
        }
        File outDirFile = new File(outDir);
        if (!outDirFile.exists()) {
            outDirFile.mkdirs();
        }
        for (File file : files) {
            if (file.getName().endsWith(".backtrace")) {
                if (DEBUG) {
                    System.out.println("[转换trace]:" + file.getName());
                }
                Result convertResult = convertFile(file, outDir);
                if (!convertResult.success) {
                    //发生错误，中断
                    return convertResult;
                }
            }
        }
        result.success = true;
        return result;
    }

    private Result convertFile(File file, String outDir) {
        Result result = new Result();

        //输出文件.systrace格式
        String outFileName = file.getName().split("\\.")[0] + ".systrace";
        File outFile = new File(outDir, outFileName);

        BufferedReader reader = null;
        PrintWriter writer = null;
        try {
            //输入—— .backtrace文件
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader r = new InputStreamReader(fis, "UTF-8");
            reader = new BufferedReader(r);

            String lineTxt = null;
            lineTxt = reader.readLine();
            if (lineTxt != null && lineTxt.startsWith("#Backtrack:")) {
                //是Backtrack格式的文件，创建输出文件
                outFile.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(outFile, false);
                Writer w = new OutputStreamWriter(fileOutputStream, "UTF-8");
                writer = new PrintWriter(w);
                //写入文件头
                writer.println(TRACE_HEAD);

                //解析头信息
                lineTxt = lineTxt.substring("#Backtrack:".length());
                String[] params = lineTxt.split("&");
                String pkgName = "";
                String processId = "";
                String threadId = "";
                for (String param : params) {
                    if (param.startsWith("pkgName")) {
                        pkgName = param.split("=")[1];
                    } else if (param.startsWith("processId")) {
                        processId = param.split("=")[1];
                    } else if (param.startsWith("threadId")) {
                        threadId = param.split("=")[1];
                    }
                }
                //转换数据信息
                //输入格式：methodId,timeMicroseconds,<B或者E>
                //输出格式：<包名-线程id>  ( <线程id>) [000] .... <时间>: tracing_mark_write: <B或者E>|<进程ID>|<TAG>
                DecimalFormat df = new DecimalFormat("#0.000000");
                while ((lineTxt = reader.readLine()) != null) {
                    //输入格式：methodId,timeMicroseconds,<B或者E>
                    String[] split = lineTxt.split(",");
                    int methodId = Integer.parseInt(split[0]);
                    long timeMicroseconds = Long.parseLong(split[1]);
                    String status = split[2];

                    double timeSecond = timeMicroseconds / 1000000d;
                    String methodName = getMethodName(methodId);
                    //输出格式：<包名-线程id>  ( <线程id>) [000] .... <时间>: tracing_mark_write: <B或者E>|<进程ID>|<TAG>

                    String traceLine = pkgName + "-" + threadId + "  ( " + threadId + ") [000] .... " +  df.format(timeSecond) + ": tracing_mark_write: " + status + "|" + processId + "|" + methodName;
                    writer.println(traceLine);
                }
                result.success = true;
            } else {
                //不是Backtrack格式的文件
                result.success = false;
                result.msg = "convert File error :" + file.getName() + ", Exception : 不是Backtrack格式的文件";
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.success = false;
            result.msg = "convert File error :" + file.getName() + ", Exception : " + e.getMessage();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private String getMethodName(int methodId) {
        String methodName = mapping.get(methodId);
        return methodName != null ? methodName : String.valueOf(methodId);
    }
}
