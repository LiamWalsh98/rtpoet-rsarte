package ca.jahed.rtpoet.rsarte

import ca.jahed.rtpoet.rsarte.rts.RSARTELibrary
import ca.jahed.rtpoet.rsarte.rts.protocols.RTExceptionProtocol
import ca.jahed.rtpoet.rsarte.rts.protocols.RTExternalProtocol
import ca.jahed.rtpoet.rtmodel.*
import ca.jahed.rtpoet.rtmodel.cppproperties.*
import ca.jahed.rtpoet.rtmodel.rts.protocols.RTFrameProtocol
import ca.jahed.rtpoet.rtmodel.rts.protocols.RTLogProtocol
import ca.jahed.rtpoet.rtmodel.rts.protocols.RTTimingProtocol
import ca.jahed.rtpoet.rtmodel.sm.*
import ca.jahed.rtpoet.rtmodel.types.RTType
import com.ibm.xtools.uml.rt.core.RTFactory
import com.ibm.xtools.uml.rt.core.internal.util.UMLRTProfile.*

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EClassifier
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.uml2.uml.*


import java.io.File

class RSARTEReader private constructor(private val resource: Resource) {
    private val content = mutableMapOf<EClassifier, MutableList<EObject>>()
    private val cache = mutableMapOf<EObject, Any>()

    init {
        resource.resourceSet.resources.forEach { res ->
            res.allContents.forEach {
                content.getOrPut(it.eClass()) { mutableListOf() }.add(it)
            }
        }
    }

    private constructor(file: String) : this(RSARTELibrary.createResourceSet()
        .getResource(URI.createFileURI(File(file).absolutePath), true))

    companion object {
        @JvmStatic
        fun read(file: String): RTModel {
            return RSARTEReader(file).read()
        }

        @JvmStatic
        fun read(resource: Resource): RTModel {
            return RSARTEReader(resource).read()
        }
    }

    private fun read(): RTModel {
        val model = resource.contents[0] // should be the model package
        return visit(model!!) as RTModel
    }

    private fun visit(eObj: EObject): Any {
        return cache.getOrPut(eObj) {
            when (eObj) {

                is Operation -> visitOperation(eObj)
                is OpaqueBehavior -> visitOpaqueBehavior(eObj)
                is OpaqueExpression -> visitOpaqueExpression(eObj)
                is Parameter -> visitParameter(eObj)
                is Port -> visitPort(eObj)
                is Vertex -> visitVertex(eObj)
                is Transition -> visitTransition(eObj)
                is Trigger -> visitTrigger(eObj)
                is StateMachine -> visitStateMachine(eObj)

                is Package -> {
                    when {
                        eObj.eContainer() == null -> visitModel(eObj) // package with no parent = model package
                        isProtocolContainer(eObj) -> visitProtocol(eObj)
                        else -> visitPackage(eObj)
                    }
                }
                is Class -> {
                    if (isCapsule(eObj)) visitCapsule(eObj)
                    else visitClass(eObj)
                }
                is Property -> {
                    when {
                        isRTPort(eObj) -> visitPort(eObj as Port)
                        isCapsulePart(eObj) -> visitCapsulePart(eObj)
                        else -> visitAttribute(eObj)
                    }
                }

                else -> throw RuntimeException("Unexpected element type ${eObj.eClass().name}")
            }
        }
    }

    private fun visitTransition(transition: Transition): RTTransition {
        val builder = RTTransition.builder(visit(transition.source) as RTGenericState,
            visit(transition.target) as RTGenericState)

        if (transition.effect != null) builder.action(visit(transition.effect) as RTAction)
        if (transition.guard != null) builder.guard(visit(transition.guard.specification) as RTAction)
        transition.triggers.forEach { builder.trigger(visit(it) as RTTrigger) }

        return builder.build()
    }

    private fun visitTrigger(trigger: Trigger): RTTrigger {
        val builder = RTTrigger.builder(visit(trigger.event) as RTSignal)
        trigger.ports.forEach { builder.port(visit(it) as RTPort) }
        return builder.build()
    }


