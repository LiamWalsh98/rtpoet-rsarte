package ca.jahed.rtpoet.rsarte

import ca.jahed.rtpoet.rsarte.rts.RSARTELibrary
import ca.jahed.rtpoet.rsarte.rts.RSARTELibrary.isModelRoot
import ca.jahed.rtpoet.rsarte.utils.RSARTEUtils
import ca.jahed.rtpoet.rtmodel.*
import ca.jahed.rtpoet.rtmodel.sm.*
import ca.jahed.rtpoet.rtmodel.types.RTType

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
                is Connector -> visitConnector(eObj)
                is ConnectorEnd -> visitConnectorEnd(eObj)

                is Transition -> visitTransition(eObj)
                is Trigger -> visitTrigger(eObj)
                is StateMachine -> visitStateMachine(eObj)
                is Interaction -> visitInteraction(eObj)

                is CallEvent -> visitCallEvent(eObj)

                is Package -> {
                    when {
                        isModelRoot(eObj) -> visitModel(eObj) // package with no parent = model package
                        UMLRTProfile.isProtocolContainer(eObj) -> visitProtocol(eObj)
                        else -> visitPackage(eObj)
                    }
                }
                is Class -> {
                    if (UMLRTProfile.isCapsule(eObj)) visitCapsule(eObj)
                    else visitClass(eObj)
                }
                is Property -> {
                    when {
                        UMLRTProfile.isCapsulePart(eObj) -> visitCapsulePart(eObj)
                        else -> visitAttribute(eObj)
                    }
                }

                is PrimitiveType -> visitType(eObj)


                else -> throw RuntimeException("Unexpected element type ${eObj.eClass().name}")
            }
        }
    }

    private fun visitType(type: Type): RTType {
        return when (type) {
            is Enumeration -> visit(type) as RTEnumeration
            is Class -> if (RSARTEUtils.isCapsule(type)) visit(type) as RTCapsule else visit(type) as RTClass
            is Collaboration -> visit(RSARTEUtils.getProtocol(type)!!) as RTProtocol
            else -> RSARTELibrary.getType(type.name)
        }
    }

    private fun visitCallEvent(event: CallEvent): RTSignal {
        // todo: !!! maybe handle system signals? (is this actually necessary?)
        if (RSARTELibrary.isSystemSignal(event))
            return RSARTELibrary.getSystemSignal(event)

        val operation = event.operation!!
        val builder = RTSignal.builder(operation.name)
        operation.ownedParameters.forEach { builder.parameter(visit(it) as RTParameter) }
        return builder.build()
    }

    private fun visitConnector(connector: Connector): RTConnector {
        return RTConnector.builder(
            visit(connector.ends[0]) as RTConnectorEnd,
            visit(connector.ends[1]) as RTConnectorEnd).build()
    }

    private fun visitConnectorEnd(connectorEnd: ConnectorEnd): RTConnectorEnd {
        val part = if (connectorEnd.partWithPort != null) visit(connectorEnd.partWithPort) as RTCapsulePart else null
        return RTConnectorEnd(visit(connectorEnd.role as Port) as RTPort, part)
    }

    private fun visitInteraction(interaction: Interaction): RTOperation {
        val builder = RTOperation.builder(interaction.name)

        interaction.fragments
        interaction.messages

        return builder.build()
    }

    private fun visitTransition(transition: Transition): RTTransition {
        val builder = RTTransition.builder(visit(transition.source) as RTGenericState,
            visit(transition.target) as RTGenericState)

        if (transition.effect != null) {
            val action = visit(transition.effect) as RTAction
            action.name = transition.effect.name
            builder.action(action)
        }
        if (transition.guard != null) builder.guard(visit(transition.guard.specification) as RTAction)

        // todo: handle triggers
        transition.triggers.forEach { builder.trigger(visit(it) as RTTrigger) }

        val build = builder.build()
        build.name = transition.name

        return build
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
//                PseudostateKind.SHALLOW_HISTORY_LITERAL -> RTPseudoState.history(vertex.name).build()
                PseudostateKind.JOIN_LITERAL -> RTPseudoState.join(vertex.name).build()
                PseudostateKind.JUNCTION_LITERAL -> RTPseudoState.junction(vertex.name).build()
                PseudostateKind.ENTRY_POINT_LITERAL -> RTPseudoState.entryPoint(vertex.name).build()
                PseudostateKind.EXIT_POINT_LITERAL -> RTPseudoState.exitPoint(vertex.name).build()
                else -> {
                    if (UMLRTProfile.isRTHistoryState(vertex)) {
                        RTPseudoState.history(vertex.name).build()
                    }
                    else {
                        throw RuntimeException("Unknown pseudostate kind ${vertex.kind}")
                    }
                }
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

        val build = builder.build()
        build.name = stateMachine.name

        return build
    }

