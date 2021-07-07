import ca.jahed.rtpoet.rsarte.RSARTEReader
import ca.jahed.rtpoet.rsarte.RSARTEWriter
import ca.jahed.rtpoet.rsarte.rts.RSARTELibrary
import ca.jahed.rtpoet.rsarte.rts.SystemPorts
import ca.jahed.rtpoet.rtmodel.*
import ca.jahed.rtpoet.rtmodel.sm.RTPseudoState
import ca.jahed.rtpoet.rtmodel.sm.RTState
import ca.jahed.rtpoet.rtmodel.sm.RTStateMachine
import ca.jahed.rtpoet.rtmodel.sm.RTTransition
import ca.jahed.rtpoet.rtmodel.types.primitivetype.RTInteger
import ca.jahed.rtpoet.visualizer.RTVisualizer
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.XMIResource
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.junit.jupiter.api.Test
import java.io.File
import java.io.IOException
import java.util.*


class TestLib {

    private fun loadResourceModel(name: String): Resource {
        val url = javaClass.classLoader.getResource("models/$name")
        return RSARTELibrary.createResourceSet().getResource(URI.createURI(url!!.toString()), true)!!
    }

    private fun loadFileModel(file: String): Resource {
        return RSARTELibrary.createResourceSet().getResource(URI.createFileURI(File(file).absolutePath), true)!!
    }

    private fun pingerPonger(): RTModel {
        val ppProtocol =
            RTProtocol.builder("PPProtocol")
                .output(RTSignal.builder("ping").parameter(RTParameter.builder("round", RTInteger)))
                .input(RTSignal.builder("pong").parameter(RTParameter.builder("round", RTInteger)))
                .build()

        val pinger =
            RTCapsule.builder("Pinger")
                .attribute(RTAttribute.builder("count", RTInteger))
                .port(RTPort.builder("ppPort", ppProtocol).external())
                .port(SystemPorts.log())
                .statemachine(
                    RTStateMachine.builder()
                        .state(RTPseudoState.initial("initial"))
                        .state(RTState.builder("playing"))
                        .transition(
                            RTTransition.builder("initial", "playing")
                                .action("""
                                    this->count = 1;
                                    ppPort.ping(count).send();
                                """)
                        )
                        .transition(
                            RTTransition.builder("playing", "playing")
                                .trigger("ppPort", "pong")
                                .action("""
                                   log.log("Round %d: got pong!", round);
                                   ppPort.ping(++count).send();
                                """)
                        )
                )
                .build()

        val ponger =
            RTCapsule.builder("Ponger")
                .port(RTPort.builder("ppPort", ppProtocol).external().conjugate())
                .port(SystemPorts.log())
                .statemachine(
                    RTStateMachine.builder()
                        .state(RTPseudoState.initial("initial"))
                        .state(RTState.builder("playing"))
                        .transition(RTTransition.builder("initial", "playing"))
                        .transition(
                            RTTransition.builder("playing", "playing")
                                .trigger("ppPort", "ping")
                                .action("""
                                   log.log("Round %d: got ping!", round);
                                   ppPort.pong(round++).send();
                                """)
                        )
                )
                .build()

        val top =
            RTCapsule.builder("Top")
                .part(RTCapsulePart.builder("pinger", pinger))
                .part(RTCapsulePart.builder("ponger", ponger))
                .connector(
                    RTConnector.builder()
                    .end1(RTConnectorEnd.builder("ppPort", "pinger"))
                    .end2(RTConnectorEnd.builder("ppPort", "ponger"))
                )
                .build()

        return RTModel.builder("PingerPonger", top)
            .capsule(pinger)
            .capsule(ponger)
            .protocol(ppProtocol)
            .build()
    }


    private fun saveModel(model: RTModel) {
        File("output").mkdirs()

        val resource = RSARTELibrary.createResourceSet().createResource(
            URI.createFileURI(File("output", "${model.name}.emx").absolutePath)) as XMIResource

        RSARTEWriter.write(resource, model)
        resource.save(null)
    }

    @Test
    fun TestPingerPonger() {
        val model = loadResourceModel("PingPong1.emx")
        model.contents.forEach {println(it)}
        val rtModel = RSARTEReader.read(model)
    }

    @Test
    fun TestAndShowPingerPonger() {
        val model = loadResourceModel("PingPong1.emx")
        model.contents.forEach {println(it)}
        val rtModel = RSARTEReader.read(model)
        RTVisualizer.draw(rtModel)

    }

    @Test
    fun WritePingerPonger() {
//        val model = loadResourceModel("PingPong1.emx")
//        val rtModel = RSARTEReader.read(model)

        saveModel(pingerPonger())
    }

    @Test
    fun ConvertModelToPapyrusRT() {
        val rsarteModel = loadResourceModel("CPPModel.emx")
        val rtModel = RSARTEReader.read(rsarteModel)

        rtModel.save("output/CPPModel.rtmodel")


//        PapyrusRTWriter.write("output/CPPModel.uml", rtModel)
    }

    @Test
    fun ReadAndWriteRSRATEModel() {
        val rsarteModel = loadResourceModel("CPPModel.emx")
        val rtModel = RSARTEReader.read(rsarteModel)



        RSARTEWriter.write("output/CPPModelGenerated.emx", rtModel)
    }


    @Test
    fun RemoveModelID() {

        var resource = loadResourceModel("CPPModel.emx")

        try {
            (resource as XMIResource).eObjectToIDMap.clear()
            resource.save(null)
        } catch (e: IOException) {
            e.printStackTrace()
        }

//        val reg = Resource.Factory.Registry.INSTANCE
//        val m = reg.extensionToFactoryMap
//        m["key"] = XMIResourceFactoryImpl()
//        val resSet: ResourceSet = ResourceSetImpl()
//        val saveResource: Resource = resSet.createResource(URI.createFileURI("CPPModel_mod.emx"))
//        saveResource.contents.add(resource.contents[0])
//        saveResource.save(Collections.EMPTY_MAP)

    }

}