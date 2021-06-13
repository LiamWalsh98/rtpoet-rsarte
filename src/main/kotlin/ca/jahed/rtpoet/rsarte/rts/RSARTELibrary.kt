package ca.jahed.rtpoet.rsarte.rts

import ca.jahed.rtpoet.rtmodel.rts.RTLibrary
import ca.jahed.rtpoet.rtmodel.rts.RTSystemSignal
import ca.jahed.rtpoet.rtmodel.rts.classes.RTSystemClass
import ca.jahed.rtpoet.rtmodel.rts.protocols.RTSystemProtocol
import ca.jahed.rtpoet.rtmodel.types.RTType
import ca.jahed.rtpoet.rtmodel.types.primitivetype.RTPrimitiveType
import com.ibm.xtools.uml.msl.internal.util.UML2Constants.URI_DEFAULT_PROFILE
import com.ibm.xtools.uml.rt.core.internal.l10n.ResourceManager.UMLRealTime
import com.ibm.xtools.umlnotation.UmlnotationPackage
import com.ibm.xtools.umldt.rt.cpp.core.*
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.eclipse.uml2.uml.*
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl
import org.eclipse.uml2.uml.resources.*
import org.eclipse.uml2.uml.util.UMLUtil
import java.net.URL


object RSARTELibrary : RTLibrary {
    private val pathMap = mutableMapOf<String, URL>()
    private val profiles = mutableMapOf<String, Profile>()
//    private val protocols = mutableMapOf<String, ProtocolContainer>()
//    private val classes = mutableMapOf<String, Class>()
//    private val types = mutableMapOf<String, PrimitiveType>()
//    private val events = mutableMapOf<ProtocolContainer, MutableMap<String, MessageEvent>>()
//    private val signals = mutableMapOf<RTSystemSignal, MessageEvent>()

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









        resourceSet.resourceFactoryRegistry.extensionToFactoryMap["emx"] = UMLResourceFactoryImpl()
        resourceSet.resourceFactoryRegistry.extensionToFactoryMap["epx"] = UMLResourceFactoryImpl()
        resourceSet.resourceFactoryRegistry.extensionToFactoryMap["uml"] = UMLResourceFactoryImpl()

//        resourceSet.resourceFactoryRegistry.contentTypeToFactoryMap["org.eclipse.uml2.uml_5_0_0"] = UMLResourceFactoryImpl()



//        resourceSet.packageRegistry[UMLRealTime.eNSL_URI] = UMLRealTime.eINSTANCE
        resourceSet.packageRegistry[UMLPackage.eNS_URI] = UMLPackage.eINSTANCE
        resourceSet.packageRegistry["http://www.eclipse.org/uml2/2.1.0/UML"] = UMLPackage.eINSTANCE

//        resourceSet.packageRegistry["platform:/plugin/com.ibm.xtools.umldt.rt.cpp.core/libraries/RTClasses.emx"] = UMLPackage.eINSTANCE

        resourceSet.packageRegistry[UmlnotationPackage.eNS_URI] = UmlnotationPackage.eINSTANCE

        pathMap.forEach {
            resourceSet.uriConverter.uriMap[URI.createURI(it.key)] = URI.createURI(it.value.toString())
        }

        pathMap.keys.forEach { resourceSet.getResource(URI.createURI(it), true) }

        loadProfiles(resourceSet)
//        loadClasses(resourceSet)
//        loadTypes(resourceSet)
//        loadProtocols(resourceSet)
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
        TODO("Not yet implemented")
    }

    override fun getSystemSignal(event: RTSystemSignal): Any {
        TODO("Not yet implemented")
    }

    override fun getSystemSignal(protocol: RTSystemProtocol, signal: RTSystemSignal): Any {
        TODO("Not yet implemented")
    }

    override fun getSystemSignal(signal: Any): RTSystemSignal {
        TODO("Not yet implemented")
    }

    override fun getSystemSignal(protocol: String, name: String): RTSystemSignal {
        TODO("Not yet implemented")
    }

    override fun getType(type: RTType): Any {
        TODO("Not yet implemented")
    }

    override fun getType(name: String): RTPrimitiveType {
        TODO("Not yet implemented")
    }

    override fun isSystemClass(klass: Any): Boolean {
        TODO("Not yet implemented")
    }

    override fun isSystemProtocol(protocol: Any): Boolean {
        TODO("Not yet implemented")
    }

    override fun isSystemSignal(event: Any): Boolean {
        TODO("Not yet implemented")
    }

    fun isModelRoot(pk: Package) : Boolean {
        return pk.eContainer() == null
    }
}