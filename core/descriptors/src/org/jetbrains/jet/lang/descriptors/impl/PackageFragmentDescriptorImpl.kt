/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.lang.descriptors.impl

import org.jetbrains.jet.lang.descriptors.PackageFragmentDescriptor
import org.jetbrains.jet.lang.descriptors.ModuleDescriptor
import org.jetbrains.jet.lang.resolve.name.FqName
import org.jetbrains.jet.lang.resolve.name.shortNameOrRoot
import org.jetbrains.jet.lang.descriptors.DeclarationDescriptor
import org.jetbrains.jet.lang.descriptors.PackageFragmentProvider
import org.jetbrains.jet.lang.resolve.scopes.JetScope
import org.jetbrains.jet.lang.types.TypeSubstitutor
import org.jetbrains.jet.lang.descriptors.DeclarationDescriptorVisitor

fun PackageFragmentDescriptorOrNull(
        provider: PackageFragmentProvider,
        module: ModuleDescriptor,
        fqName: FqName,
        memberScope: PackageFragmentDescriptor.() -> JetScope?
): PackageFragmentDescriptor? {
    class NoMemberScopeException: RuntimeException()

    try {
        return PackageFragmentDescriptorImpl(provider, module, fqName, {memberScope() ?: throw NoMemberScopeException() })
    }
    catch (e: NoMemberScopeException) {
        return null
    }
}

class PackageFragmentDescriptorImpl(
        private val _provider: PackageFragmentProvider,
        private val module: ModuleDescriptor,
        private val _fqName: FqName,
        memberScope: PackageFragmentDescriptor.() -> JetScope
) : DeclarationDescriptorImpl(listOf(), _fqName.shortNameOrRoot()), PackageFragmentDescriptor {

    override fun getContainingDeclaration() = module
    override fun getProvider() = _provider
    override fun getFqName() = _fqName

    private val _memberScope = memberScope()
    override fun getMemberScope() = _memberScope

    override fun substitute(substitutor: TypeSubstitutor) = this

    override fun <R, D> accept(visitor: DeclarationDescriptorVisitor<R, D>, data: D): R  = visitor.visitPackageFragmentDescriptor(this, data) as R

    override fun toString(): String? {
        return "package fragment $_fqName"
    }
}