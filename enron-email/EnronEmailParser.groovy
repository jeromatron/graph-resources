import org.apache.tinkerpop.gremlin.structure.Graph
import org.apache.tinkerpop.gremlin.structure.Vertex

/**
 *       script courtesy of Daniel Kuppitz
 *
 *       NOTE: No indexing involved, since this parser is involved purely for
 *             checking reading and writing functionality within Gremlin shell
 *
 *
 *      Pre-condition:  This script assumes all of the backend configuration has been
 *                      properly set up, and stored in a Gremlin shell variable 'conf'
 *                      and graph = TitanFactory.open(conf)
 *
 *
 *      Post-condition: The Enron email data set is loaded into a TitanGraph instance
 *
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
        graph.close()
    }
}
