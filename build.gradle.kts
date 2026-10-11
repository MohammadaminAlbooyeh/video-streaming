plugins {
    alias(plugins.androidApplication) apply false
    alias(plugins.kotlinAndroid) apply false
    alias(plugins.composeCompiler) apply false
    alias(plugins.ksp) apply false
    alias(plugins.hilt) apply false
}

allprojects {
    group = "it.bollofacile"
    version = "1.0.0"
}