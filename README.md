# Backtrack
## 简介
Backtrack是一个性能优化分析工具，通过字节码插桩记录方法的入栈和出栈，在出现卡顿时回溯堆栈来还原卡顿现场(目前只支持主线程)，并通过工具将结果转化为perfetto支持的格式以便可视化

> 目前支持的功能有：
> 1. 卡顿分析：可自定义卡顿阈值，当一帧绘制时间超过阈值时，会将当前帧内的主线程调用栈导出
> 2. 启动时长分析：开启功能开关后，在需要结束的位置调用指定方法，将期间主线程调用栈导出

## 接入
#### 1、project的build.gradle配置
仓库配置：
```
repositories {
    maven { url 'https://jitpack.io' }
}
```
dependencies增加plugin依赖：
```
dependencies {
    classpath 'com.github.JiTianYuan.Backtrack:backtrack_plugin:v1.0.0'
}
```
#### 2、app的build.gradle配置
添加插件：
```
apply plugin: 'com.jty.backtrack'
```
添加依赖：
```
dependencies {
    implementation 'com.github.JiTianYuan.Backtrack:backtrack:v1.0.0'
}
```

支持的配置项：
```
backtrack {
    methodIdStart = "0x01"      //生成的方法id的起始值，配合插件化使用
    whiteListFile = "backtrack-whitelist.txt"   //配置插桩白名单
}
```

#### 3、代码混淆配置：
```
-keep class com.jty.backtrack.core.Backtrack{*;}
```

#### 4、插桩白名单配置
```
# 类的白名单
-keepClass com.jty.backtrack_demo.MyApplication

# 包的白名单
-keepPackage android.*
-keepPackage androidx.*

# 方法白名单
-keepMethod com.jty.backtrack_demo.MyApplication.attachBaseContext
```
#### 5、启动耗时分析功能
如果需要分析启动耗时，可以在Backtrack初始化时配置`recordStartUp(true)` ，并且在启动流程结束的地方调用`Backtrack.getInstance().recordStartUpEnd()`。

**注:**
1. 如果设置了`recordStartUp(true)`，就必须调用`Backtrack.getInstance().recordStartUpEnd()`，否则会不停的记录堆栈信息导致内存溢出
2. `Backtrack.getInstance().recordStartUpEnd()`必须在主线程调用

## 使用

#### Step1：初始化（越早越好）：
``` java
Config config = new Config.Builder()
        .debuggable(true)   // debug模式下会打印log，过滤”Backtrack“
        .outputDir(SDCARD.getStorageDir() + PATH.FOLDER_NAME + File.separator + "Backtrace")  //输出文件夹
        .jankFrameThreshold(10)  // 丢帧阈值，当连续丢帧达n帧时，保存卡顿信息。默认值10
        .recordStartUp(false)   //是否开启启动耗时分析，需要配合 Backtrack.getInstance().recordStartUpEnd() 使用
        .initialStackSize(1024 * 1024)  //初始化栈大小，一个栈占用12字节，默认大小1024*1024(12M)
        .build();
Backtrack.init(context, config);
```
#### Step2：执行打包，生成mapping文件
会在build/outputs/mapping下生成methodMapping.txt 和 ignoreMethodMapping.txt文件
methodMapping.txt保存着插桩方法的方法id和方法名的映射
ignoreMethodMapping.txt是被过滤的未插桩的方法

#### Step3：正常使用app，当连续丢帧满足配置条件时，会自动保存信息

#### Step4：从配置的输出路径中取出.backtrace文件

#### Step5：使用解析工具分析文件
解析工具是data_parser 这个module，直接运行Main.java（需要javaFx环境）或者使用releaseJar目录下的jar包都可以
解析工具中分别填入methodMapping.txt所在的文件夹、.backtrace所在的文件夹、输出文件夹。点击GO生成.systrace格式的文件
> 如果jar包打不开，可以运行`com.jty.backtrack.data_parser.TestMain`，代码中输入参数

#### Step6：使用perfetto查看systrace文件
使用Chrome浏览器打开网址：https://ui.perfetto.dev/#!/
将.systrace文件拖进去即可

**注:**
> 运行时异常会导致方法有begin无end，从而引起`"Did Not Finished"`问题，导致堆栈信息不准确。为了解决这个问题，我们试图对堆栈信息进行还原，并添加了标记:
> * 方法前有(E)标记，表示当前方法发生了异常，被外层方法try-catch住了，方法的end时间为catch的时间
> * 方法前有(?)标记，表示当前方法没有end的原因未知，我们尝试对它进行了修补，但结果不一定正确(堆栈层级和嵌套关系可能不对)
> * 方法前有(F)标记，表示当前堆栈被强制dump，丢失end事件，这种情况目前发生在启动耗时分析场景，用户主动调用了`Backtrack.getInstance().recordStartUpEnd()`

