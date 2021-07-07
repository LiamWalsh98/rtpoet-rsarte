package ca.jahed.rtpoet.rsarte


import ca.jahed.rtpoet.rsarte.UMLRTProfile.*
import ca.jahed.rtpoet.rsarte.rts.RSARTELibrary
import ca.jahed.rtpoet.rsarte.rts.RSARTELibrary.isModelRoot
import ca.jahed.rtpoet.rtmodel.*
import ca.jahed.rtpoet.rtmodel.rts.RTSystemSignal
import ca.jahed.rtpoet.rtmodel.sm.*
import ca.jahed.rtpoet.rtmodel.types.RTType
import ca.jahed.rtpoet.rtmodel.types.primitivetype.RTPrimitiveType
import ca.jahed.rtpoet.rtmodel.visitors.RTCachedVisitor
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.uml2.uml.*
import java.io.File


class RSARTEWriter private constructor(private val resource: Resource) : RTCachedVisitor() {

    private val original = mutableMapOf<Element, RTElement>()
    private lateinit var model: Package
    private var toApplyStereotypes: ArrayList<Element> = ArrayList()

//    private val xmi = XMIResource

    private constructor(file: String) : this(
        RSARTELibrary.createResourceSet()
        .createResource(URI.createFileURI(File(file).absolutePath)))

    companion object {
        @JvmStatic
        fun write(file: String, model: RTModel) {
            RSARTEWriter(file).write(model, true)
        }

        @JvmStatic
        fun write(resource: Resource, model: RTModel) {
            RSARTEWriter(resource).write(model)
        }
    }

    override fun visit(element: RTElement): Any {
        val copy = super.visit(element)
        original[copy as Element] = element
        return copy
    }

    fun getOriginal(element: Element): RTElement? {
        return original[element]
    }

    private fun write(model: RTModel, save: Boolean = false) {
        val umlModel = visit(model) as Package

        resource.contents.add(umlModel)

        resolveStereotypes()


        if (save) {
            resource.save(null)
        }
    }

    fun resolveStereotypes() {
        val profile = RSARTELibrary.getProfile("UMLRealTime")
        val default = RSARTELibrary.getProfile("Default")

        toApplyStereotypes.forEach{
            when (it) {

                is Class -> (it.applyStereotype(profile.getMember("Capsule") as Stereotype))

                is Collaboration -> (it.applyStereotype(profile.getMember("Protocol") as Stereotype))

                is Port -> {
                    val kind : EnumerationLiteral
                    val registrationKinds = profile.getMember("PortRegistrationKind").ownedElements

                    // todo: refactor below enum conversion into utils
                    kind = when ((getOriginal(it) as RTPort).registrationType) {
                        RTPort.RegistrationType.AUTOMATIC -> {
                            registrationKinds.firstOrNull { x -> (x as EnumerationLiteral).name == "Automatic" } as EnumerationLiteral
                        }
                        RTPort.RegistrationType.APPLICATION -> {
                            registrationKinds.firstOrNull { x -> (x as EnumerationLiteral).name == "Application" } as EnumerationLiteral
                        }
                        RTPort.RegistrationType.AUTOMATIC_LOCKED -> {
                            registrationKinds.firstOrNull { x -> (x as EnumerationLiteral).name == "Automatic (locked)" } as EnumerationLiteral
                        }
                    }
                    val portStereotype = profile.getMember("RTPort")

                    (it.applyStereotype(portStereotype as Stereotype))
                    it.setValue(portStereotype, "isWired", (getOriginal(it) as RTPort).wired)
                    it.setValue(portStereotype, "isConjugate", (getOriginal(it) as RTPort).conjugated)
                    it.setValue(portStereotype, "isNotification", (getOriginal(it) as RTPort).notification)
                    it.setValue(portStereotype, "isPublish", (getOriginal(it) as RTPort).publish)
                    it.setValue(portStereotype, "registration", kind)
                    val override = (getOriginal(it) as RTPort).registrationOverride
                    if (override != "") {
                        it.setValue(portStereotype, "registrationOverride", override)
                    }
                }

                is CallEvent -> {
                    val prot = (getOriginal(it.eContainer() as Package) as RTProtocol)

                    when ((getOriginal(it) as RTSignal)) {
                        in prot.inputSignals -> it.applyStereotype(profile.getMember("InEvent") as Stereotype)
                        in prot.outputSignals -> it.applyStereotype(profile.getMember("OutEvent") as Stereotype)
//                        in prot.inOutSignals -> todo: preprocess inOut signals as separate input and output signals
                    }

                }//"InEvent/OutEvent"


                is Package -> {
                    if (isModelRoot(it)) {
                        val defaultLang = default.getMember("DefaultLanguage")
                        it.applyStereotype(defaultLang as Stereotype)
                        it.setValue(defaultLang, "defaultLanguage", "C++")
                    } else {
                        it.applyStereotype(profile.getMember("ProtocolContainer") as Stereotype)
                    }
                }


                is Operation -> (it.applyStereotype(profile.getMember("Trigger") as Stereotype))

                is Connector -> (it.applyStereotype(profile.getMember("RTConnector") as Stereotype))

                is Property -> (it.applyStereotype(profile.getMember("CapsulePart") as Stereotype))

                is BehaviorExecutionSpecification -> (it.applyStereotype(profile.getMember("Coregion") as Stereotype))

                is Pseudostate -> (it.applyStereotype(profile.getMember("RTHistoryState") as Stereotype))

                is RedefinableElement -> (it.applyStereotype(profile.getMember("RTRedefinableElement") as Stereotype))

                is Message -> (it.applyStereotype(profile.getMember("RTMessage") as Stereotype))


            }
        }
    }

