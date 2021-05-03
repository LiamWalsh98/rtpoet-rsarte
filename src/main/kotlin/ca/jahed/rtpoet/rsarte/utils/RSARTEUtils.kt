package ca.jahed.rtpoet.rsarte.utils

import com.ibm.xtools.uml.rt.core.internal.util.UMLRTProfile

import org.eclipse.emf.ecore.EObject
import org.eclipse.uml2.uml.Element
import org.eclipse.uml2.uml.UMLPackage


class RSARTEUtils {


    fun isCapsule(element: EObject?): Boolean {
        return element != null && element.eClass() == UMLPackage.Literals.CLASS && UMLRTProfile.hasAppliedStereotype(
            element as Element?,
            UMLRTProfile.CapsuleStereotype
        )
    }

}
