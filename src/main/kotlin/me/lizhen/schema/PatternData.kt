package me.lizhen.schema

import kotlinx.serialization.Serializable
import me.lizhen.algorithms.LogicOperator
import me.lizhen.solvers.PatternSolution

public interface IdentifiablePattern {
    val patternId: String
}

@Serializable
public data class PatternNode(
    override val patternId: String,
    val type: String
) : IdentifiablePattern

@Serializable
public enum class PatternType(val value: Int) {
    Node(0), Edge(1), Constraint(2)
}

@Serializable
public enum class ComparisonOperator(val value: Int) {
    Equal(0), NotEqual(1),
    Greater(2), GreaterOrEqual(3), Less(4), LessOrEqual(5),
    MatchRegex(6)
}


@Serializable
public data class PatternConstraint(
    override val patternId: String,
    val targetType: PatternType,
    val targetPatternId: String,

    val property: String,
    val operator: ComparisonOperator,
    val value: String,

    val isReversed: Boolean = false
) : IdentifiablePattern


@Serializable
public data class PatternEdge(
    override val patternId: String,

    val type: String,

    val fromPatternId: String,
    val toPatternId: String,
) : IdentifiablePattern

@Serializable
public data class PatternAggregation(
    val nodes: List<String>,
    val edges: List<String>,
    val multiplier: Int,
)

@Serializable
public data class Pattern(
    val nodes: List<PatternNode>,
    val edges: List<PatternEdge>?,
    val constraints: List<PatternConstraint>?,
)

@Serializable
public data class AggregatedPattern(
    val nodes: List<PatternNode>,
    val edges: List<PatternEdge>?,
    val constraints: List<PatternConstraint>?,
    val aggregations: List<PatternAggregation>?,

    val logicOperators: List<PatternLogicOperator>?,
    val connections: List<ConstraintConnection>?
)

@Serializable
public data class PatternLogicOperator(
    override val patternId: String,
    val type: LogicOperator
) : IdentifiablePattern

@Serializable
public data class ConstraintConnection(
    val from: String,
    val to: String
)

@Serializable
public data class CompositePattern(
    val nodes: List<PatternNode>,
    val edges: List<PatternEdge>?,
    val constraints: List<PatternConstraint>?,

    val logicOperators: List<PatternLogicOperator>?,
    val connections: List<ConstraintConnection>?
) {

    inline fun nodes(nodeBuilder: CompositePattern.() -> Unit) {

    }

    inline fun edges(edgeBuilder: CompositePattern.() -> Unit) {

    }

    inline fun constraint(constraintBuilder: CompositePattern.() -> Unit) {

    }

    infix fun String.to(patternId: String): PatternEdge {
        return PatternEdge(
            fromPatternId = this,
            toPatternId = patternId,
            patternId = "",
            type = "PatternType.Node",
        )
    }

    companion object {


        operator fun invoke(contextualBuilder: CompositePattern.() -> Unit) {

        }
    }
}

//
//
//public class CompositePatternBuildContext {
//    var nodes: MutableList<PatternNode> = mutableListOf()
//    var edges: MutableList<PatternEdge> = mutableListOf()
//
//    constructor(builder: CompositePatternBuildContext.() -> Unit) {
//
//    }
//
//    infix fun String.to(patternId: String): PatternEdge {
//        return PatternEdge(
//            fromPatternId = this,
//            toPatternId = patternId,
//            patternId = "",
//            type = "PatternType.Node",
//        )
//    }
//
//    infix fun String.concept(patternId: String): PatternEdge {
//        return PatternEdge(
//            fromPatternId = this,
//            toPatternId = patternId,
//            patternId = "",
//            type = "PatternType.Node",
//        )
//    }
//
//    fun build(): CompositePattern {
//        return CompositePattern(
//            nodes,
//            edges,
//            null,
//            null,
//            null,
//        )
//    }
//}
//
//
//fun pattern(contextualBuilder: CompositePatternBuildContext.() -> Unit): CompositePattern {
//    return CompositePatternBuildContext(contextualBuilder).build()
//}
//
//
//val c = pattern {
//    "Amy" concept "People"
//    "Ben" concept "People"
//    "Dylon" concept "People"
//
//    "Amy" to "Ben"
//    "Amy" to "Dylon"
//}


fun PatternSolution.uniqKey(): String = nodes.toList().joinToString("") {
    it.second.nodeId.toString()
} + edges.toList().joinToString("") {
    it.second.edgeId.toString()
}


fun WorkspaceNode.satisfies(constraint: PatternConstraint): Boolean {
    val prop = properties[constraint.property].orEmpty()
    val satisfiesRaw = when (constraint.operator) {
        ComparisonOperator.MatchRegex -> prop.matches(Regex(constraint.value))
        ComparisonOperator.Equal -> prop == constraint.value
        ComparisonOperator.NotEqual -> prop != constraint.value
        ComparisonOperator.Less -> prop < constraint.value
        ComparisonOperator.LessOrEqual -> prop <= constraint.value
        ComparisonOperator.Greater -> prop > constraint.value
        ComparisonOperator.GreaterOrEqual -> prop >= constraint.value
    }
    return if (constraint.isReversed) !satisfiesRaw else satisfiesRaw
}

fun List<PatternConstraint>?.validate(workspaceNode: WorkspaceNode): Boolean {
    if (this == null) return true

    return this.all {
        workspaceNode.satisfies(it)
    }
}


@Serializable
data class AggregatedPatternSolution(
    val pattern: Pattern,
    val solution: List<PatternSolution>,
)