    override fun visitModel(model: RTModel): Package {
        val umlModel = UMLFactory.eINSTANCE.createPackage()



        umlModel.name = model.name


        // todo: figure out how to declare top capsule
//        val topAnnotation = EcoreFactory.eINSTANCE.createEAnnotation()
//        topAnnotation.source = "UMLRT_Default_top"
//        topAnnotation.details.put("top_name", model.top.capsule.name)
//        umlModel.eAnnotations.add(topAnnotation)

//        val langAnnotation = EcoreFactory.eINSTANCE.createEAnnotation()
//        langAnnotation.source = "http://www.eclipse.org/papyrus-rt/language/1.0.0"
//        langAnnotation.details.put("language", "umlrt-cpp")
//        umlModel.eAnnotations.add(langAnnotation)

        umlModel.applyProfile(RSARTELibrary.getProfile("Standard"))
        umlModel.applyProfile(RSARTELibrary.getProfile("Default"))
        umlModel.applyProfile(RSARTELibrary.getProfile("Deployment"))
        umlModel.applyProfile(RSARTELibrary.getProfile("CppPropertySets"))
        umlModel.applyProfile(RSARTELibrary.getProfile("UMLRealTime"))
        umlModel.applyProfile(RSARTELibrary.getProfile("InteractionProfile"))


        // todo: add imported packages to RSRATE Models:
        //  CppPrimitiveDatatypes
        //  RTClasses
        //  RTComponents
//        umlModel.importedPackages.add()


//        umlModel.applyProfile(RSARTELibrary.getProfile("Ecore"))
//        umlModel.applyProfile(RSARTELibrary.getProfile("UML"))
//        umlModel.applyProfile(RSARTELibrary.getProfile("PropertySets"))
//        umlModel.applyProfile(RSARTELibrary.getProfile("ProfileBase"))
//        umlModel.applyProfile(RSARTELibrary.getProfile("SystemElements"))


        this.model = umlModel

        model.capsules.forEach { umlModel.packagedElements.add(visit(it) as Class) }

        model.protocols.forEach { umlModel.packagedElements.add(visit(it) as Package) }


        //eAnnotations
        //packageImport

        /*
        todo: finish code below

        model.classes.forEach { umlModel.packagedElements.add(visit(it) as Class) }

        model.enumerations.forEach { umlModel.packagedElements.add(visit(it) as Enumeration) }

        model.artifacts.forEach { umlModel.packagedElements.add(visit(it) as Artifact) }

        model.packages.forEach { umlModel.packagedElements.add(visit(it) as Package) }

        */

        toApplyStereotypes.add(umlModel)

        return umlModel
    }



