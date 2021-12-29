package net.arwix.urania.core.annotation

@RequiresOptIn(level = RequiresOptIn.Level.WARNING, message = "This API is experimental. It may be changed in the future without notice.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ExperimentalUrania