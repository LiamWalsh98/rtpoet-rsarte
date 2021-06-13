//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ca.jahed.rtpoet.rsarte;

import com.ibm.xtools.uml.msl.internal.operations.ElementOperations;
import com.ibm.xtools.uml.msl.internal.redefinition.RedefUtil;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Collaboration;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.RedefinableElement;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage.Literals;

public class UMLRTProfile {
    public static final String Name = "UMLRealTime";
    public static final URI Id;
    public static final String Capsule = "Capsule";
    public static final String CapsuleStereotype = "UMLRealTime::Capsule";
    public static final String RTPortStereotype = "UMLRealTime::RTPort";
    public static final String RTPortStereotype_isConjugate = "isConjugate";
    public static final String RTPortStereotype_isWired = "isWired";
    public static final String RTPortStereotype_isNotification = "isNotification";
    public static final String RTPortStereotype_isPublish = "isPublish";
    public static final String RTPortStereotype_registration = "registration";
    public static final String RTPortStereotype_registrationOverride = "registrationOverride";
    public static final String RTPortRegistrationKind_Automatic = "Automatic";
    public static final String RTPortRegistrationKind_Automatic_Locked = "Automatic (locked)";
    public static final String RTPortRegistrationKind_Application = "Application";
    public static final String ProtocolContainerStereotype = "UMLRealTime::ProtocolContainer";
    public static final String Protocol = "Protocol";
    public static final String ProtocolStereotype = "UMLRealTime::Protocol";
    public static final String ProtocolRoleKeyword = "ProtocolRole";
    public static final String StateHistoryKind = "StateHistoryKind";
    public static final String RTHistorystateStereotype_kind = "historyKind";
    public static final String RTHistorystateKind_Deep = "Deep";
    public static final String RTHistorystateKind_Shallow = "Shallow";
    public static final String RTHistorystateStereotype = "UMLRealTime::RTHistorystate";
    public static final String InEvent = "InEvent";
    public static final String InEventStereotype = "UMLRealTime::InEvent";
    public static final String OutEvent = "OutEvent";
    public static final String OutEventStereotype = "UMLRealTime::OutEvent";
    public static final String TriggerOperationStereotype = "UMLRealTime::Trigger";
    public static final String RTConnectorStereotype = "UMLRealTime::RTConnector";
    public static final String RTConnectorStereotype_delay = "delay";
    public static final String RTConnectorStereotype_multiplicity = "multiplicity";
    public static final String CapsulePart = "CapsulePart";
    public static final String CapsulePartStereotype = "UMLRealTime::CapsulePart";
    public static final String CapsulePart_isSubstitutable = "isSubstitutable";
    public static final String RegistrationKindEnumeration = "PortRegistrationKind";
    public static final String Coregion = "Coregion";
    public static final String CoregionStereotype = "UMLRealTime::Coregion";
    public static final String UtilityStereotype = "Standard::Utility";
    public static final String RTRedefinableElementStereotypeSimpleName = "RTRedefinableElement";
    public static final String RTRedefinableElementStereotype = "UMLRealTime::RTRedefinableElement";
    public static final String RTRedefinableElementStereotype_rootFragment = "rootFragment";
    public static final String RTMessage = "RTMessage";
    public static final String RTMessageStereotype = "UMLRealTime::RTMessage";
    public static final String RTMessageStereotype_sendPort = "sendPort";
    public static final String RTMessageStereotype_receivePort = "receivePort";
    public static final String DEPENDENCY_TARGET = "Target";
    public static final String KIND_IN_HEADER = "kindInHeader";
    public static final String KIND_IN_IMPLEMENTATION = "kindInImplementation";
    public static final String USES_KIND_FORWARD = "forward reference";
    public static final String USES_KIND_INCLUSION = "inclusion";
    public static final String USES_KIND_NONE = "none";
    public static final String SystemElements_Name = "SystemElements";
    public static final String SystemProtocol = "SystemElements::SystemProtocol";
    public static final URI SystemElement_URI;

    static {
        Id = URI.createURI("pathmap://RT_PROPERTIES/UMLRealTime.epx");
        SystemElement_URI = URI.createURI("pathmap://RT_PROPERTIES/SystemElements.epx");
    }

    public static Profile apply(Package rootPackage) {
        return apply(rootPackage, Id);
    }

    public static Profile apply(Package rootPackage, URI id) {
        ResourceSet resourceSet = rootPackage.eResource().getResourceSet();
        Profile profile = getProfileFromResourceSet(resourceSet, id);
        if (profile != null && !rootPackage.getAllAppliedProfiles().contains(profile)) {
            rootPackage.applyProfile(profile);
        }

        return profile;
    }

    public static Enumeration findEnumeration(String enumerationName, ResourceSet set) {
        Profile profile = getProfileFromResourceSet(set, Id);
        return profile != null ? (Enumeration)profile.getOwnedMember(enumerationName, false, Literals.ENUMERATION) : null;
    }

    public static Enumeration findEnumeration(String enumerationName, Element element) {
        if (element != null) {
            Package rootPackage = ElementOperations.getRootPackage(element);
            if (rootPackage != null) {
                Resource resource = rootPackage.eResource();
                if (resource != null) {
                    return findEnumeration(enumerationName, resource.getResourceSet());
                }
            }
        }

        return null;
    }

    public static EnumerationLiteral getEnumerationLiteral(Enumeration enumeration, String type) {
        return enumeration != null ? enumeration.getOwnedLiteral(type) : null;
    }

    private static Profile getProfileFromResourceSet(ResourceSet set, URI id) {
        if (set != null) {
            Resource resource = set.getResource(id, true);
            if (resource != null) {
                return (Profile)EcoreUtil.getObjectByType(resource.getContents(), Literals.PROFILE);
            }
        }

        return null;
    }