    override fun visitCapsule(capsule: RTCapsule): Any {

        // todo: find out how to reference model which owns the capsule
        //  in order to create the capsule using the original factory

//        val capsuleClass = UMLFactory.eINSTANCE.createClass()
        val capsuleClass = UMLFactory.eINSTANCE.createClass()



        capsuleClass.name = capsule.name
//        val cls : Class;
//        val domain = TransactionUtil.getEditingDomain(cls) // In case a write transaction is necessary


//        var topPkg: Package? = null
//        var e: EObject? = cls
//        while (e != null) {
//            if (e is Package) topPkg = e
//            e = e.eContainer()
//        }
//        model.packagedElements.add(capsuleClass)



//        val profile = topPkg!!.getAppliedProfile("UMLRealTime")

//        val allApplied = profile.allAppliedProfiles




//        UMLRealTime


        //eAnnotations
        //ownedAttribute
        //ownedConnector
        //ownedBehavior
        //Interaction

        val attributes = capsule.attributes

        capsule.attributes.forEach { capsuleClass.ownedAttributes.add(visit(it) as Property) }

        capsule.parts.forEach {
            val part = visit(it) as Property
            capsuleClass.ownedAttributes.add(part)
            toApplyStereotypes.add(part)
        }

        // todo: add to toApplyStereotypes list for each capsule part

        capsule.ports.forEach { capsuleClass.ownedAttributes.add(visit(it) as Port) }
        capsule.connectors.forEach { capsuleClass.ownedConnectors.add(visit(it) as Connector)}
        // "let" handles possible null object
        capsule.stateMachine?.let {capsuleClass.ownedBehaviors.add(visit(it) as StateMachine) }

//        todo: finish code below
        capsuleClass.ownedOperations.forEach { capsuleClass.ownedBehaviors.add(it.methods[0]) }
        capsule.attributes.forEach { capsuleClass.ownedAttributes.add(visit(it) as Property) }
        capsule.operations.forEach { capsuleClass.ownedOperations.add(visit(it) as Operation) }



//        if (capsule.properties != null) {
//            val props = visit(capsule.properties!!) as CapsuleProperties
//            props.base_Class = umlClass
//            resource.contents.add(props)
//        }

//        val umlrtCapsule = RTCapsule.(model, capsule.name)
//        umlrtCapsule.base_Class = capsuleClass
//        resource.contents.add(umlrtCapsule)

        toApplyStereotypes.add(capsuleClass)

        return capsuleClass

    }

    override fun visitAttribute(attribute: RTAttribute): Property {
        val umlProperty = UMLFactory.eINSTANCE.createProperty()
        umlProperty.name = attribute.name
        umlProperty.type = visit(attribute.type) as Type
        umlProperty.upper = attribute.replication
        umlProperty.lower = attribute.replication
        umlProperty.visibility = VisibilityKind.get(attribute.visibility.ordinal)

//        if (attribute.properties != null) {
//            val props = visit(attribute.properties!!) as AttributeProperties
//            props.base_Property = umlProperty
//            resource.contents.add(props)
//        }

        return umlProperty
    }

