/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.translate.expression

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.inline.util.FunctionWithWrapper
import org.jetbrains.kotlin.js.translate.context.Namer
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils

private val METADATA_PROPERTIES_COUNT = 2

class InlineMetadata(val tag: JsStringLiteral, val function: FunctionWithWrapper) {
    companion object {
        @JvmStatic
        fun compose(function: JsFunction, descriptor: CallableDescriptor, context: TranslationContext): InlineMetadata {
            val tag = JsStringLiteral(Namer.getFunctionTag(descriptor, context.config))
            val inliningContext = context.inlineFunctionContext!!
            val block = JsBlock(inliningContext.importBlock.statements + inliningContext.declarationsBlock.statements)
            block.statements += JsReturn(function)
            return InlineMetadata(tag, FunctionWithWrapper(function, block))
        }

        @JvmStatic
        fun decompose(expression: JsExpression?): InlineMetadata? =
                when (expression) {
                    is JsInvocation -> decomposeCreateFunctionCall(expression)
                    else -> null
                }

        private fun decomposeCreateFunctionCall(call: JsInvocation): InlineMetadata? {
            val qualifier = call.qualifier
            if (qualifier !is JsNameRef || qualifier.ident != Namer.DEFINE_INLINE_FUNCTION) return null

            val arguments = call.arguments
            if (arguments.size != METADATA_PROPERTIES_COUNT) return null

            val tag = arguments[0] as? JsStringLiteral ?: return null
            val callExpression = arguments[1]
            val function = tryExtractFunction(callExpression) ?: return null

            return InlineMetadata(tag, function)
        }

        @JvmStatic
        fun tryExtractFunction(callExpression: JsExpression): FunctionWithWrapper? {
            if (callExpression !is JsFunction) return null
            fun simple() = FunctionWithWrapper(callExpression, null)

            val name = callExpression.name ?: return simple()
            if (callExpression.body.statements.size != 2) return simple()
            val (assignment, returnStatement) = callExpression.body.statements

            val assignmentExpression = (assignment as? JsExpressionStatement ?: return simple()).expression
            val (lhs, rhs) = JsAstUtils.decomposeAssignmentToVariable(assignmentExpression) ?: return simple()
            if (lhs != name) return simple()

            val wrapperBody = ((rhs as? JsInvocation)?.qualifier as? JsFunction)?.body ?: return simple()
            val function = (wrapperBody.statements.lastOrNull() as? JsReturn)?.expression as? JsFunction ?: return simple()

            if (returnStatement !is JsReturn) return simple()
            val returnInvocation = returnStatement.expression as? JsInvocation ?: return simple()
            val returnName = (returnInvocation.qualifier as? JsNameRef)?.name ?: return simple()
            if (returnName != name || returnInvocation.arguments.isNotEmpty()) return simple()

            return FunctionWithWrapper(function, JsBlock(wrapperBody.statements.filter { it !is JsReturn }))
        }
    }

    val functionWithMetadata: JsExpression
        get() {
            val propertiesList = listOf(tag, wrapInlineFunction())
            return JsInvocation(Namer.createInlineFunction(), propertiesList)
        }

    private fun wrapInlineFunction(): JsExpression {
        val (function, wrapperBody) = this.function
        if (wrapperBody == null) return function

        val runner = JsFunction(function.scope, JsBlock(), "")
        val wrapperName = JsScope.declareTemporaryName("runner")
        runner.name = wrapperName

        val builder = JsFunction(runner.scope, JsBlock(), "")
        builder.body.statements += wrapperBody.statements
        builder.body.statements += JsReturn(JsInvocation(JsAstUtils.pureFqn(wrapperName, null)))

        runner.body.statements += JsAstUtils.assignment(wrapperName.makeRef(), JsInvocation(builder)).makeStmt()
        runner.body.statements += JsReturn(JsInvocation(wrapperName.makeRef()))

        return runner
    }
}