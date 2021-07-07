package ca.jahed.rtpoet.rsarte.rts

import ca.jahed.rtpoet.rsarte.rts.primitivetype.*
import ca.jahed.rtpoet.rsarte.UMLRTProfile
import ca.jahed.rtpoet.rsarte.rts.protocols.RTExceptionProtocol
import ca.jahed.rtpoet.rsarte.rts.protocols.RTExternalProtocol
import ca.jahed.rtpoet.rtmodel.rts.RTLibrary
import ca.jahed.rtpoet.rtmodel.rts.RTSystemSignal
import ca.jahed.rtpoet.rtmodel.rts.classes.RTSystemClass
import ca.jahed.rtpoet.rtmodel.rts.protocols.RTFrameProtocol
import ca.jahed.rtpoet.rtmodel.rts.protocols.RTLogProtocol
import ca.jahed.rtpoet.rtmodel.rts.protocols.RTSystemProtocol
import ca.jahed.rtpoet.rtmodel.rts.protocols.RTTimingProtocol
import ca.jahed.rtpoet.rtmodel.types.RTType
import ca.jahed.rtpoet.rtmodel.types.primitivetype.*
import com.ibm.xtools.uml.msl.internal.util.UML2Constants.URI_DEFAULT_PROFILE
import com.ibm.xtools.umlnotation.UmlnotationPackage
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.uml2.uml.*
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl
import java.net.URL


object RSARTELibrary : RTLibrary {
    private val pathMap = mutableMapOf<String, URL>()
    private val profiles = mutableMapOf<String, Profile>()
    private val models = mutableMapOf<String, Model>()
    private val protocols = mutableMapOf<String, Package>()
    private val classes = mutableMapOf<String, Class>()
    private val types = mutableMapOf<String, PrimitiveType>()
    private val events = mutableMapOf<Package, MutableMap<String, MessageEvent>>()
    private val signals = mutableMapOf<RTSystemSignal, MessageEvent>()

    fun createResourceSet(): ResourceSetImpl {
        val resourceSet = ResourceSetImpl()
        init(resourceSet)
        return resourceSet
    }