    override fun visitProtocol(protocol: RTProtocol): Any {
//        if (protocol is RTSystemProtocol)
//            return RSARTELibrary.getProtocol(protocol).base_Package

        val umlPackage = UMLFactory.eINSTANCE.createPackage()
        umlPackage.name = protocol.name

        // PROTOCOL
        // base object: Collaboration (UML)
        // stereotype: Protocol (UMLRT)

        // MESSAGE
        // base object: CallEvent (UML)
        // stereotypes:
        //      umlProfile.getMember("InEvent") (UMLRT)
        //      umlProfile.getMember("OutEvent") (UMLRT)

        val umlProfile = RSARTELibrary.getProfile("UMLRealTime")

        val umlCollaboration = UMLFactory.eINSTANCE.createCollaboration()
        umlCollaboration.name = protocol.name
        umlPackage.packagedElements.add(umlCollaboration)

        // Protocol Stereotype
//        applyStereotype(umlCollaboration, ProtocolStereotype)
        toApplyStereotypes.add(umlCollaboration)
        // Protocol Container Stereotype
        toApplyStereotypes.add(umlPackage)
//        applyStereotype(umlPackage, ProtocolContainerStereotype)

        val i1 = UMLFactory.eINSTANCE.createInterface()
        i1.name = protocol.name
        umlPackage.packagedElements.add(i1)

        protocol.inputSignals.forEach { i1.ownedOperations.add((visit(it) as CallEvent).operation) }

        val i2 = UMLFactory.eINSTANCE.createInterface()
        i2.name = protocol.name + "~"
        umlPackage.packagedElements.add(i2)

        protocol.outputSignals.forEach { i2.ownedOperations.add((visit(it) as CallEvent).operation) }

        val ir1 = UMLFactory.eINSTANCE.createInterfaceRealization()
        ir1.clients.add(umlCollaboration)
        ir1.suppliers.add(i1)
        ir1.contract = i1
        umlCollaboration.interfaceRealizations.add(ir1)

//        val ir2 = UMLFactory.eINSTANCE.createInterfaceRealization()
//        ir2.clients.add(umlCollaboration)
//        umlCollaboration.interfaceRealizations.add(ir2)

        val u1 = UMLFactory.eINSTANCE.createUsage()
        u1.clients.add(umlCollaboration)
        u1.suppliers.add(i2)
        umlPackage.packagedElements.add(u1)

        protocol.inputSignals.forEach { umlPackage.packagedElements.add(visit(it) as MessageEvent) }
        protocol.outputSignals.forEach { umlPackage.packagedElements.add(visit(it) as MessageEvent) }
        protocol.inOutSignals.filterNot { it is RTSystemSignal }
            .forEach { umlPackage.packagedElements.add(visit(it) as MessageEvent) }
        umlPackage.packagedElements.add(visit(protocol.anySignal) as MessageEvent)

//        val rtp = UMLRealTimeFactory.eINSTANCE.createProtocol()
//        rtp.base_Collaboration = umlCollaboration
//        resource.contents.add(rtp)
//
//        val rtm1 = UMLRealTimeFactory.eINSTANCE.createRTMessageSet()
//        rtm1.rtMsgKind = RTMessageKind.IN
//        rtm1.base_Interface = i1
//        resource.contents.add(rtm1)
//
//        val rtm2 = UMLRealTimeFactory.eINSTANCE.createRTMessageSet()
//        rtm2.rtMsgKind = RTMessageKind.OUT
//        rtm2.base_Interface = i2
//        resource.contents.add(rtm2)
//
//        val rtm3 = UMLRealTimeFactory.eINSTANCE.createRTMessageSet()
//        rtm3.rtMsgKind = RTMessageKind.IN_OUT
//        rtm3.base_Interface = i3
//        resource.contents.add(rtm3)
//
//        val umlrtProtocolContainer = UMLRealTimeFactory.eINSTANCE.createProtocolContainer()
//        umlrtProtocolContainer.base_Package = umlPackage
//        resource.contents.add(umlrtProtocolContainer)

        return umlPackage

    }

    override fun visitSignal(signal: RTSignal): MessageEvent {
        if (signal is RTSystemSignal)
            return RSARTELibrary.getSystemSignal(signal)

        if (signal.isAny) {
            val c = UMLFactory.eINSTANCE.createAnyReceiveEvent()
            c.name = signal.name
            return c
        }

        val umlCallEvent = UMLFactory.eINSTANCE.createCallEvent()
        umlCallEvent.name = signal.name

        umlCallEvent.operation = UMLFactory.eINSTANCE.createOperation()
        umlCallEvent.operation.name = signal.name
        signal.parameters.forEach { umlCallEvent.operation.ownedParameters.add(visit(it) as Parameter) }

        toApplyStereotypes.add(umlCallEvent)

        return umlCallEvent
    }

    override fun visitParameter(param: RTParameter): Parameter {
        val umlParameter = UMLFactory.eINSTANCE.createParameter()
        umlParameter.name = param.name
        umlParameter.type = visit(param.type) as Type

//        umlParameter.lower = param.replication
//        umlParameter.upper = param.replication

        umlParameter.direction = ParameterDirectionKind.IN_LITERAL

//        if (param.properties != null) {
//            val props = visit(param.properties!!) as ParameterProperties
//            props.base_Parameter = umlParameter
//            resource.contents.add(props)
//        }

        return umlParameter
    }

    override fun visitType(type: RTType): Type {
        return when (type) {
            is RTPrimitiveType -> RSARTELibrary.getType(type)
            is RTEnumeration -> visit(type) as Type
            is RTCapsule -> visit(type) as Type
            is RTClass -> visit(type) as Type
            else -> throw RuntimeException("Unexpected type class ${type.javaClass.simpleName}")
        }
    }

