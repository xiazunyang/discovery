package cn.numeron.discovery.ksp

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration

internal fun KSDeclaration.isInterface(): Boolean {
    return this is KSClassDeclaration && classKind == ClassKind.INTERFACE
}

internal fun KSDeclaration.getQualifierName(): String {
    return packageName.asString() + '.' + simpleName.asString()
}