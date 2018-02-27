/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package com.ruin.intel.commands

import com.intellij.codeInsight.javadoc.JavaDocInfoGenerator
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

class OneLineJavaDocInfoGeneratorFactory {

    protected fun createImpl(project: Project, element: PsiElement): OneLineJavaDocInfoGenerator {
        return OneLineJavaDocInfoGenerator(project, element)
    }

    companion object {
        val instance: OneLineJavaDocInfoGeneratorFactory
            get() = ServiceManager.getService(OneLineJavaDocInfoGeneratorFactory::class.java)

        fun create(project: Project, element: PsiElement): OneLineJavaDocInfoGenerator {
            return instance.createImpl(project, element)
        }
    }
}