    public static Stereotype getAppliedStereotype(Element element, String stereotype) {
        return element == null ? null : element.getAppliedStereotype(stereotype);
    }

    public static Object getProperty(Element element, String stereotype, String property) {
        return getProperty(element, element, stereotype, property);
    }

    public static Object getProperty(Element element, EObject contextHint, String stereotype, String property) {
        return RedefUtil.getStereotypeValue(element, contextHint, stereotype, property);
    }

    public static void applyStereotype(Element element, String stereotype) {
        Stereotype type = element.getApplicableStereotype(stereotype);
        if (type == null) {
            if (!element.hasKeyword(stereotype)) {
                element.addKeyword(stereotype);
            }
        } else if (!element.isStereotypeApplied(type)) {
            element.applyStereotype(type);
        }

    }

    public static boolean hasAppliedStereotype(Element element, String stereotype) {
        return getAppliedStereotype(element, stereotype) != null;
    }

    public static boolean isCapsule(Element element) {
        return element instanceof Class && hasAppliedStereotype(element, "UMLRealTime::Capsule");
    }

    protected static boolean isPortPropertySet(Port port, EObject contextHint, String property) {
        return port != null && Boolean.TRUE.equals(RedefUtil.getStereotypeValue(port, contextHint, "UMLRealTime::RTPort", property));
    }

    public static String getRegistration(Port port, EObject contextHint) {
        Object value = getProperty(port, contextHint, "UMLRealTime::RTPort", "registration");
        if (value instanceof String) {
            return (String)value;
        } else {
            return value instanceof EnumerationLiteral ? ((EnumerationLiteral)value).getName() : "";
        }
    }

    public static String getRegistrationOverride(Port port, EObject contextHint) {
        Object value = getProperty(port, contextHint, "UMLRealTime::RTPort", "registrationOverride");
        return value instanceof String ? (String)value : "";
    }

    public static boolean isNotification(Port port, EObject contextHint) {
        return isPortPropertySet(port, contextHint, "isNotification");
    }

    public static boolean isConjugated(Port port) {
        Port rootFragment = (Port)RedefUtil.getRootFragment(port);
        return isPortPropertySet(rootFragment, rootFragment, "isConjugate");
    }

    public static boolean isWired(Port port) {
        Port rootFragment = (Port)RedefUtil.getRootFragment(port);
        return isPortPropertySet(rootFragment, rootFragment, "isWired");
    }

    public static boolean isPublish(Port port) {
        return isPortPropertySet(port, port, "isPublish");
    }

    public static boolean isProtocolContainer(Element element) {
        return element instanceof Package && hasAppliedStereotype(element, "UMLRealTime::ProtocolContainer");
    }

    public static boolean isProtocol(Element element) {
        return element instanceof Collaboration && hasAppliedStereotype(element, "UMLRealTime::Protocol");
    }

    public static boolean isPrimitiveType(Element element) {
        return element instanceof PrimitiveType;
    }

    public static boolean isCapsulePart(Element element) {
        return element instanceof Property && hasAppliedStereotype(element, "UMLRealTime::CapsulePart");
    }

    public static boolean isSubstitutable(Property capsulePart) {
        Property rootFragment = (Property)RedefUtil.getRootFragment(capsulePart);
        Stereotype stereotype = getAppliedStereotype(rootFragment, "UMLRealTime::CapsulePart");
        if (stereotype == null) {
            return true;
        } else {
            Object value = rootFragment.getValue(stereotype, "isSubstitutable");
            return Boolean.TRUE.equals(value);
        }
    }

    private UMLRTProfile() {
    }

    public static boolean isInEvent(Element element) {
        return element instanceof CallEvent && hasAppliedStereotype(element, "UMLRealTime::InEvent");
    }

    public static boolean isOutEvent(Element element) {
        return element instanceof CallEvent && hasAppliedStereotype(element, "UMLRealTime::OutEvent");
    }

    public static boolean isTriggerOperation(Operation element) {
        return hasAppliedStereotype(element, "UMLRealTime::Trigger");
    }

    public static boolean isRTPort(EObject element) {
        return element instanceof Port && hasAppliedStereotype((Port)element, "UMLRealTime::RTPort");
    }

    public static boolean isRTHistoryState(Element element) {
        return element instanceof Pseudostate && hasAppliedStereotype(element, "UMLRealTime::RTHistorystate");
    }

    public static boolean isConnectionPointDeepHistory(Pseudostate state) {
        if (isRTHistoryState(state)) {
            EnumerationLiteral value = (EnumerationLiteral)state.getValue(getAppliedStereotype(state, "UMLRealTime::RTHistorystate"), "historyKind");
            if (value != null) {
                return "Deep".equals(value.getName());
            }
        }

        return false;
    }

    public static boolean isRTRedefinableElement(Element element) {
        return element instanceof RedefinableElement && hasAppliedStereotype(element, "UMLRealTime::RTRedefinableElement");
    }

    public static RedefinableElement getRootFragment(Element fragment) {
        return isRTRedefinableElement(fragment) ? (RedefinableElement)fragment.getValue(getAppliedStereotype(fragment, "UMLRealTime::RTRedefinableElement"), "rootFragment") : null;
    }

    public static boolean isUtilityClass(Class clazz) {
        return hasAppliedStereotype(clazz, "Standard::Utility");
    }

    public static boolean isSystemProtocol(Element protocol) {
        return isProtocol(protocol) ? hasAppliedStereotype(protocol, "SystemElements::SystemProtocol") : false;
    }
}
