package ca.jahed.rtpoet.rsarte.rts

import com.ibm.xtools.uml.rt.core.internal.l10n.ResourceManager.UMLRealTime
import com.ibm.xtools.umlnotation.UmlnotationPackage
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.eclipse.uml2.uml.UMLPackage
import java.net.URL


object RSARTELibrary {
    private val pathMap = mutableMapOf<String, URL>()

    fun createResourceSet(): ResourceSetImpl {
        val resourceSet = ResourceSetImpl()
        init(resourceSet)
        return resourceSet
    }

    private fun init(resourceSet: ResourceSet) {

        pathMap["pathmap://UML2_MSL_PROFILES/Default.epx"] =
            javaClass.classLoader.getResource("profiles/Default.epx")!!
        pathMap["pathmap://RT_PROPERTIES/UMLRealTime.epx"] =
            javaClass.classLoader.getResource("profiles/UMLRealTime.epx")!!
        pathMap["pathmap://UML2_MSL_PROFILES/ProfileBase.epx"] =
            javaClass.classLoader.getResource("profiles/ProfileBase.epx")!!


        resourceSet.resourceFactoryRegistry.extensionToFactoryMap["emx"] = XMIResourceFactoryImpl()
        resourceSet.resourceFactoryRegistry.extensionToFactoryMap["epx"] = XMIResourceFactoryImpl()

//        resourceSet.packageRegistry[UMLRealTime.eNSL_URI] = UMLRealTime.eINSTANCE
        resourceSet.packageRegistry[UMLPackage.eNS_URI] = UMLPackage.eINSTANCE
        resourceSet.packageRegistry[UmlnotationPackage.eNS_URI] = UmlnotationPackage.eINSTANCE

        pathMap.forEach {
            resourceSet.uriConverter.uriMap[URI.createURI(it.key)] = URI.createURI(it.value.toString())
        }

        pathMap.keys.forEach { resourceSet.getResource(URI.createURI(it), true) }
    }
}