    private fun init(resourceSet: ResourceSet) {



        pathMap["pathmap://UML_PROFILES/Standard.profile.uml"] =
            javaClass.classLoader.getResource("profiles/Standard.profile.uml")!!

        pathMap["pathmap://UML2_MSL_PROFILES/Default.epx"] =
            javaClass.classLoader.getResource(URI_DEFAULT_PROFILE)!!

        pathMap["pathmap://UML2_MSL_PROFILES/Deployment.epx"] =
            javaClass.classLoader.getResource("profiles/Deployment.epx")!!

        pathMap["pathmap://CPP_LANGUAGE_PROFILE/CppPropertySets.epx"] =
            javaClass.classLoader.getResource("profiles/CppPropertySets.epx")!!

        pathMap["pathmap://RT_PROPERTIES/UMLRealTime.epx"] =
            javaClass.classLoader.getResource("profiles/UMLRealTime.epx")!!

        pathMap["pathmap://RT_PROPERTIES/InteractionProfile.epx"] =
            javaClass.classLoader.getResource("profiles/InteractionProfile.epx")!!


        pathMap["pathmap://UML_PROFILES/Ecore.profile.uml"] =
            javaClass.classLoader.getResource("profiles/Ecore.profile.uml")!!

        pathMap["pathmap://UML_METAMODELS/UML.metamodel.uml"] =
            javaClass.classLoader.getResource("metamodels/UML.metamodel.uml")!!

        pathMap["pathmap://PROPERTY_SETS/PropertySets.epx"] =
            javaClass.classLoader.getResource("propertySetProfile/PropertySets.epx")!!

        pathMap["pathmap://RT_PROPERTIES/SystemElements.epx"] =
            javaClass.classLoader.getResource("profiles/SystemElements.epx")!!

        pathMap["pathmap://UML2_MSL_PROFILES/ProfileBase.epx"] =
            javaClass.classLoader.getResource("profiles/ProfileBase.epx")!!

        pathMap["platform:/plugin/com.ibm.xtools.umldt.rt.cpp.core/libraries/RTClasses.emx"] =
            javaClass.classLoader.getResource("libraries/RTClasses.emx")!!

        pathMap["platform:/plugin/com.ibm.xtools.umldt.rt.cpp.core/libraries/CppPrimitiveDatatypes.emx"] =
            javaClass.classLoader.getResource("libraries/CppPrimitiveDatatypes.emx")!!

        pathMap["platform:/plugin/com.ibm.xtools.umldt.rt.cpp.core/libraries/RTComponents.emx"] =
            javaClass.classLoader.getResource("libraries/RTComponents.emx")!!






        resourceSet.resourceFactoryRegistry.extensionToFactoryMap["emx"] = UMLResourceFactoryImpl()
        resourceSet.resourceFactoryRegistry.extensionToFactoryMap["epx"] = UMLResourceFactoryImpl()
        resourceSet.resourceFactoryRegistry.extensionToFactoryMap["uml"] = UMLResourceFactoryImpl()

//        resourceSet.resourceFactoryRegistry.contentTypeToFactoryMap["org.eclipse.uml2.uml_5_0_0"] = UMLResourceFactoryImpl()



//        resourceSet.packageRegistry[UMLRealTime.eNSL_URI] = UMLRealTime.eINSTANCE
        resourceSet.packageRegistry[UMLPackage.eNS_URI] = UMLPackage.eINSTANCE
        resourceSet.packageRegistry["http://www.eclipse.org/uml2/2.1.0/UML"] = UMLPackage.eINSTANCE

//        resourceSet.packageRegistry["platform:/plugin/com.ibm.xtools.umldt.rt.cpp.core/libraries/RTClasses.emx"] = UMLPackage.eINSTANCE
//        resourceSet.packageRegistry["platform:/plugin/com.ibm.xtools.umldt.rt.cpp.core/libraries/CppPrimitiveDatatypes.emx"] = UMLPackage.eINSTANCE
//        resourceSet.packageRegistry["platform:/plugin/com.ibm.xtools.umldt.rt.cpp.core/libraries/RTComponents.emx"] = UMLPackage.eINSTANCE


        resourceSet.packageRegistry[UmlnotationPackage.eNS_URI] = UmlnotationPackage.eINSTANCE

        pathMap.forEach {
            resourceSet.uriConverter.uriMap[URI.createURI(it.key)] = URI.createURI(it.value.toString())
        }

        pathMap.keys.forEach { resourceSet.getResource(URI.createURI(it), true) }

        loadProfiles(resourceSet)
        loadClasses(resourceSet)
        loadTypes(resourceSet)
        loadProtocols(resourceSet)
        // todo: implement the above functions

        EcoreUtil.resolveAll(resourceSet)

    }

    private fun loadProfiles(resourceSet: ResourceSet) {
        resourceSet.resources.forEach { resource ->
            EcoreUtil.getObjectsByType<Profile>(resource.contents,
                UMLPackage.Literals.PROFILE).forEach {
                profiles[it.name] = it
            }
        }
    }

    private fun loadTypes(resourceSet: ResourceSet) {
        resourceSet.resources.forEach { resource ->
            EcoreUtil.getObjectsByType<Model>(resource.contents,
                UMLPackage.Literals.MODEL).forEach { model ->
                EcoreUtil.getObjectsByType<PrimitiveType>(model.packagedElements,
                    UMLPackage.Literals.PRIMITIVE_TYPE).forEach {
                    types[it.name] = it
                }
            }
        }
    }

    private fun loadClasses(resourceSet: ResourceSet) {
        resourceSet.resources.forEach { resource ->
            EcoreUtil.getObjectsByType<Model>(resource.contents,
                UMLPackage.Literals.MODEL).forEach { model ->
                EcoreUtil.getObjectsByType<Class>(model.packagedElements,
                    UMLPackage.Literals.CLASS).forEach {
                    classes[it.name] = it
                }
            }
        }
    }

    private fun loadProtocols(resourceSet: ResourceSet) {
        resourceSet.resources.forEach { resource ->
            EcoreUtil.getObjectsByType<Package>(resource.contents,
                UMLPackage.Literals.PACKAGE).forEach { element ->
                    element.packagedElements.filterIsInstance<Package>().forEach { protocol ->
                        // todo: differentiate packages from standard.uml and RTClasses.emx
                        if (UMLRTProfile.isProtocolContainer(protocol)) {

//                            RSARTELibrary.protocols[protocol.name] = protocol
                            (protocol.packagedElements.filterIsInstance<Collaboration>()[0])
                                .`package`.packagedElements.filterIsInstance<MessageEvent>().forEach { event ->
                                    when (event) {
                                        is CallEvent -> RSARTELibrary.events.getOrPut(protocol)
                                        { mutableMapOf() }[event.operation.name] = event
                                        is AnyReceiveEvent -> RSARTELibrary.events.getOrPut(protocol)
                                        { mutableMapOf() }[event.name] = event
                                    }
                                }

                            RSARTELibrary.events.forEach { (protocol, eventMap) ->
                                eventMap.forEach { (eventName, event) ->
                                    RSARTELibrary.signals[RSARTELibrary.getSystemSignal(
                                        protocol.name,
                                        eventName
                                    )] = event
                                }
                            }
                        }
                    }

            }

        }
    }


