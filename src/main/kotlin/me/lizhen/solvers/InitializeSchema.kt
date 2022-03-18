package me.lizhen.solvers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.lizhen.schema.*

fun IdeographContext.initializeSchema() {
    CoroutineScope(Dispatchers.IO).launch {
        conceptNodes = mongoService
            .getCollection<ConceptNode>("concept_node")
            .find()
            .toList()
        conceptTypeDict = conceptNodes.associateBy { it.name }
    }
    CoroutineScope(Dispatchers.IO).launch {
        propertyNodes = mongoService
            .getCollection<PropertyNode>("property_node")
            .find()
            .toList()
    }
    CoroutineScope(Dispatchers.IO).launch {
        relationNodes = mongoService
            .getCollection<RelationNode>("relation_node")
            .find()
            .toList()
    }
    CoroutineScope(Dispatchers.IO).launch {
        hasRelationConceptEdges = mongoService
            .getCollection<HasRelationConceptEdge>("hasRelationConcept_edge")
            .find()
            .toList()
        relationConceptFromDict = hasRelationConceptEdges.groupBy { it.fromId }
        relationConceptDict = hasRelationConceptEdges.associateBy { it.relationId }
    }
    CoroutineScope(Dispatchers.IO).launch {
        hasPropertyEdges = mongoService
            .getCollection<HasPropertyEdge>("hasProperty_edge")
            .find()
            .toList()
        propertyFromDict = hasPropertyEdges.groupBy { it.fromId }
    }
}
