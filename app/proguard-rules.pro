# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/liangshiquan/Downloads/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

###Jar###############################################################
-keep class android.support.v4.**{ *;}
#pinyin4j-2.5.0.jar
-keep class com.hp.hpl.sparta.**{ *;}
-keep class demo.**{ *;}
-dontwarn demo.**
-keep class net.sourceforge.pinyin4j.**{ *;}
#WAPS
-keep public class cn.waps.** {*;}
-keep public interface cn.waps.** {*;}
-dontwarn cn.waps.**
#Umeng
-keep public class com.porster.gift.R$*{
public static final int *;
}
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
###Jar###############################################################

###App###############################################################
-keep class com.porster.gift.model.**{ *;}
-keep class com.porster.gift.widget.**{ *;}
###App###############################################################