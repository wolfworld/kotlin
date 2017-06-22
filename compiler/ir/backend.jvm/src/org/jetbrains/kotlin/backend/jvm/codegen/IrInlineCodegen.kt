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

package org.jetbrains.kotlin.backend.jvm.codegen

import org.jetbrains.kotlin.codegen.Callable
import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.kotlin.codegen.ValueKind
import org.jetbrains.kotlin.codegen.inline.InlineCodegen
import org.jetbrains.kotlin.codegen.inline.LambdaInfo
import org.jetbrains.kotlin.codegen.inline.SourceCompilerForInline
import org.jetbrains.kotlin.codegen.inline.TypeParameterMappings
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.utils.keysToMap
import org.jetbrains.org.objectweb.asm.Type

class IrInlineCodegen(
        codegen: ExpressionCodegen,
        state: GenerationState,
        function: FunctionDescriptor,
        typeParameterMappings: TypeParameterMappings,
        sourceCompiler: SourceCompilerForInline
) : InlineCodegen<ExpressionCodegen>(codegen, state, function, typeParameterMappings, sourceCompiler), IrCallGenerator {

    override fun putClosureParametersOnStack(next: LambdaInfo, functionReferenceReceiver: StackValue?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun genValueAndPut(valueParameterDescriptor: ValueParameterDescriptor?, argumentExpression: IrExpression, parameterType: Type, parameterIndex: Int, codegen: ExpressionCodegen, blockInfo: BlockInfo) {
        //if (isInliningParameter(argumentExpression, valueParameterDescriptor)) {
        if (argumentExpression is IrBlock && argumentExpression.origin == IrStatementOrigin.LAMBDA) {
            val irReference = argumentExpression.statements.filterIsInstance<IrFunctionReference>().single()
            val irFunction = irReference.symbol.owner
            TODO()
        }
        else {
            putValueOnStack(argumentExpression, parameterType, valueParameterDescriptor?.index ?: -1)
        }
    }


    fun putValueOnStack(argumentExpression: IrExpression, valueType: Type, paramIndex: Int) {
        val onStack = codegen.gen(argumentExpression, valueType, BlockInfo.create())
        putArgumentOrCapturedToLocalVal(onStack.type, onStack, -1, paramIndex, ValueKind.CAPTURED)
    }

    override fun beforeValueParametersStart() {
        invocationParamBuilder.markValueParametersStart()
    }

    override fun genCall(callableMethod: Callable, callDefault: Boolean, codegen: ExpressionCodegen, expression: IrMemberAccessExpression) {
        val typeArguments = expression.descriptor.typeParameters.keysToMap { expression.getTypeArgumentOrDefault(it) }
        performInline(typeArguments, callDefault, codegen)
    }
}