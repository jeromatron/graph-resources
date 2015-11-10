import org.apache.tinkerpop.gremlin.structure.Graph
import org.apache.tinkerpop.gremlin.structure.Vertex

/**
 *  Loads the Enron email communications network data set into a Graph instance.
 *  script courtesy of Daniel Kuppitz
 */
class EnronEmailParser {
    public static void load(Graph graph, String pathToData) {

        graph.createIndex("emailId", Vertex.class)

        // time to load uo the data
        def g = graph.traversal()

        new File(pathToData).eachLine { def line ->
            if (line.startsWith('#')) {
                // ignore the comments at beginning of the data file
                return
            }

            def parts = line.split('\t')
            def source = parts[0].toInteger()
            def target = parts[1].toInteger()

            def sourceV = g.V().has('emailId', source).tryNext().orElseGet {
                graph.addVertex(label, 'email', 'emailId', source)
            }
            def targetV = g.V().has('emailId', target).tryNext().orElseGet {
                graph.addVertex(label, 'email', 'emailId', target)
            }

            sourceV.addEdge('emailed', targetV)
        }
        if (graph.features().graph().supportsTransactions())
            graph.tx().commit();
        graph.close()
    }
}
