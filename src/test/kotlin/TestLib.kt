import ca.jahed.rtpoet.rsarte.RSARTEReader
import ca.jahed.rtpoet.rsarte.rts.RSARTELibrary
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.junit.jupiter.api.Test
import java.io.File

class TestLib {

    private fun loadResourceModel(name: String): Resource {
        val url = javaClass.classLoader.getResource("models/$name")
        return RSARTELibrary.createResourceSet().getResource(URI.createURI(url!!.toString()), true)!!
    }

    private fun loadFileModel(file: String): Resource {
        return RSARTELibrary.createResourceSet().getResource(URI.createFileURI(File(file).absolutePath), true)!!
    }

    @Test
    internal fun TestPingerPonger() {
        val model = loadResourceModel("PingPong1.emx")
        model.contents.forEach {println(it)}
        val rtModel = RSARTEReader.read(model)
    }
}