    override fun visitPart(part: RTCapsulePart): Any {
        val umlProperty = UMLFactory.eINSTANCE.createProperty()
        umlProperty.name = part.name
        umlProperty.upper = part.replication
        umlProperty.type = visit(part.capsule) as Class

        umlProperty.aggregation = if (part.plugin) AggregationKind.SHARED_LITERAL else AggregationKind.COMPOSITE_LITERAL
        umlProperty.lower = if (part.optional) 0 else part.replication




//        val umlrtCapsulePart = UMLRealTimeFactory.eINSTANCE.createCapsulePart()
//        umlrtCapsulePart.base_Property = umlProperty
//        resource.contents.add(umlrtCapsulePart)

        return umlProperty
    }

    override fun visitPort(port: RTPort): Any {
        val umlPort = UMLFactory.eINSTANCE.createPort()
        umlPort.name = port.name
        umlPort.setIsBehavior(port.behaviour)
//        umlPort.setIsConjugated(port.conjugated)
//        PortOperations.setIsConjugated(umlPort, true)

//        val rtPort = RTFactory.RTRedefFactory.redefFactory.getPortRedefinition()
//        resource.contents.add(rtPort)

//        if (port.conjugated) {
//            PortOperations.setIsConjugated(umlPort, true)
//        }

        // todo: apply isConjugate stereotype to appropriate ports
//        applyStereotype(umlPort, "isConjugate=\"true\"")
        umlPort.setIsService(port.service)
        umlPort.upper = port.replication
        umlPort.lower = port.replication
        umlPort.visibility = VisibilityKind.get(port.visibility.ordinal)
//        umlPort.type = (visit(port.protocol) as Package).packagedElements[0] as Type

//        val umlrtPort = UMLRealTimeFactory.eINSTANCE.createRTPort()
//        umlrtPort.base_Port = umlPort
//        umlrtPort.setIsPublish(port.publish)
//        umlrtPort.setIsWired(port.wired)
//        umlrtPort.setIsNotification(port.notification)
//        umlrtPort.registration = PortRegistrationType.get(port.registrationType.ordinal)
//        umlrtPort.registrationOverride = port.registrationOverride
//        resource.contents.add(umlrtPort)
        toApplyStereotypes.add(umlPort)

        return umlPort
    }

    override fun visitConnector(connector: RTConnector): Any {
        val umlConnector = UMLFactory.eINSTANCE.createConnector()
        umlConnector.name = connector.name

        val umlEnd1 = UMLFactory.eINSTANCE.createConnectorEnd()
        umlEnd1.role = visit(connector.end1.port) as Port
        umlEnd1.partWithPort =
            if (connector.end1.part != null) visit(connector.end1.part!!) as Property
            else null

        val umlEnd2 = UMLFactory.eINSTANCE.createConnectorEnd()
        umlEnd2.role = visit(connector.end2.port) as Port
        umlEnd2.partWithPort =
            if (connector.end2.part != null) visit(connector.end2.part!!) as Property
            else null

        umlConnector.ends.add(umlEnd1)
        umlConnector.ends.add(umlEnd2)

        toApplyStereotypes.add(umlConnector)

//        val umlrtConnector = UMLRealTimeFactory.eINSTANCE.createRTConnector()
//        umlrtConnector.base_Connector = umlConnector
//        resource.contents.add(umlrtConnector)
        return umlConnector
    }

    override fun visitStateMachine(statemachine: RTStateMachine): Any {
        val umlStateMachine = UMLFactory.eINSTANCE.createStateMachine()
        umlStateMachine.name = statemachine.name

        val umlRegion = UMLFactory.eINSTANCE.createRegion()
        umlRegion.name = "Region1"
        statemachine.states().forEach { umlRegion.subvertices.add(visit(it) as Vertex) }
        statemachine.transitions().forEach { umlRegion.transitions.add(visit(it) as Transition) }
        umlStateMachine.regions.add(umlRegion)

        return umlStateMachine
    }

