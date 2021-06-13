package ca.jahed.rtpoet.rsarte.utils

import ca.jahed.rtpoet.rsarte.EMFUtils
import ca.jahed.rtpoet.rsarte.rts.protocols.RTExceptionProtocol
import ca.jahed.rtpoet.rsarte.rts.protocols.RTExternalProtocol
import ca.jahed.rtpoet.rtmodel.rts.protocols.RTFrameProtocol
import ca.jahed.rtpoet.rtmodel.rts.protocols.RTLogProtocol
import ca.jahed.rtpoet.rtmodel.rts.protocols.RTSystemProtocol
import ca.jahed.rtpoet.rtmodel.rts.protocols.RTTimingProtocol
import com.ibm.xtools.uml.rt.core.internal.util.UMLRTProfile

import org.eclipse.emf.ecore.EObject
import org.eclipse.uml2.uml.*


object RSARTEUtils {


    fun isCapsule(element: EObject?): Boolean {
        return element != null && element.eClass() == UMLPackage.Literals.CLASS && UMLRTProfile.hasAppliedStereotype(
            element as Element?,
            UMLRTProfile.CapsuleStereotype
        )
    }

    fun getProtocol(pkg: Collaboration): Package? {

        return pkg.eContainer() as Package?
    }

    fun getSystemProtocol(protocol : Collaboration) : RTSystemProtocol {
        return when (protocol.name) {
            "Log" -> RTLogProtocol
            "Timing" -> RTTimingProtocol
            "Frame" -> RTFrameProtocol
            "Exception" -> RTExceptionProtocol
            else -> RTExternalProtocol
        }

    }

//    fun getMessageSet(iface: Interface):  {
//        return EMFUtils.getReferencingObjectByType(iface.eResource().contents,
////            UMLRealTimePackage.Literals.RT_MESSAGE_SET, iface) as RTMessageSet?
//    }

}