    private fun visitVertex(vertex: Vertex): RTGenericState {
        if (vertex is Pseudostate) {
            return when (vertex.kind) {
                PseudostateKind.INITIAL_LITERAL -> RTPseudoState.initial(vertex.name).build()
                PseudostateKind.CHOICE_LITERAL -> RTPseudoState.choice(vertex.name).build()
                PseudostateKind.SHALLOW_HISTORY_LITERAL -> RTPseudoState.history(vertex.name).build()
                PseudostateKind.JOIN_LITERAL -> RTPseudoState.join(vertex.name).build()
                PseudostateKind.JUNCTION_LITERAL -> RTPseudoState.junction(vertex.name).build()
                PseudostateKind.ENTRY_POINT_LITERAL -> RTPseudoState.entryPoint(vertex.name).build()
                PseudostateKind.EXIT_POINT_LITERAL -> RTPseudoState.exitPoint(vertex.name).build()
                else -> throw RuntimeException("Unknown pseudosate kind ${vertex.kind}")
            }

        } else {
            vertex as State

            return if (vertex.regions.isEmpty()) {
                val builder = RTState.builder(vertex.name)
                if (vertex.entry != null) builder.entry(visit(vertex.entry) as RTAction)
                if (vertex.exit != null) builder.entry(visit(vertex.exit) as RTAction)
                builder.build()
            } else {
                val builder = RTCompositeState.builder(vertex.name)
                if (vertex.entry != null) builder.entry(visit(vertex.entry) as RTAction)
                if (vertex.exit != null) builder.entry(visit(vertex.exit) as RTAction)

                vertex.regions[0].subvertices.forEach { builder.state(visit(it) as RTGenericState) }
                vertex.regions[0].transitions.forEach { builder.transition(visit(it) as RTTransition) }
                vertex.connectionPoints.forEach { builder.state(visit(it) as RTGenericState) }
                builder.build()
            }
        }
    }

    private fun visitStateMachine(stateMachine: StateMachine): RTStateMachine {
        val builder = RTStateMachine.builder()
        stateMachine.regions[0].subvertices.forEach { builder.state(visit(it) as RTGenericState) }
        stateMachine.regions[0].transitions.forEach { builder.transition(visit(it) as RTTransition) }
        return builder.build()
    }

    private fun visitCapsulePart(part: Property): RTCapsulePart {
        val builder = RTCapsulePart.builder(part.name, visit(part.type) as RTCapsule)

        builder.replication(part.upper)
        if (part.lower < 1) builder.optional()
        if (part.isComposite) builder.plugin()

        return builder.build()
    }

    private fun visitPort(port: Port): RTPort {
        val builder = RTPort.builder(port.name, visit(port.type) as RTProtocol)
            .replication(port.upper)

        if (isWired(port)) builder.wired()
        if (isConjugated(port)) builder.conjugate()
        if (isNotification(port, null)) builder.notification() // ignoring context hint
        if (isPublish(port)) builder.publish()
        if (port.isBehavior) builder.behaviour()
        if (port.isService) builder.service()
        builder.registrationOverride(getRegistrationOverride(port, null)) // ignoring context hint

        when (getRegistration(port, null)) { // ignoring context hint
            RTPortRegistrationKind_Application -> builder.appRegistration()
            RTPortRegistrationKind_Automatic_Locked -> builder.autoLockedRegistration()
            else -> builder.autoRegistration()
        }

        when (port.visibility) {
            VisibilityKind.PUBLIC_LITERAL -> builder.publicVisibility()
            VisibilityKind.PRIVATE_LITERAL -> builder.privateVisibility()
            else -> builder.protectedVisibility()
        }

        return builder.build()
    }

    private fun visitParameter(parameter: Parameter): RTParameter {
        val builder = RTParameter.builder(parameter.name, visit(parameter.type) as RTType)
            .replication(parameter.upper)
        return builder.build()
    }