    override fun getProfile(name: String): Profile {
        return profiles[name]!!
    }

    override fun getProtocol(protocol: RTSystemProtocol): Any {
        TODO("Not yet implemented")
    }

    override fun getSystemClass(klass: RTSystemClass): Any {
        TODO("Not yet implemented")
    }

    override fun getSystemClass(klass: Any): RTSystemClass {
        TODO("Not yet implemented")
    }

    override fun getSystemClass(name: String): RTSystemClass {
        TODO("Not yet implemented")
    }

    override fun getSystemProtocol(protocol: Any): RTSystemProtocol {
        TODO("Not yet implemented")
    }

    override fun getSystemProtocol(name: String): RTSystemProtocol {
        return when (name) {
            "Log" -> RTLogProtocol
            "Timing" -> RTTimingProtocol
            "Frame" -> RTFrameProtocol
            "Exception" -> RTExceptionProtocol
            "External" -> RTExternalProtocol
            else -> throw RuntimeException("Unknown system protocol $name")
        }
    }

    override fun getSystemSignal(event: RTSystemSignal): MessageEvent {
        return RSARTELibrary.signals[event]!!
    }

    override fun getSystemSignal(protocol: RTSystemProtocol, signal: RTSystemSignal): MessageEvent {
        return (RSARTELibrary.events[RSARTELibrary.getProtocol(protocol)]!![signal.name])!!

    }

    override fun getSystemSignal(signal: Any): RTSystemSignal {
        signal as MessageEvent
        val eventName = if (signal is CallEvent) signal.operation.name else signal.name

        for (protocol in events.keys)
            if (events[protocol]!!.containsValue(signal))
                return getSystemSignal(protocol.name, eventName)
        throw java.lang.RuntimeException("Unknown system signal $eventName")
    }

    override fun getSystemSignal(protocol: String, name: String): RTSystemSignal {
        return RSARTELibrary.getSystemProtocol(protocol).inputs().find { (it.name == name || it.name == "*")  } as RTSystemSignal
    }

    override fun getType(type: RTType): PrimitiveType {
        return types[type.name]!!
    }

    override fun getType(name: String): RTPrimitiveType {
        return when (name) {
            "Boolean" -> RTBoolean
            "Integer" -> RTInteger
            "String" -> RTString
            "Real" -> RTReal
            "UnlimitedNatural" -> RTUnlimitedNatural

            "char" -> RTChar
            "double" -> RTDouble
            "float" -> RTFloat
            "bool" -> RTBool
            "int" -> RTInt
            "int8_t" -> RTInt8
            "int16_t" -> RTInt16
            "int32_t" -> RTInt32
            "int64_t" -> RTInt64
            "long" -> RTLong
            "long double" -> RTLongDouble
            "short" -> RTShort
            "unsigned char" -> RTUnsignedChar
            "unsigned int" -> RTUnsignedInt
            "uint8_t" -> RTUnsignedInt8
            "uint16_t" -> RTUnsignedInt16
            "uint32_t" -> RTUnsignedInt32
            "uint64_t" -> RTUnsignedInt64
            "unsigned long" -> RTUnsignedLong
            "unsigned short" -> RTUnsignedShort
            "wchar_t" -> RTWChar
            "void" -> RTVoid
            else -> throw RuntimeException("Unknown system type $name")
        }
    }

    override fun isSystemClass(klass: Any): Boolean {
        TODO("Not yet implemented")
    }

    override fun isSystemProtocol(protocol: Any): Boolean {
        TODO("Not yet implemented")
    }

    override fun isSystemSignal(event: Any): Boolean {
        for (protocol in events.keys)
            if (events[protocol]!!.containsValue(event))
                return true
        return false
    }

    fun isModelRoot(pk: Package) : Boolean {
        return pk.eContainer() == null
    }
}