    override fun visitState(state: RTState): State {
        val umlState = UMLFactory.eINSTANCE.createState()
        umlState.name = state.name

        umlState.entry = if (state.entryAction != null) visit(state.entryAction!!) as OpaqueBehavior else null
        umlState.exit = if (state.exitAction != null) visit(state.exitAction!!) as OpaqueBehavior else null

//        resource.contents.add(umlState)

        return umlState
    }

    override fun visitPseudoState(state: RTPseudoState): Pseudostate {
        val umlState = UMLFactory.eINSTANCE.createPseudostate()
        umlState.name = null

        umlState.kind = when (state.kind) {
            RTPseudoState.Kind.INITIAL -> PseudostateKind.INITIAL_LITERAL
            RTPseudoState.Kind.HISTORY -> PseudostateKind.SHALLOW_HISTORY_LITERAL
            RTPseudoState.Kind.JOIN -> PseudostateKind.JOIN_LITERAL
            RTPseudoState.Kind.JUNCTION -> PseudostateKind.JUNCTION_LITERAL
            RTPseudoState.Kind.CHOICE -> PseudostateKind.CHOICE_LITERAL
            RTPseudoState.Kind.ENTRY_POINT -> PseudostateKind.ENTRY_POINT_LITERAL
            RTPseudoState.Kind.EXIT_POINT -> PseudostateKind.EXIT_POINT_LITERAL
            else -> throw RuntimeException("Unknown pseudostate kind ${state.kind}")
        }

        return umlState
    }

    override fun visitTransition(transition: RTTransition): Transition {
        val umlTransition = UMLFactory.eINSTANCE.createTransition()
        umlTransition.name = transition.name

        umlTransition.source = visit(transition.source) as Vertex
        umlTransition.target = visit(transition.target) as Vertex
        umlTransition.effect = if (transition.action != null) visit(transition.action!!) as OpaqueBehavior else null

        if (transition.guard != null) {
            val guard = transition.guard!!
            val umlExpression = UMLFactory.eINSTANCE.createOpaqueExpression()
            umlExpression.name = guard.name
            umlExpression.languages.add(guard.language)
            umlExpression.bodies.add(guard.body)

            val umlConstraint = UMLFactory.eINSTANCE.createConstraint()
            umlConstraint.specification = umlExpression
            umlTransition.guard = umlConstraint
        }

        transition.triggers.forEach { umlTransition.triggers.add(visit(it) as Trigger) }

        return umlTransition
    }

    override fun visitTrigger(trigger: RTTrigger): Trigger {
        val umlTrigger = UMLFactory.eINSTANCE.createTrigger()
        umlTrigger.event = visit(trigger.signal) as MessageEvent
        trigger.ports.forEach { umlTrigger.ports.add(visit(it) as Port) }
        return umlTrigger
    }

    override fun visitAction(action: RTAction): OpaqueBehavior {
        val umlOpaqueBehaviour = UMLFactory.eINSTANCE.createOpaqueBehavior()
        umlOpaqueBehaviour.name = action.name
        umlOpaqueBehaviour.bodies.add(action.body)
        umlOpaqueBehaviour.languages.add(action.language)
        return umlOpaqueBehaviour
    }

//    private fun setCapsuleRTProperties(newCapsule: com.ibm.xtools.uml.rt.core.RTCapsule) {
//        val pm = PropertyManager(newCapsule.getUML2Class())
//        val typeNames: Array<String> = pm.getTypeNamesForElement(pm.getActiveLanguage())
//        for (typeName in typeNames) {
//            if (typeName == "Capsule") {
//                val groupNames: Array<String> = pm.getGroupNames(pm.getActiveLanguage(), typeName)
//                for (groupName in groupNames) {
//                    if (groupName == "General") {
//                        val props: List<Property> = pm.getProperties(pm.getActiveLanguage(), typeName, groupName)
//                        for (property in props) {
//                            if (property.name.equals("headerPreface")) {
//                                pm.changeValue(
//                                    pm.getActiveLanguage(),
//                                    typeName,
//                                    groupName,
//                                    "headerPreface",
//                                    "Copyright: IBM"
//                                )
//                            }
//                            if (property.name.equals("implementationPreface")) {
//                                pm.changeValue(
//                                    pm.getActiveLanguage(),
//                                    typeName,
//                                    groupName,
//                                    "implementationPreface",
//                                    "Copyright: IBM"
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }


}


