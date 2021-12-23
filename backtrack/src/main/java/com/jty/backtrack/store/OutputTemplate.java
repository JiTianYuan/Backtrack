package com.jty.backtrack.store;

import com.jty.backtrack.utils.StatusSpec;

/**
 * @author jty
 * @date 2021/11/10
 * 输出模板
 */
class OutputTemplate {
    //Backtrack:pkgName=$pkg_name$;processId=$process_id$;threadId=$thread_id$;
    private static final String HEAD = "Backtrack:pkgName=$pkg_name$;processId=$process_id$;threadId=$thread_id$;";


    // Backtrack:pkgName=$pkg_name$;processId=$process_id$;threadId=$thread_id$;
    static String buildHead(String pkgName, int processId, long threadId) {
        return "#Backtrack:pkgName=" + pkgName + "&processId=" + processId + "&threadId=" + threadId;
    }

    // $方法id$,$时间(微秒)$,$进栈出栈（B或者E）$
    static String buildData(int methodId, long timeMicroseconds, long status) {
        String flag = "";
        if (StatusSpec.STATUS_IN == status){
            flag = "B";
        } else if (StatusSpec.STATUS_OUT == status) {
            flag = "E";
        }else if (StatusSpec.STATUS_EXCEPTION == status){
            flag = "T";
        }
        return methodId + "," + timeMicroseconds + "," + flag;
    }
}
