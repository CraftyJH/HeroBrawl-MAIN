# Kotlin serialization rules
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class com.herobrawl.game.**$$serializer { *; }
-keepclassmembers class com.herobrawl.game.** {
    *** Companion;
}
-keepclasseswithmembers class com.herobrawl.game.** {
    kotlinx.serialization.KSerializer serializer(...);
}
