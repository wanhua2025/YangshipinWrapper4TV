-keep class com.lwtdzh.yangshipinwrapper4tv.** { *; }
-keep class tv.danmaku.ijk.** { *; }
-keep class xyz.doikki.** { *; }
-dontwarn tv.danmaku.ijk.**
-dontwarn xyz.doikki.**

-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**

-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

-keep class org.jellyfin.** { *; }
-dontwarn org.jellyfin.**

-keep class androidx.** { *; }
-dontwarn androidx.**

-keep class **.R$* { *; }
-keep class **.R { *; }

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers class * {
    public <init>(android.content.Context);
}

-dontwarn javax.annotation.**
-dontwarn kotlin.**