//    private fun visitState(state : State) : RTState {
//        val builder = RTStateBuilder(state.name, )
//    }
//
//    private fun visitPseudostate(state : Pseudostate) : RTPseudoState {
//
//    }

    private fun visitCapsulePart(part: Property): RTCapsulePart {
        val builder = RTCapsulePart.builder(part.name, visit(part.type) as RTCapsule)

        builder.replication(part.upper)
        if (part.lower < 1) builder.optional()
        if (part.isComposite) builder.plugin()

        return builder.build()
    }

    private fun visitPort(port: Port): RTPort {
        val builder = RTPort.builder(port.name, visit(port.type.eContainer()) as RTProtocol)
            .replication(port.upper)

        if (UMLRTProfile.isWired(port)) builder.wired()
        if (UMLRTProfile.isConjugated(port)) builder.conjugate()
        if (UMLRTProfile.isNotification(port, null)) builder.notification() // ignoring context hint
        if (UMLRTProfile.isPublish(port)) builder.publish()
        if (port.isBehavior) builder.behaviour()
        if (port.isService) builder.service()
        builder.registrationOverride(UMLRTProfile.getRegistrationOverride(port, null)) // ignoring context hint

        when (UMLRTProfile.getRegistration(port, null)) { // ignoring context hint
            UMLRTProfile.RTPortRegistrationKind_Application -> builder.appRegistration()
            UMLRTProfile.RTPortRegistrationKind_Automatic_Locked -> builder.autoLockedRegistration()
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

//        val builder : RTParameterBuilder
//        // todo: handle null types of system protocols
//        if (parameter.type == null){
//            builder = RTParameter.builder(parameter.name, RTNullType())
//                .replication(parameter.upper)
//        }
//        else{
        val builder = RTParameter.builder(parameter.name, visit(parameter.type) as RTType)
                .replication(parameter.upper)
//        }
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

        klass.attributes.forEach {
            when (it) {
                is Port -> builder.port(visit(it) as RTPort)
                is Property -> when {
                    UMLRTProfile.isCapsulePart(it) -> builder.part(visit(it) as RTCapsulePart)
                    else -> builder.attribute(visit(it) as RTAttribute)
                }
                // todo: verify attributes function properly
            }
        }

        klass.ownedConnectors.forEach { builder.connector(visit(it) as RTConnector) }

        klass.ownedBehaviors.forEach {
            when (it) {
                is StateMachine -> builder.statemachine(visit(it) as RTStateMachine)
                is Interaction -> builder.operation(visit(it) as RTOperation)
            }
        }

        return builder.build()
    }

    private fun visitProtocol(protocol: Package): RTProtocol {
        val builder = RTProtocol.builder(protocol.name)
        val protocolType = protocol.packagedElements.first { it is Collaboration }

        if (UMLRTProfile.isSystemProtocol(protocolType)) {
            return RSARTEUtils.getSystemProtocol(protocolType as Collaboration)
        }

        val umlProfile = RSARTELibrary.getProfile("UMLRealTime")

        val operationMap = mutableMapOf<Operation, RTSignal>()
        protocol.packagedElements.filterIsInstance<CallEvent>().forEach {
            operationMap[it.operation] = visit(it) as RTSignal
            val stereotypes = it.appliedStereotypes
            stereotypes.forEach { type ->
                when (type) {
                    // todo: handle in/out events by checking for duplicate events with both "in" and "out"
                        // post-process this by adding them to rtpoet inOutSignals list
                    umlProfile.getMember("InEvent") -> builder.input(operationMap[it.operation]!!)
                    umlProfile.getMember("OutEvent") -> builder.output(operationMap[it.operation]!!)
                    else -> throw RuntimeException("Could not find signal type (In or Out) ${it.eClass().name}")
                }
            }
        }

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

        // todo: specify which capsule is "top"

        pkg.packagedElements.forEach {
            when (it) {
                is Class ->
                    if (UMLRTProfile.isCapsule(it))
                        builder.capsule(visit(it) as RTCapsule)
                    else builder.klass(visit(it) as RTClass)
                is Package -> if (UMLRTProfile.isProtocolContainer(it)) builder.protocol(visit(it) as RTProtocol)
                    else builder.pkg(visit(it) as RTPackage)
            }
        }

        return builder.build()
    }

    private fun visitPackage(pkg: Package): RTPackage {
        val rtPkg = RTPackage(pkg.name)
        return rtPkg
    }


}