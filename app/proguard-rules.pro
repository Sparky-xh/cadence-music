# Keep data/DTO/entity classes used by Room, Retrofit + kotlinx.serialization reflection
-keepclassmembers class com.cadence.music.data.remote.dto.** { *; }
-keepclassmembers class com.cadence.music.data.local.entity.** { *; }
-keep class com.cadence.music.domain.model.** { *; }

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }

# Media3
-keep class androidx.media3.** { *; }

# Firebase / Facebook SDK models
-keep class com.google.firebase.** { *; }
-keep class com.facebook.** { *; }
