# Keep kotlinx.serialization generated serializers for AppConfig model classes
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.appforge.generated.**$$serializer { *; }
-keepclassmembers class com.appforge.generated.** {
    *** Companion;
    }
    -keepclasseswithmembers class com.appforge.generated.** {
        kotlinx.serialization.KSerializer serializer(...);
        }
        