    private fun visitOpaqueBehavior(behavior: OpaqueBehavior): RTAction {
        return RTAction.builder(behavior.bodies.getOrNull(0)?.toString()).build()
    }

    private fun visitOpaqueExpression(expression: OpaqueExpression): RTAction {
        return RTAction.builder(expression.bodies.getOrNull(0)?.toString()).build()
    }

    private fun visitOperation(operation: Operation): RTOperation {
        // Pure UML Objects
        val builder = RTOperation.builder(operation.name)
        operation.methods.forEach { builder.action(visit(it) as RTAction) }
        operation.ownedParameters.forEach {
            when (it.direction) {
                ParameterDirectionKind.RETURN_LITERAL -> builder.ret(visit(it) as RTParameter)
                else -> builder.parameter(visit(it) as RTParameter)
            }
        }
        return builder.build()
    }

    private fun visitAttribute(property: Property): RTAttribute {
        // Pure UML Objects
        val builder = RTAttribute.builder(property.name, visit(property.type) as RTType).replication(property.upper)
        when (property.visibility) {
            VisibilityKind.PUBLIC_LITERAL -> builder.publicVisibility()
            VisibilityKind.PRIVATE_LITERAL -> builder.privateVisibility()
            else -> builder.protectedVisibility()
        }
        return builder.build()
    }

    private fun visitCapsule(klass: Class): RTCapsule {
        val builder = RTCapsule.builder(klass.name)
        return builder.build()
    }

    private fun visitProtocol(protocol: Package): RTProtocol {
        val builder = RTProtocol.builder(protocol.name)

        if (isSystemProtocol(protocol)) {
            return when (protocol.name) {
                "Log" -> RTLogProtocol
                "Timing" -> RTTimingProtocol
                "Frame" -> RTFrameProtocol
                "Exception" -> RTExceptionProtocol
                else -> RTExternalProtocol
            }
        }


        // todo: finish protocol
        return builder.build()
    }

    private fun visitClass(klass: Class): RTClass {
        // for capsule parts are of type "class"
        val builder = RTClass.builder(klass.name)
        klass.ownedOperations.forEach {
            builder.operation(visit(it) as RTOperation)
        }
        klass.ownedAttributes.forEach {
            builder.attribute(visit(it) as RTAttribute)
        }
        return builder.build()
    }

    private fun visitModel(pkg: Package): RTModel {
        val builder = RTModel.builder(pkg.name)


//        val capsule = RTCapsule

//        val topName = model.getEAnnotation("UMLRT_Default_top")?.details?.get("top_name") ?: "Top"
//        val topClass = EMFUtils.getObjectByType(content,
//            UMLPackage.Literals.CLASS, mapOf(Pair("name", topName)))!! as Class

//        val builder = RTModel.builder(model.name, visit(topClass) as RTCapsule)


        pkg.packagedElements.forEach {
            when (it) {
//                is Class -> builder.klass(visit(it) as RTClass)
//                is Enumeration -> builder.enumeration(visit(it) as RTEnumeration)
                is Class -> if (isCapsule(it)) builder.capsule(visit(it) as RTCapsule)
                    else builder.klass(visit(it) as RTClass)
                is Package -> if (isProtocolContainer(it)) builder.protocol(visit(it) as RTProtocol)
                    else builder.pkg(visit(it) as RTPackage)
            }
        }


//        val sourceCapsule = RTFactory.CapsuleFactory.createCapsule()
        return builder.build()
    }

    private fun visitPackage(pkg: Package): RTPackage {
        val rtPkg = RTPackage(pkg.name)

        return rtPkg
    }
}



// ownedAttribute   uml:Port

// type             uml:Collaboration

// ownedBehaviour   uml:StateMachine
//                  uml:Interaction

// subvertex        uml:Pseudostate

// effect           uml:OpaqueBehaviour

// fragment         uml:CombinedFragment

// specification    uml:OpaqueExpression

// minint           uml:LiteralInteger