# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class org.bouncycastle.** { *; }
-keep class com.swedaiaiwallpapersart.backgroundanimewallpaperaiphoto.** { *; }

# Keep Conscrypt classes (if used)
-keep class org.conscrypt.** { *; }

# Keep OpenJSSE classes (if used)
-keep class org.openjsse.** { *; }
-keep class com.google.gson.** { *; }

-dontwarn android.os.ServiceManager.*
-dontwarn com.bun.miitmdid.core.MdidSdkHelper.*
-dontwarn com.bun.miitmdid.interfaces.IIdentifierListener.*
-dontwarn com.bun.miitmdid.interfaces.IdSupplier.*
-dontwarn com.google.firebase.iid.FirebaseInstanceId.*
-dontwarn com.google.firebase.iid.InstanceIdResult.*
-dontwarn com.huawei.hms.ads.identifier.AdvertisingIdClient$Info.*
-dontwarn com.huawei.hms.ads.identifier.AdvertisingIdClient.*
-dontwarn com.tencent.android.tpush.otherpush.OtherPushClient.*
-dontwarn org.bouncycastle.jsse.BCSSLParameters.*
-dontwarn org.bouncycastle.jsse.BCSSLSocket.*
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider.*
-dontwarn org.conscrypt.Conscrypt$Version.*
-dontwarn org.conscrypt.Conscrypt.*
-dontwarn org.openjsse.javax.net.ssl.SSLParameters.*
-dontwarn org.openjsse.javax.net.ssl.SSLSocket.*
-dontwarn org.openjsse.net.ssl.OpenJSSE.*
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn kotlin.reflect.jvm.internal.**
-keep class com.swedaiaiwallpapersart.backgroundanimewallpaperaiphoto.ratrofit** { <init>();}


# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
#-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# With R8 full mode generic signatures are stripped for classes that are not kept.
-keep,allowobfuscation,allowshrinking class retrofit2.Response

-keepclassmembers class com.bluell.roomdecoration.interiordesign.* {
    public <init>();
}

# Keep all classes and their members within your package
-keep class com.bluell.roomdecoration.interiordesign.** { *; }

# Keep all constructors within your package
-keepclassmembers class com.bluell.roomdecoration.interiordesign.** {
    public <init>();
}

# If Firebase uses reflection, keep the necessary classes and methods
-keep class com.bluell.roomdecoration.interiordesign.** {
    *;
}
